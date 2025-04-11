package bonun.bustime.external.bus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 버스 노선 정보 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusRouteRequest {

    private String routeNo;    // 버스 노선 번호 (ex: "113", "250")
    private Integer cityCode;  // 도시 코드
    private Integer pageNo;    // 페이지 번호
    private Integer numOfRows; // 한 페이지당 결과 수

    // 비즈니스 요구사항에 따라 필요한 필드 추가 가능
}