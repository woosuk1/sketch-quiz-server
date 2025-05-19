#!/bin/bash

# 개발용 MongoDB 컨테이너 최초 생성 스크립트
# 한번 이 명령 실행 한 후에는 docker mongodb start 로 실행 가능

docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=root \
  mongo
