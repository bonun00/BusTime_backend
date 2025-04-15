package bonun.bustime.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bus.api")
public class BusApiProperties {


    @Value("${public-api.service-key}")
    private String serviceKey;


    // 버스 위치 정보 URL
    @Value("${bus.api.bus-location.api-url}")
    private String busLocationUrl;

    @Value("${bus.api.info.api-url}")
    private String busInfoUrl;

    @Value("${bus.api.stop-location.api-url}")
    private String stopLocationUrl;

    @Value("${bus.api.stop-time.api-uri}")
    private String stopTimeApiUri;

    @Value("${bus.api.city-code}")
    private Integer cityCode;
    @Value("${bus.api.page-no}")
    private Integer pageNo;
    @Value("${bus.api.num-of-rows}")
    private Integer numOfRows;
    @Value("${bus.api.type}")
    private String type;

}