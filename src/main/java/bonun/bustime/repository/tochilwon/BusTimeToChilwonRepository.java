package bonun.bustime.repository.tochilwon;

import bonun.bustime.dto.BusSearchDTO;
import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.tochilwon.BusTimeToChilwonEntity;
import bonun.bustime.entity.tochilwon.RouteChilwonEntity;
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
      AND route.id IN (
          SELECT DISTINCT r.route.id
          FROM BusTimeToChilwonEntity r
          WHERE r.bus.busNumber = :busNumber
            AND r.arrivalTime = :arrivalTime
      )
      AND stop.stopName <> route.endLocation.stopName
    ORDER BY bte.arrivalTime ASC
""")
    List<BusTimeToChilwonEntity> findAllByBusNumber(
            @Param("busNumber") String busNumber,
            @Param("arrivalTime")LocalTime arrivalTime
    );




    /**
     * (B) 출발/도착 정류장으로 칠원 노선 시간표 조회
     *     - 출발 정류장: departureStop
     *     - 도착 정류장: arrivalStop
     *
     *  예를 들어, 'route' 테이블에 'endLocation' 정보가 있으면,
     *  'endLocation.stopName' == arrivalStop 인 노선만 필터링 가능.
     *
     *  혹은 아래처럼 단순히 "출발 정류장 == X" & "도착 정류장 == Y" 로직을
     *  어떻게 구성할지에 따라 달라질 수 있습니다.
     */
    @Query("""
SELECT new bonun.bustime.dto.BusSearchDTO(
  b1.arrivalTime,
  b2.arrivalTime,
   bus.busNumber
)
FROM BusTimeToChilwonEntity b1
  JOIN b1.stop s1
  JOIN b1.route r
  JOIN b1.bus bus
  JOIN BusTimeToChilwonEntity b2 ON b2.route = r
  JOIN b2.stop s2
  JOIN b2.bus bus2
WHERE s1.stopName = :departureStop
  AND s2.stopName = :arrivalStop
  AND b1.arrivalTime < b2.arrivalTime
ORDER BY b1.arrivalTime
""")
    List<BusSearchDTO> findChilwonSchedules(
            @Param("departureStop") String departureStop,
            @Param("arrivalStop") String arrivalStop
    );



}