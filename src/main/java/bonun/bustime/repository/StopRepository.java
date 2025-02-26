package bonun.bustime.repository;

import bonun.bustime.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<StopEntity, Long> {

    StopEntity findByStopName(String stopName);

}