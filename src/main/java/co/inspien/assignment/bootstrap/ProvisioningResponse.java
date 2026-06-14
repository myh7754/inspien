package co.inspien.assignment.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inspien 제공 API 응답 JSON DTO.
 * ORDER_TB_CONN / SHIPMENT_TB_CONN / FTP_CONN 은 필드별로 개별 암호화된 객체로 온다.
 */
public class ProvisioningResponse {

    @JsonProperty("APPLICANT_KEY")
    private String applicantKey;

    @JsonProperty("ORDER_TB_CONN")
    private ConnInfo orderTbConn;

    @JsonProperty("SHIPMENT_TB_CONN")
    private ConnInfo shipmentTbConn;

    @JsonProperty("FTP_CONN")
    private FtpConnInfo ftpConn;

    @JsonProperty("SAMPLE_DATA")
    private String sampleData;

    public String getApplicantKey()       { return applicantKey; }
    public ConnInfo getOrderTbConn()      { return orderTbConn; }
    public ConnInfo getShipmentTbConn()   { return shipmentTbConn; }
    public FtpConnInfo getFtpConn()       { return ftpConn; }
    public String getSampleData()         { return sampleData; }

    /** DB 접속정보 (ORDER_TB_CONN, SHIPMENT_TB_CONN) */
    public static class ConnInfo {
        @JsonProperty("URL")      private String url;
        @JsonProperty("ID")       private String id;
        @JsonProperty("PASSWORD") private String password;
        @JsonProperty("TABLE")    private String table;

        public String getUrl()      { return url; }
        public String getId()       { return id; }
        public String getPassword() { return password; }
        public String getTable()    { return table; }
    }

    /** FTP 접속정보 */
    public static class FtpConnInfo {
        @JsonProperty("URL")      private String url;
        @JsonProperty("PORT")     private String port;
        @JsonProperty("ID")       private String id;
        @JsonProperty("PASSWORD") private String password;
        @JsonProperty("PATH")     private String path;

        public String getUrl()      { return url; }
        public String getPort()     { return port; }
        public String getId()       { return id; }
        public String getPassword() { return password; }
        public String getPath()     { return path; }
    }
}
