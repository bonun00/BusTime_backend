package bonun.bustime.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BusTimeDTO {
    private String busNumber;
    private String stopName;
    private String routeName;
    private LocalTime arrivalTime;

    public BusTimeDTO(String busNumber, String stopName, String routeName, LocalTime arrivalTime) {
        this.busNumber = busNumber;
        this.stopName = stopName;
        this.routeName = routeName;
        this.arrivalTime = arrivalTime;
    }
}