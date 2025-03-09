package bonun.bustime;

import bonun.bustime.api.BusApi;
import bonun.bustime.service.ExcelToCilwonService;
import bonun.bustime.service.ExcelToMasanService;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ExcelDataLoader {

    private final ExcelToCilwonService excelToCilwonService;
    private final ExcelToMasanService excelToMasanService;
    private final BusApi busApiService;
    public ExcelDataLoader(ExcelToCilwonService excelToCilwonService, ExcelToMasanService excelToMasanService, BusApi busApi) {
        this.excelToCilwonService = excelToCilwonService;
        this.excelToMasanService = excelToMasanService;
        this.busApiService = busApi;
    }

    @PostConstruct
    public void loadExcelData() {
        String filePath = "src/main/resources/static/data.xlsx"; // 엑셀 파일 경로
        String filePath2 = "src/main/resources/static/data2.xlsx";
        try {
            System.out.println("엑셀 데이터를 저장합니다...");
            excelToCilwonService.saveExcelData(filePath);
            excelToMasanService.saveExcelData(filePath2);
            System.out.println("엑셀 데이터 저장 완료!");
        } catch (Exception e) {
            System.err.println("엑셀 데이터 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}