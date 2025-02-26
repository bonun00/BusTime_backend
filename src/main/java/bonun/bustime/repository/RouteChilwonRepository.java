package bonun.bustime.repository;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.RouteChilwonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RouteChilwonRepository extends JpaRepository<RouteChilwonEntity, Long> {
    // 노선 이름으로 검색 (예: "마산→칠원")
    RouteChilwonEntity findByBus(BusEntity bus);


}