package co.inspien.assignment.bootstrap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 제공 API 접속정보(AES-128/ECB/PKCS5Padding, Base64) 복호화기.
 *
 * <p>복호화 키 = 전화번호 문자열(UTF-8) → SHA-1 해싱 → 앞 16바이트(128bit).
 * 키는 앱 생존 동안 고정이므로 생성 시 1회 파생해 재사용한다
 * (ORDER_TB_CONN / SHIPMENT_TB_CONN / FTP_CONN 세 필드를 같은 키로 복호화).
 */
public class ConnectionInfoDecryptor {

    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private final SecretKeySpec key;

    public ConnectionInfoDecryptor(String phoneNumber) {
        this.key = deriveKey(phoneNumber);
    }

    /**
     * Base64로 인코딩된 AES 암호문을 복호화해 평문 문자열(UTF-8)로 돌려준다.
     */
    public String decrypt(String base64Ciphertext) {
        try {
            byte[] ciphertext = Base64.getDecoder().decode(base64Ciphertext);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("접속정보 복호화 실패", e);
        }
    }

    private static SecretKeySpec deriveKey(String phoneNumber) {
        try {
            byte[] sha1 = MessageDigest.getInstance("SHA-1")
                    .digest(phoneNumber.getBytes(StandardCharsets.UTF_8));
            byte[] key16 = Arrays.copyOf(sha1, 16); // 앞 16바이트 = AES-128 키
            return new SecretKeySpec(key16, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("복호화 키 파생 실패", e);
        }
    }
}
