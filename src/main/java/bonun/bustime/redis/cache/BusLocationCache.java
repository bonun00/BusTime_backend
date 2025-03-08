package bonun.bustime.redis.cache;

import bonun.bustime.dto.BusLocationDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BusLocationCache {

    private static final Logger log = LoggerFactory.getLogger(BusLocationCache.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ✅ Redis에 마산 & 칠원 데이터를 저장 (유효시간: 1분)
     */
    public void saveLocations(String category, String routeId, List<BusLocationDTO> locations) {
        String redisKey = "bus_location:" + category + ":" + routeId; // ✅ 키 형식 유지

        try {
            Object existingData = redisTemplate.opsForValue().get(redisKey);
            String newDataJson = objectMapper.writeValueAsString(locations != null ? locations : List.of());

            if (existingData != null && existingData.toString().equals(newDataJson)) {
                log.info("🔄 기존 데이터와 동일하여 저장을 건너뜀 (key={})", redisKey);
                return;
            }

            redisTemplate.opsForValue().set(redisKey, newDataJson, 1, TimeUnit.MINUTES);
            log.info("✅ Redis에 버스 위치 데이터 저장 완료 (key={}, size={})", redisKey, locations != null ? locations.size() : 0);
        } catch (Exception e) {
            log.error("🚨 Redis 저장 중 JSON 변환 오류", e);
        }
    }

    /**
     * ✅ Redis에서 마산 버스 위치 조회
     */
    public List<BusLocationDTO> getMasanLocations(String routeId) {
        return getLocations("masan", routeId);
    }

    /**
     * ✅ Redis에서 칠원 버스 위치 조회
     */
    public List<BusLocationDTO> getChilwonLocations(String routeId) {
        return getLocations("chilwon", routeId);
    }

    /**
     * ✅ 공통적으로 Redis에서 데이터 조회
     */
    private List<BusLocationDTO> getLocations(String category, String routeId) {
        String redisKey = "bus_location:" + category + ":" + routeId; // ✅ 키 형식 유지
        Object cachedData = redisTemplate.opsForValue().get(redisKey);

        if (cachedData == null) {
            log.warn("🚨 Redis에서 key={} 데이터를 찾을 수 없습니다.", redisKey);
            return List.of();
        }

        try {
            return objectMapper.readValue(cachedData.toString(), new TypeReference<List<BusLocationDTO>>() {});
        } catch (Exception e) {
            log.error("🚨 Redis 데이터 변환 오류 (key={})", redisKey, e);
            return List.of();
        }
    }
}