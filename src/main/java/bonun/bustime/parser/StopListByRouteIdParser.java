package bonun.bustime.parser;

import bonun.bustime.dto.LocationByStopDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StopListByRouteIdParser {

    private final ObjectMapper objectMapper;

    public List<LocationByStopDTO> parseStopLocations(ResponseEntity<String> response, String routeId) {
        try {
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
                stops.add(new LocationByStopDTO(nodeNm, nodeId, lat, lng));
            }

            log.info("📍 정류장 {}개 조회 완료: routeId={}", stops.size(), routeId);
            return stops;

        } catch (Exception e) {
            log.error("🚨 정류장 파싱 실패: routeId={}", routeId, e);
            return Collections.emptyList();
        }
    }
}