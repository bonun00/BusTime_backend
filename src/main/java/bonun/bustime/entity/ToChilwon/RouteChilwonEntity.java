package bonun.bustime.entity.ToChilwon;

import bonun.bustime.api.entity.RouteIdEntity;
import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.StopEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RouteChilwonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private BusEntity bus;
    // 🔴 여러 노선 -> 하나의 RouteIdEntity
    //  (하나의 routeId / routeNo를 여러 행이 참조 가능)

    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private StopEntity startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private StopEntity endLocation;

    // 🔹 새로 추가: 출발지 버스 시간
    private LocalTime startLocationTime;

    // 🔹 새로 추가: 종점 버스 시간
    private LocalTime endLocationTime;

    public RouteChilwonEntity(BusEntity bus, StopEntity startLocation, StopEntity endLocation) {
        this.bus = bus;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }
}