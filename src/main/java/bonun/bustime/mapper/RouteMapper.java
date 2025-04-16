package bonun.bustime.mapper;

import bonun.bustime.dto.BusRouteDTO;
import bonun.bustime.entity.RouteIdEntity;
import org.springframework.stereotype.Component;

@Component
public class RouteMapper {

    /**
     * BusRouteDTO를 RouteIdEntity로 변환
     */

    public RouteIdEntity toEntity(BusRouteDTO response) {
        RouteIdEntity entity = new RouteIdEntity();
        entity.setRouteId(response.routeId());
        entity.setRouteNo(response.routeNo());
        entity.setStartNodeName(response.startNodeName());
        entity.setEndNodeName(response.endNodeName());
        entity.setStartVehicleTime(response.startVehicleTime());
        entity.setEndVehicleTime(response.endVehicleTime());
        entity.setDirection(response.direction());
        return entity;
    }
}