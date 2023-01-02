package com.reasure.zomsurvival.entity.goal;

import com.reasure.zomsurvival.util.MathUtil;
import com.reasure.zomsurvival.util.SpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SetOrBreakBlockGoal extends Goal {
    protected PathfinderMob mob;
    protected LivingEntity target;
    protected Action action;
    protected int blockInteractTime;
    protected int breakTime;
    protected int lastProgress;
    protected BlockState block;
    protected BlockPos.MutableBlockPos blockPos;
    protected long lastStuckCheckTime;
    protected Vec3 lastStuckCheckPos;
    protected boolean isStuck;

    public SetOrBreakBlockGoal(PathfinderMob mob) {
        this.mob = mob;
        this.action = Action.NONE;
        this.blockInteractTime = -1;
        this.blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
        this.lastProgress = -1;
        this.lastStuckCheckTime = mob.level.getGameTime();
        this.lastStuckCheckPos = Vec3.ZERO;
        this.isStuck = false;
    }

    protected void checkStuck() {
        if (mob.level.getGameTime() - lastStuckCheckTime > 25L) {
            float dist = (mob.getSpeed() >= 1.0f ? mob.getSpeed() : mob.getSpeed() * mob.getSpeed()) * 25f;
            isStuck = mob.position().distanceToSqr(lastStuckCheckPos) < (double) (dist * dist);

            lastStuckCheckTime = mob.level.getGameTime();
            lastStuckCheckPos = mob.position();
        }
    }

    @Override
    public boolean canUse() {
        if (mob.level instanceof ServerLevel level) {
            if (MathUtil.getDay(level) >= SpawnConfig.ZOMBIE_SET_OR_BREAK_BLOCLK_DAY.get()) {
                target = mob.getTarget();
                if (!isRightTarget()) return false;
                checkStuck();
                return isStuck;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (action == Action.NONE) return false;
        if (blockInteractTime < 0) return false;
        if (isRightTarget()) {
            if (action == Action.SET || action == Action.JUMP_SET)
                return mob.level.getBlockState(blockPos).isAir();
            return mob.level.getBlockState(blockPos).is(block.getBlock());
        }
        return false;
    }

    @Override
    public void start() {
        int dy = target.getBlockY() - mob.getBlockY();
        if (dy < 0) { // 플레이어가 밑에 있으면 자기 발 밑 블럭을 부숨
            blockPos.set(mob.getBlockX(), mob.getBlockY() - 1, mob.getBlockZ());
            block = mob.level.getBlockState(blockPos);
            setActionBreak();
        } else if (dy > 1) { // 플레이어가 2칸 위에 있으면 자기 위 블럭을 부수거나 점프하고 자기 자리에 블럭 쌓음
            blockPos.set(mob.getBlockX(), mob.getBlockY() + 2, mob.getBlockZ());
            block = mob.level.getBlockState(blockPos);
            if (block.isAir()) {
                blockInteractTime = 30;
                blockPos.set(mob.blockPosition());
                action = Action.JUMP_SET;
            } else {
                block = mob.level.getBlockState(blockPos);
                setActionBreak();
            }
        } else { // 아니라면 자기 앞(또는 옆) 블럭을 부수거나 자기 앞(또는 옆)의 밑에 블럭을 설치. (다리 만들듯이)
            if (fineNearestBlock()) {
                if (block.isAir()) {
                    blockInteractTime = 40;
                    action = Action.SET;
                } else {
                    setActionBreak();
                }
            } else {
                action = Action.NONE;
            }
        }
    }

    // 순서: 앞.눈 -> 앞.발 -> 앞.발밑 -> 왼.눈 -> 왼.발 -> 왼.발밑- > 오.눈 -> 오.발 -> 오.발밑
    // 반환: 부술 블록을 찾았는지 여부
    private boolean fineNearestBlock() {
        Direction front = mob.getDirection();
        Direction[] checkDirs = {front, front.getCounterClockWise(), front.getClockWise(), front.getOpposite()};
        for (Direction dir : checkDirs) {
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getBlockY() + 1, mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (!block.isAir()) return true;
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getY(), mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (!block.isAir()) return true;
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getY() - 1, mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (block.isAir()) return true;
        }
        return false;
    }

    private void setActionBreak() {
        breakTime = getBlockBreakTick();
        blockInteractTime = breakTime;
        action = Action.BREAK;
    }

    @Override
    public void tick() {
        blockInteractTime--;
        if (action == Action.BREAK) {
            if (mob.level.random.nextInt(10) == 0 && !mob.swinging) {
                mob.swing(mob.getUsedItemHand());
            }
            // 블록을 부수는 중
            int progress = (int) ((float) (breakTime - blockInteractTime) / (float) breakTime * 10.0f);
            if (progress != lastProgress) {
                mob.level.destroyBlockProgress(mob.getId(), blockPos, progress);
                lastProgress = progress;
            }
        }
        // 해당 목표가 끝나감
        if (blockInteractTime == 0) {
            if (action == Action.BREAK) {
                // 블록을 다 부숨
                block = mob.level.getBlockState(blockPos); // 혹시 모를 버그를 위해 다시 블록 가져오기
                mob.level.removeBlock(blockPos, false);
                BlockEntity blockEntity = block.hasBlockEntity() ? mob.level.getBlockEntity(blockPos) : null;
                Block.dropResources(block, mob.level, blockPos, blockEntity, mob, mob.getMainHandItem());
            } else if (action == Action.JUMP_SET) {
                // 블록을 설치하기 전에 점프
                mob.getJumpControl().jump();
            }
        }
    }

    @Override
    public void stop() {
        if (action == Action.BREAK)
            mob.level.destroyBlockProgress(mob.getId(), blockPos, -1);
        else if (action == Action.SET || action == Action.JUMP_SET)
            mob.level.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
        action = Action.NONE;
        blockInteractTime = -1;
        lastProgress = -1;
        isStuck = false;
        lastStuckCheckTime = mob.level.getGameTime();
        lastStuckCheckPos = mob.position();
    }

    protected int getBlockBreakTick() {
        float speedMultiplier = 1.0f;
        ItemStack handTool = mob.getMainHandItem();
        boolean correctTool = !handTool.isEmpty() && handTool.isCorrectToolForDrops(block);
        if (correctTool) {
            speedMultiplier = handTool.getDestroySpeed(block);
            speedMultiplier += MathUtil.pow(handTool.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY)) + 1;
        }
        float damage = speedMultiplier / block.getDestroySpeed(mob.level, blockPos);
        damage /= correctTool ? 100 : 30;
        if (damage > 1) return 0;
        return Math.round(1.0f / damage);
    }

    protected boolean isRightTarget() {
        return target != null && target.canBeSeenAsEnemy();
    }

    enum Action {
        JUMP_SET, // 점프 후 블럭 설치
        SET, // 블럭 설치
        BREAK, // 블럭 부숨
        NONE
    }
}
