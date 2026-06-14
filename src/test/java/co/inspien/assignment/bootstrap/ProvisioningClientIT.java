package co.inspien.assignment.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 Inspien API를 호출하는 통합테스트.
 * application-secret.yml 에 이름·전화·이메일이 채워져 있어야 실행 가능.
 * 실행: ./gradlew integrationTest
 */
@Tag("integration")
@SpringBootTest
@DisplayName("제공 API 호출 및 복호화 통합")
class ProvisioningClientIT {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningClientIT.class);

    @Autowired ProvisioningClient provisioningClient;
    @Autowired ProvisioningProperties props;

    @Test
    @DisplayName("API 호출 시 APPLICANT_KEY와 암호화된 접속정보, SAMPLE_DATA를 수신한다")
    void fetches_all_fields_from_provisioning_api() {
        ProvisioningResponse response = provisioningClient.fetch();

        assertThat(response.applicantKey()).isNotBlank();
        assertThat(response.orderTbConn()).isNotNull();
        assertThat(response.orderTbConn().url()).isNotBlank();
        assertThat(response.shipmentTbConn()).isNotNull();
        assertThat(response.ftpConn()).isNotNull();
        assertThat(response.sampleData()).isNotBlank();

        log.info("APPLICANT_KEY : {}", response.applicantKey());
    }

    @Test
    @DisplayName("수신한 암호문을 전화번호 키로 복호화하면 DB/FTP 접속정보가 나온다")
    void decrypts_connection_info_with_phone_number_key() {
        ProvisioningResponse response = provisioningClient.fetch();
        ConnectionInfoDecryptor decryptor =
                new ConnectionInfoDecryptor(props.applicant().phoneNumber());

        ProvisioningResponse.ConnInfo order    = response.orderTbConn();
        ProvisioningResponse.ConnInfo shipment = response.shipmentTbConn();
        ProvisioningResponse.FtpConnInfo ftp   = response.ftpConn();

        String orderUrl    = decryptor.decrypt(order.url());
        String orderTable  = decryptor.decrypt(order.table());
        String ftpUrl      = decryptor.decrypt(ftp.url());
        String ftpPort     = decryptor.decrypt(ftp.port());

        assertThat(orderUrl).isNotBlank();
        assertThat(ftpUrl).isNotBlank();

        log.info("ORDER DB URL   : {}", orderUrl);
        log.info("ORDER TABLE    : {}", orderTable);
        log.info("FTP URL        : {}", ftpUrl);
        log.info("FTP PORT       : {}", ftpPort);
        log.info("ORDER DB ID    : {}", decryptor.decrypt(order.id()));
        log.info("ORDER PASSWORD : {}", decryptor.decrypt(order.password()));
        log.info("SHIPMENT TABLE : {}", decryptor.decrypt(shipment.table()));
    }

    @Test
    @DisplayName("SAMPLE_DATA를 디코딩하면 한글이 깨지지 않은 주문 XML이 나온다")
    void decodes_sample_data_to_xml() {
        ProvisioningResponse response = provisioningClient.fetch();
        SampleDataDecoder decoder = new SampleDataDecoder();

        String xml = decoder.decode(response.sampleData());

        assertThat(xml).contains("<HEADER>");
        assertThat(xml).contains("<ITEM>");
        log.info("SAMPLE_DATA XML :\n{}", xml);
    }
}
