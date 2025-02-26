package bonun.bustime.service;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.entity.RouteMasanEntity;
import bonun.bustime.entity.BusTimeToMasanEntity; // 새 엔티티
import bonun.bustime.repository.BusRepository;
import bonun.bustime.repository.StopRepository;
import bonun.bustime.repository.RouteMasanRepository;
import bonun.bustime.repository.BusTimeToMasanRepository;
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
public class ExcelToMasanService {

    private final BusRepository busRepository;
    private final StopRepository stopRepository;
    private final RouteMasanRepository routeMasanRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;

    public ExcelToMasanService(BusRepository busRepository,
                               StopRepository stopRepository,
                               RouteMasanRepository routeMasanRepository,
                               BusTimeToMasanRepository busTimeToMasanRepository) {
        this.busRepository = busRepository;
        this.stopRepository = stopRepository;
        this.routeMasanRepository = routeMasanRepository;
        this.busTimeToMasanRepository = busTimeToMasanRepository;
    }

    @Transactional
    public void saveExcelDataMasan(String filePath) {
        System.out.println("[ExcelToMasanService] 엑셀 파일 저장 시작: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // (A) 예시: Row 4 -> 정류장 헤더 (열 3..27)
            Row locationRow = sheet.getRow(4);
            if (locationRow == null) {
                System.out.println("Row 4가 비어있습니다. 정류장 헤더를 찾을 수 없습니다.");
                return;
            }

            int startCol = 3;
            int endCol = 27;

            // colIndex -> StopEntity 매핑
            Map<Integer, StopEntity> locationMap = new HashMap<>();
            for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                String stopName = getStringCell(locationRow.getCell(colIndex));
                if (!stopName.isEmpty()) {
                    StopEntity stop = stopRepository.findByStopName(stopName);
                    if (stop == null) {
                        stop = new StopEntity();
                        stop.setStopName(stopName);
                        stopRepository.save(stop);
                        System.out.println("[saveExcelDataMasan] 정류장 생성: " + stopName);
                    } else {
                        System.out.println("[saveExcelDataMasan] 정류장 재사용: " + stopName);
                    }
                    locationMap.put(colIndex, stop);
                }
            }

            // (B) Row 5..87 -> 버스/시간 데이터
            int dataStartRow = 5;
            int dataEndRow = 87; // 실제 엑셀 구조에 맞게 조정

            for (int rowIndex = dataStartRow; rowIndex <= dataEndRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // 열 0 -> 버스번호
                String busNumber = getStringCell(row.getCell(0));
                if (busNumber.isEmpty()) continue;

                // BusEntity 조회/생성
                BusEntity bus = busRepository.findByBusNumber(busNumber);
                if (bus == null) {
                    bus = new BusEntity();
                    bus.setBusNumber(busNumber);
                    busRepository.save(bus);
                    System.out.println("[saveExcelDataMasan] 버스 생성: " + busNumber);
                } else {
                    System.out.println("[saveExcelDataMasan] 버스 재사용: " + busNumber);
                }

                // (1) RouteEntity 조회/생성
                // 예: 열 1 -> 출발 정류장 이름, 열 28 -> 종점 이름
                String startStopName = getStringCell(row.getCell(1));
                String endStopName = getStringCell(row.getCell(28));
                StopEntity startLocation = null;
                StopEntity endLocation = null;

                if (!startStopName.isEmpty()) {
                    startLocation = stopRepository.findByStopName(startStopName);
                    if (startLocation == null) {
                        startLocation = new StopEntity();
                        startLocation.setStopName(startStopName);
                        stopRepository.save(startLocation);
                        System.out.println("[saveExcelDataMasan] 출발 정류장 생성: " + startStopName);
                    }
                }
                if (!endStopName.isEmpty()) {
                    endLocation = stopRepository.findByStopName(endStopName);
                    if (endLocation == null) {
                        endLocation = new StopEntity();
                        endLocation.setStopName(endStopName);
                        stopRepository.save(endLocation);
                        System.out.println("[saveExcelDataMasan] 종점 정류장 생성: " + endStopName);
                    }
                }

                RouteMasanEntity route = routeMasanRepository.findByBus(bus);
                if (route == null && startLocation != null && endLocation != null) {
                    route = new RouteMasanEntity(bus, startLocation, endLocation);
                    routeMasanRepository.save(route);
                    System.out.println(String.format(
                            "[saveExcelDataMasan] Route 생성: bus=%s, 출발=%s, 종점=%s",
                            busNumber, startStopName, endStopName
                    ));
                }
                if (route == null) {
                    // route가 null이면 BusTimeToMasanEntity 저장 불가 (nullable=false)
                    System.out.println("[saveExcelDataMasan] route가 null, 시간 저장 스킵: rowIndex=" + rowIndex);
                    continue;
                }

                // (2) 열 3..27 => 정류장별 시간 (LocalTime, 시 올림 보정)
                for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                    StopEntity stop = locationMap.get(colIndex);
                    if (stop == null) continue;

                    // 1) 문자열로 가져옴
                    String timeStr = parseLocalTimeRaisingHour(row.getCell(colIndex));
                    if (timeStr == null || timeStr.isEmpty()) {
                        continue;
                    }
                    // 2) LocalTime.of(...)는 parseLocalTimeRaisingHour가 이미 수행
                    //    timeStr가 "HH:mm"으로 보정된 결과
                    LocalTime arrivalTime = LocalTime.parse(timeStr);

                    // BusTimeToMasanEntity 생성
                    BusTimeToMasanEntity busTime = new BusTimeToMasanEntity(
                            bus,
                            stop,
                            route,
                            arrivalTime
                    );

                    busTimeToMasanRepository.save(busTime);
                    System.out.println(String.format(
                            "[saveExcelDataMasan] 저장 -> bus=%s, stop=%s, routeId=%d, arrivalTime=%s, rowIndex=%d, colIndex=%d",
                            busNumber,
                            stop.getStopName(),
                            route.getId(),
                            arrivalTime.toString(),
                            rowIndex,
                            colIndex
                    ));
                }
            }

            System.out.println("[ExcelToMasanService] 엑셀 파일 저장 완료!");

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 읽기 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 숫자/문자열 -> String (단순 변환)
     */
    private String getStringCell(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    /**
     * "HH:mm" 형태로 시 올림 보정해서 반환.
     * 분=60이면 분=0, 시+1. 시=24면 23:59로 처리.
     *
     * @return "HH:mm" 형식의 문자열 or null
     */
    private String parseLocalTimeRaisingHour(Cell cell) {
        if (cell == null) return null;
        try {
            // 숫자형이면 0.xxxx -> HH:mm
            if (cell.getCellType() == CellType.NUMERIC) {
                double numericVal = cell.getNumericCellValue();
                if (!Double.isNaN(numericVal)) {
                    int hours = (int) Math.floor(numericVal * 24);
                    double fraction = (numericVal * 24) - hours;
                    int minutes = (int) Math.round(fraction * 60);

                    if (minutes == 60) {
                        minutes = 0;
                        hours += 1;
                        if (hours == 24) {
                            hours = 23;
                            minutes = 59;
                        }
                    }
                    return String.format("%02d:%02d", hours, minutes);
                }
                return null;
            }
            // 문자열이면 "HH:mm" 직접 파싱
            else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                // split(":") 후 시올림 보정
                String[] parts = val.split(":");
                if (parts.length == 2) {
                    int hours = Integer.parseInt(parts[0]);
                    int minutes = Integer.parseInt(parts[1]);
                    if (minutes == 60) {
                        minutes = 0;
                        hours += 1;
                        if (hours == 24) {
                            hours = 23;
                            minutes = 59;
                        }
                    }
                    return String.format("%02d:%02d", hours, minutes);
                }
                // 형식이 다르면 null
                return null;
            }
            return null;
        } catch (Exception e) {
            System.out.println("parseLocalTimeRaisingHour 오류: " + e.getMessage());
            return null;
        }
    }
}