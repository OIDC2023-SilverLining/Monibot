# Monibot
오픈 인프라 개발 경진대회 (OIDC 2023) Chat GPT를 활용한 Monitoring ChatBot

# 디렉토리 구조
```
.
├── script: 서버 개발 전 프로토타입 개발을 위한 스크립트들
├── src: 서버 코드
│   ├── slackAppServer: Slack Chat Bot Server 코드
│   └── cacheServer: cache Server 코드
```

# 코드 형성 관리
- feature 추가시 issue를 먼저 등록하고 작업
- branch명 규칙: `{issue번호}-{microservice명}-{작업 내용}`
- main에 직접 commit하는 경우는 지양
- commit명의 prefix는 "feat", "docs", "refactor", "deploy"중 하나를 선택
