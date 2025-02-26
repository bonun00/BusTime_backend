package bonun.bustime.controller;

import bonun.bustime.DTO.BusTimeDTO;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.service.BusTimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus") // 기본 API 경로 추가
public class BusTimeController {

    private final BusTimeService busTimeService;

    public BusTimeController(BusTimeService busTimeService) {
        this.busTimeService = busTimeService;
    }


    @GetMapping("/Masan-times")
    public ResponseEntity<List<BusTimeDTO>> getArrivalMasanTimes(
            @RequestParam("busNumber") String busNumber,
            @RequestParam("stopName") String stopName) {

        List<BusTimeDTO> arrivalTimes = busTimeService.getArrivalMasanTimes(busNumber, stopName);

        return ResponseEntity.ok(arrivalTimes);
    }



    @GetMapping("/chilwon-times")
    public ResponseEntity<List<BusTimeDTO>> getArrivalChilwonTimes(
            @RequestParam("busNumber") String busNumber,
            @RequestParam("stopName") String stopName) {

        List<BusTimeDTO> arrivalTimes = busTimeService.getArrivalChilwonTimes(busNumber, stopName);

        return ResponseEntity.ok(arrivalTimes);
    }


    /**
     * 칠원 노선과 연관된 모든 정류장(StopEntity)을 중복 없이 조회
     */
    @GetMapping("/chilwon/stops")
    public List<StopEntity> getAllChilwonStops() {
        return busTimeService.getAllChilwonStops();
    }



    @GetMapping("/masan/stops")
    public List<StopEntity> getAllMasanStops() {
        return busTimeService.getAllMasanStops();
    }



    @GetMapping("/chilwon/route")
    public List<BusTimeDTO> getChilwonBusRoute(
            @RequestParam("busNumber") String busNumber
    ) {
        return busTimeService.getBusChilwonRouteTimes(busNumber);
    }

    @GetMapping("/masan/route")
    public List<BusTimeDTO> getMasanBusRoute(
            @RequestParam("busNumber") String busNumber
    ) {
        return busTimeService.getBusMasanRouteTimes(busNumber);
    }


}