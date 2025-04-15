package bonun.bustime.controller;


import bonun.bustime.external.StopTimeClient;
import bonun.bustime.dto.ArrivalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class StopTimeController {

    private final StopTimeClient stopTimeClient;

    @GetMapping("/arrival")
    public List<ArrivalInfo> getArrivalInfo(@RequestParam String nodeId) {
        return stopTimeClient.getArrivalInfoByNodeId(nodeId);
    }
}