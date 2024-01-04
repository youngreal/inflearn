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
echo "> Docker Compose를 이용해 어플리케이션 배포 진행" >> $DEPLOY_LOG_PATH
docker-compose -f $DEPLOY_PATH/docker-compose up --build > $DEPLOY_ERR_LOG_PATH

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH

