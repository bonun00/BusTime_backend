package bonun.bustime.api.repository;


import bonun.bustime.api.entity.RouteIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RouteIdRepository extends JpaRepository<RouteIdEntity, Long> {
    Optional<RouteIdEntity> findByRouteId(String routeId);

//    Optional<RouteIdEntity> findByRouteNo(String busNumber);
    @Query("SELECT r FROM RouteIdEntity r WHERE r.routeNo = :routeNo")
    List<RouteIdEntity> findByRouteNo(@Param("routeNo") String routeNo);
}