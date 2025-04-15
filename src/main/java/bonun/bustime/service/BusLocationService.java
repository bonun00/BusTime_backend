package bonun.bustime.service;

import bonun.bustime.dto.BusLocationDTO;
import bonun.bustime.external.BusLocationClient;
import bonun.bustime.parser.BusLocationParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLocationService {

    private final BusLocationClient busLocationClient;
    private final BusLocationParser busLocationParser;

    /**
     * ✅ Caffeine 캐시 적용: routeId별 버스 위치 정보를 1분 동안 캐싱
     */
    @Cacheable(value = "busLocation", key = "#routeId")
    public List<BusLocationDTO> getBusLocations(String routeId) {
        if (routeId == null || routeId.isEmpty()) {
            log.warn("🚨 유효하지 않은 routeId: {}", routeId);
            return Collections.emptyList();
        }

        try {
            ResponseEntity<String> response = busLocationClient.getBusLocationByRouteId(routeId);
            List<BusLocationDTO> locations = busLocationParser.parseBusLocations(response);

            for (BusLocationDTO dto : locations) {
                log.info("🚌 vehicleId={} 노선명={} 노선 Id={}",
                        dto.vehicleId(), dto.routenm(),routeId);
            }

            return locations;

        } catch (Exception e) {
            log.error("🚨 버스 위치 조회 중 오류 발생: routeId={}", routeId, e);
            return Collections.emptyList();
        }
    }
}