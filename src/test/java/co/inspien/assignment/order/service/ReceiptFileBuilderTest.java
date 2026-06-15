package co.inspien.assignment.order.service;
import co.inspien.assignment.order.dto.OrderRecord;
import co.inspien.assignment.order.dto.ReceiptFile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("영수증 파일 생성기")
class ReceiptFileBuilderTest {

    // 2026-06-15 12:00:00 (Asia/Seoul) 로 시간 고정 → 파일명 결정적 검증
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-15T03:00:00Z"), SEOUL);
    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    private final ReceiptFileBuilder builder = new ReceiptFileBuilder(FIXED_CLOCK, "문영훈");

    private static final List<OrderRecord> RECORDS = List.of(
            new OrderRecord("USER1", "홍길동", "서울특별시 금천구", "ITEM1", "청바지", 21000),
            new OrderRecord("USER2", "유관순", "서울특별시 구로구", "ITEM2", "티셔츠", 15800)
    );
    private static final List<String> ORDER_IDS = List.of("A113", "B114");

    private static final String EXPECTED_CONTENT =
            "A113^USER1^ITEM1^KEY999^홍길동^서울특별시 금천구^청바지^21000\n" +
            "B114^USER2^ITEM2^KEY999^유관순^서울특별시 구로구^티셔츠^15800\n";

    @Test
    @DisplayName("파일명은 INSPIEN_{참여자명}_{yyyyMMddHHmmss}.txt 형식이다")
    void buildsFilename() {
        ReceiptFile file = builder.build(RECORDS, ORDER_IDS, "KEY999");

        assertThat(file.filename()).isEqualTo("INSPIEN_문영훈_20260615120000.txt");
    }

    @Test
    @DisplayName("내용은 레코드별 1행, ^ 구분·\\n 줄끝, 스펙 필드 순서를 따른다")
    void buildsContentLines() {
        ReceiptFile file = builder.build(RECORDS, ORDER_IDS, "KEY999");

        assertThat(new String(file.content(), EUC_KR)).isEqualTo(EXPECTED_CONTENT);
    }

    @Test
    @DisplayName("내용은 EUC-KR로 인코딩된다 (UTF-8 아님)")
    void encodesContentInEucKr() {
        ReceiptFile file = builder.build(RECORDS, ORDER_IDS, "KEY999");

        assertThat(file.content()).isEqualTo(EXPECTED_CONTENT.getBytes(EUC_KR));
        // 한글이 있으므로 EUC-KR과 UTF-8 바이트는 서로 달라야 한다
        assertThat(file.content()).isNotEqualTo(EXPECTED_CONTENT.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("레코드 수와 ORDER_ID 수가 다르면 거부한다")
    void rejectsMismatchedCounts() {
        assertThatThrownBy(() -> builder.build(RECORDS, List.of("A113"), "KEY999"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
