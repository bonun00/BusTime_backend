package bonun.bustime.entity.tomasan;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.StopEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@ToString(exclude = {"bus", "stop", "route"})
@NoArgsConstructor
@AllArgsConstructor
public class BusTimeToMasanEntity {

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
    private RouteMasanEntity route; // 노선 정보 추가

    @Column(nullable = false)
    private LocalTime arrivalTime; // 도착 시간

    public BusTimeToMasanEntity(BusEntity bus, StopEntity stop, RouteMasanEntity route, LocalTime arrivalTime) {
        this.bus = bus;
        this.stop = stop;
        this.route = route;
        this.arrivalTime = arrivalTime;
    }
}