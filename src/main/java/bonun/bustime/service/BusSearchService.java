package bonun.bustime.service;

import bonun.bustime.dto.TimePairDTO;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.entity.ToChilwon.BusTimeToChilwonEntity;
import bonun.bustime.entity.ToMasan.BusTimeToMasanEntity;
import bonun.bustime.repository.ToChilwon.BusTimeToChilwonRepository;
import bonun.bustime.repository.ToMasan.BusTimeToMasanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusSearchService {



    private final BusTimeToChilwonRepository busTimeToChilwonRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;


    /**
     * 출발 정류장(departureStop)과 도착 정류장(arrivalStop)을 기준으로
     * 칠원 노선 버스 시간표 조회
     */

    public List<TimePairDTO> getChilwonSchedules(String departureStop, String arrivalStop) {
        return busTimeToChilwonRepository.findChilwonSchedules(departureStop, arrivalStop);
    }

    /**
     * 출발 정류장(departureStop)과 도착 정류장(arrivalStop)을 기준으로
     * 마산 노선 버스 시간표 조회
     */
    public List<TimePairDTO> getMasanSchedules(String departureStop, String arrivalStop) {
        return busTimeToMasanRepository.findMasanSchedules(departureStop, arrivalStop);
    }

}
