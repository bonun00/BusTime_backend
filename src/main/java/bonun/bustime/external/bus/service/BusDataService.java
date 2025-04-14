package bonun.bustime.external.bus.service;

import bonun.bustime.external.bus.entity.RouteIdEntity;
import bonun.bustime.external.bus.repository.RouteIdRepository;
import bonun.bustime.external.bus.client.BusApiClient;
import bonun.bustime.external.bus.dto.response.BusRouteResponse;
import bonun.bustime.external.bus.mapper.RouteMapper;
import bonun.bustime.external.bus.parser.BusDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusDataService {

    private final BusApiClient busApiClient;
    private final BusDataParser dataParser;
    private final RouteMapper routeMapper;
    private final RouteIdRepository routeIdRepository;

    /**
     * 버스 번호로 노선 정보를 조회하고 저장
     */
    public String fetchAndSaveRouteId(String busNumber) {
        try {
            // 1. API 호출
            ResponseEntity<String> response = busApiClient.fetchBusRouteInfo(busNumber);

            // 2. routeNo 일치 여부 확인
            String routeNo = dataParser.extractRouteNo(response);
            if (routeNo == null) {
                return "해당 버스 번호에 대한 정보가 없습니다.(routeno=null)";
            }

            if (!routeNo.startsWith(busNumber)) {
                log.warn("🚨 API 응답 routeno={} 이 busNumber={}로 시작하지 않음", routeNo, busNumber);
            }

            // 3. 노선 정보 파싱
            List<BusRouteResponse> routeInfoList = dataParser.parseRouteInfo(response);
            if (routeInfoList.isEmpty()) {
                return "해당 버스 번호에 대한 routeId를 찾을 수 없습니다.";
            }

            // 4. DB에 저장
            int saveCount = saveRouteEntities(routeInfoList, busNumber);

            return "routeId 저장 완료 (총 " + saveCount + "개)";
        } catch (Exception e) {
            log.error("API 요청 중 오류 발생", e);
            return "API 요청 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * 노선 정보 저장 처리
     */
    private int saveRouteEntities(List<BusRouteResponse> routeInfoList, String busNumber) {
        int saveCount = 0;

        for (BusRouteResponse routeInfo : routeInfoList) {
            String routeNo = routeInfo.getRouteNo();
            if (routeNo == null) continue;

            // "113-", "250-" 로 시작하는지만 체크
            if (!(routeNo.startsWith("113-") || routeNo.startsWith("250-"))) {
                continue;
            }

            // 이미 존재하는지 확인
            Optional<RouteIdEntity> existing = routeIdRepository.findByRouteId(routeInfo.getRouteId());
            if (existing.isPresent()) {
//                log.info("이미 저장된 routeId={}", routeInfo.getRouteId());
            } else {
                // DTO -> Entity 변환 및 저장
                RouteIdEntity entity = routeMapper.toEntity(routeInfo);
                routeIdRepository.save(entity);

                log.info("✅ routeId={} 저장 완료 (routeNo={}, direction={})",
                        entity.getRouteId(), entity.getRouteNo(), entity.getDirection());
                saveCount++;
            }
        }

        return saveCount;
    }
}