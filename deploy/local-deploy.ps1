# VOYZ 로컬 배포 스크립트 (PowerShell)
# 사용법: .\deploy\local-deploy.ps1

$EC2_HOST = "13.125.251.36"
$KEY_PATH = "~/Downloads/voyz-deploy.pem"

Write-Host "=== VOYZ 배포 시작 ===" -ForegroundColor Green

# 1. Spring Boot 빌드
Write-Host "1. Spring Boot 빌드 중..." -ForegroundColor Yellow
Set-Location backend
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "빌드 실패!" -ForegroundColor Red
    exit 1
}
Set-Location ..

# 2. JAR 파일 EC2로 복사
Write-Host "2. JAR 파일 EC2로 복사 중..." -ForegroundColor Yellow
scp -i $KEY_PATH backend/target/*.jar ubuntu@${EC2_HOST}:~/app/

# 3. ML 서비스 파일 복사
Write-Host "3. ML 서비스 파일 복사 중..." -ForegroundColor Yellow
scp -i $KEY_PATH -r ml/* ubuntu@${EC2_HOST}:~/ml-app/

# 4. 환경변수 설정 및 서비스 재시작
Write-Host "4. 서비스 재시작 중..." -ForegroundColor Yellow
ssh -i $KEY_PATH ubuntu@${EC2_HOST} @"
# Backend 재시작
sudo pkill -f 'java.*voiz' || true
cd ~/app
nohup java -jar -Dspring.profiles.active=prod -Duser.timezone=Asia/Seoul -Djwt.secret="534807D07902DE69EA0A8A4C5C62C6806197CC3CC162CD1AA5BBC58DB0AE67AC" *.jar > app.log 2>&1 &

# ML 서비스 재시작
sudo pkill -f 'uvicorn.*main:app' || true
cd ~/ml-app
python3 -m venv venv || true
source venv/bin/activate
pip install -r requirements.txt
echo "OPENAI_API_KEY=$env:OPENAI_API_KEY" > .env
nohup venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000 > ml.log 2>&1 &

# 상태 확인
sleep 5
curl -f http://localhost:8081 || echo "Backend 시작 중..."
curl -f http://localhost:8000 || echo "ML 서비스 시작 중..."
"@

Write-Host "=== 배포 완료 ===" -ForegroundColor Green
Write-Host "웹사이트: http://${EC2_HOST}" -ForegroundColor Cyan