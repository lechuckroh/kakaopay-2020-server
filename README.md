# 카카오페이 뿌리기

* [요구사항](docs/requirement.md)

목차
* [Quick Start](#quick-start)
* [Requirements](#requirements)
* [Build](#build)
* [Run](#run)
* [DB Migration](#db-migration)
* [DB Schema](#db-schema)
* [API Documents](#api-documents)
* [Considerations](#considerations)

## Quick Start
docker-compose를 사용해 데모를 실행할 수 있습니다:

```bash
$ cd deploy/docker-compose
$ docker-compose up -d
$ ./demo.sh
```
> `demo.sh`를 실행하려면 [httpie](https://httpie.org/), [jq](https://stedolan.github.io/jq/) 가 설치되어 있어야 합니다.


gradle을 사용해 직접 빌드 및 테스트:
```bash
$ ./gradlew clean build
```

## Requirements
도커를 사용해 실행하거나, 로컬에 설치된 Java를 사용해 실행할 수 있습니다.

도커 사용시:
* docker
* docker-compose

Java 사용시:
* Java 11+

## Build
도커를 사용해 실행하는 경우 빌드 과정을 건너뛰고 바로 [실행](#run) 문서를 참고하세요.

### 도커 이미지 빌드
docker가 설치되어 있어야 합니다:
```bash
$ make image
```

`lechuckroh/kakaopay-2020-server:latest` 도커 이미지가 생성됩니다.

> 도커 이미지는 CI 도구를 사용해 자동으로 업로드되기 때문에 별도로 빌드할 필요가 없습니다. 

### 배포 패키지 빌드
```bash
$ ./gradlew clean build jacocoTestReport
```

배포 패키지는 `build/distributions/kakaopay.tar` 파일로 생성됩니다.

빌드 결과 생성되는 리포트는 다음과 같습니다:
* JUnit 테스트 결과: `build/reports/tests/test/index.html`
* JaCoCo 테스트 커버리지: `build/reports/jacoco/index.html`
* Detekt 정적 코드 분석: `build/reports/detekt.html`


## Run
아래 실행 방법 중 하나를 선택해 실행할 수 있습니다.

### 도커를 사용해 실행
```bash
$ cd deploy/docker-compose
$ docker-compose up -d
$ ./demo.sh
```

> `demo.sh`를 실행하려면 [HTTPie](https://httpie.org/), [jq](https://stedolan.github.io/jq/) 가 설치되어 있어야 합니다.

### gradle을 사용해 실행
```bash
$ ./gradlew bootRun
```

### 배포 패키지를 사용해 실행
```bash
# 배포 패키지 압축 해제
$ tar xvf build/distributions/kakaopay -C build/distributions

# bin 디렉토리로 이동
$ cd build/distributions/kakaopay/bin

# 실행
$ ./kakaopay
```

## DB Migration
[Liquibase](https://www.liquibase.org/)를 사용해 자동으로 DB 마이그레이션을 진행하며, 데모를 위한 초기 데이터가 같이 추가됩니다.

마이그레이션 데이터는 `src/main/resources/db.changelog/` 디렉토리에서 확인할 수 있습니다.

기본 추가 데이터는 다음과 같습니다:

| 사용자ID | 대화방ID |
|:------:|:------:|
| 1      | AAA    |
| 2      | AAA    |
| 3      | AAA    |
| 1      | BBB    |
| 3      | BBB    |
| 5      | BBB    |


## DB Schema
DB 스키마는 [octopus-db-tools](https://github.com/lechuckroh/octopus-db-tool)를 사용해 `docs/database.ojson` 파일에 저장됩니다.

실행파일을 [다운로드](https://github.com/lechuckroh/octopus-db-tool/releases)한 후, 스키마를 다른 형식의 파일로 변환해 볼 수 있습니다.

```bash
# 리눅스용 설치
$ wget https://github.com/lechuckroh/octopus-db-tool/releases/download/1.0.17/oct-linux64.zip -O linux.zip && unzip linux.zip && rm linux.zip

# 맥용 설치
$ wget https://github.com/lechuckroh/octopus-db-tool/releases/download/1.0.17/oct-darwin64.zip -O osx.zip && unzip osx.zip && rm osx.zip

$ cd docs

# 엑셀 파일로 변환
$ make xlsx

# liquibase용 changelog 생성
$ make liquibase

# 코틀린 소스코드 생성
$ make jpa-kotlin
```

## API Documents
API 서버를 실행한 다음, [HTTPie](https://httpie.org/)를 사용해 API 호출하는 예제를 추가합니다.

### 뿌리기
* 요청: `POST /sprinkle`
* Body
  * `sum`: 뿌릴 금액
  * `count`: 뿌릴 인원 

예제:
```bash
$ http POST localhost:8080/sprinkle X-USER-ID:1 X-ROOM-ID:AAA sum=100 count=5

{
    "data": "cDf"
}
```

### 받기
* 요청: `POST /sprinkle/receive`
* Body
  * `token`: 뿌리기 토큰

예제:
```bash
$ http POST localhost:8080/sprinkle/receive X-USER-ID:2 X-ROOM-ID:AAA token=cDf

{
    "data": 10
}
```

### 뿌리기 상태 조회
* 요청: `GET /sprinkle`
* 파라미터
  * `token`: 뿌리기 토큰

예제:
```bash
$ http GET localhost:8080/sprinkle?token=cDf X-USER-ID:1 X-ROOM-ID:AAA

{
    "data": {
        "createdAt": "2020-06-27T05:43:44.788+00:00",
        "receivedAmount": 10,
        "receivedList": [
            {
                "amount": 10,
                "userId": 2
            }
        ],
        "totalAmount": 100
    }
}
```

## Considerations
### 뿌리기 고려 사항
* 뿌리기 토큰이 중복될 수 있기 때문에, 뿌리기 저장시 PK 중복에러가 발생하지 않을 때까지 반복 생성 후 저장 시도.
* 최대 반복 생성 시도 횟수를 초과한 경우, 뿌리기 실패 응답 반환
* 최대 반복 생성 시도 횟수를 지정하지 않는 경우, 모든 가용 토큰의 조합이 더 이상 없는 경우 무한 루프 발생.

### 뿌릴 금액 분배
* 금액 분배 로직은 퍼포먼스 이슈가 없도록 최대한 빠른 계산 필요. 시간 복잡도: `O(n log n)`
* 분배할 수 없는 경우 뿌리기 실패 응답 전송

### 받기 고려 사항
* 한 번에 모든 데이터를 읽어오기 위해 여러 테이블을 조인하기 보다는, 필요한 정보를 최소한으로 읽어오면서 Fail-Fast 적용.
* 조회 후 업데이트 실행 중간에 다른 사용자가 받기를 해서 DB를 업데이트한 경우, 다른 항목 받기 시도
* 더 이상 받을 수 있는 항목이 없어지면 받기 실패 응답 
* 분배 가능한 건수가 여러개 있는 경우, 분배할 항목을 선택할 때 랜덤 선택 사용. 가능 항목을 순서대로 분배하는 경우 다른 서버에서 분배하는 것과 충돌할 가능성이 높아짐.

### UTC 사용
* 서버가 위치한 곳에 따라 시간대가 모두 다를 수 있기 때문에, API서버와 DB 모두 UTC를 사용
