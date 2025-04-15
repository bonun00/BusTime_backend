package bonun.bustime.external;

import bonun.bustime.config.BusApiProperties;
import bonun.bustime.dto.ArrivalInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StopTimeClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BusApiProperties properties;

    public List<ArrivalInfo> getArrivalInfoByNodeId(String nodeId) {
        try {
            URI uri = buildApiUrl(nodeId);
            log.info("📡 도착정보 API 요청: {}", uri);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            JsonNode itemsNode = objectMapper.readTree(response.getBody())
                    .path("response").path("body").path("items").path("item");
            List<ArrivalInfo> result = new ArrayList<>();

            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    String routeno = item.path("routeno").asText();
                    if (routeno.startsWith("113") || routeno.startsWith("250")) {
                        result.add(parseArrivalInfo(item));
                    }
                }
            } else if (itemsNode.isObject()) {
                String routeno = itemsNode.path("routeno").asText();
                if (routeno.startsWith("113") || routeno.startsWith("250")) {
                    result.add(parseArrivalInfo(itemsNode));
                }
            }

            log.info("🚌 도착 정보 {}건 조회됨 for nodeId={}", result.size(), nodeId);
            return result;

        } catch (Exception e) {
            log.error("🚨 도착정보 조회 실패: nodeId={}", nodeId, e);
            return Collections.emptyList();
        }
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



    private ArrivalInfo parseArrivalInfo(JsonNode item) {
        return new ArrivalInfo(
                item.path("nodeid").asText(),
                item.path("nodenm").asText(),
                item.path("routeid").asText(),
                item.path("routeno").asText(),
                item.path("routetp").asText(),
                item.path("vehicletp").asText(),
                item.path("arrtime").asInt()/60,
                item.path("arrprevstationcnt").asInt()
        );
    }
}