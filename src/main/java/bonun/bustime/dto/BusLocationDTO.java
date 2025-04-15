package bonun.bustime.dto;



public record BusLocationDTO(

        String vehicleId,
        double latitude,
        double longitude,
        String routenm,
        String nodenm,
        String routeId

) {
}