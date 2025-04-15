package bonun.bustime.dto;

import java.time.LocalTime;

public record BusTimeDTO(
        String busNumber,
        String stopName,
        String routeName,
        LocalTime arrivalTime
){}