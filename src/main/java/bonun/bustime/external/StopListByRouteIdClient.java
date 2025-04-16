package bonun.bustime.external;

import bonun.bustime.config.BusApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class StopListByRouteIdClient {

    private final RestTemplate restTemplate;
    private final BusApiProperties properties;

    public ResponseEntity<String> fetchStopLocations(String routeId) {
        URI uri = buildApiUrl(routeId);
        log.info("📡 공공 API 요청 URI: {}", uri);
        return restTemplate.getForEntity(uri, String.class);
    }

    private URI buildApiUrl(String routeId) {
        return UriComponentsBuilder.fromHttpUrl(properties.getStopLocationUrl())
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("pageNo", properties.getPageNo())
                .queryParam("numOfRows", properties.getNumOfRows())
                .queryParam("_type", properties.getType())
                .queryParam("cityCode", properties.getCityCode())
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();
    }
}