package bonun.bustime.config;

import bonun.bustime.excel.ExcelToChilwonService;
import bonun.bustime.excel.ExcelToMasanService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExcelDataLoader {

    private final ExcelToChilwonService excelToChilwonService;
    private final ExcelToMasanService excelToMasanService;

    @Value("${bus.excel.chilwon-path:src/main/resources/static/data.xlsx}")
    private String chilwonExcelPath;

    @Value("${bus.excel.masan-path:src/main/resources/static/data2.xlsx}")
    private String masanExcelPath;

    @PostConstruct
    public void loadExcelData() {

        log.info("엑셀 데이터 로드 시작");

        try {
            log.info("칠원 방향 엑셀 데이터 로드 중... (파일: {})", chilwonExcelPath);
            excelToChilwonService.saveExcelData(chilwonExcelPath);
            log.info("칠원 방향 엑셀 데이터 로드 완료");

            log.info("마산 방향 엑셀 데이터 로드 중... (파일: {})", masanExcelPath);
            excelToMasanService.saveExcelData(masanExcelPath);
            log.info("마산 방향 엑셀 데이터 로드 완료");

            log.info("모든 엑셀 데이터 로드 완료");
        } catch (Exception e) {
            log.error("엑셀 데이터 로드 중 오류 발생", e);
            // 오류 발생 시 추가 처리가 필요하다면 여기에 구현
        }
    }
}