package bonun.bustime.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@ToString(exclude = {"bus", "stop", "route"})
@NoArgsConstructor // JPA 기본 생성자
@AllArgsConstructor
public class BusTimeToChilwonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private BusEntity bus; // 버스 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private StopEntity stop; // 정류장 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteChilwonEntity route; // 노선 정보 추가

    @Column(nullable = false)
    private LocalTime arrivalTime; // 도착 시간

    public BusTimeToChilwonEntity(BusEntity bus, StopEntity stop, RouteChilwonEntity route, LocalTime arrivalTime) {
        this.bus = bus;
        this.stop = stop;
        this.route = route;
        this.arrivalTime = arrivalTime;
    }
}