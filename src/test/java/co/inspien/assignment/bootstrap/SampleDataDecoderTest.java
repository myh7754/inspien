package co.inspien.assignment.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SAMPLE_DATA 필드는 EUC-KR로 인코딩된 XML을 Base64로 감싼 문자열이다.
 * SampleDataDecoder는 이것을 풀어 UTF-8 XML 문자열로 돌려준다.
 */
@DisplayName("SAMPLE_DATA 디코더")
class SampleDataDecoderTest {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");

    @Test
    @DisplayName("Base64(EUC-KR XML)를 디코딩하면 한글이 깨지지 않은 XML 문자열이 나온다")
    void decodes_base64_euckr_xml_to_utf8_string() {
        String originalXml = "<?xml version=\"1.0\" encoding=\"EUC-KR\"?>" +
                "<ORDER><USER_ID>user1</USER_ID><NAME>홍길동</NAME><ADDRESS>서울시 강남구</ADDRESS></ORDER>";

        // Inspien API가 하는 것: XML을 EUC-KR 바이트로 → Base64 인코딩
        String base64SampleData = Base64.getEncoder()
                .encodeToString(originalXml.getBytes(EUC_KR));

        SampleDataDecoder decoder = new SampleDataDecoder();
        String result = decoder.decode(base64SampleData);

        assertThat(result).contains("홍길동");
        assertThat(result).contains("서울시 강남구");
        assertThat(result).contains("<ORDER>");
    }
}
