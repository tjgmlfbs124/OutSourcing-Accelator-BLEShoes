## 걸음자세 및 이동거리 측정 모듈개발 (똑신)
 # Business 
9축가속도 센서모듈이 개발된 모듈과 Application이 Bluetooth로연결되어, 모듈이 블루투스로 전송하는 데이터를 수신하여 시각화하된 데이터를 앱에서 제공.

 # Technical Lead
- Web Frontend (Android Java)
- fastBleLib 라이브러리를 활용한 BLE 연결기능 구성
- BLE Broadcast 활용한 1:N 연결처리 (양쪽발)
- 모듈이 전송하는  16Bit Low Data를 프로토콜에 따라 처리
- 단계적 프로토콜 검사를 통해, 잘못된 데이터를 필터링 함으로써   실시간 시각화 데이터에 필요한 계산 및 처리속도 개선
- 일정한 데이터가 아닌 값을 필터링하는 로직 추가
- Scalable Layout Library를 통한 해상도별 UI처리 

 # Project Lead
- 1주일간의 테스트를 거친 데이터 신뢰성 확보를 위해 노력
- GitHub를 통해 개발 프로세스 기록
- 최소한의  min SDK 구성을 목표
- “부모님” 대상을 타겟으로 UI 구상을 위한 노력
- 
## 사업개요
- 신생아들의 걸음마 자세를 파악하기위해, 블루투스와 가속도 센서로 이루어진 모듈을 신생아 발에 부착
- 블루투스를 통해서 모듈이 안드로이드로 9축 가속도 데이터를 전송
- 안드로이드에서 그 값을 시각화하여 렌더링
- 본 프로젝트는 안드로이드 개발을 담당

## 개발개요
- Application Name : "똑신"
- Develop Tool : Android Studio
- compileSdk Version : 29
- minSDK Version : 21

- 완료일 : 2020-05-25


## 개발 사진
<img src="https://user-images.githubusercontent.com/25836808/102574375-f422f000-4133-11eb-8512-eaddc95a4d52.png" width="30%">
<img src="https://user-images.githubusercontent.com/25836808/102574401-03a23900-4134-11eb-88b8-94c2e7f908d0.png" width="30%">
<img src="https://user-images.githubusercontent.com/25836808/102573748-790d0a00-4132-11eb-9cdd-1ea18b9b9ec0.png" width="30%">
