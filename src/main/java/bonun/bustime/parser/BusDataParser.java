package bonun.bustime.parser;

import bonun.bustime.dto.BusRouteDTO;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BusDataParser {

    /**
     * API 응답에서 버스 노선 정보 추출
     */
    public List<BusRouteDTO> parseRouteInfo(ResponseEntity<String> response) {
        List<BusRouteDTO> result = new ArrayList<>();
        try {
            Object itemObject = getItemObject(response);
            if (itemObject == null) return result;

            if (itemObject instanceof JSONArray) {
                JSONArray itemArray = (JSONArray) itemObject;
                for (Object obj : itemArray) {
                    JSONObject item = (JSONObject) obj;
                    BusRouteDTO route = parseRouteItem(item);
                    if (route != null) result.add(route);
                }
            } else if (itemObject instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObject;
                BusRouteDTO route = parseRouteItem(item);
                if (route != null) result.add(route);
            }
        } catch (Exception e) {
            log.error("🚨 JSON 파싱 오류", e);
        }
        return result;
    }

    /**
     * 단일 항목 파싱
     */
    private BusRouteDTO parseRouteItem(JSONObject item) {
        try {
            String routeId = (String) item.get("routeid");
            String routeNo = (String) item.get("routeno");
            String startNodeName = (String) item.get("startnodenm");
            String endNodeName = (String) item.get("endnodenm");
            String startVehicleTime = item.get("startvehicletime") == null ? "" : item.get("startvehicletime").toString();
            String endVehicleTime = item.get("endvehicletime") == null ? "" : item.get("endvehicletime").toString();

            if (routeId == null || routeNo == null) {
                return null;
            }

            String direction = determineDirection(startNodeName, endNodeName);

            return new BusRouteDTO(
                    routeId,
                    routeNo,
                    startNodeName != null ? startNodeName : "",
                    endNodeName != null ? endNodeName : "",
                    startVehicleTime,
                    endVehicleTime,
                    direction
            );
        } catch (Exception e) {
            log.error("🚨 route item 파싱 오류", e);
            return null;
        }
    }

    /**
     * 방향 결정 로직
     */
    private String determineDirection(String startNode, String endNode) {
        if (startNode == null) startNode = "";
        if (endNode == null) endNode = "";

        if ((startNode.contains("마산") || startNode.contains("창원")) && (endNode.contains("마산") || endNode.contains("창원"))) {
            return "기타";
        } else if (startNode.contains("마산") || startNode.contains("창원")) {
            return "칠원";
        } else if (endNode.contains("마산") || endNode.contains("창원")) {
            return "마산";
        } else {
            return "이건 뭐징";
        }
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

    /**
     * API 응답에서 routeNo 추출
     */
    public String extractRouteNo(ResponseEntity<String> response) {
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
}