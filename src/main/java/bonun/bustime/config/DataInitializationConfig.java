package bonun.bustime.config;

import bonun.bustime.external.bus.service.BusDataService;
import bonun.bustime.external.bus.service.BusStopService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializationConfig {

    private final BusDataService busDataService;
    private final BusStopService routePathSaveService;

    @Value("#{'${bus.routes.to-fetch}'.split(',')}")
    private List<String> routesToFetch;

    @PostConstruct
    public void initializeData() {
        log.info("📝 버스 노선 데이터 초기화 시작: {}", routesToFetch);

        for (String routeNumber : routesToFetch) {
            String result = busDataService.fetchAndSaveRouteId(routeNumber.trim());
            log.info("📝 버스 노선 {} 초기화 결과: {}", routeNumber, result);
        }
        log.info("📝 버스 노선 데이터 초기화 완료");
        log.info("Initializing data");
        routePathSaveService.saveAllRoutePaths();
        log.info("Data loaded");
    }
}