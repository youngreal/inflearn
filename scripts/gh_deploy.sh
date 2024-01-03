#!/bin/bash
PROJECT_NAME="inflearn"
JAR_PATH="/home/ec2-user/inflearn/build/libs/*.jar"

# 배포 경로
DEPLOY_PATH=/home/ec2-user/$PROJECT_NAME/

# 배포 로그
DEPLOY_LOG_PATH="/home/ec2-user/$PROJECT_NAME/deploy.log"

# 배포 에러 로그 관리
DEPLOY_ERR_LOG_PATH="/home/ec2-user/$PROJECT_NAME/deploy_err.log"

# 어플리케이션 로그 관리
APPLICATION_LOG_PATH="/home/ec2-user/$PROJECT_NAME/application.log"

BUILD_JAR=$(ls "$JAR_PATH")
JAR_NAME=$(basename "$BUILD_JAR")

echo "==== 배포 시작: $(date +%c) ====" >> $DEPLOY_LOG_PATH

echo "> build 파일명: $JAR_NAME" >> $DEPLOY_LOG_PATH
echo " build 파일 복사" >> $DEPLOY_LOG_PATH
cp "$BUILD_JAR" $DEPLOY_PATH

echo "> 현재 동작중인 어플리케이션 pid 체크" >> $DEPLOY_LOG_PATH
CURRENT_PD=$(pgrep -f "$JAR_NAME")

# 강제로 죽이는게 최선은 아닌것 같다.
if [ -z "$CURRNET_PID" ]
then
  echo "> 현재 동작중인 어플리케이션 존재 X" >> $DEPLOY_LOG_PATH
else
  echo "> 현재 동작중인 어플리케이션 존재 O" >> $DEPLOY_LOG_PATH
  echo "> 현재 동작중인 어플리케이션 강제 종료 진행" >> $DEPLOY_LOG_PATH
  echo " kill -9 $CURRNET_PID" >> $DEPLOY_LOG_PATH
  kill -9 "$CURRNET_PID"
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "> DEPLOY_JAR 배포" >> $DEPLOY_PATH
# 백그라운드에서 어플리케이션 실행을 위해 추가
nohup java -jar -Dspring.profiles.active=prod "$DEPLOY_JAR" >> $APPLICATION_LOG_PATH 2> $DEPLOY_ERR_LOG_PATH &

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH

