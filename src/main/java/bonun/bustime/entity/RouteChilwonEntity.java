package bonun.bustime.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor // JPA를 위한 기본 생성자 추가
@AllArgsConstructor
public class RouteChilwonEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 버스 번호 대신, BusEntity 자체를 1:1로 참조
    @OneToOne
    @JoinColumn(name = "bus_id", unique = true)
    private BusEntity bus;

    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private StopEntity startLocation; // 출발 정류장

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private StopEntity endLocation;   // 종점 정류장

    public RouteChilwonEntity(BusEntity bus, StopEntity startLocation, StopEntity endLocation) {
        this.bus = bus;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }
}