package bonun.bustime.api.service;

import bonun.bustime.api.BusApi;
import bonun.bustime.repository.BusRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class BusApiService {
    private final BusRepository busRepository;
    private final BusApi busApi;

    public BusApiService( BusApi busApi, BusRepository busRepository) {

        this.busApi = busApi;
        this.busRepository = busRepository;
    }

    @PostConstruct
    public void fetchAndSaveAllRouteIds() {

        busApi.fetchAndSaveRouteId("113");
        busApi.fetchAndSaveRouteId("250");
    }
}