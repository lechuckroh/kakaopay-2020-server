#!/bin/bash

userId=1
roomId=AAA
baseUrl=localhost:8080

function sprinkle {
  local sum=${1}
  local count=${2}

  echo "$(http --body POST $baseUrl/sprinkle X-USER-ID:$userId X-ROOM-ID:$roomId sum=$sum count=$count | jq -r '.data')"
}

function receive {
  local receiveUserId=${1}
  local token=${2}

  http --body POST $baseUrl/sprinkle/receive X-USER-ID:$receiveUserId X-ROOM-ID:$roomId token=$token
}

function status {
  local token=${1}

  http --body GET $baseUrl/sprinkle?token=$token X-USER-ID:$userId X-ROOM-ID:$roomId
}

echo 뿌리기 요청...
token=$(sprinkle 100 5)
echo 뿌리기 완료. 토큰: $token

echo 사용자2 받기 요청
receive 2 $token

echo 사용자2 다시 받기 요청
receive 2 $token

echo 사용자3 받기 요청
receive 3 $token

echo 뿌리기 상태 조회
status $token

