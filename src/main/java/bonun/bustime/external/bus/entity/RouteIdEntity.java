package bonun.bustime.external.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RouteIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 노선 고유 ID
    @Column(nullable = false, unique = true)
    private String routeId;

    // 버스 번호 (예: "113-05", "250-10" 등)
    private String routeNo;

    // 출발 정류장 이름
    private String startNodeName;

    // 도착 정류장 이름
    private String endNodeName;

    // 출발 시간
    private String startVehicleTime;  // "0655" or 1250 등

    // 도착 시간
    private String endVehicleTime;    // "1840" 등

    // 방향 ("마산", "창원", "기타")
    @Column(name = "direction")
    private String direction;
}