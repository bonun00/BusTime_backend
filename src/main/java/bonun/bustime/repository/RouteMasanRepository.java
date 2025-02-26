package bonun.bustime.repository;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.RouteMasanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteMasanRepository extends JpaRepository<RouteMasanEntity, Long> {
    // 노선 이름으로 검색 (예: "마산→칠원")
    RouteMasanEntity findByBus(BusEntity bus);


}