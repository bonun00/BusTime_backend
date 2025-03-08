package bonun.bustime.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class BusLocationDTO {
    private String vehicleId;  // 차량ID나 번호

    private double latitude;   // 위도
    private double longitude;  // 경도
    private String routenm;
    private String nodenm;

}