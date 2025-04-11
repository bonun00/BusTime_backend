package bonun.bustime.external.bus.repository;


import bonun.bustime.external.bus.entity.RouteIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RouteIdRepository extends JpaRepository<RouteIdEntity, Long> {
    Optional<RouteIdEntity> findByRouteId(String routeId);

    List<RouteIdEntity> findByDirectionAndRouteNo (@Param("Direction") String direction,@Param("routeNo") String routeNo);

    List<RouteIdEntity> findByDirection(String direction);


}