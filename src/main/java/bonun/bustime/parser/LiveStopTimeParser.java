package bonun.bustime.parser;

import bonun.bustime.dto.LiveStopTimeDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveStopTimeParser {

    private final ObjectMapper objectMapper;

    public List<LiveStopTimeDTO> parseArrivalInfo(ResponseEntity<String> response, String nodeId) {
        try {
            JsonNode itemsNode = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item");

            List<LiveStopTimeDTO> result = new ArrayList<>();

            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    String routeno = item.path("routeno").asText();
                    if (routeno.startsWith("113") || routeno.startsWith("250")) {
                        result.add(toArrivalInfo(item));
                    }
                }
            } else if (itemsNode.isObject()) {
                String routeno = itemsNode.path("routeno").asText();
                if (routeno.startsWith("113") || routeno.startsWith("250")) {
                    result.add(toArrivalInfo(itemsNode));
                }
            }

            log.info("🚌 도착 정보 {}건 조회됨 for nodeId={}", result.size(), nodeId);
            return result;
        } catch (Exception e) {
            log.error("🚨 도착정보 파싱 실패: nodeId={}", nodeId, e);
            return Collections.emptyList();
        }
    }

    private LiveStopTimeDTO toArrivalInfo(JsonNode item) {
        return new LiveStopTimeDTO(
                item.path("nodeid").asText(),
                item.path("nodenm").asText(),
                item.path("routeid").asText(),
                item.path("routeno").asText(),
                item.path("routetp").asText(),
                item.path("vehicletp").asText(),
                item.path("arrtime").asInt() / 60,
                item.path("arrprevstationcnt").asInt()
        );
    }
}