# VOYZ

소규모 비즈니스를 위한 통합 마케팅 플랫폼

## 프로젝트 구조

```
VOYZ/
├── backend/                 # Spring Boot REST API
├── frontend/               # Android Kotlin 앱 (Jetpack Compose)
├── ml/                     # FastAPI 데이터 분석 서버
└── README.md
```

## 최근 업데이트 (2025.07.29)

### ✅ Frontend 구조 개선
- fragment/ → screen/ 폴더 통합 및 카테고리별 정리 완료
- CalendarComponent 분리 최적화 (498줄 → 컴포넌트별 분리)
- 불필요한 빈 폴더들 제거 (domain/, di/, viewmodel/)

각 프로젝트의 자세한 내용은 해당 폴더의 README를 참조하세요.