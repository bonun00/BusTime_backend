package bonun.bustime.dto;


import java.time.LocalTime;

public record TimePairDTO(
        LocalTime departureTime, LocalTime arrivalTime,String busNumber) {
}