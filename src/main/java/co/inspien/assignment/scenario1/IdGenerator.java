package co.inspien.assignment.scenario1;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ORDER_ID / SHIPMENT_ID 채번기.
 * 형식: 대문자 1자 + 숫자 3자리 (예: A113). 총 26,000가지.
 * PK 충돌 시 호출 측에서 재호출(NFR-ID-02).
 */
public class IdGenerator {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        char letter = LETTERS.charAt(rnd.nextInt(LETTERS.length()));
        int digits = rnd.nextInt(1000);
        return String.format("%c%03d", letter, digits);
    }
}
