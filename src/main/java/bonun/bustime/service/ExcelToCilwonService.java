package bonun.bustime.service;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.BusTimeToChilwonEntity;
import bonun.bustime.entity.RouteChilwonEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.repository.BusRepository;
import bonun.bustime.repository.BusTimeToChilwonRepository;
import bonun.bustime.repository.RouteChilwonRepository;
import bonun.bustime.repository.StopRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelToCilwonService {

    private final StopRepository stopRepository;
    private final BusRepository busRepository;
    private final BusTimeToChilwonRepository busTimeToChilwonRepository;
    private final RouteChilwonRepository routeChilwonRepository;

    public ExcelToCilwonService(StopRepository stopRepository,
                                BusRepository busRepository,
                                BusTimeToChilwonRepository busTimeToChilwonRepository,
                                RouteChilwonRepository routeChilwonRepository) {
        this.stopRepository = stopRepository;
        this.busRepository = busRepository;
        this.busTimeToChilwonRepository = busTimeToChilwonRepository;
        this.routeChilwonRepository = routeChilwonRepository;
    }

    @Transactional
    public void saveExcelData(String filePath) {
        System.out.println("엑셀 데이터를 저장합니다...");

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // (A) Row 5: 정류장 헤더 (열 1..18) 읽기
            Row stopRow = sheet.getRow(5);
            if (stopRow == null) {
                System.out.println("Row 5가 비어있어 정류장 헤더를 읽을 수 없습니다.");
                return;
            }

            // stopMap: colIndex(1..18) -> StopEntity
            Map<Integer, StopEntity> stopMap = new HashMap<>();
            for (int colIndex = 1; colIndex <= 18; colIndex++) {
                String stopName = extractCellValue(stopRow.getCell(colIndex));
                if (!stopName.isEmpty()) {
                    StopEntity stop = stopRepository.findByStopName(stopName);
                    if (stop == null) {
                        stop = new StopEntity();
                        stop.setStopName(stopName);
                        stopRepository.save(stop);
                    }
                    stopMap.put(colIndex, stop);
                }
            }

            // (B) Row 7+ : 버스별 행 처리
            for (int rowIndex = 7; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // 1) 버스 번호 (열 0)
                String busNumber = extractCellValue(row.getCell(0));
                if (busNumber.isEmpty()) {
                    continue;
                }

                // BusEntity 조회/생성
                BusEntity bus = busRepository.findByBusNumber(busNumber);
                if (bus == null) {
                    bus = new BusEntity();
                    bus.setBusNumber(busNumber);
                    busRepository.save(bus);
                }

                // 2) 출발점: "처음 발견한 시간" (열 1..18) => Row 5 헤더
                StopEntity startLocation = findStartLocationByFirstTime(row, stopMap);

                // 3) 종점: 열 23의 정류장
                StopEntity endLocation = findStopInCell(row, 23);

                // 4) RouteEntity 조회/생성 (버스 1:1)
                RouteChilwonEntity route = routeChilwonRepository.findByBus(bus);
                if (route == null && startLocation != null && endLocation != null) {
                    route = new RouteChilwonEntity(bus, startLocation, endLocation);
                    routeChilwonRepository.save(route);
                    System.out.println("Route 생성: bus=" + busNumber
                            + ", 출발=" + startLocation.getStopName()
                            + ", 종점=" + endLocation.getStopName());
                }

                // route가 null이면 시간 저장 불가 -> 스킵
                if (route == null) {
                    System.out.println("Route가 null → 시간 저장 스킵 (rowIndex=" + rowIndex + ")");
                    continue;
                }

                // 5) 시간 파싱 -> BusTimeEntity 저장
                parseColumns1To18(row, bus, route, stopMap);
                parseColumns19Plus(row, bus, route);
            }

            System.out.println("엑셀 데이터 저장 완료!");

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 읽기 중 오류 발생", e);
        }
    }

    /**
     * "처음 발견한 시간" (열 1..18) -> Row 5 헤더(stopMap)의 정류장을 출발점으로 사용
     */
    private StopEntity findStartLocationByFirstTime(Row row, Map<Integer, StopEntity> stopMap) {
        // 열 1..18 사이에서 첫 "HH:mm" 발견
        for (int colIndex = 1; colIndex <= 18; colIndex++) {
            String value = extractCellValue(row.getCell(colIndex));
            if (value.matches("\\d{1,2}:\\d{2}")) {
                // 이 열이 첫 시간 -> Row 5 헤더의 정류장이 출발점
                return stopMap.get(colIndex);
            }
        }
        return null;
    }

    /**
     * 열 23에서 정류장 이름을 읽어 종점으로 사용
     */
    private StopEntity findStopInCell(Row row, int colIndex) {
        String val = extractCellValue(row.getCell(colIndex));
        if (val.isEmpty() || val.matches("\\d{1,2}:\\d{2}")) {
            return null; // 비어있거나 시간이면 종점 불가
        }
        StopEntity stop = stopRepository.findByStopName(val);
        if (stop == null) {
            stop = new StopEntity();
            stop.setStopName(val);
            stopRepository.save(stop);
        }
        return stop;
    }

    /**
     * 열 1..18: Row 5 헤더로 정류장 매핑 -> 시간 저장
     */
    private void parseColumns1To18(Row row, BusEntity bus, RouteChilwonEntity route, Map<Integer, StopEntity> stopMap) {
        for (int colIndex = 1; colIndex <= 18; colIndex++) {
            StopEntity stop = stopMap.get(colIndex);
            if (stop == null) continue;

            String timeValue = extractCellValue(row.getCell(colIndex));
            if (timeValue.matches("\\d{1,2}:\\d{2}")) {
                LocalTime arrivalTime = LocalTime.parse(timeValue);

                // 중복 체크
                BusTimeToChilwonEntity existing =
                        busTimeToChilwonRepository.findByBusAndStopAndRouteAndArrivalTime(bus, stop, route, arrivalTime);
                if (existing == null) {
                    BusTimeToChilwonEntity busTime = new BusTimeToChilwonEntity(bus, stop, route, arrivalTime);
                    busTimeToChilwonRepository.save(busTime);
                }
            }
        }
    }

    /**
     * 열 19+ : "정류장 -> 시간" 형태로 매핑
     */
    private void parseColumns19Plus(Row row, BusEntity bus, RouteChilwonEntity route) {
        int lastCol = row.getLastCellNum() - 1;
        StopEntity currentStop = null;
        for (int colIndex = 19; colIndex <= lastCol; colIndex++) {
            String val = extractCellValue(row.getCell(colIndex));
            if (val.isEmpty()) continue;

            // 시간이면 -> currentStop에 BusTimeEntity 저장
            if (val.matches("\\d{1,2}:\\d{2}")) {
                if (currentStop != null) {
                    LocalTime arrivalTime = LocalTime.parse(val);
                    BusTimeToChilwonEntity existing = busTimeToChilwonRepository.findByBusAndStopAndRouteAndArrivalTime(
                            bus, currentStop, route, arrivalTime
                    );
                    if (existing == null) {
                        BusTimeToChilwonEntity busTime = new BusTimeToChilwonEntity(bus, currentStop, route, arrivalTime);
                        busTimeToChilwonRepository.save(busTime);
                    }
                }
            } else {
                // 정류장
                StopEntity foundStop = stopRepository.findByStopName(val);
                if (foundStop == null) {
                    foundStop = new StopEntity();
                    foundStop.setStopName(val);
                    stopRepository.save(foundStop);
                }
                currentStop = foundStop;
            }
        }
    }

    /**
     * 셀 값 추출 (문자열/숫자 -> HH:mm 등)
     */
    private String extractCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue()
                        .replace("\n", "")
                        .trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalTime time = cell.getLocalDateTimeCellValue().toLocalTime();
                    return time.toString(); // "HH:mm:ss"
                } else {
                    double numericTime = cell.getNumericCellValue();
                    int hours = (int) (numericTime * 24);
                    int minutes = (int) ((numericTime * 24 - hours) * 60);
                    return String.format("%02d:%02d", hours, minutes);
                }
            default:
                return "";
        }
    }
}