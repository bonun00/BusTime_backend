package bonun.bustime.entity.tomasan;

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
public class RouteMasanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 버스와 N:1 매핑 (버스번호가 같아도 여러 행 저장 가능)
    @ManyToOne
    @JoinColumn(name = "bus_id")
    private BusEntity bus;


    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private StopEntity startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private StopEntity endLocation;

    //  추가: 출발 시각(가장 이른 시간)
    private LocalTime startLocationTime;

    //  추가: 종점 시각(가장 늦은 시간)
    private LocalTime endLocationTime;

    public RouteMasanEntity(BusEntity bus, StopEntity startLocation, StopEntity endLocation) {
        this.bus = bus;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }
}