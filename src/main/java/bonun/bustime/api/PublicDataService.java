package bonun.bustime.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



@Service
public class PublicDataService {


    private final RestTemplate restTemplate;

    public PublicDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchBusRoute() {
        // 공공데이터 포털 API 엔드포인트
        String url = "https://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteNoList";
//        String url = "http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList";
        String serviceKey = "tNbsVnMlaZ7jFtVUDgBJTlDNg%2FVFa7R7XUYyegbItZY61%2FL%2FSgsl%2BFUP39TKdewZ5gwiPvYv3oaL6Zx8fv5iBg%3D%3D";
        int pageNo = 1;
        int numOfRows = 300;
        String responseType = "xml";
        int cityCode = 38320;
        int routeNo=113;
        // URL에 쿼리 매개변수를 추가
        String uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", responseType)
                .queryParam("cityCode", cityCode)
                .queryParam("routeNo", routeNo)
                .build()
                .toUriString();
        System.out.println("Generated URL: " + uriBuilder);

        try {
            // API 요청 및 응답 처리
            ResponseEntity<String> response = restTemplate.getForEntity(uriBuilder, String.class);
            System.out.println("API Response Status Code: " + response.getStatusCode());
            System.out.println("API Response Body: " + response.getBody());
            return response.getBody(); // JSON/XML 응답 데이터 반환
        } catch (RestClientException e) {
            System.err.println("Error while calling API: " + e.getMessage());
            e.printStackTrace();
            return "Error occurred while fetching data from public API.";
        }
    }
}