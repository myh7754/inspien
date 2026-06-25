package co.inspien.assignment.order.parser;
import co.inspien.assignment.order.dto.OrderRecord;
import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주문 XML(HEADER 1:N ITEM) 파서.
 * HEADER와 ITEM을 USER_ID로 join해 flat OrderRecord 목록으로 변환한다.
 * 입력 XML은 루트 태그 없이 HEADER/ITEM이 나란히 오므로 래핑 후 파싱.
 */
@Component
public class OrderXmlParser {

    public List<OrderRecord> parse(String xml) {
        try {
            Document doc = parseXml("<root>" + xml + "</root>");
            Map<String, HeaderInfo> headers = collectHeaders(doc);
            return buildRecords(doc, headers);
        } catch (InspienException e) {
            throw e; // 검증 성격(예: HEADER 없는 ITEM) 예외는 그대로 전파
        } catch (Exception e) {
            throw new IllegalArgumentException("주문 XML 파싱 실패", e);
        }
    }

    private Document parseXml(String xml) throws Exception {
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(bytes));
    }

    private Map<String, HeaderInfo> collectHeaders(Document doc) {
        Map<String, HeaderInfo> map = new HashMap<>();
        NodeList headers = doc.getElementsByTagName("HEADER");
        for (int i = 0; i < headers.getLength(); i++) {
            Element h = (Element) headers.item(i);
            String userId = text(h, "USER_ID");
            map.put(userId, new HeaderInfo(userId, text(h, "NAME"), text(h, "ADDRESS")));
        }
        return map;
    }

    private List<OrderRecord> buildRecords(Document doc, Map<String, HeaderInfo> headers) {
        List<OrderRecord> records = new ArrayList<>();
        NodeList items = doc.getElementsByTagName("ITEM");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String userId = text(item, "USER_ID");
            HeaderInfo header = headers.get(userId);
            if (header == null) { // 대응하는 HEADER 없는 ITEM = 불완전 입력 → 조용히 버리지 않고 거부
                throw new InspienException(ErrorCode.VALIDATION_ERROR,
                        "대응하는 HEADER가 없는 ITEM이 있습니다: USER_ID=" + userId);
            }

            records.add(new OrderRecord(
                    userId,
                    header.name(),
                    header.address(),
                    text(item, "ITEM_ID"),
                    text(item, "ITEM_NAME"),
                    Integer.parseInt(text(item, "PRICE"))
            ));
        }
        return records;
    }

    private String text(Element el, String tag) {
        NodeList nodes = el.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private record HeaderInfo(String userId, String name, String address) {}
}
