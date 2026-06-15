package co.inspien.assignment.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 영수증 FTP 전송 대상 접속정보.
 * 제공 API 응답의 FTP_CONN을 복호화한 값을 application-secret.yml(inspien.ftp)에 보관한다.
 * (커밋 금지 — gitignore 처리됨)
 */
@ConfigurationProperties(prefix = "inspien.ftp")
public record FtpProperties(
        String url,
        int port,
        String username,
        String password,
        String path
) {
}
