package bonun.bustime.external.bus.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverDirectionsClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public List<List<Double>> getPathWithViaPoints(PublicBusApiClient.BusStopLocation start, PublicBusApiClient.BusStopLocation goal, List<PublicBusApiClient.BusStopLocation> viaPoints) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving")
                .queryParam("start", start.getLng() + "," + start.getLat())
                .queryParam("goal", goal.getLng() + "," + goal.getLat())
                .queryParam("option", "trafest");

        if (!viaPoints.isEmpty()) {
            String viaStr = viaPoints.stream()
                    .map(p -> p.getLng() + "," + p.getLat())
                    .collect(Collectors.joining("|"));
            builder.queryParam("waypoints", viaStr);
        }

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode pathArray = root.path("route").path("trafast").get(0).path("path");

            List<List<Double>> result = new ArrayList<>();
            for (JsonNode coord : pathArray) {
                double lng = coord.get(0).asDouble();
                double lat = coord.get(1).asDouble();
                result.add(Arrays.asList(lat, lng)); // [lat, lng]
            }

            log.info("🧭 경로 segment 조회 완료 (경유지 포함, 점 수: {})", result.size());
            return result;

        } catch (Exception e) {
            log.warn("🚫 경유지 경로 조회 실패 (start={}, goal={}, viaSize={})",
                    start, goal, viaPoints.size(), e);
            return Collections.emptyList();
        }
    }
}