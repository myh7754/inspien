package co.inspien.assignment.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

    @Autowired ProvisioningClient provisioningClient;
    @Autowired ProvisioningProperties props;

    @Test
    @DisplayName("API 호출 시 APPLICANT_KEY와 암호화된 접속정보, SAMPLE_DATA를 수신한다")
    void fetches_all_fields_from_provisioning_api() {
        ProvisioningResponse response = provisioningClient.fetch();

        assertThat(response.getApplicantKey()).isNotBlank();
        assertThat(response.getOrderTbConn()).isNotNull();
        assertThat(response.getOrderTbConn().getUrl()).isNotBlank();
        assertThat(response.getShipmentTbConn()).isNotNull();
        assertThat(response.getFtpConn()).isNotNull();
        assertThat(response.getSampleData()).isNotBlank();

        System.out.println("✅ APPLICANT_KEY : " + response.getApplicantKey());
    }

    @Test
    @DisplayName("수신한 암호문을 전화번호 키로 복호화하면 DB/FTP 접속정보가 나온다")
    void decrypts_connection_info_with_phone_number_key() {
        ProvisioningResponse response = provisioningClient.fetch();
        ConnectionInfoDecryptor decryptor =
                new ConnectionInfoDecryptor(props.getApplicant().getPhoneNumber());

        ProvisioningResponse.ConnInfo order    = response.getOrderTbConn();
        ProvisioningResponse.ConnInfo shipment = response.getShipmentTbConn();
        ProvisioningResponse.FtpConnInfo ftp   = response.getFtpConn();

        String orderUrl    = decryptor.decrypt(order.getUrl());
        String orderTable  = decryptor.decrypt(order.getTable());
        String ftpUrl      = decryptor.decrypt(ftp.getUrl());
        String ftpPort     = decryptor.decrypt(ftp.getPort());

        assertThat(orderUrl).isNotBlank();
        assertThat(ftpUrl).isNotBlank();

        System.out.println("✅ ORDER DB URL   : " + orderUrl);
        System.out.println("✅ ORDER TABLE    : " + orderTable);
        System.out.println("✅ FTP URL        : " + ftpUrl);
        System.out.println("✅ FTP PORT       : " + ftpPort);
        System.out.println("✅ ORDER DB ID    : " + decryptor.decrypt(order.getId()));
        System.out.println("✅ ORDER PASSWORD : " + decryptor.decrypt(order.getPassword()));
        System.out.println("✅ SHIPMENT TABLE : " + decryptor.decrypt(shipment.getTable()));
    }

    @Test
    @DisplayName("SAMPLE_DATA를 디코딩하면 한글이 깨지지 않은 주문 XML이 나온다")
    void decodes_sample_data_to_xml() {
        ProvisioningResponse response = provisioningClient.fetch();
        SampleDataDecoder decoder = new SampleDataDecoder();

        String xml = decoder.decode(response.getSampleData());

        assertThat(xml).contains("<HEADER>");
        assertThat(xml).contains("<ITEM>");
        System.out.println("✅ SAMPLE_DATA XML :\n" + xml);
    }
}
