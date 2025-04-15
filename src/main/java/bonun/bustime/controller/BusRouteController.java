package bonun.bustime.controller;

import bonun.bustime.entity.BusStopEntity;
import bonun.bustime.repository.BusStopRepository;

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