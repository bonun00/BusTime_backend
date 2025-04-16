package bonun.bustime.controller;


import bonun.bustime.dto.LiveStopTimeDTO;
import bonun.bustime.service.LiveStopTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class LiveStopTimeController {

    private final LiveStopTimeService liveStopTimeService;

    @GetMapping("/arrival")
    public List<LiveStopTimeDTO> getArrivalInfo(@RequestParam String nodeId) {
        return liveStopTimeService.getArrivalInfo(nodeId);
    }
}