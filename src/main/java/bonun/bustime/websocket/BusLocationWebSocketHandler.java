//package bonun.bustime.websocket;
//
//import bonun.bustime.api.service.ActiveRouteService;
//import bonun.bustime.redis.cache.BusLocationCache;
//import bonun.bustime.dto.BusLocationDTO;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Controller;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class BusLocationWebSocketHandler {
//
//    private final BusLocationCache busLocationCache;
//    private final ActiveRouteService activeRouteService;
//
//    /**
//     * ✅ 클라이언트가 "/app/masan"으로 요청하면 실시간 버스 데이터를 반환
//     */
//    @MessageMapping("/masan")
//    @SendTo("/topic/masan")
//    public List<BusLocationDTO> sendMasanBusLocations() {
//        return getBusLocations("masan");
//    }
//
//    /**
//     * ✅ 클라이언트가 "/app/chilwon"으로 요청하면 실시간 버스 데이터를 반환
//     */
//    @MessageMapping("/chilwon")
//    @SendTo("/topic/chilwon")
//    public List<BusLocationDTO> sendChilwonBusLocations() {
//        return getBusLocations("chilwon");
//    }
//
//    /**
//     * ✅ 30초마다 자동으로 버스 위치 정보를 클라이언트에게 전송
//     */
//    @Scheduled(fixedRate = 60000) // 30초마다 실행
//    public void updateBusLocations() {
//        List<BusLocationDTO> masanData = getBusLocations("masan");
//        List<BusLocationDTO> chilwonData = getBusLocations("chilwon");
//
//        // 웹소켓을 통해 데이터 전송
//        WebSocketSender.sendToTopic("/topic/masan", masanData);
//        WebSocketSender.sendToTopic("/topic/chilwon", chilwonData);
//    }
//
//    /**
//     * ✅ Redis에서 버스 위치 정보 가져오기
//     */
//    private List<BusLocationDTO> getBusLocations(String category) {
//        List<String> activeRouteIds = category.equals("masan") ?
//                activeRouteService.getActiveMasanRouteIds() :
//                activeRouteService.getActiveChilwonRouteIds();
//
//        List<BusLocationDTO> allBusLocations = new ArrayList<>();
//        for (String routeId : activeRouteIds) {
//            List<BusLocationDTO> locations = category.equals("masan") ?
//                    busLocationCache.getMasanLocations(routeId) :
//                    busLocationCache.getChilwonLocations(routeId);
//
//            allBusLocations.addAll(locations);
//        }
//        return allBusLocations;
//    }
//}