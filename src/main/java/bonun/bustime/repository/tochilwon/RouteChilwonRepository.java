package bonun.bustime.repository.tochilwon;

import bonun.bustime.entity.tochilwon.RouteChilwonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteChilwonRepository extends JpaRepository<RouteChilwonEntity, Long> {

}