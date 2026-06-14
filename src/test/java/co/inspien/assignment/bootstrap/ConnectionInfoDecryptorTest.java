package co.inspien.assignment.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 제공 API가 내려주는 접속정보(ORDER_TB_CONN, SHIPMENT_TB_CONN, FTP_CONN)는
 * AES-128(ECB, PKCS5Padding)으로 암호화되어 Base64로 인코딩되어 온다.
 * 복호화 키 = 본인 전화번호(UTF-8) → SHA-1 → 앞 16바이트(128bit).
 *
 * 이 테스트는 구현과 코드를 공유하지 않는 표준 JCE를 '독립 오라클'로 써서
 * 스펙 규칙대로 암호화한 값을, 구현이 원문 그대로 복호화하는지 검증한다.
 */
@DisplayName("접속정보 AES 복호화기")
class ConnectionInfoDecryptorTest {

    private static final String PHONE = "010-1234-5678";

    @Test
    @DisplayName("올바른 전화번호로 암호화된 접속정보를 원문 그대로 복호화한다")
    void decrypts_payload_encrypted_with_phone_derived_key() throws Exception {
        String plaintext = "{\"DRIVER\":\"mysql\",\"URL\":\"jdbc:mysql://host:3306/db\",\"USER\":\"u\",\"PASSWORD\":\"p\"}";
        String base64Ciphertext = encryptPerSpec(plaintext, PHONE);

        ConnectionInfoDecryptor decryptor = new ConnectionInfoDecryptor(PHONE);
        String result = decryptor.decrypt(base64Ciphertext);

        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("전화번호(키)가 틀리면 복호화가 실패해 예외를 던진다")
    void throws_when_decrypted_with_wrong_phone_number() throws Exception {
        String plaintext = "{\"URL\":\"jdbc:mysql://host:3306/db\"}";
        String base64Ciphertext = encryptPerSpec(plaintext, PHONE);

        // 다른 전화번호 → 다른 키 → 패딩 검증 실패로 복호화가 터져야 한다 (조용한 손상 금지)
        ConnectionInfoDecryptor wrongKeyDecryptor = new ConnectionInfoDecryptor("010-9999-0000");

        assertThatThrownBy(() -> wrongKeyDecryptor.decrypt(base64Ciphertext))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- 독립 오라클: 과제 스펙 규칙대로 표준 JCE로 암호화 (구현 코드 미사용) ---
    private static String encryptPerSpec(String plaintext, String phone) throws Exception {
        byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(phone.getBytes(StandardCharsets.UTF_8));
        byte[] key16 = Arrays.copyOf(sha1, 16);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16, "AES"));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
