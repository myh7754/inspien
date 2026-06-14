package co.inspien.assignment.scenario1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("주문 XML 파서")
class OrderXmlParserTest {

    private final OrderXmlParser parser = new OrderXmlParser();

    @Test
    @DisplayName("HEADER 1건 + ITEM 2건 → flat 레코드 2건, 각 행에 주문자 정보 포함")
    void parses_one_header_two_items_into_two_flat_records() {
        String xml = """
                <HEADER>
                    <USER_ID>USER01</USER_ID>
                    <NAME>홍길동</NAME>
                    <ADDRESS>서울시 강남구</ADDRESS>
                    <STATUS>N</STATUS>
                </HEADER>
                <ITEM>
                    <USER_ID>USER01</USER_ID>
                    <ITEM_ID>ITEM45</ITEM_ID>
                    <ITEM_NAME>청바지</ITEM_NAME>
                    <PRICE>95000</PRICE>
                </ITEM>
                <ITEM>
                    <USER_ID>USER01</USER_ID>
                    <ITEM_ID>ITEM15</ITEM_ID>
                    <ITEM_NAME>티셔츠</ITEM_NAME>
                    <PRICE>33000</PRICE>
                </ITEM>
                """;

        List<OrderRecord> records = parser.parse(xml);

        assertThat(records).hasSize(2);

        OrderRecord first = records.get(0);
        assertThat(first.userId()).isEqualTo("USER01");
        assertThat(first.name()).isEqualTo("홍길동");
        assertThat(first.address()).isEqualTo("서울시 강남구");
        assertThat(first.itemId()).isEqualTo("ITEM45");
        assertThat(first.itemName()).isEqualTo("청바지");
        assertThat(first.price()).isEqualTo(95000);

        OrderRecord second = records.get(1);
        assertThat(second.itemId()).isEqualTo("ITEM15");
        assertThat(second.name()).isEqualTo("홍길동"); // 주문자 정보가 복제되었는지
    }

    @Test
    @DisplayName("HEADER 여러 개 + 각각 다른 ITEM → 모두 USER_ID로 올바르게 join")
    void parses_multiple_headers_with_their_items() {
        String xml = """
                <HEADER>
                    <USER_ID>USER01</USER_ID>
                    <NAME>홍길동</NAME>
                    <ADDRESS>서울 강남</ADDRESS>
                    <STATUS>N</STATUS>
                </HEADER>
                <ITEM>
                    <USER_ID>USER01</USER_ID>
                    <ITEM_ID>ITEM01</ITEM_ID>
                    <ITEM_NAME>상품A</ITEM_NAME>
                    <PRICE>10000</PRICE>
                </ITEM>
                <HEADER>
                    <USER_ID>USER02</USER_ID>
                    <NAME>김철수</NAME>
                    <ADDRESS>부산 해운대</ADDRESS>
                    <STATUS>N</STATUS>
                </HEADER>
                <ITEM>
                    <USER_ID>USER02</USER_ID>
                    <ITEM_ID>ITEM02</ITEM_ID>
                    <ITEM_NAME>상품B</ITEM_NAME>
                    <PRICE>20000</PRICE>
                </ITEM>
                <ITEM>
                    <USER_ID>USER02</USER_ID>
                    <ITEM_ID>ITEM03</ITEM_ID>
                    <ITEM_NAME>상품C</ITEM_NAME>
                    <PRICE>30000</PRICE>
                </ITEM>
                """;

        List<OrderRecord> records = parser.parse(xml);

        assertThat(records).hasSize(3);
        assertThat(records.get(0).name()).isEqualTo("홍길동");
        assertThat(records.get(1).name()).isEqualTo("김철수");
        assertThat(records.get(2).name()).isEqualTo("김철수");
        assertThat(records.get(2).itemId()).isEqualTo("ITEM03");
    }
}
