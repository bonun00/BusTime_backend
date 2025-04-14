package bonun.bustime.external.bus.repository;


import bonun.bustime.external.bus.entity.BusStopEntity;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface BusStopRepository extends JpaRepository<BusStopEntity, String> {

    boolean existsByNodeId(String nodeId);
    List<BusStopEntity> findByDirection( String direction);
}