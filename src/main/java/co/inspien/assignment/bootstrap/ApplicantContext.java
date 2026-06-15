package co.inspien.assignment.bootstrap;

/**
 * 부팅 시 확보되는 참여자 식별 정보.
 *
 * <p>{@code key}는 제공 API 응답의 {@code APPLICANT_KEY}(ORDER_TB·영수증에 기록),
 * {@code name}은 참여자명(영수증 파일명에 사용)이다.
 * raw String 대신 타입으로 묶어 빈 주입 시 모호성을 없애고 의도를 드러낸다.
 */
public record ApplicantContext(String key, String name) {}
