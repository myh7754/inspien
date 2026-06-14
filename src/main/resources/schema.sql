-- ORDER_TB: 실시간 주문 적재 (시나리오1)
-- STATUS: 'N'=배치 미전송, 'Y'=배치 전송완료 (시나리오2 연결고리)
BEGIN
    EXECUTE IMMEDIATE '
        CREATE TABLE ORDER_TB (
            ORDER_ID      VARCHAR2(4)   NOT NULL,
            APPLICANT_KEY VARCHAR2(100) NOT NULL,
            USER_ID       VARCHAR2(100),
            ITEM_ID       VARCHAR2(100),
            NAME          VARCHAR2(200),
            ADDRESS       VARCHAR2(500),
            ITEM_NAME     VARCHAR2(200),
            PRICE         NUMBER,
            STATUS        VARCHAR2(1)   DEFAULT ''N'' NOT NULL,
            CONSTRAINT PK_ORDER_TB PRIMARY KEY (ORDER_ID, APPLICANT_KEY)
        )
    ';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF; -- -955: 이미 존재하는 테이블, 무시
END;

-- SHIPMENT_TB: 운송사 배치 적재 (시나리오2)
BEGIN
    EXECUTE IMMEDIATE '
        CREATE TABLE SHIPMENT_TB (
            SHIPMENT_ID   VARCHAR2(4)   NOT NULL,
            APPLICANT_KEY VARCHAR2(100) NOT NULL,
            ORDER_ID      VARCHAR2(4),
            ITEM_ID       VARCHAR2(100),
            ADDRESS       VARCHAR2(500),
            CONSTRAINT PK_SHIPMENT_TB PRIMARY KEY (SHIPMENT_ID, APPLICANT_KEY)
        )
    ';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
