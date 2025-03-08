package bonun.bustime.api.service;

import bonun.bustime.dto.BusLocationDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BusLocationService {

    private static final Logger log = LoggerFactory.getLogger(BusLocationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    // 공공데이터포털 인증키 (URL 인코딩 여부 확인)
    private final String SERVICE_KEY = "tNbsVnMlaZ7jFtVUDgBJTlDNg%2FVFa7R7XUYyegbItZY61%2FL%2FSgsl%2BFUP39TKdewZ5gwiPvYv3oaL6Zx8fv5iBg%3D%3D";
    private final String BASE_URL = "https://apis.data.go.kr/1613000/BusLcInfoInqireService/getRouteAcctoBusLcList";

    /**
     * 🚍 routeId를 받아서 해당 노선의 버스 위치 정보(위도/경도 등)를 가져오는 메서드
     */
    public List<BusLocationDTO> getBusLocations(String routeId) {
        List<BusLocationDTO> resultList = new ArrayList<>();
        try {
            // 1) URI 생성
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("serviceKey", SERVICE_KEY)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 10)
                    .queryParam("_type", "json")
                    .queryParam("cityCode", 38320)   // 예시
                    .queryParam("routeId", routeId) // 실제 routeId 파라미터
                    .build(true)
                    .toUri();

            log.info("🚍 버스 위치 조회 API 호출 URI = {}", uri);

            // 2) 요청
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            log.info("🚍-=-==--=-==-=-= 버스 위치 API 응답: {}", response.getBody());

            // 3) JSON 파싱
            String respBody = response.getBody();
            if (respBody == null) {
                return resultList;
            }

            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(respBody);
            JSONObject respObj = (JSONObject) root.get("response");
            if (respObj == null) return resultList;

            JSONObject body = (JSONObject) respObj.get("body");
            if (body == null) return resultList;

            // ✅ totalCount 체크 (0이면 데이터 없음)
            Long totalCount = parseTotalCount(body.get("totalCount"));
            if (totalCount == null || totalCount <= 0) {
                log.info("⚠️ totalCount = {} → 데이터가 없으므로 빈 리스트 반환", totalCount);
                return resultList;
            }

            JSONObject items = (JSONObject) body.get("items");
            if (items == null) return resultList;

            Object itemObj = items.get("item");
            if (itemObj == null) return resultList;

            // 4) item이 JSONArray인 경우
            if (itemObj instanceof JSONArray) {
                JSONArray itemArray = (JSONArray) itemObj;
                for (Object obj : itemArray) {
                    JSONObject item = (JSONObject) obj;
                    BusLocationDTO dto = parseBusLocation(item);
                    if (dto != null) {
                        resultList.add(dto);
                    }
                }
            }
            // 5) 단일 객체인 경우
            else if (itemObj instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObj;
                BusLocationDTO dto = parseBusLocation(item);
                if (dto != null) {
                    resultList.add(dto);
                }
            }

        } catch (Exception e) {
            log.error("🚨 버스 위치 조회 중 오류", e);
        }
        return resultList;
    }

    /**
     * 📌 totalCount 파싱 (예외 처리 포함)
     */
    private Long parseTotalCount(Object totalCountObj) {
        if (totalCountObj == null) return null;
        try {
            return Long.parseLong(totalCountObj.toString());
        } catch (NumberFormatException e) {
            log.error("🚨 totalCount 파싱 오류", e);
            return null;
        }
    }

    /**
     * 📌 BusLocationDTO로 변환하는 메서드 (데이터 타입 검사 포함)
     */
    private BusLocationDTO parseBusLocation(JSONObject item) {
        try {
            BusLocationDTO dto = new BusLocationDTO();

            // 🔹 vehicleId (문자열로 변환)
            dto.setVehicleId(Optional.ofNullable(item.get("vehicleno"))
                    .map(Object::toString)
                    .orElse("UNKNOWN"));

            // 🔹 gpslati (위도)
            dto.setLatitude(parseDouble(item.get("gpslati")));

            // 🔹 gpslong (경도)
            dto.setLongitude(parseDouble(item.get("gpslong")));

            dto.setRoutenm(item.get("routenm").toString());
            dto.setNodenm(item.get("nodenm").toString());

            return dto;
        } catch (Exception e) {
            log.error("🚨 버스 위치 파싱 오류", e);
            return null;
        }
    }

    /**
     * 📌 Object를 Double로 변환 (예외 처리 포함)
     */
    private Double parseDouble(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Double) {
                return (Double) obj;
            } else {
                return Double.parseDouble(obj.toString());
            }
        } catch (NumberFormatException e) {
            log.error("🚨 숫자 변환 오류 (parseDouble): {}", obj);
            return null;
        }
    }
}