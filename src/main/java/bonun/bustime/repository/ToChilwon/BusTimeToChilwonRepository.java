package bonun.bustime.repository.ToChilwon;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.ToChilwon.BusTimeToChilwonEntity;
import bonun.bustime.entity.ToChilwon.RouteChilwonEntity;
import bonun.bustime.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository

public interface BusTimeToChilwonRepository extends JpaRepository<BusTimeToChilwonEntity, Long> {

    // 특정 버스번호와 정류장이름에 해당하는 스케줄을 가져오는 쿼리 메서드
    @Query("""
    SELECT bte 
    FROM BusTimeToChilwonEntity bte
    JOIN FETCH bte.bus bus
    JOIN FETCH bte.stop stop
    JOIN FETCH bte.route route
    WHERE bus.busNumber LIKE %:busNumber%
      AND stop.stopName LIKE %:stopName%
    ORDER BY bte.arrivalTime ASC
""")
    List<BusTimeToChilwonEntity> findByBus_BusNumberContainingAndStop_StopNameContaining(String busNumber, String stopName);
//    List<BusTimeEntity> findByBus_BusNumberAndStop_StopName(String busNumber, String stopName);
// 이미 존재하는 (버스, 정류장, 노선, 도착시간)을 검색하는 메서드
    BusTimeToChilwonEntity findByBusAndStopAndRouteAndArrivalTime(
            BusEntity bus,
            StopEntity stop,
            RouteChilwonEntity route,
            LocalTime arrivalTime
    );

    @Query("""
    SELECT DISTINCT s
    FROM BusTimeToChilwonEntity b
    JOIN b.stop s
    WHERE s.id NOT IN (
        SELECT DISTINCT r.endLocation.id FROM RouteChilwonEntity r
    )
""")
    List<StopEntity> findAllStops();


    /**
     * 특정 버스번호의 전체 운행 노선(정류장 + 도착시간)을 시간 순서대로 조회
     */
    @Query("""
    SELECT DISTINCT bte
    FROM BusTimeToChilwonEntity bte
    JOIN FETCH bte.bus bus
    JOIN FETCH bte.stop stop
    JOIN FETCH bte.route route
    WHERE bus.busNumber = :busNumber
       AND route.id = (
          SELECT MIN(r.id)
          FROM RouteChilwonEntity r
          WHERE r.bus.busNumber = :busNumber
      )
       AND stop.stopName <> route.endLocation.stopName
    ORDER BY bte.arrivalTime ASC
""")
    List<BusTimeToChilwonEntity> findAllByBusNumber(@Param("busNumber") String busNumber);





}