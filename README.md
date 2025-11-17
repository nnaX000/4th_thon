<div align="center">
  <h1>🚇 4호선톤 – CRUX 백엔드 레포</h1>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
    <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"/>
  </p>

  <p>대화를 흩날리지 말고, 자산화하세요.<br>AI 기반 학습 구조화 플랫폼</p>
</div>

<hr>

<h2>📌 Overview</h2>
<div>
  <p>
    CRUX는 AI 대화를 기반으로 학습 내용을 자동으로 구조화하고, 다시 꺼내볼 수 있는 
    <b>학습 자산</b>으로 전환하는 학습 구조화 도구입니다.
  </p>
  <ul>
    <li>대화 속 핵심 개념 자동 요약</li>
    <li>오개념 교정 + 공식 문서 매칭</li>
    <li>주제별 자동 리포트 생성</li>
    <li>저장 · 내보내기 · 히스토리 관리</li>
  </ul>
  <p><b>목표:</b> 대화를 흘려보내지 않고 자산화하는 경험 제공</p>
</div>



<h2>🛠 Backend Responsibilities</h2>

- **GPT 공유 링크 기반 대화 크롤링**

  공유된 GPT 링크를 파싱해 전체 대화 메시지를 자동 수집·정제하는 크롤러 및 백엔드 파이프라인 구현.

- **GPT API 분석 엔진 개발**
  
  대화에서 ‘새로 배운 개념’, ‘교정된 개념’, ‘추천 자료’를 자동 추출하는 분석 로직 설계 및 구현.

- **SSE 기반 실시간 분석 진행 상황 전송**
  
  GPT 분석 단계별 진행 상태를 Server-Sent Events(SSE)로 프론트에 실시간 스트리밍하는 API 개발.

- **통합/주제별 리포트 자동 생성**
  
  분석된 결과를 문서화하여 ‘통합 리포트’, ‘주제별 리포트’ 두 종류의 문서를 자동 생성하는 기능 개발.


<h2>🎯 Problem</h2>
<div>
  <h3>1) 대화는 많은데, 배움은 없다</h3>
  <ul>
    <li>GPT 사용량 대비 지식 축적 어려움</li>
    <li>오개념 · 잘못된 정보 · 구버전 코드 혼재</li>
    <li>긴 대화는 복습 비용 증가로 학습 효율 감소</li>
  </ul>

  <h3>2) AI 의존 역설</h3>
  <ul>
    <li>AI 요약은 편하지만 <b>수동적 소비</b>에서 끝남</li>
    <li>정확성 검증 및 최신 정보 확보 어려움</li>
    <li>선택적 학습을 돕는 구조화 도구 부재</li>
  </ul>
</div>

<h2>🔍 Key Features</h2>
<div>
  <h3>1️⃣ 대화 링크 자동 구조화</h3>
  <ul>
    <li>링크 붙여넣기만 하면 분석 시작</li>
    <li>메시지 의미 변화 기반 주제 자동 감지 및 분할</li>
  </ul>

  <h3>2️⃣ 핵심 개념 자동 정리</h3>
  <ul>
    <li>새 개념 · 반복 개념 · 혼동 개념 자동 추출</li>
    <li>공식 문서 매칭을 통한 정확성 강화</li>
    <li>LLM 미포함 최신 문서도 자동 탐색</li>
  </ul>

  <h3>3️⃣ 리포트 생성 및 내보내기</h3>
  <ul>
    <li>주제별 / 통합 리포트 생성</li>
    <li>PDF · Markdown · Notion으로 내보내기 지원</li>
    <li>내부 저장 기능 지원</li>
  </ul>

  <h3>4️⃣ 히스토리 기반 학습 관리</h3>
  <ul>
    <li>히트맵 캘린더로 생성된 리포트 시각화</li>
    <li>학습 패턴 분석 및 동기 부여</li>
    <li>꾸준함을 기록으로 확인 가능</li>
  </ul>
</div>

<hr>

<h2>🧑‍💻 Team</h2>
<div>
  <ul>
    <li><b>PD:</b> 하지민</li>
    <li><b>FE:</b> 이보연, 윤세연</li>
    <li><b>BE:</b> 김나영, 김도윤</li>
  </ul>
</div>
