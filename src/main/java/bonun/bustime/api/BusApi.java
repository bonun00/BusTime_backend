package bonun.bustime.api;

import bonun.bustime.api.entity.RouteIdEntity;
import bonun.bustime.api.repository.RouteIdRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class BusApi {

    private static final Logger log = LoggerFactory.getLogger(BusApi.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final RouteIdRepository routeIdRepository;

    // 🔹 서비스 키 & API URL
    private final String SERVICE_KEY =
            "tNbsVnMlaZ7jFtVUDgBJTlDNg%2FVFa7R7XUYyegbItZY61%2FL%2FSgsl%2BFUP39TKdewZ5gwiPvYv3oaL6Zx8fv5iBg%3D%3D";
    private final String API_URL = "https://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteNoList";

    public BusApi(RouteIdRepository routeIdRepository) {
        this.routeIdRepository = routeIdRepository;
    }

    /**
     * 🚀 busNumber (ex: "113", "250") 입력 → API 조회 → routeId & 기타정보 파싱
     */
    public String fetchAndSaveRouteId(String busNumber) {
        try {
            // 1) API 호출
            URI url = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .queryParam("serviceKey", SERVICE_KEY)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 200)
                    .queryParam("_type", "json")
                    .queryParam("cityCode", 38320)
                    .queryParam("routeNo", busNumber) // 🔹 "113" or "250"
                    .build(true)
                    .toUri();

            log.info("🟢 API 요청 URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            log.info("🟢 API 응답: {}", response.getBody());

            // 2) routeNo 일치 여부 확인
            String routeno = extractRouteNo(response);
            if (routeno == null) {
                return "해당 버스 번호에 대한 정보가 없습니다.(routeno=null)";
            }
            // 만약 "113-05" 같은게 나올 수 있으므로, "113"과 일치하는지 비교
            // 여기서는 "113"이 포함되면 저장한다는 식으로 처리
            if (!routeno.startsWith(busNumber)) {
                log.warn("🚨 API 응답 routeno={} 이 busNumber={}로 시작하지 않음", routeno, busNumber);
            }

            // 3) routeId + 기타정보 파싱
            List<RouteIdEntity> routeEntities = extractRouteEntities(response);
            if (routeEntities.isEmpty()) {
                return "해당 버스 번호에 대한 routeId를 찾을 수 없습니다.";
            }

            // 4) DB에 저장 (routeNo가 "113-" 또는 "250-"로 시작하는지 확인)
            int saveCount = 0;
            for (RouteIdEntity route : routeEntities) {
                // ✅ routeNo 예: "113-05", "250-10"
                String rNo = route.getRouteNo();
                if (rNo == null) continue;

                // 🚨 "113-", "250-" 로 시작하는지만 체크
                if (! (rNo.startsWith("113-") || rNo.startsWith("250-")) ) {
                    // 필요하다면 rNo.split("-")[0] == "113" or "250" 체크
                    continue;
                }

                // 이미 존재하는지 확인
                Optional<RouteIdEntity> existing = routeIdRepository.findByRouteId(route.getRouteId());
                if (existing.isPresent()) {
                    log.info("이미 저장된 routeId={}", route.getRouteId());
                } else {
                    // DB에 저장
                    routeIdRepository.save(route);
                    log.info("✅ routeId={} 저장 완료 (routeNo={}, direction={})",
                            route.getRouteId(), route.getRouteNo(), route.getDirection());
                    saveCount++;
                }
            }

            return "routeId 저장 완료 (총 " + saveCount + "개)";

        } catch (Exception e) {
            log.error("API 요청 중 오류 발생", e);
            return "API 요청 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * 🔍 API 응답에서 routeIdEntity 리스트 추출 (startnodenm, endnodenm 등)
     */
    private List<RouteIdEntity> extractRouteEntities(ResponseEntity<String> response) {
        List<RouteIdEntity> result = new ArrayList<>();
        try {
            Object itemObject = getItemObject(response);
            if (itemObject == null) return result;

            if (itemObject instanceof JSONArray) {
                JSONArray itemArray = (JSONArray) itemObject;
                for (Object obj : itemArray) {
                    JSONObject item = (JSONObject) obj;
                    RouteIdEntity route = parseRouteItem(item);
                    if (route != null) result.add(route);
                }
            } else if (itemObject instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObject;
                RouteIdEntity route = parseRouteItem(item);
                if (route != null) result.add(route);
            }

        } catch (Exception e) {
            log.error("🚨 JSON 파싱 오류(extractRouteEntities)", e);
        }
        return result;
    }

    /**
     * 🔍 하나의 item에서 routeNo, routeId, startnodenm, endnodenm, startvehicletime, endvehicletime 파싱
     */
    private RouteIdEntity parseRouteItem(JSONObject item) {
        try {
            String routeId = (String) item.get("routeid");
            String routeNo = (String) item.get("routeno");
            String startNodeName = (String) item.get("startnodenm");
            String endNodeName = (String) item.get("endnodenm");
            // 시간도 파싱 (문자열 형태)
            String startVehicleTime = item.get("startvehicletime") == null ? "" : item.get("startvehicletime").toString();
            String endVehicleTime = item.get("endvehicletime") == null ? "" : item.get("endvehicletime").toString();

            if (routeId == null || routeNo == null) {
                return null;
            }

            RouteIdEntity entity = new RouteIdEntity();
            entity.setRouteId(routeId);
            entity.setRouteNo(routeNo);
            entity.setStartNodeName(startNodeName != null ? startNodeName : "");
            entity.setEndNodeName(endNodeName != null ? endNodeName : "");
            entity.setStartVehicleTime(startVehicleTime);
            entity.setEndVehicleTime(endVehicleTime);

            // 🔹 direction 결정
            String direction = determineDirection(entity.getStartNodeName(), entity.getEndNodeName());
            entity.setDirection(direction);

            return entity;

        } catch (Exception e) {
            log.error("🚨 route item 파싱 오류", e);
            return null;
        }
    }

    /**
     * 🔍 "마산" / "창원" 포함 여부로 direction 설정
     */
    private String determineDirection(String startNode, String endNode) {
        if (startNode == null) startNode = "";
        if (endNode == null) endNode = "";

        if (startNode.contains("마산") || endNode.contains("마산")) {
            return "마산";
        } else if (startNode.contains("창원") || endNode.contains("창원")) {
            return "창원";
        } else {
            return "기타";
        }
    }

    /**
     * API 응답 JSON에서 첫 번째 항목의 "routeno" 값을 추출 (중요도 낮음)
     */
    private String extractRouteNo(ResponseEntity<String> response) {
        try {
            Object itemObject = getItemObject(response);
            if (itemObject == null) return null;

            if (itemObject instanceof JSONArray) {
                JSONArray itemArray = (JSONArray) itemObject;
                if (!itemArray.isEmpty()) {
                    JSONObject firstItem = (JSONObject) itemArray.get(0);
                    return (String) firstItem.get("routeno");
                }
            } else if (itemObject instanceof JSONObject) {
                JSONObject firstItem = (JSONObject) itemObject;
                return (String) firstItem.get("routeno");
            }
        } catch (Exception e) {
            log.error("🚨 JSON 파싱 오류 (extractRouteNo)", e);
        }
        return null;
    }

    /**
     * 공통: API 응답 JSON에서 'response -> body -> items -> item' 객체 추출
     */
    private Object getItemObject(ResponseEntity<String> response) throws Exception {
        String responseBody = response.getBody();
        if (responseBody == null || !responseBody.trim().startsWith("{")) {
            throw new RuntimeException("Unexpected API response: " + responseBody);
        }
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(responseBody);

        JSONObject responseObj = (JSONObject) jsonObject.get("response");
        if (responseObj == null) return null;

        JSONObject body = (JSONObject) responseObj.get("body");
        if (body == null) return null;

        JSONObject items = (JSONObject) body.get("items");
        if (items == null) return null;

        return items.get("item"); // JSONArray or JSONObject or null
    }
}