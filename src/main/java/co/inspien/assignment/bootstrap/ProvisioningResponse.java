package co.inspien.assignment.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProvisioningResponse(
        @JsonProperty("APPLICANT_KEY")    String applicantKey,
        @JsonProperty("ORDER_TB_CONN")    ConnInfo orderTbConn,
        @JsonProperty("SHIPMENT_TB_CONN") ConnInfo shipmentTbConn,
        @JsonProperty("FTP_CONN")         FtpConnInfo ftpConn,
        @JsonProperty("SAMPLE_DATA")      String sampleData
) {
    public record ConnInfo(
            @JsonProperty("URL")      String url,
            @JsonProperty("ID")       String id,
            @JsonProperty("PASSWORD") String password,
            @JsonProperty("TABLE")    String table
    ) {}

    public record FtpConnInfo(
            @JsonProperty("URL")      String url,
            @JsonProperty("PORT")     String port,
            @JsonProperty("ID")       String id,
            @JsonProperty("PASSWORD") String password,
            @JsonProperty("PATH")     String path
    ) {}
}
