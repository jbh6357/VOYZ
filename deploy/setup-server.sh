#!/bin/bash

# VOYZ 프로젝트 서버 초기 설정 스크립트
# Ubuntu 22.04 LTS 기준

set -e  # 에러 발생 시 중단

echo "=== VOYZ 서버 설정 시작 ==="

# 1. 시스템 업데이트
echo "1. 시스템 패키지 업데이트..."
sudo apt update && sudo apt upgrade -y

# 2. 필수 패키지 설치
echo "2. 필수 패키지 설치..."
sudo apt install -y \
    openjdk-17-jdk \
    nginx \
    git \
    curl \
    wget \
    unzip \
    python3.9 \
    python3-pip \
    python3-venv \
    build-essential \
    libssl-dev \
    libffi-dev \
    python3-dev

# 3. Java 환경 설정
echo "3. Java 환경 설정..."
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc

# 4. 디렉토리 구조 생성
echo "4. 애플리케이션 디렉토리 생성..."
sudo mkdir -p /opt/voyz/{backend,ml,logs,config}
sudo chown -R $USER:$USER /opt/voyz

# 5. 방화벽 설정
echo "5. 방화벽 설정..."
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8081/tcp  # Spring Boot
sudo ufw allow 8000/tcp  # FastAPI (내부 통신용, 필요시)
sudo ufw --force enable

# 6. Nginx 기본 설정 백업
echo "6. Nginx 설정 백업..."
sudo cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.backup

# 7. ML 서비스용 Python 가상환경 생성
echo "7. Python 가상환경 설정..."
cd /opt/voyz/ml
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip

# 8. 로그 디렉토리 권한 설정
echo "8. 로그 디렉토리 설정..."
sudo mkdir -p /var/log/voyz
sudo chown -R $USER:$USER /var/log/voyz

# 9. systemd 서비스 디렉토리 확인
echo "9. systemd 서비스 준비..."
sudo mkdir -p /etc/systemd/system

echo "=== 기본 서버 설정 완료 ==="
echo ""
echo "다음 단계:"
echo "1. GitHub Actions에서 애플리케이션 배포"
echo "2. Nginx 설정 파일 적용"
echo "3. systemd 서비스 파일 설정"
echo "4. SSL 인증서 설정 (선택사항)"

# Java 버전 확인
java -version

# Python 버전 확인
python3 --version

# Nginx 상태 확인
sudo systemctl status nginx --no-pager