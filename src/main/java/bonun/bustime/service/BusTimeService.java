package bonun.bustime.service;

import bonun.bustime.dto.BusTimeDTO;
import bonun.bustime.entity.tochilwon.BusTimeToChilwonEntity;
import bonun.bustime.entity.tomasan.BusTimeToMasanEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.repository.tochilwon.BusTimeToChilwonRepository;
import bonun.bustime.repository.tomasan.BusTimeToMasanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusTimeService {

    private final BusTimeToChilwonRepository busTimeToChilwonRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;


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
                )).toList();
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
                )).toList();
    }


    /**
     * 저장된 모든 정류장(StopEntity)을 중복 없이 조회
     */
    public List<StopEntity> getAllChilwonStops() {

        return busTimeToChilwonRepository.findAllStops();
    }
    public List<StopEntity> getAllMasanStops() {

        return busTimeToMasanRepository.findAllStops();
    }


    /**
     * (busNumber, stopName)을 받아,
     * 1) 버스번호로 전체 노선(정류장+시간) 조회
     * 2) DTO로 변환
     * 3) 필요하다면 stopName으로 필터링 or 하이라이트 가능
     */
    public List<BusTimeDTO> getBusChilwonRouteTimes(String busNumber,String time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.parse(time, formatter);
        List<BusTimeToChilwonEntity> busTimes =
                busTimeToChilwonRepository.findAllByBusNumber(busNumber,localTime);

        // 2) DTO 변환
        return busTimes.stream()
                .map(bte -> new BusTimeDTO(
                        bte.getBus().getBusNumber(),
                        bte.getStop().getStopName(),
                        bte.getRoute().getEndLocation().getStopName(),
                        bte.getArrivalTime()
                ))
                .toList();
    }


    public List<BusTimeDTO> getBusMasanRouteTimes(String busNumber, String time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.parse(time, formatter);
        List<BusTimeToMasanEntity> busTimes =
                busTimeToMasanRepository.findAllByBusNumber(busNumber,localTime);

        return busTimes.stream()
                .map(bte -> new BusTimeDTO(
                        bte.getBus().getBusNumber(),
                        bte.getStop().getStopName(),
                        bte.getRoute().getEndLocation().getStopName(),
                        bte.getArrivalTime()
                ))
                .toList();
    }



}