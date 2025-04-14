package bonun.bustime.external.bus.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicBusApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${public-api.service-key}")
    private String serviceKey;

    public List<BusStopLocation> getStopsByRouteId(String routeId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 100)
                    .queryParam("_type", "json")
                    .queryParam("cityCode", 38320)
                    .queryParam("routeId", routeId)
                    .build(true)
                    .toUri();

            log.info("📡 공공 API 요청 URI: {}", uri);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            JsonNode items = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item");

            List<JsonNode> sortedItems = new ArrayList<>();
            items.forEach(sortedItems::add);
            sortedItems.sort(Comparator.comparingInt(n -> n.path("nodeord").asInt()));

            List<BusStopLocation> stops = new ArrayList<>();
            for (JsonNode item : sortedItems) {
                double lat = item.path("gpslati").asDouble();
                double lng = item.path("gpslong").asDouble();
                String nodeNm = item.path("nodenm").asText();
                String nodeId = item.path("nodeid").asText();
                stops.add(new BusStopLocation(nodeNm,nodeId,lat,lng));
            }

            log.info("📍 정류장 {}개 조회 완료: routeId={}", stops.size(), routeId);
            return stops;

        } catch (Exception e) {
            log.error("🚨 공공 API 호출 실패: routeId={}", routeId, e);
            return Collections.emptyList();
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class BusStopLocation {
        private final String nodeNm;
        private final String nodeId;
        private final double lat;
        private final double lng;



    }
}
