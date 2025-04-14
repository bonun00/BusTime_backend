package bonun.bustime.repository.tomasan;

import bonun.bustime.dto.TimePairDTO;
import bonun.bustime.entity.tomasan.BusTimeToMasanEntity;
import bonun.bustime.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
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
    WHERE s.id NOT IN (
        SELECT DISTINCT r.startLocation.id FROM RouteMasanEntity r
        UNION
        SELECT DISTINCT r.endLocation.id FROM RouteMasanEntity r
    )
""")
    List<StopEntity> findAllStops();

    /**
     * 특정 버스번호의 전체 운행 노선(정류장 + 도착시간)을 시간 순서대로 조회
     */
    @Query("""
    SELECT DISTINCT bte
    FROM BusTimeToMasanEntity bte
    JOIN FETCH bte.bus bus
    JOIN FETCH bte.stop stop
    JOIN FETCH bte.route route
    WHERE bus.busNumber = :busNumber
      AND route.id IN (
          SELECT DISTINCT r.route.id
          FROM BusTimeToMasanEntity r
          WHERE r.bus.busNumber = :busNumber
            AND r.arrivalTime = :arrivalTime
      )
      AND stop.stopName <> route.endLocation.stopName
    ORDER BY bte.arrivalTime ASC
""")
    List<BusTimeToMasanEntity> findAllByBusNumber(
            @Param("busNumber") String busNumber,
            @Param("arrivalTime") LocalTime arrivalTime);




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
SELECT new bonun.bustime.dto.TimePairDTO(
  b1.arrivalTime,
  b2.arrivalTime,
  bus.busNumber
)
FROM BusTimeToMasanEntity b1
  JOIN b1.stop s1
  JOIN b1.route r
  JOIN b1.bus bus
  JOIN BusTimeToMasanEntity b2 ON b2.route = r
  JOIN b2.stop s2
  JOIN b2.bus bus2
WHERE s1.stopName = :departureStop
  AND s2.stopName = :arrivalStop
  AND b1.arrivalTime < b2.arrivalTime
ORDER BY b1.arrivalTime
""")
    List<TimePairDTO> findMasanSchedules(
            @Param("departureStop") String departureStop,
            @Param("arrivalStop") String arrivalStop
    );




}