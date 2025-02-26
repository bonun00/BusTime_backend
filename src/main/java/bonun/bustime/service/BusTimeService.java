package bonun.bustime.service;

import bonun.bustime.DTO.BusTimeDTO;
import bonun.bustime.entity.BusTimeToChilwonEntity;
import bonun.bustime.entity.BusTimeToMasanEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.repository.BusTimeToChilwonRepository;
import bonun.bustime.repository.BusTimeToMasanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusTimeService {

    private final BusTimeToChilwonRepository busTimeToChilwonRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;
    public BusTimeService(BusTimeToChilwonRepository busTimeToChilwonRepository, BusTimeToMasanRepository busTimeToMasanRepository) {
        this.busTimeToChilwonRepository = busTimeToChilwonRepository;
        this.busTimeToMasanRepository = busTimeToMasanRepository;
    }


    @Transactional(readOnly = true)
    public List<BusTimeDTO> getArrivalMasanTimes(String busNumber, String stopName) {
        List<BusTimeToMasanEntity> busTimes = busTimeToMasanRepository.findByBus_BusNumberContainingAndStop_StopNameContaining(busNumber, stopName);

        // DTO로 변환하여 반환

        return busTimes.stream()
                .map(busTime -> new BusTimeDTO(
                        busTime.getBus().getBusNumber(),   // 버스번호
                        busTime.getStop().getStopName(),   // 정류장 이름
                        busTime.getRoute().getEndLocation().getStopName(),      // 노선 이름
                        busTime.getArrivalTime()           // 도착 시간
                ))
                .collect(Collectors.toList());
    }


    /**
     * 특정 버스번호와 정류장이름으로 도착시간을 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public List<BusTimeDTO> getArrivalChilwonTimes(String busNumber, String stopName) {
        List<BusTimeToChilwonEntity> busTimes = busTimeToChilwonRepository.findByBus_BusNumberContainingAndStop_StopNameContaining(busNumber, stopName);

        // DTO로 변환하여 반환

        return busTimes.stream()
                .map(busTime -> new BusTimeDTO(
                        busTime.getBus().getBusNumber(),   // 버스번호
                        busTime.getStop().getStopName(),   // 정류장 이름
                        busTime.getRoute().getEndLocation().getStopName(),      // 노선 이름
                        busTime.getArrivalTime()           // 도착 시간
                ))
                .collect(Collectors.toList());
    }


    /**
     * 저장된 모든 정류장(StopEntity)을 중복 없이 조회
     */
    public List<StopEntity> getAllChilwonStops() {
        // BusTimeToChilwonRepository에 정의된 JPQL:
        // SELECT DISTINCT s FROM BusTimeToChilwonEntity b JOIN b.stop s
        return busTimeToChilwonRepository.findAllStops();
    }
    public List<StopEntity> getAllMasanStops() {
        // BusTimeToChilwonRepository에 정의된 JPQL:
        // SELECT DISTINCT s FROM BusTimeToChilwonEntity b JOIN b.stop s
        return busTimeToMasanRepository.findAllStops();
    }


    /**
     * (busNumber, stopName)을 받아,
     * 1) 버스번호로 전체 노선(정류장+시간) 조회
     * 2) DTO로 변환
     * 3) 필요하다면 stopName으로 필터링 or 하이라이트 가능
     */
    public List<BusTimeDTO> getBusChilwonRouteTimes(String busNumber) {
        // 1) DB 조회: 해당 버스번호의 모든 정류장 운행 시간
        List<BusTimeToChilwonEntity> busTimes =
                busTimeToChilwonRepository.findAllByBusNumber(busNumber);

        // 2) DTO 변환
        List<BusTimeDTO> routeList = busTimes.stream()
                .map(bte -> new BusTimeDTO(
                        bte.getBus().getBusNumber(),
                        bte.getStop().getStopName(),
                        // 예: 종점 이름을 routeName으로 삼는다거나, 필요에 따라
                        bte.getRoute().getEndLocation().getStopName(),
                        bte.getArrivalTime()
                ))
                .collect(Collectors.toList());

        // 3) stopName을 추가로 활용(필요시)
        // 예: stopName이 비어있지 않다면, 그 정류장을 상위로 표시/필터
        // 여기서는 단순히 "전체 노선" 반환
        return routeList;
    }


    public List<BusTimeDTO> getBusMasanRouteTimes(String busNumber) {
        // 1) DB 조회: 해당 버스번호의 모든 정류장 운행 시간
        List<BusTimeToMasanEntity> busTimes =
                busTimeToMasanRepository.findAllByBusNumber(busNumber);

        // 2) DTO 변환
        List<BusTimeDTO> routeList = busTimes.stream()
                .map(bte -> new BusTimeDTO(
                        bte.getBus().getBusNumber(),
                        bte.getStop().getStopName(),
                        // 예: 종점 이름을 routeName으로 삼는다거나, 필요에 따라
                        bte.getRoute().getEndLocation().getStopName(),
                        bte.getArrivalTime()
                ))
                .collect(Collectors.toList());

        // 3) stopName을 추가로 활용(필요시)
        // 예: stopName이 비어있지 않다면, 그 정류장을 상위로 표시/필터
        // 여기서는 단순히 "전체 노선" 반환
        return routeList;
    }



}