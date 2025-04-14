package bonun.bustime.repository.tochilwon;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.tochilwon.RouteChilwonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RouteChilwonRepository extends JpaRepository<RouteChilwonEntity, Long> {
    // 노선 이름으로 검색 (예: "마산→칠원")
    RouteChilwonEntity findByBus(BusEntity bus);

    List<RouteChilwonEntity> findByBusIdAndStartLocationIdAndEndLocationId(
            Long busId,
            Long startLocationId,
            Long endLocationId
    );

}