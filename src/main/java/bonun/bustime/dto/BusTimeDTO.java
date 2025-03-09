package bonun.bustime.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@RequiredArgsConstructor
public class BusTimeDTO {
    private final String busNumber;
    private final String stopName;
    private final String routeName;
    private final LocalTime arrivalTime;

}