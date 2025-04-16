package bonun.bustime.external;

import bonun.bustime.config.BusApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusInfoClient {

    private final RestTemplate restTemplate;
    private final BusApiProperties properties;

    /**
     * busNumber로 버스 노선 정보를 조회하는 API 호출
     */
    public ResponseEntity<String> fetchBusRouteInfo(String busNumber) {
        URI url = buildApiUrl(busNumber);
        log.info("🟢 API 요청 URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

        log.info("🟢 API 응답 상태 코드: {}", response.getStatusCode());
        return response;
    }

    private URI buildApiUrl(String busNumber) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBusInfoUrl())
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("pageNo", properties.getPageNo())
                .queryParam("numOfRows", properties.getNumOfRows())
                .queryParam("_type", properties.getType())
                .queryParam("cityCode", properties.getCityCode())
                .queryParam("routeNo", busNumber)
                .build(true)
                .toUri();
    }
}