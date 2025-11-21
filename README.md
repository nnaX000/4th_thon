<div align="center">
  <h1>🚇 4호선톤 – CRUX 백엔드 레포</h1>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
    <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"/>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  </p>

  <p>대화를 흩날리지 말고, 자산화하세요.<br>AI 기반 학습 구조화 플랫폼</p>
</div>

<hr>

# 📌 프로젝트 소개

**2025 멋쟁이 사자처럼 4호선톤 특별상**을 수상하였습니다.

CRUX는 AI 대화를 기반으로 학습 내용을 자동으로 구조화하고, 다시 꺼내볼 수 있는 **학습 자산**으로 전환하는 학습 구조화 도구입니다.

GPT 대화 속에서 흩어진 정보들을 자동으로 정제해 '새로운 개념’, ‘교정된 개념’, ‘추천 문서’를 한 번에 정리해주며 사용자의 학습 히스토리를 기반으로 성장하는 학습 경험을 제공합니다.

---

## 주요 기능
<br>

| 기능 | 설명 |
|------|------|
| **대화 기반 핵심 개념 자동 요약** | GPT 대화를 분석해 개념·오개념·교정 내용 자동 추출 |
| **공식 문서 매칭** | 최신 문서 및 공식 레퍼런스와 자동 연결 |
| **주제별/통합 리포트 자동 생성** | 분석된 대화를 기반으로 문서 형태로 재구성 |
| **내보내기 & 저장 기능** | PDF · Markdown · Notion으로 내보내기 지원 |
| **히스토리 관리** | 히트맵 기반 학습 패턴 시각화 |

---

# 💡 담당한 개발 내용
<br>

> **GPT 링크 크롤링부터 분석 엔진, SSE 실시간 스트리밍, 리포트 생성**까지 핵심 백엔드 기능 전반을 직접 구현했습니다.
<br><br>

## 1. GPT 링크 기반 대화 크롤링
- 공유된 GPT 대화 링크에서 share ID를 파싱해 원본 대화 데이터를 자동 수집
- jina.ai 프록시를 이용해 CORS 우회 크롤링 처리
- 수집된 대화를 텍스트 정제(escape 제거, 노이즈 제거) 후 저장하는 파이프라인 구현
<br>

## 2. GPT API 분석 엔진 개발
- 대화 속에서 ‘새 개념’, ‘교정 개념’, ‘참고 자료’ 자동 추출
- 의미 기반 분석 로직 설계 및 모델 프롬프팅 구성  
<br>

## 3. SSE 기반 실시간 분석 단계 스트리밍
- 분석 과정(수집 → 분석 → 정리)을 단계별로 프론트에 실시간 전송
- Server-Sent Events(SSE) 기반 비동기 스트리밍 API 구축  
<br>

## 4. 리포트 생성 시스템 개발
- 분석된 데이터를 정리해 **통합 리포트 / 주제별 리포트 자동 생성**
- 정규화 json 형태로 반환
<br><br>

---

## 개발 기간
2025.01 ~ 2025.02

---

## 팀 구성 및 역할
<br>

| 이름 | 역할 |
|------|------|
| **하지민** | 기획 디자인 |
| **이보연** | 프론트엔드 |
| **윤세연** | 프론트엔드 |
| **김나영** | 백엔드 |
| **김도윤** | 백엔드 |

<br><br>
