#!/bin/bash
PROJECT_NAME="inflearn"

# 배포 경로
DEPLOY_PATH=/home/ec2-user/$PROJECT_NAME/

# 배포 로그
DEPLOY_LOG_PATH="$DEPLOY_PATH/deploy.log"

# 배포 에러 로그 관리
DEPLOY_ERR_LOG_PATH="$DEPLOY_PATH/deploy_err.log"

# Docker 서비스 시작
echo 'Docker 서비스를 시작합니다.'
sudo systemctl start docker

# Docker Compose 권한 설정
echo 'Docker Compose 권한을 설정합니다.'
sudo chmod +x /usr/local/bin/docker-compose
sudo chmod 666 /var/run/docker.sock

echo "==== 배포 시작: $(date +%c) ====" >> $DEPLOY_LOG_PATH

echo "> 현재 동작중인 docker compose pid 체크" >> $DEPLOY_LOG_PATH
CURRNET_PID=$(docker-compose -f $DEPLOY_PATH/docker-compose.yml ps -q)

# 강제로 죽이는게 최선은 아닌것 같다.
if [ -z "$CURRNET_PID" ]
then
  echo "> 현재 동작중인 어플리케이션 존재 X" >> $DEPLOY_LOG_PATH
else
  echo "> 현재 동작중인 어플리케이션 존재 O" >> $DEPLOY_LOG_PATH
  echo "> 현재 동작중인 어플리케이션 강제 종료 진행" >> $DEPLOY_LOG_PATH
  echo "docker-compose -f $DEPLOY_PATH/docker-compose.yml down" >> $DEPLOY_LOG_PATH
  docker-compose -f $DEPLOY_PATH/docker-compose.yml down
  sleep 3
fi

echo "> Docker Compose를 이용해 어플리케이션 배포 진행" >> $DEPLOY_LOG_PATH

# Docker Compose로 어플리케이션을 백그라운드에서 실행
docker-compose -f $DEPLOY_PATH/docker-compose.yml up -d 2> $DEPLOY_ERR_LOG_PATH

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH

