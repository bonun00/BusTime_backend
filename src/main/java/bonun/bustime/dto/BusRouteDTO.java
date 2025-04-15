package bonun.bustime.dto;



public record BusRouteDTO(
        String routeId,
        String routeNo,
        String startNodeName,
        String endNodeName,
        String startVehicleTime,
        String endVehicleTime,
        String direction
) {}