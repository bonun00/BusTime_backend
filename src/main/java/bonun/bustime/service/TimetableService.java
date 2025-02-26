//package bonun.bustime.service;
//
////import bonun.bustime.DTO.TimetableDTO;
//import bonun.bustime.entity.BusEntity;
//import bonun.bustime.entity.StopEntity;
//import bonun.bustime.entity.TimetableEntity;
//import bonun.bustime.repository.BusRepository;
//import bonun.bustime.repository.StopRepository;
//import bonun.bustime.repository.TimetableRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class TimetableService {
//
//    private final StopRepository stopRepository;
//    private final BusRepository busRepository;
//    private final TimetableRepository timetableRepository;
//
//    public TimetableService(StopRepository stopRepository, BusRepository busRepository, TimetableRepository timetableRepository) {
//        this.stopRepository = stopRepository;
//        this.busRepository = busRepository;
//        this.timetableRepository = timetableRepository;
//    }
//
//    /**
//     * 정류장 이름과 버스 번호를 기반으로 시간표 조회
//     */
//    public List<TimetableDTO> getTimetableByStopNameAndBusNumber(String stopName, String busNumber) {
//        // 정류장 정보 조회
//        StopEntity stop = stopRepository.findByStopName(stopName);
//        if (stop == null) {
//            throw new IllegalArgumentException("정류장을 찾을 수 없습니다: " + stopName);
//        }
//
//        // 버스 정보 조회
//        BusEntity bus = busRepository.findByBusNumber(busNumber);
//        if (bus == null) {
//            throw new IllegalArgumentException("버스를 찾을 수 없습니다: " + busNumber);
//        }
//        String busName = bus.getBusNumber().split("-")[0];
//        // 시간표 조회
//        List<TimetableEntity> timetables = timetableRepository.findByStopStopNameAndBusBusNumber(stop.getStopName(),busName);
//
//        // 시간표를 DTO로 변환하여 반환
//        return timetables.stream()
//                .map(timetable -> new TimetableDTO(
//                        timetable.getTimetableId(),
//                        timetable.getStop().getStopId(),
//                        timetable.getBus().getBusId(),
//                        timetable.getTime()
//                ))
//                .collect(Collectors.toList());
//    }
//}