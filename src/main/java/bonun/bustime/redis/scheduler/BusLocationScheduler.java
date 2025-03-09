package bonun.bustime.redis.scheduler;

import bonun.bustime.api.service.BusLocationService;
import bonun.bustime.dto.BusLocationDTO;
import bonun.bustime.api.service.ActiveRouteService;
import bonun.bustime.redis.cache.BusLocationCache;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BusLocationScheduler {

    private final ActiveRouteService activeRouteService;
    private final BusLocationService busLocationService;
    private final BusLocationCache busLocationCache;


    /**
     * 1분마다 현재 운행 중인 마산 & 칠원 노선들의 실시간 위치 조회
     */
    @Scheduled(fixedRate = 60000)
    public void updateActiveBusLocations() {
        System.out.println("🔄 스케줄링 시작: 현재 운행 중인 노선 위치 조회");

        // 1️⃣ 마산 노선 업데이트
        List<String> activeMasanRouteIds = activeRouteService.getActiveMasanRouteIds();
        updateBusLocations("masan", activeMasanRouteIds);

        // 2️⃣ 칠원 노선 업데이트
        List<String> activeChilwonRouteIds = activeRouteService.getActiveChilwonRouteIds();
        updateBusLocations("chilwon", activeChilwonRouteIds);

        System.out.println("✅ 현재 운행 중인 노선 위치 업데이트 완료");
    }

    /**
     * ✅ 특정 카테고리(마산 or 칠원) 버스 위치를 업데이트하는 메서드
     */
    private void updateBusLocations(String category, List<String> activeRouteIds) {
        if (activeRouteIds.isEmpty()) {
            System.out.println("현재 운행 중인 " + category + " 노선이 없습니다.");
            return;
        }

        for (String routeId : activeRouteIds) {
            List<BusLocationDTO> locations = busLocationService.getBusLocations(routeId);

            // 🔹 차량 번호 목록 추출
            List<String> vehicleNumbers = locations.stream()
                    .map(BusLocationDTO::getRoutenm)  // 🚍 차량 번호 추출
                    .distinct()  // 중복 제거
                    .toList();

            // 🔹 ✅ category 추가하여 Redis에 저장
            busLocationCache.saveLocations(category, routeId, locations);

            // 🚀 차량 번호까지 포함하여 출력
            System.out.println("category=" + category + ", routeId=" + routeId +
                    " -> " + locations.size() + "대 버스 위치 저장 완료" +
                    " | 차량 번호: " + vehicleNumbers);
        }
    }
}