package co.inspien.assignment.bootstrap;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * 제공 API 응답의 SAMPLE_DATA 필드 디코더.
 * SAMPLE_DATA = EUC-KR로 인코딩된 주문 XML을 Base64로 감싼 문자열.
 * Base64 디코딩 → EUC-KR 바이트 → Java String(유니코드 문자)으로 변환한다.
 */
public class SampleDataDecoder {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    public String decode(String base64SampleData) {
        byte[] eucKrBytes = Base64.getDecoder().decode(base64SampleData);
        return new String(eucKrBytes, EUC_KR);
    }
}
