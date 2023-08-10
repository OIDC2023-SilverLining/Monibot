# Monibot
오픈 인프라 개발 경진대회 (OIDC 2023) Chat GPT를 활용한 Monitoring ChatBot

## 디렉토리 구조
```
.
├── script: 서버 개발 전 프로토타입 개발을 위한 스크립트들
├── src: 서버 코드
│   ├── slackAppServer: Slack Chat Bot Server 코드
│   └── cacheServer: cache Server 코드
```

## 코드 형성 관리
- feature 추가시 issue를 먼저 등록하고 작업
- branch명 규칙: `{issue번호}-{microservice명}-{작업 내용}`
- main에 직접 commit하는 경우는 지양
- commit명의 prefix는 "feat", "docs", "refactor", "deploy"중 하나를 선택

## 주요기능

```

1. 모니터링 (“/monitor”)
- 챗봇을 활용한 모니터링 쿼리 실행 및 시각화 작업 자동화
- 자연어 입력으로 모니터링 쿼리 생성
- PromQL 쿼리 생성 및 실행
- Grafana 대시보드 생성 및 URL 반환

2. GPT Cache
- 유사한 내용을 질문하는 경우 캐싱되어 있던 promql query를 사용하도록 하여 비용 절감
- 실제 의미에 대한 유사도를 파악하기 위해 딥러닝 모델을 사용

3. Alert Metric (“/alert-metric”)
- 지정한 메트릭이 설정된 임계값 이상 혹은 이하일 경우 알람 발생
- 일시적인 스파이크나 짧은 기간의 이상 현상으로 인한 불필요한 알람 방지 기능 탑재

4. Alert Loki 기능  (“/alert-loki”)
- 파드에서 발생하는 에러 로그를 분석하여 상태 진단 수행 
- LogQL에 대한 응답 중 Error Log가 있으면, Chatgpt API를 이용하여 해결 방안을 얻은 후 Slack 전송


```

## 구조
![image](img/1.png)

### 모니터링
![image](img/2.png)

### GPT Cache
![image](img/3.png)

### Alert Metric
![image](img/4.png)

### Alert Loki
![image](img/5.png)

## 사용자 UI
![image](img/7.png)

## 테스트하기

```bash

# git clone 받기

git clone https://github.com/OIDC2023-SilverLining/monibot.git

# monibot 폴더로 이동

cd monibot

# 차트 설치

helm install monibot ./monibot

# 설치 성공 여부 확인

helm list
kubectl get pods

```

## 활용 기술

- Language - java, python
- slackAppServer: Spring Boot, Slack API, Prometheus API, Grafana Dashboard API
- gptCache: Python FastAPI, SQLite VSS(Vector Similarity Search)
- Database - Postgres, sqllite3

## etc
- [DEMO VIDEO URL](https://drive.google.com/file/d/1GTDzRXXI5YavTSLDWHPtPMT03reMGFq0/view?usp=sharing)
- Slack App과 서버 연결 가이드 [WIKI](https://github.com/OIDC2023-SilverLining/monibot/wiki/Slack-App%EA%B3%BC-Server-%EC%97%B0%EA%B2%B0-Guide)
- 다른 세부사항은 `WIKI` 및 `Projects` 패널 참조 : [WIKI](https://github.com/open-tube/open-tube/wiki)
