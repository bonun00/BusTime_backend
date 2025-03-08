package bonun.bustime.repository.ToMasan;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.ToMasan.RouteMasanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteMasanRepository extends JpaRepository<RouteMasanEntity, Long> {
    // 노선 이름으로 검색 (예: "마산→칠원")
    RouteMasanEntity findByBus(BusEntity bus);


}