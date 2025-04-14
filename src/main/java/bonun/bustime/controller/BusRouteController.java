package bonun.bustime.controller;

import bonun.bustime.external.bus.entity.BusStopEntity;
import bonun.bustime.external.bus.repository.BusStopRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusRouteController {
    //TODO 단일책임원칙에따라 서비스계층 DTO
    private final BusStopRepository routePathRepository;

    @GetMapping("/path")
    public ResponseEntity<List<BusStopEntity>> findRoutePathByRouteId(@RequestParam("direction") String direction) {


        List<BusStopEntity>result = routePathRepository.findByDirection(direction);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}