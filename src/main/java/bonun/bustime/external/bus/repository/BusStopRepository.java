package bonun.bustime.external.bus.repository;


import bonun.bustime.external.bus.entity.BusStopEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface BusStopRepository extends JpaRepository<BusStopEntity, String> {

    List<BusStopEntity> findByRouteIdAndDirection(String routeId, String direction);
    boolean existsByNodeId(String nodeId);
    List<BusStopEntity> findByDirection( String direction);
}