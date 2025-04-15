package bonun.bustime.dto;


public record LocationByStopDTO(
        String nodeNm,
        String nodeId,
        double lat,
        double lng
){}