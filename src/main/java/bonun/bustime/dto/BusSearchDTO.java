package bonun.bustime.dto;


import java.time.LocalTime;

public record BusSearchDTO(
        LocalTime departureTime, LocalTime arrivalTime,String busNumber) {
}