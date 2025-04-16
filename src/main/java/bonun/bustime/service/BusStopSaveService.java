package bonun.bustime.service;

import bonun.bustime.dto.LocationByStopDTO;
import bonun.bustime.external.StopListByRouteIdClient;
import bonun.bustime.entity.NodeIdEntity;
import bonun.bustime.entity.RouteIdEntity;
import bonun.bustime.parser.StopListByRouteIdParser;
import bonun.bustime.repository.StopListByRouteIdRepository;
import bonun.bustime.repository.RouteIdRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusStopSaveService {

    private final RouteIdRepository routeIdRepository;
    private final StopListByRouteIdClient stopListByRouteIdClient;
    private final StopListByRouteIdParser stopListByRouteIdParser;
    private final StopListByRouteIdRepository stopListByRouteIdRepository;

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
            ResponseEntity<String> response = stopListByRouteIdClient.fetchStopLocations(routeId);

            List<LocationByStopDTO> stops = stopListByRouteIdParser.parseStopLocations(response, routeId);

            if (stops.isEmpty()) {
                log.warn("⚠️ 정류장이 없어서 생략: routeId={}", routeId);
                continue;
            }

            for (LocationByStopDTO stop : stops) {
                if (stopListByRouteIdRepository.existsByNodeId(stop.nodeId())) {
                    log.debug("⏩ 이미 존재하는 정류장 생략: {}", stop.nodeId());
                    continue;
                }
                NodeIdEntity entity = NodeIdEntity.builder()
                        .routeId(routeId)
                        .direction(direction)
                        .nodeId(stop.nodeId())
                        .nodeNm(stop.nodeNm())
                        .latitude(stop.lat())
                        .longitude(stop.lng())
                        .build();

                stopListByRouteIdRepository.save(entity);
            }

            log.info("✅ {} 방향 {} 노선의 정류장 {}개 저장 완료", direction, routeId, stops.size());
        }
    }
}