# Zombie Survivor

version: 0.2

## 모드 설명

* 하루가 지날 때마다 더 많은 좀비가 나타납니다!
* 10일마다 일부 좀비가 더 똑똑해집니다.
* 좀비는 점점 강해집니다.

## 설정 파일
* **Spawn Count Type**: 좌표 별 좀비 스폰 시도 횟수 설정 방법
  * `DEFAULT`: 3 * spawn increase amount * 일수
  * `RANDOM`: rand(0 ~ 3 * spawn increase amount * 일수)
  * 기본값: DEFAULT


* **Spawn Increase Amount**: 날이 지날 때, 증가되는 좀비 스폰 량
  * 범위: 0 ~ 10000
  * 기본값: 50


* **Random Coord Type**: 각 기준 좌표에서 랜덤 좌표를 설정 방법. 기준 좌표의 x, z 좌표에 해당 수식으로 나온 값을 더함.
  * `TRIANGLE`: rand(0 ~ 5) - rand(0 ~ 5): -5 ~ 5 중에서 0이 나올 확률이 높음. 바닐라 마크 방식
  * `UNIFORM`: rand(-5 ~ 5): -5 ~ 5가 골고루 나옴.
  * 기본값: TRIANGLE


* **??? Spawn Weight**: 해당 몬스터의 스폰 비율 가중치를 설정합니다.