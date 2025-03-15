package bonun.bustime.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSender {

    private static SimpMessagingTemplate messagingTemplate;

    public WebSocketSender(SimpMessagingTemplate template) {
        WebSocketSender.messagingTemplate = template;
    }

    /**
     * ✅ 지정된 /topic으로 메시지를 전송
     */
    public static void sendToTopic(String destination, Object message) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend(destination, message);
        }
    }
}