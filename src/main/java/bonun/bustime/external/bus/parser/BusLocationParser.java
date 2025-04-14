package bonun.bustime.external.bus.parser;

import bonun.bustime.external.bus.dto.BusLocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BusLocationParser {

    /**
     * API 응답에서 버스 위치 정보 추출
     */
    public List<BusLocationDTO> parseBusLocations(ResponseEntity<String> response) {
        List<BusLocationDTO> resultList = new ArrayList<>();
        try {
            String respBody = Optional.ofNullable(response.getBody())
                    .map(String::trim)
                    .filter(body -> !body.isEmpty() && body.startsWith("{"))
                    .orElse(null);

            if (respBody == null) {
                log.warn("API 응답이 비정상입니다.");
                return resultList;
            }
            try {
                JSONParser parser = new JSONParser();
                JSONObject root = (JSONObject) parser.parse(respBody);

                // response 객체 확인
                JSONObject respObj = (JSONObject) root.get("response");
                if (respObj == null) {
                    log.warn("응답에서 'response' 객체를 찾을 수 없습니다. 응답: {}", respBody);
                    return resultList;
                }

                // body 객체 확인
                JSONObject body = (JSONObject) respObj.get("body");
                if (body == null) {
                    log.warn("응답에서 'body' 객체를 찾을 수 없습니다.");
                    return resultList;
                }

                // totalCount 체크 (0이면 데이터 없음)
                Long totalCount = parseTotalCount(body.get("totalCount"));
                if (totalCount == null || totalCount <= 0) {
                    log.info("⚠️ totalCount = {} → 데이터가 없으므로 빈 리스트 반환", totalCount);
                    return resultList;
                }

                // items 객체 확인
                JSONObject items = (JSONObject) body.get("items");
                if (items == null) {
                    log.warn("응답에서 'items' 객체를 찾을 수 없습니다.");
                    return resultList;
                }

                // item 객체 또는 배열 확인
                Object itemObj = items.get("item");
                if (itemObj == null) {
                    log.warn("응답에서 'item' 객체를 찾을 수 없습니다.");
                    return resultList;
                }

                // item이 JSONArray인 경우
                if (itemObj instanceof JSONArray) {
                    JSONArray itemArray = (JSONArray) itemObj;
                    for (Object obj : itemArray) {
                        if (obj instanceof JSONObject) {
                            JSONObject item = (JSONObject) obj;
                            BusLocationDTO dto = parseBusLocation(item);
                            if (dto != null) {
                                resultList.add(dto);
                            }
                        } else {
                            log.warn("배열 내 항목이 JSONObject가 아닙니다: {}", obj);
                        }
                    }
                }
                // 단일 객체인 경우
                else if (itemObj instanceof JSONObject) {
                    JSONObject item = (JSONObject) itemObj;
                    BusLocationDTO dto = parseBusLocation(item);
                    if (dto != null) {
                        resultList.add(dto);
                    }
                } else {
                    log.warn("'item'이 JSONObject나 JSONArray가 아닙니다: {}", itemObj.getClass().getName());
                }
            } catch (ParseException e) {
                // 파싱 오류 발생 시 응답 상세 로깅
                log.error("JSON 파싱 오류. 응답: {}", maskSensitiveData(respBody), e);
                return resultList;
            }

        } catch (Exception e) {
            log.error("🚨 API 응답 파싱 중 예상치 못한 오류", e);
        }
        return resultList;
    }

    /**
     * 민감한 데이터(예: API 키)가 로그에 노출되지 않도록 처리
     */
    private String maskSensitiveData(String response) {
        if (response == null) return null;
        if (response.length() > 1000) {
            return response.substring(0, 1000) + "... (truncated)";
        }
        return response;
    }

    /**
     * totalCount 파싱 (예외 처리 포함)
     */
    private Long parseTotalCount(Object totalCountObj) {
        if (totalCountObj == null) return null;
        try {
            return Long.parseLong(totalCountObj.toString());
        } catch (NumberFormatException e) {
            log.error("🚨 totalCount 파싱 오류: {}", totalCountObj, e);
            return null;
        }
    }

    /**
     * BusLocationDTO로 변환하는 메서드 (데이터 타입 검사 포함)
     */
    private BusLocationDTO parseBusLocation(JSONObject item) {
        try {
            BusLocationDTO dto = new BusLocationDTO();

            // vehicleId (문자열로 변환)
            dto.setVehicleId(Optional.ofNullable(item.get("vehicleno"))
                    .map(Object::toString)
                    .orElse("UNKNOWN"));

            // gpslati (위도)
            dto.setLatitude(parseDouble(item.get("gpslati")));

            // gpslong (경도)
            dto.setLongitude(parseDouble(item.get("gpslong")));

            // 추가 필드 설정
            dto.setRoutenm(Optional.ofNullable(item.get("routenm"))
                    .map(Object::toString)
                    .orElse(""));

            dto.setNodenm(Optional.ofNullable(item.get("nodenm"))
                    .map(Object::toString)
                    .orElse(""));
            dto.setRouteId(Optional.ofNullable(item.get("nodeid"))
                    .map(Object::toString)
                    .orElse(""));

            return dto;
        } catch (Exception e) {
            log.error("🚨 버스 위치 파싱 오류: {}", item, e);
            return null;
        }
    }

    /**
     * Object를 Double로 변환 (예외 처리 포함)
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