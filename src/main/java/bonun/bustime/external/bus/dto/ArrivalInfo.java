package bonun.bustime.external.bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalInfo {
    private String nodeId;
    private String nodeNm;
    private String routeId;
    private String routeNo;
    private String routeTp;
    private String vehicleTp;
    private int arrTime; // 초 단위
    private int arrPrevStationCnt;
}