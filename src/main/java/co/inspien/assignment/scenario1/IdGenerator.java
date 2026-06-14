package co.inspien.assignment.scenario1;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ORDER_ID / SHIPMENT_ID 채번기.
 * 형식: 대문자 1자 + 숫자 3자리 (예: A113). 총 26,000가지.
 *
 * 공유 테이블 환경이라 "마지막 번호 + 1" 같은 순차 채번의 전제가 깨진다.
 * 따라서 순서를 포기하고 랜덤으로 후보를 뽑은 뒤,
 * 실제 INSERT 시 복합 PK 충돌이 나면 호출 측에서 재호출한다(NFR-ID-02).
 * 상태가 없어 재시작·동시성에 강하고 ORDER/SHIPMENT 공용으로 쓸 수 있다.
 */
@Component
public class IdGenerator {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        char letter = LETTERS.charAt(rnd.nextInt(LETTERS.length()));
        int digits = rnd.nextInt(1000);
        return String.format("%c%03d", letter, digits);
    }
}
