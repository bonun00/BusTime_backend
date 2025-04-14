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


    @Value("${bus.api.service-key}")
    private String serviceKey;
    @Value("${bus.api.api-url}")
    private String apiUrl;
    @Value("${bus.api.city-code}")
    private Integer cityCode;
    @Value("${bus.api.page-no}")
    private Integer pageNo;
    @Value("${bus.api.num-of-rows}")
    private Integer numOfRows;
}