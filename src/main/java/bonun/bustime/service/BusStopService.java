package bonun.bustime.service;

import bonun.bustime.dto.LocationByStopDTO;
import bonun.bustime.external.PublicBusApiClient;
import bonun.bustime.entity.BusStopEntity;
import bonun.bustime.entity.RouteIdEntity;
import bonun.bustime.repository.BusStopRepository;
import bonun.bustime.repository.RouteIdRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusStopService {

    private final RouteIdRepository routeIdRepository;
    private final PublicBusApiClient publicBusApiClient;
    private final BusStopRepository busStopRepository;


    public void saveAllRoutePaths() {
        saveByDirection("마산");
        saveByDirection("칠원");
    }


    public void saveByDirection(String direction) {
        log.info("💬 방향으로 노선 조회 시작: {}", direction);
        List<RouteIdEntity> routeList = routeIdRepository.findByDirection(direction);
        log.info("🔍 조회 결과: {}개", routeList.size());

        for (RouteIdEntity route : routeList) {
            String routeId = route.getRouteId();
            List<LocationByStopDTO> stops = publicBusApiClient.getStopsByRouteId(routeId);

            if (stops.isEmpty()) {
                log.warn("⚠️ 정류장이 없어서 생략: routeId={}", routeId);
                continue;
            }

            for (LocationByStopDTO stop : stops) {
                if (busStopRepository.existsByNodeId(stop.nodeId())) {
                    log.debug("⏩ 이미 존재하는 정류장 생략: {}", stop.nodeId());
                    continue;
                }
                BusStopEntity entity = BusStopEntity.builder()
                        .routeId(routeId)
                        .direction(direction)
                        .nodeId(stop.nodeId())
                        .nodeNm(stop.nodeNm())
                        .latitude(stop.lat())
                        .longitude(stop.lng())
                        .build();

                busStopRepository.save(entity);
            }

            log.info("✅ {} 방향 {} 노선의 정류장 {}개 저장 완료", direction, routeId, stops.size());
        }
    }
}