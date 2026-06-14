package co.inspien.assignment.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Inspien 제공 API 호출 클라이언트.
 * Basic Auth로 POST → 암호화된 접속정보(JSON) 수신.
 */
@Component
public class ProvisioningClient {

    private final RestClient restClient;
    private final ProvisioningProperties props;
    private final ObjectMapper objectMapper;

    public ProvisioningClient(ProvisioningProperties props) {
        this.props = props;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(props.getUrl())
                .defaultHeaders(h -> h.setBasicAuth(
                        props.getAuth().getUsername(),
                        props.getAuth().getPassword()))
                .build();
    }

    /**
     * 제공 API를 호출해 암호화된 접속정보를 수신한다.
     * 응답 Content-Type이 없거나 text/plain으로 오므로 String으로 받아 직접 파싱.
     */
    public ProvisioningResponse fetch() {
        ProvisioningProperties.Applicant applicant = props.getApplicant();

        Map<String, String> body = Map.of(
                "NAME",         applicant.getName(),
                "PHONE_NUMBER", applicant.getPhoneNumber(),
                "E_MAIL",       applicant.getEmail()
        );

        String raw = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(raw, ProvisioningResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("제공 API 응답 파싱 실패: " + raw, e);
        }
    }
}
