package bonun.bustime.websocket;

import bonun.bustime.external.bus.service.BusLocationService;
import bonun.bustime.service.ActiveRouteService;
import bonun.bustime.external.bus.dto.BusLocationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Controller
@RequiredArgsConstructor
public class BusLocationWebSocketHandler {

    private final BusLocationService busLocationService;
    private final ActiveRouteService activeRouteService;
    private final SimpMessagingTemplate messagingTemplate;

    private final ConcurrentHashMap<String, List<BusLocationDTO>> cachedLocations = new ConcurrentHashMap<>();

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    public void init() {
        updateCachedData();
    }

    @MessageMapping("/masan")
    @SendTo("/topic/masan")
    public List<BusLocationDTO> sendMasanBusLocations() {
        List<BusLocationDTO> result = cachedLocations.getOrDefault("masan", getBusLocations("masan"));
        log.info("📥 [Client 요청] /app/masan → /topic/masan 전송: {}대", result.size());
        return result;

    }

    @MessageMapping("/chilwon")
    @SendTo("/topic/chilwon")
    public List<BusLocationDTO> sendChilwonBusLocations() {
        return cachedLocations.getOrDefault("chilwon", getBusLocations("chilwon"));
    }

    @Scheduled(fixedRate = 60000)
    public void updateBusLocations() {
        updateCachedData();

        List<BusLocationDTO> masan = cachedLocations.get("masan");
        List<BusLocationDTO> chilwon = cachedLocations.get("chilwon");

        log.info("📤 [WebSocket 전송] 🧭 마산 {}대 → /topic/masan", masan != null ? masan.size() : 0);
        log.info("📤 [WebSocket 전송] 🧭 칠원 {}대 → /topic/chilwon", chilwon != null ? chilwon.size() : 0);

        messagingTemplate.convertAndSend("/topic/masan", cachedLocations.get("masan"));
        messagingTemplate.convertAndSend("/topic/chilwon", cachedLocations.get("chilwon"));
    }

    private void updateCachedData() {
        cachedLocations.put("masan", getBusLocations("masan"));
        cachedLocations.put("chilwon", getBusLocations("chilwon"));
    }

    private List<BusLocationDTO> getBusLocations(String category) {
        List<String> activeRouteIds = category.equals("masan") ?
                activeRouteService.getActiveMasanRouteIds() :
                activeRouteService.getActiveChilwonRouteIds();

        List<BusLocationDTO> allBusLocations = new ArrayList<>();
        for (String routeId : activeRouteIds) {
            log.warn("🚨 조회 지역{}", category);
            List<BusLocationDTO> locations = busLocationService.getBusLocations(routeId);

            allBusLocations.addAll(locations);
        }
        return allBusLocations;
    }
}