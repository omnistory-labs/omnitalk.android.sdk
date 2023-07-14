<p align="center">
  <img src="https://github.com/Luna-omni/readmdtest/assets/125844802/a910cb80-de3b-44d8-9f37-0ccd08b9dd19" width="500" height="100">
</p><br/>

# Omnitalk Android SDK 

옴니톡은 WebRTC 기반의 CPaaS 플랫폼 서비스입니다. 옴니톡 SDK를 통해 Web/App에서 간단하게 실시간 통신을 구현할 수 있습니다.


## Feature Overview

| feature |  implemented |
|---|:---:|
|  Audio/Video |  ✔️ |
|  Device Setting |  ✔️ |
|  Audio & Video Mute |  ✔️ |
|  Audio & Video Unmute |  ✔️ |
|  Chatting |  ✔️ |
|  SIP call |  ✔️ |

## Pre-Requisite

- 옴니톡 서비스키 & 서비스 아이디
  - [옴니톡 홈페이지](https://omnitalk.io) 를 방문하여 서비스 키와 아이디를 발급 받아주세요.
  - 혹은 [이곳](https://omnitalk.io/demo/audio) 에서 1시간 동안 무료로 사용할 수 있는 키를 받아주세요.

## Getting Started

Omnitalk Android SDK는 maven repository에 배포되어 있습니다.
build.gradle 파일에 아래와 같이 설정하여 SDK dependency를 추가 합니다.
최신 버전 정보는 https://central.sonatype.com/artifact/io.omnitalk/omnitalksdk 에서 확인할 수 있습니다.
```
implementation 'io.omnitalk:omnitalksdk:x.x.x'
```

* Manifest 설정
    * SDK 사용에 필요한 권한을 획득하기 위해서 Android Manifest 설정합니다.
    * 기본적으로 `CAMERA`, `RECORD_AUDIO` 등 일부 권한을 필요로 합니다.
    * 아래의 사용자 권한들을 추가해 줍니다.

| 파라미터 | 설명 |
|---|:---:|
| INTERNET | 네트워크 통신을 위해서 필요한 권한 |
| CAMERA | 카메라 영상을 송출하기 위해서 필요한 권한 |
| RECORD_AUDIO | 오디오 음성을 송출하기 위해서 필요한 권한 |
| MODIFY_AUDIO_SETTINGS | 오디오 장치 관리를 위해서 필요한 권한 |

## Documentation

쉽고 자세한 [문서](https://docs.omnitalk.io/android)를 제공하고 있습니다. 


## Issue 

옴니톡을 사용하면서 발생하는 이슈나 궁금점은  [issue](https://github.com/omnistory-labs/omnitalk.android.sdk/issues) 페이지를 확인해 주세요.

## Example Projects

옴니톡 SDK로 구현된 간단한 데모를 확인해 보세요.
- [android 데모](https://github.com/omnistory-labs/omnitalk.android.sdk/tree/demo) 