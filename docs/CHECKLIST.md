# 구현 체크리스트 — 기능 단위

> **용도**: 기능 단위 작업 추적. **1 항목 ≈ 1 커밋**(과제가 요구한 작업단위 커밋).
> 시간 배분은 `SCHEDULE.md`, 요구사항 정의는 `PRD.md`, 설계는 `design.md` 참조.
>
> 표기: `[TDD]` = 테스트 먼저 / `[IT]` = 통합테스트 / `[—]` = 단순작업. `→` = 선행 의존.

---

## Phase 0. 환경 ✅ 완료
- [x] Spring Boot 4.1 / Java 21 / Gradle 스켈레톤
- [x] 의존성(webmvc, jdbc, validation, lombok, jackson-xml, commons-net)
- [x] `DataSourceAutoConfiguration` 임시 exclude (부팅 가능화)

## Phase 1. 설계 (진행 중)
- [x] PRD 작성 (`docs/PRD.md`)
- [ ] design 확정 (`docs/design.md`) — 채번 알고리즘 TBD 결정 후 close
- [ ] 아키텍처 before/after 다이어그램 초안

## Phase 2. Bootstrap — 접속정보 확보 ⚠️ 모든 작업의 출발점
- [x] `[TDD]` AES-128 복호화기 (`ConnectionInfoDecryptor`) — 키=전화번호 SHA-1 앞16B `FR-BOOT-02`
- [ ] `[TDD]` SAMPLE_DATA 디코더 (base64 → EUC-KR XML) `FR-BOOT-03`
- [x] `[—]` 개인정보/참여자명 외부설정 분리 (`application-secret`) `C-01`
- [ ] `[IT]` 제공 API 호출 (`ProvisioningClient`, Basic Auth) `FR-BOOT-01`
- [ ] `[—]` 복호화 → DBMS 확정 → JDBC 드라이버 추가, exclude 제거 `C-02`
- [ ] `[—]` ORDER_TB / SHIPMENT_TB 생성(DDL 실행), 연결 확인

## Phase 3. 시나리오 1 — 실시간 주문 (REST) → Phase 2
- [ ] `[TDD]` XML 1:N → flat 파서 (`OrderXmlParser`) `FR-S1-02`
- [ ] `[TDD]` 입력 검증 (`OrderValidator`) `FR-S1-01-a`
- [ ] `[TDD]` 채번기 (`IdGenerator`, 대문자1+숫자3, 동시성) `NFR-ID-01/02`
- [ ] `[IT]` ORDER_TB 적재 (`OrderRepository`, STATUS='N' 고정) `FR-S1-03/03-a`
- [ ] `[IT]` 영수증 파일 생성 + FTP 전송 (`ReceiptFtpSender`) `FR-S1-06/07/08`
- [ ] `[TDD]` 오케스트레이션 + 트랜잭션 경계(전략 B) (`OrderService`) `FR-S1-09, NFR-TX-01`
- [ ] `[—]` REST 수신 + 응답 JSON (`OrderController`) `FR-S1-09-a`
- [ ] `[IT]` happy path 통합 (XML POST → DB+FTP → SUCCESS)

## Phase 4. 시나리오 2 — 운송 배치 (스케줄러) → Phase 3
- [ ] `[IT]` STATUS='N' 조회 + SHIPMENT 적재 (`ShipmentRepository`) `FR-S2-02/03`
- [ ] `[TDD]` 건별 처리 + STATUS='Y' 갱신 (`ShipmentBatchService`) `FR-S2-05/05-a`
- [ ] `[—]` 5분 스케줄러 (`ShipmentScheduler`) `FR-S2-01`
- [ ] `[IT]` 배치 멱등성 (재실행 중복적재 없음) `NFR-ID-03`

## Phase 5. 견고성 — 보상 / 운영 (NFR) → Phase 3,4
- [ ] `[TDD]` 보상 트랜잭션 (commit 실패 시 FTP 삭제) `NFR-TX-02/03`
- [ ] `[—]` 모니터링 로그 (요청단위 로컬파일) `NFR-LOG-01/02`
- [ ] `[—]` 예외 계층 + 단계별 처리 (JDBC/FTP 구분) `NFR-EXC-01/02/03`
- [ ] `[IT]` 강제 실패 시나리오 (FTP 차단 등) 검증 `AC-06`

## Phase 6. 마무리 / 제출 → 전체
- [ ] `[—]` 아키텍처 before/after 이미지 확정 `S4`
- [ ] `[—]` README (실행법, 설계 요약)
- [ ] `[IT]` 데모 리허설 (참여자명+당일날짜 DB 조회 / FTP 파일 확인) `AC-07`
- [ ] `[—]` 커밋 이력 정돈
- [ ] `[—]` 면접 PPT (15분, 설계과정·AI활용)
- [ ] `[—]` 메일 제출 (yejin.kim@inspien.co.kr)

---

## 의존 순서 요약
```
Phase 2 (Bootstrap) ──▶ Phase 3 (S1) ──▶ Phase 4 (S2)
                                    └──▶ Phase 5 (보상/운영) ──▶ Phase 6 (제출)
```
**Phase 2가 막히면 전부 막힘** → 토요일 최우선.
