package bonun.bustime.external.bus.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class BusLocationClient {

    private final RestTemplate restTemplate;

    @Value("${bus.api.location.service-key}")
    private String serviceKey;

    @Value("${bus.api.location.base-url}")
    private String baseUrl;

    @Value("${bus.api.location.city-code}")
    private Integer cityCode;

    /**
     * routeId를 이용하여 버스 위치 정보를 조회하는 API 호출
     */
    public ResponseEntity<String> getBusLocationByRouteId(String routeId) {
        URI uri = buildLocationApiUri(routeId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        log.debug("버스 위치 API 응답 코드: {}", response.getStatusCode());

        return response;
    }

    private URI buildLocationApiUri(String routeId) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 10)
                .queryParam("_type", "json")
                .queryParam("cityCode", cityCode)
                .queryParam("routeId", routeId)
                .build(true)
                .toUri();
    }
}