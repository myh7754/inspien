package co.inspien.assignment.scenario1;

import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 주문 레코드를 영수증 파일(파일명 + 내용)로 변환한다.
 * - 내용 1행 포맷(FR-S1-08): ORDER_ID^USER_ID^ITEM_ID^APPLICANT_KEY^NAME^ADDRESS^ITEM_NAME^PRICE\n
 * - 파일명(FR-S1-07): INSPIEN_{참여자명}_{yyyyMMddHHmmss}.txt
 * - 인코딩: 수신측(쇼핑몰 회계) 규약에 맞춰 EUC-KR (제공 SAMPLE_DATA가 EUC-KR)
 */
@RequiredArgsConstructor
public class ReceiptFileBuilder {

    /** 수신측 규약. 다른 인코딩이 필요하면 이 한 줄만 교체. */
    private static final Charset CHARSET = Charset.forName("EUC-KR");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Clock clock;
    private final String applicantName;

    public ReceiptFile build(List<OrderRecord> records, List<String> orderIds, String applicantKey) {
        if (records.size() != orderIds.size()) {
            throw new IllegalArgumentException(
                    "레코드 수와 ORDER_ID 수가 다릅니다: " + records.size() + " vs " + orderIds.size());
        }

        StringBuilder content = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            OrderRecord r = records.get(i);
            content.append(orderIds.get(i)).append('^')
                    .append(r.userId()).append('^')
                    .append(r.itemId()).append('^')
                    .append(applicantKey).append('^')
                    .append(r.name()).append('^')
                    .append(r.address()).append('^')
                    .append(r.itemName()).append('^')
                    .append(r.price()).append('\n');
        }

        String filename = "INSPIEN_" + applicantName + "_"
                + LocalDateTime.now(clock).format(TIMESTAMP) + ".txt";

        return new ReceiptFile(filename, content.toString().getBytes(CHARSET));
    }
}
