package bonun.bustime.repository;

import bonun.bustime.entity.BusTimeToChilwonEntity;
import bonun.bustime.entity.BusTimeToMasanEntity;
import bonun.bustime.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusTimeToMasanRepository extends JpaRepository<BusTimeToMasanEntity, Long> {
    @Query("""
        SELECT bte 
        FROM BusTimeToMasanEntity bte
        JOIN FETCH bte.bus bus
        JOIN FETCH bte.stop stop
        JOIN FETCH bte.route route
        WHERE bus.busNumber LIKE %:busNumber%
          AND stop.stopName LIKE %:stopName%
        ORDER BY bte.arrivalTime ASC
    """)
    List<BusTimeToMasanEntity> findByBus_BusNumberContainingAndStop_StopNameContaining(String busNumber, String stopName);



    @Query("""
    SELECT DISTINCT s
    FROM BusTimeToMasanEntity b
    JOIN b.stop s
""")
    List<StopEntity> findAllStops();

    /**
     * 특정 버스번호의 전체 운행 노선(정류장 + 도착시간)을 시간 순서대로 조회
     */
    @Query("""
        SELECT bte
        FROM BusTimeToMasanEntity bte
        JOIN FETCH bte.bus bus
        JOIN FETCH bte.stop stop
        JOIN FETCH bte.route route
        WHERE bus.busNumber LIKE %:busNumber%
        ORDER BY bte.arrivalTime ASC
    """)
    List<BusTimeToMasanEntity> findAllByBusNumber(@Param("busNumber") String busNumber);


}