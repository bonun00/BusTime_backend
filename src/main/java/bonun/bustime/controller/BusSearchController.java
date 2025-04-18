package bonun.bustime.controller;

import bonun.bustime.dto.BusSearchDTO;
import bonun.bustime.service.BusSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusSearchController {

    private final BusSearchService busSearchService;

    /**
     * 칠원 노선 시간표 조회
     * GET /bus/route-chilwon-schedules?departureStop=...&arrivalStop=...
     */
    @GetMapping("/route-chilwon-schedules")
    public List<BusSearchDTO> getChilwonSchedules(
            @RequestParam("departureStop") String departureStop,
            @RequestParam("arrivalStop") String arrivalStop
    ) {
        return busSearchService.getChilwonSchedules(departureStop, arrivalStop);
    }

    /**
     * 마산 노선 시간표 조회
     * GET /bus/route-masan-schedules?departureStop=...&arrivalStop=...
     */
    @GetMapping("/route-masan-schedules")
    public List<BusSearchDTO> getMasanSchedules(
            @RequestParam("departureStop") String departureStop,
            @RequestParam("arrivalStop") String arrivalStop
    ) {
        return busSearchService.getMasanSchedules(departureStop, arrivalStop);
    }
}