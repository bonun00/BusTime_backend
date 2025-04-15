package bonun.bustime.external;

import bonun.bustime.config.BusApiProperties;
import bonun.bustime.dto.LocationByStopDTO;
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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BusApiProperties properties;

    public List<LocationByStopDTO> getStopsByRouteId(String routeId) {
        try {
            URI uri = buildApiUrl(routeId);

            log.info("📡 공공 API 요청 URI: {}", uri);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            JsonNode items = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item");

            List<JsonNode> sortedItems = new ArrayList<>();
            items.forEach(sortedItems::add);
            sortedItems.sort(Comparator.comparingInt(n -> n.path("nodeord").asInt()));

            List<LocationByStopDTO> stops = new ArrayList<>();
            for (JsonNode item : sortedItems) {
                double lat = item.path("gpslati").asDouble();
                double lng = item.path("gpslong").asDouble();
                String nodeNm = item.path("nodenm").asText();
                String nodeId = item.path("nodeid").asText();
                stops.add(new LocationByStopDTO(nodeNm,nodeId,lat,lng));
            }

            log.info("📍 정류장 {}개 조회 완료: routeId={}", stops.size(), routeId);
            return stops;

        } catch (Exception e) {
            log.error("🚨 공공 API 호출 실패: routeId={}", routeId, e);
            return Collections.emptyList();
        }
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
