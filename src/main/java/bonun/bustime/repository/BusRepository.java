package bonun.bustime.repository;

import bonun.bustime.entity.BusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<BusEntity, Long> {
    BusEntity findByBusNumber(String busNumber);



}