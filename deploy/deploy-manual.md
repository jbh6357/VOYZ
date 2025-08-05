# VOYZ 수동 배포 가이드

## 1. 서버 초기 설정

SSH로 EC2 접속:
```bash
ssh -i ~/your-key.pem ubuntu@YOUR_EC2_IP
```

서버 설정 스크립트 실행:
```bash
# 스크립트 업로드 후
chmod +x setup-server.sh
./setup-server.sh
```

## 2. 애플리케이션 파일 복사

로컬에서:
```bash
# Backend JAR 파일 복사
scp -i ~/your-key.pem backend/target/*.jar ubuntu@YOUR_EC2_IP:/opt/voyz/backend/voyz.jar

# ML 서비스 파일 복사
scp -i ~/your-key.pem -r ml/* ubuntu@YOUR_EC2_IP:/opt/voyz/ml/

# 설정 파일 복사
scp -i ~/your-key.pem deploy/*.service ubuntu@YOUR_EC2_IP:/tmp/
scp -i ~/your-key.pem deploy/nginx.conf ubuntu@YOUR_EC2_IP:/tmp/
```

## 3. Nginx 설정

서버에서:
```bash
# Nginx 설정 적용
sudo cp /tmp/nginx.conf /etc/nginx/sites-available/voyz
sudo ln -s /etc/nginx/sites-available/voyz /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

## 4. Systemd 서비스 설정

```bash
# 서비스 파일 복사
sudo cp /tmp/*.service /etc/systemd/system/

# JWT Secret 설정 (voyz-backend.service 파일 수정)
sudo nano /etc/systemd/system/voyz-backend.service
# Environment="JWT_SECRET=YOUR_ACTUAL_JWT_SECRET" 수정

# ML 서비스 환경 변수 설정
sudo nano /etc/systemd/system/voyz-ml.service
# Environment="OPENAI_API_KEY=YOUR_ACTUAL_API_KEY" 수정

# Google credentials 파일 생성
sudo nano /opt/voyz/ml/google-credentials.json
# Google Cloud JSON 키 내용 붙여넣기

# 서비스 활성화 및 시작
sudo systemctl daemon-reload
sudo systemctl enable voyz-backend voyz-ml
sudo systemctl start voyz-backend
sudo systemctl start voyz-ml

# 상태 확인
sudo systemctl status voyz-backend
sudo systemctl status voyz-ml
```

## 5. ML 서비스 의존성 설치

```bash
cd /opt/voyz/ml
source venv/bin/activate
pip install -r requirements.txt
deactivate
```

## 6. 로그 확인

```bash
# Backend 로그
tail -f /var/log/voyz/backend.log

# ML 서비스 로그
tail -f /var/log/voyz/ml.log

# Nginx 로그
tail -f /var/log/nginx/voyz_access.log
tail -f /var/log/nginx/voyz_error.log
```

## 7. 서비스 테스트

```bash
# Backend 헬스체크
curl http://localhost:8081

# ML 서비스 헬스체크
curl http://localhost:8000

# Nginx 통합 테스트
curl http://YOUR_EC2_IP
```

## 문제 해결

### 포트 확인
```bash
sudo netstat -tlnp | grep -E '(8081|8000|80)'
```

### 서비스 재시작
```bash
sudo systemctl restart voyz-backend
sudo systemctl restart voyz-ml
sudo systemctl restart nginx
```

### 방화벽 상태
```bash
sudo ufw status
```