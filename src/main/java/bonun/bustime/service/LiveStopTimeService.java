package bonun.bustime.service;

import bonun.bustime.dto.LiveStopTimeDTO;
import bonun.bustime.external.LiveStopTimeClient;
import bonun.bustime.parser.LiveStopTimeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class LiveStopTimeService {
    private final LiveStopTimeClient stopTimeClient;
    private final LiveStopTimeParser stopTimeParser;

    public List<LiveStopTimeDTO> getArrivalInfo(String nodeId) {
        var response = stopTimeClient.fetchArrivalInfo(nodeId);
        return stopTimeParser.parseArrivalInfo(response, nodeId);
    }
}