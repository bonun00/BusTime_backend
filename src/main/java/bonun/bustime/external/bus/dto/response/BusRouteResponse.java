package bonun.bustime.external.bus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 버스 노선 정보 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusRouteResponse {
    // 기본 API 응답 필드
    private String routeId;           // 노선 ID
    private String routeNo;           // 노선 번호 (ex: "113-05")
    private String startNodeName;     // 기점 (ex: "마산터미널")
    private String endNodeName;       // 종점 (ex: "칠원읍")
    private String startVehicleTime;  // 첫차 시간
    private String endVehicleTime;    // 막차 시간

    // 추가 필드 (비즈니스 로직용)
    private String direction;         // 방향 (ex: "마산", "칠원", "기타")

    // 필요에 따라 추가 필드
    private String routeType;         // 노선 유형
    private Integer routeLength;      // 노선 길이
    private Integer routeInterval;    // 배차 간격

    // 편의 메서드
    public boolean isValidRoute() {
        return routeId != null && routeNo != null;
    }

    /**
     * 특정 버스 노선으로 시작하는지 확인
     */
    public boolean startsWithRouteNumber(String routeNumber) {
        if (routeNo == null) return false;
        return routeNo.startsWith(routeNumber + "-");
    }

    /**
     * 노선 ID와 번호만 포함된 간단한 문자열 표현 반환
     */
    @Override
    public String toString() {
        return "BusRoute(routeId=" + routeId + ", routeNo=" + routeNo + ")";
    }
}