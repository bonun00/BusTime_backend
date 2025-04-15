package bonun.bustime.repository.tomasan;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.tomasan.RouteMasanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteMasanRepository extends JpaRepository<RouteMasanEntity, Long> {

}