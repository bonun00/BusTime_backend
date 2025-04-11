package bonun.bustime.external.bus.mapper;

import bonun.bustime.external.bus.entity.RouteIdEntity;
import bonun.bustime.external.bus.dto.response.BusRouteResponse;
import org.springframework.stereotype.Component;

@Component
public class RouteMapper {

    /**
     * BusRouteResponse를 RouteIdEntity로 변환
     */
    public RouteIdEntity toEntity(BusRouteResponse response) {
        RouteIdEntity entity = new RouteIdEntity();
        entity.setRouteId(response.getRouteId());
        entity.setRouteNo(response.getRouteNo());
        entity.setStartNodeName(response.getStartNodeName());
        entity.setEndNodeName(response.getEndNodeName());
        entity.setStartVehicleTime(response.getStartVehicleTime());
        entity.setEndVehicleTime(response.getEndVehicleTime());
        entity.setDirection(response.getDirection());
        return entity;
    }
}