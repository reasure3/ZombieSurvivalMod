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
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

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
                return isValidBlockToBreak(mob.level.getBlockState(blockPos));
            return mob.level.getBlockState(blockPos).is(block.getBlock());
        }
        return false;
    }

    @Override
    public void start() {
        int dy = target.getBlockY() - mob.getBlockY();
        if (dy < 0) { // ??????????????? ?????? ????????? ?????? ??? ??? ????????? ??????
            blockPos.set(mob.getBlockX(), mob.getBlockY() - 1, mob.getBlockZ());
            block = mob.level.getBlockState(blockPos);
            if (isValidBlockToBreak(block)) setActionBreak();
            else action = Action.NONE;
        } else if (dy > 1) { // ??????????????? 2??? ?????? ????????? ?????? ??? ????????? ???????????? ???????????? ?????? ????????? ?????? ??????
            blockPos.set(mob.getBlockX(), mob.getBlockY() + 2, mob.getBlockZ());
            block = mob.level.getBlockState(blockPos);
            if (!isValidBlockToBreak(block)) {
                blockInteractTime = 30;
                blockPos.set(mob.blockPosition());
                action = Action.JUMP_SET;
            } else {
                block = mob.level.getBlockState(blockPos);
                setActionBreak();
            }
        } else { // ???????????? ?????? ???(?????? ???) ????????? ???????????? ?????? ???(?????? ???)??? ?????? ????????? ??????. (?????? ????????????)
            if (findNearestBlock()) {
                if (!isValidBlockToBreak(block)) {
                    blockInteractTime = 40;
                    action = Action.SET;
                } else {
                    setActionBreak();
                }
            } else {
                action = Action.NONE;
            }
        }

        if ((action == Action.BREAK || action == Action.BREAK_INSTANCE) && !ForgeHooks.canEntityDestroy(mob.level, blockPos, mob)) {
            action = Action.NONE;
        }
    }

    // ??????: ???.??? -> ???.??? -> ???.?????? -> ???.??? -> ???.??? -> ???.??????- > ???.??? -> ???.??? -> ???.??????
    // ??????: ?????? ????????? ???????????? ??????
    private boolean findNearestBlock() {
        Direction front = mob.getDirection();
        Direction[] checkDirs = {front, front.getCounterClockWise(), front.getClockWise(), front.getOpposite()};
        for (Direction dir : checkDirs) {
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getBlockY() + 1, mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (isValidBlockToBreak(block)) return true;
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getBlockY(), mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (isValidBlockToBreak(block)) return true;
            blockPos.set(mob.getBlockX() + dir.getStepX(), mob.getBlockY() - 1, mob.getBlockZ() + dir.getStepZ());
            block = mob.level.getBlockState(blockPos);
            if (!isValidBlockToBreak(block)) return true;
        }
        return false;
    }

    private void setActionBreak() {
        breakTime = getBlockBreakTick();
        blockInteractTime = breakTime;
        if (breakTime == 0) action = Action.BREAK_INSTANCE;
        else if (breakTime < 0) action = Action.NONE;
        else action = Action.BREAK;
    }

    @Override
    public void tick() {
        if (action == Action.BREAK) {
            if (mob.level.random.nextInt(10) == 0 && !mob.swinging) {
                mob.swing(mob.getUsedItemHand());
            }
            // ????????? ????????? ???
            int progress = (int) ((float) (breakTime - blockInteractTime) / (float) breakTime * 10.0f);
            if (progress != lastProgress) {
                mob.level.destroyBlockProgress(mob.getId(), blockPos.immutable(), progress);
                lastProgress = progress;
            }
        }
        // ?????? ????????? ?????????
        if (blockInteractTime == 0) {
            if (action == Action.BREAK_INSTANCE || action == Action.BREAK) {
                // ????????? ??? ??????
                block = mob.level.getBlockState(blockPos); // ?????? ?????? ????????? ?????? ?????? ?????? ????????????
                BlockEntity blockEntity = block.hasBlockEntity() ? mob.level.getBlockEntity(blockPos) : null;
                mob.level.removeBlock(blockPos, false);
                // this.mob.level.levelEvent(1021, blockPos, 0);
                this.mob.level.levelEvent(2001, blockPos, Block.getId(mob.level.getBlockState(blockPos)));
                Block.dropResources(block, mob.level, blockPos, blockEntity, mob, mob.getMainHandItem());
            } else if (action == Action.JUMP_SET) {
                // ????????? ???????????? ?????? ??????
                mob.getJumpControl().jump();
            }
        }
        blockInteractTime--;
    }

    @Override
    public void stop() {
        if (action == Action.BREAK)
            mob.level.destroyBlockProgress(mob.getId(), blockPos.immutable(), -1);
        else if (action == Action.SET || action == Action.JUMP_SET) {
            block = mob.level.getBlockState(blockPos);
            mob.level.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
            mob.level.sendBlockUpdated(blockPos, block, Blocks.DIRT.defaultBlockState(), 11);
            mob.level.gameEvent(mob, GameEvent.BLOCK_PLACE, blockPos);
        }
        action = Action.NONE;
        blockInteractTime = -1;
        lastProgress = -1;
        isStuck = false;
        lastStuckCheckTime = mob.level.getGameTime();
        lastStuckCheckPos = mob.position();
    }

    protected int getBlockBreakTick() {
        float destroySpeed = block.getDestroySpeed(mob.level, blockPos);
        if (destroySpeed < 0) return -1;
        float speedMultiplier = 1.0f;
        ItemStack handTool = mob.getMainHandItem();
        boolean correctTool = !handTool.isEmpty() && handTool.isCorrectToolForDrops(block);
        if (correctTool) {
            speedMultiplier = handTool.getDestroySpeed(block);
            speedMultiplier += MathUtil.pow(handTool.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY)) + 1;
        }
        float damage = speedMultiplier / destroySpeed;
        damage /= correctTool ? 100 : 30;
        if (damage > 1) return 0;
        return Math.round(1.0f / damage);
    }

    protected boolean isValidBlockToBreak(BlockState block) {
        return !block.isAir() && !(block.getBlock() instanceof LiquidBlock);
    }

    protected boolean isRightTarget() {
        return target != null && target.canBeSeenAsEnemy();
    }

    enum Action {
        JUMP_SET, // ?????? ??? ?????? ??????
        SET, // ?????? ??????
        BREAK, // ?????? ??????
        BREAK_INSTANCE,
        NONE
    }
}
