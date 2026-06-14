package co.inspien.assignment.scenario1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("채번기")
class IdGeneratorTest {

    private final IdGenerator generator = new IdGenerator();

    @RepeatedTest(20)
    @DisplayName("생성 ID는 '대문자 1자 + 숫자 3자리' 형식이다")
    void generated_id_matches_format() {
        assertThat(generator.generate()).matches("[A-Z]\\d{3}");
    }

    @Test
    @DisplayName("1000번 연속 생성 시 형식을 모두 만족한다")
    void all_ids_match_format_in_bulk() {
        for (int i = 0; i < 1000; i++) {
            assertThat(generator.generate()).matches("[A-Z]\\d{3}");
        }
    }

    @Test
    @DisplayName("100스레드 동시 생성 시 NPE 등 예외 없이 모두 유효한 ID를 반환한다")
    void concurrent_generation_produces_valid_ids() throws InterruptedException {
        int threads = 100;
        Set<String> results = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threads);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        results.add(generator.generate());
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        latch.await();

        assertThat(results).allMatch(id -> id.matches("[A-Z]\\d{3}"));
    }
}
