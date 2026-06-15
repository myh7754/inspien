package co.inspien.assignment.scenario1;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 영수증 파일을 FTP 서버에 전송한다(FR-S1-06).
 *
 * <p>제공된 FTP 계정({@code recruit})은 <b>업로드 전용</b>이다(STOR만 허용, RETR/DELE는 550 거부).
 * 따라서 업로드 후 삭제로 보상하는 것은 불가능하며, 보상은 연산 순서 + DB 트랜잭션 롤백으로 처리한다(NFR-TX).
 */
@Component
@RequiredArgsConstructor
public class ReceiptFtpSender {

    private static final Logger log = LoggerFactory.getLogger(ReceiptFtpSender.class);

    /** 파일명에 한글(참여자명)이 포함되므로 제어채널 인코딩을 EUC-KR로 맞춘다. */
    private static final String CONTROL_ENCODING = "EUC-KR";

    private final FtpProperties ftp;

    /** 영수증 파일을 업로드하고 업로드된 파일명을 반환한다. */
    public String upload(ReceiptFile file) {
        FTPClient client = new FTPClient();
        try {
            connectAndLogin(client);
            client.enterLocalPassiveMode();           // 방화벽 뒤 서버 대응
            client.setFileType(FTP.BINARY_FILE_TYPE); // \n 등 바이트 변형 방지
            changeWorkingDirectory(client);

            boolean stored;
            try (InputStream in = new ByteArrayInputStream(file.content())) {
                stored = client.storeFile(file.filename(), in);
            }
            if (!stored) {
                throw new InspienException(ErrorCode.FTP_UPLOAD_ERROR,
                        "영수증 업로드 실패: " + file.filename() + " (reply=" + client.getReplyString().trim() + ")");
            }
            client.logout();
            log.info("영수증 FTP 업로드 완료: {}", file.filename());
            return file.filename();
        } catch (IOException e) {
            throw new InspienException(ErrorCode.FTP_UPLOAD_ERROR, e);
        } finally {
            disconnectQuietly(client);
        }
    }

    private void connectAndLogin(FTPClient client) throws IOException {
        client.setControlEncoding(CONTROL_ENCODING);
        client.connect(ftp.url(), ftp.port());

        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new InspienException(ErrorCode.FTP_UPLOAD_ERROR, "FTP 연결 거부 (reply=" + reply + ")");
        }
        if (!client.login(ftp.username(), ftp.password())) {
            throw new InspienException(ErrorCode.FTP_UPLOAD_ERROR, "FTP 로그인 실패: " + ftp.username());
        }
    }

    private void changeWorkingDirectory(FTPClient client) throws IOException {
        if (ftp.path() != null && !ftp.path().isBlank()) {
            client.changeWorkingDirectory(ftp.path());
        }
    }

    private void disconnectQuietly(FTPClient client) {
        if (client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                log.warn("FTP 연결 종료 중 오류(무시): {}", e.getMessage());
            }
        }
    }
}
