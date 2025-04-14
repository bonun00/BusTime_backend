package bonun.bustime.external.bus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bus_stops")
public class BusStopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeId;
    private String direction;
    private String nodeId;
    private String nodeNm;
    private double latitude;
    private double longitude;
}