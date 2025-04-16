package bonun.bustime.controller;

import bonun.bustime.entity.NodeIdEntity;
import bonun.bustime.repository.StopListByRouteIdRepository;

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
public class StopListController {


    private final StopListByRouteIdRepository stopListByRouteIdRepository;

    @GetMapping("/path")
    public ResponseEntity<List<NodeIdEntity>> findRoutePathByRouteId() {


        List<NodeIdEntity>result = stopListByRouteIdRepository.findAll();

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}