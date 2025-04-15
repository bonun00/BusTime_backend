package bonun.bustime.service;

import bonun.bustime.entity.RouteIdEntity;
import bonun.bustime.entity.tochilwon.RouteChilwonEntity;
import bonun.bustime.entity.tomasan.RouteMasanEntity;
import bonun.bustime.repository.RouteIdRepository;
import bonun.bustime.repository.tochilwon.RouteChilwonRepository;
import bonun.bustime.repository.tomasan.RouteMasanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveRouteService {

    private final RouteChilwonRepository routeChilwonRepository;
    private final RouteMasanRepository routeMasanRepository;
    private final RouteIdRepository routeIdRepository;

    // 방향 상수
    private static final String DIRECTION_CHILWON = "칠원";
    private static final String DIRECTION_MASAN = "마산";

    /**
     * 현재 운행 중인 칠원 노선들의 "routeId"를 반환
     */
    public List<String> getActiveChilwonRouteIds() {
        LocalTime now = LocalTime.now();
        List<String> activeRouteIds = new ArrayList<>();
        List<RouteChilwonEntity> chilwonRoutes = routeChilwonRepository.findAll();

        for (RouteChilwonEntity route : chilwonRoutes) {
            if (isInOperation(route.getStartLocationTime(), route.getEndLocationTime(), now)) {

                addMatchingRouteId(route.getBus().getBusNumber(), activeRouteIds, DIRECTION_CHILWON);
            }
        }
        return activeRouteIds;
    }

    /**
     * 현재 운행 중인 마산 노선들의 "routeId"를 반환
     */
    public List<String> getActiveMasanRouteIds() {
        LocalTime now = LocalTime.now();
        List<String> activeRouteIds = new ArrayList<>();
        List<RouteMasanEntity> masanRoutes = routeMasanRepository.findAll();

        for (RouteMasanEntity route : masanRoutes) {
            if (isInOperation(route.getStartLocationTime(), route.getEndLocationTime(), now)) {

                addMatchingRouteId(route.getBus().getBusNumber(), activeRouteIds, DIRECTION_MASAN);
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
    private void addMatchingRouteId(String busNumber, List<String> activeRouteIds, String direction) {
        if (busNumber == null) return;

        List<RouteIdEntity> matchingRoutes = routeIdRepository.findByDirectionAndRouteNo(direction, busNumber);
        log.info("방향: {}, 버스 번호: {}에 대한 매칭 노선 수: {}", direction, busNumber, matchingRoutes.size());

        for (RouteIdEntity route : matchingRoutes) {
            activeRouteIds.add(route.getRouteId());
        }
    }
}