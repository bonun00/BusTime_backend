package bonun.bustime.dto;


public record LiveStopTimeDTO(

        String nodeId,
        String nodNm,
        String routeId,
        String routeNo,
        String routeTp,
        String vehicleTp,
        int arrTime,
        int arrPrevStationCnt

){}