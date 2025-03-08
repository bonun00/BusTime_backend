package bonun.bustime.controller;

import bonun.bustime.api.service.ActiveRouteService;
import bonun.bustime.redis.cache.BusLocationCache;
import bonun.bustime.dto.BusLocationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/bus-locations")
@RequiredArgsConstructor
public class BusLocationController {

    private final BusLocationCache busLocationCache;
    private final ActiveRouteService activeRouteService;

    /**
     * ✅ GET /bus-locations/masan 호출 시 Redis에서 마산 데이터 조회
     */
    @GetMapping(value = "/masan", produces = "application/json")
    public List<BusLocationDTO> getMasanBusLocations() {
        List<String> activeRouteIds = activeRouteService.getActiveMasanRouteIds();
        List<BusLocationDTO> allBusLocations = new ArrayList<>();

        for (String routeId : activeRouteIds) {
            List<BusLocationDTO> locations = busLocationCache.getMasanLocations(routeId);
            allBusLocations.addAll(locations);
        }

        return allBusLocations;
    }

    /**
     * ✅ GET /bus-locations/chilwon 호출 시 Redis에서 칠원 데이터 조회
     */
    @GetMapping(value = "/chilwon", produces = "application/json")
    public List<BusLocationDTO> getChilwonBusLocations() {
        List<String> activeRouteIds = activeRouteService.getActiveChilwonRouteIds();
        List<BusLocationDTO> allBusLocations = new ArrayList<>();

        for (String routeId : activeRouteIds) {
            List<BusLocationDTO> locations = busLocationCache.getChilwonLocations(routeId);
            allBusLocations.addAll(locations);
        }

        return allBusLocations;
    }
}