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
public class LiveStopTimeClient {

    private final RestTemplate restTemplate;
    private final BusApiProperties properties;

    public ResponseEntity<String> fetchArrivalInfo(String nodeId) {
        URI uri = buildApiUrl(nodeId);
        log.info("📡 도착정보 API 요청: {}", uri);
        return restTemplate.getForEntity(uri, String.class);
    }

    private URI buildApiUrl(String nodeId) {
        return UriComponentsBuilder
                .fromHttpUrl(properties.getStopTimeApiUri())
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("pageNo", properties.getPageNo())
                .queryParam("numOfRows", properties.getNumOfRows())
                .queryParam("_type", properties.getType())
                .queryParam("cityCode", properties.getCityCode())
                .queryParam("nodeId", nodeId)
                .build(true)
                .toUri();
    }
}