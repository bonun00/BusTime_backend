package bonun.bustime.api.service;

import bonun.bustime.api.entity.RouteIdEntity;
import bonun.bustime.entity.ToChilwon.RouteChilwonEntity;
import bonun.bustime.entity.ToMasan.RouteMasanEntity;
import bonun.bustime.api.repository.RouteIdRepository;
import bonun.bustime.repository.ToChilwon.RouteChilwonRepository;
import bonun.bustime.repository.ToMasan.RouteMasanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiveRouteService {

    private final RouteChilwonRepository routeChilwonRepository;
    private final RouteMasanRepository routeMasanRepository;
    private final RouteIdRepository routeIdRepository;

    /**
     * 현재 운행 중인 칠원 노선들의 "routeId"를 반환 (디버깅: 출발 및 종점 시간)
     */
    public List<String> getActiveChilwonRouteIds() {
        LocalTime now = LocalTime.now();
        List<String> activeRouteIds = new ArrayList<>();

        // 1) 칠원 노선 전체 조회
        List<RouteChilwonEntity> chilwonAll = routeChilwonRepository.findAll();
        for (RouteChilwonEntity route : chilwonAll) {


            if (isInOperation(route.getStartLocationTime(), route.getEndLocationTime(), now)) {
                System.out.println("   ✅ 현재 운행 중: " + route.getBus().getBusNumber());
                System.out.println("🔎 [칠원] 버스 번호: " + route.getBus().getBusNumber());
                System.out.println("   ▶ 출발 시간: " + route.getStartLocationTime());
                System.out.println("   ▶ 종점 시간: " + route.getEndLocationTime());



                addMatchingRouteId(route.getBus().getBusNumber(), activeRouteIds);
            }
        }
        return activeRouteIds;
    }

    /**
     * 현재 운행 중인 마산 노선들의 "routeId"를 반환 (디버깅: 출발 및 종점 시간)
     */
    public List<String> getActiveMasanRouteIds() {
        LocalTime now = LocalTime.now();
        List<String> activeRouteIds = new ArrayList<>();

        // 2) 마산 노선 전체 조회
        List<RouteMasanEntity> masanAll = routeMasanRepository.findAll();
        for (RouteMasanEntity route : masanAll) {
            if (isInOperation(route.getStartLocationTime(), route.getEndLocationTime(), now)) {
                System.out.println("   ✅ 현재 운행 중: " + route.getBus().getBusNumber());
                System.out.println("🔎 [마산] 버스 번호: " + route.getBus().getBusNumber());
                System.out.println("   ▶ 출발 시간: " + route.getStartLocationTime());
                System.out.println("   ▶ 종점 시간: " + route.getEndLocationTime());
                addMatchingRouteId(route.getBus().getBusNumber(), activeRouteIds);
            }
        }

        return activeRouteIds;
    }

    /**
     * 현재 운행 중인 노선이 있는지 확인
     */
    private boolean isInOperation(LocalTime start, LocalTime end, LocalTime now) {
        if (start == null || end == null) return false;
        return !now.isBefore(start) && !now.isAfter(end);
    }

    /**
     * busNumber와 일치하는 RouteId를 찾아서 리스트에 추가
     */
    private void addMatchingRouteId(String busNumber, List<String> activeRouteIds) {
        if (busNumber == null) return;
        List<RouteIdEntity> matchingRoutes = routeIdRepository.findByRouteNo(busNumber);

        for (RouteIdEntity route : matchingRoutes) {
            activeRouteIds.add(route.getRouteId());
        }
    }
}