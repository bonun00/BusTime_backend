package bonun.bustime.service;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.entity.ToMasan.BusTimeToMasanEntity;
import bonun.bustime.entity.ToMasan.RouteMasanEntity;
import bonun.bustime.repository.BusRepository;
import bonun.bustime.repository.StopRepository;
import bonun.bustime.repository.ToMasan.BusTimeToMasanRepository;
import bonun.bustime.repository.ToMasan.RouteMasanRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelToMasanService {

    private final BusRepository busRepository;
    private final StopRepository stopRepository;
    private final RouteMasanRepository routeMasanRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;

    @Transactional
    public void saveExcelData(String filePath) {
        System.out.println("[ExcelToMasanService] 엑셀 파일 저장 시작: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // (A) 정류장 헤더 (열 3~27)
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
                    StopEntity stop = findOrCreateStop(stopName);
                    locationMap.put(colIndex, stop);
                }
            }

            // (B) Row 5~87 -> 버스/시간 데이터
            int dataStartRow = 5;
            int dataEndRow = 87;

            for (int rowIndex = dataStartRow; rowIndex <= dataEndRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // 1. 버스 번호 (열 0)
                String busNumber = getStringCell(row.getCell(0));
                if (busNumber.isEmpty()) continue;

                BusEntity bus = findOrCreateBus(busNumber);

                // 2. 출발 정류장, 종점 정류장
                StopEntity startLocation = findOrCreateStop(getStringCell(row.getCell(1)));
                StopEntity endLocation = findOrCreateStop(getStringCell(row.getCell(28)));

                if (endLocation == null) {
                    continue;
                }

                // 3. 출발 시간 저장 (열 2)
                LocalTime startTime = parseLocalTime(row.getCell(2));

                // 4. 가장 마지막에 나타나는 시간 찾기 (열 3~27)
                LocalTime lastArrivalTime = null;
                for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                    LocalTime arrivalTime = parseLocalTime(row.getCell(colIndex));
                    if (arrivalTime != null) {
                        lastArrivalTime = arrivalTime;  // 마지막으로 등장한 시간
                    }
                }

                // 5. RouteMasanEntity 저장
                RouteMasanEntity route = new RouteMasanEntity(bus, startLocation, endLocation);
                route.setStartLocationTime(startTime);
                route.setEndLocationTime(lastArrivalTime);
                routeMasanRepository.save(route);

                // 6. 출발 시간 저장
                if (startTime != null) {
                    saveBusTime(bus, startLocation, route, startTime);
                }

                // 7. 종점 시간 저장
                if (lastArrivalTime != null) {
                    saveBusTime(bus, endLocation, route, lastArrivalTime);
                }

                // 8. 정류장별 도착 시간 저장 (열 3~27)
                for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                    StopEntity stop = locationMap.get(colIndex);
                    if (stop == null) continue;

                    LocalTime arrivalTime = parseLocalTime(row.getCell(colIndex));
                    if (arrivalTime != null) {
                        saveBusTime(bus, stop, route, arrivalTime);
                    }
                }
            }

            System.out.println("[ExcelToMasanService] 엑셀 데이터 저장 완료!");

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 읽기 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 정류장 찾거나 없으면 생성
     */
    private StopEntity findOrCreateStop(String stopName) {
        if (stopName == null || stopName.isEmpty()) return null;

        StopEntity stop = stopRepository.findByStopName(stopName);
        if (stop == null) {
            stop = new StopEntity();
            stop.setStopName(stopName);
            stopRepository.save(stop);
        }
        return stop;
    }

    /**
     * 버스 찾거나 없으면 생성
     */
    private BusEntity findOrCreateBus(String busNumber) {
        BusEntity bus = busRepository.findByBusNumber(busNumber);
        if (bus == null) {
            bus = new BusEntity();
            bus.setBusNumber(busNumber);
            busRepository.save(bus);
        }
        return bus;
    }

    /**
     * 버스 도착 시간 저장 (중복 허용)
     */
    private void saveBusTime(BusEntity bus, StopEntity stop, RouteMasanEntity route, LocalTime arrivalTime) {
        BusTimeToMasanEntity busTime = new BusTimeToMasanEntity(bus, stop, route, arrivalTime);
        busTimeToMasanRepository.save(busTime);
    }

    /**
     * 숫자/문자열 -> String 변환
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
     * "HH:mm" 형태로 변환
     */
    private LocalTime parseLocalTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double numericVal = cell.getNumericCellValue();
                int hours = (int) Math.floor(numericVal * 24);
                int minutes = (int) Math.round((numericVal * 24 - hours) * 60);

                if (minutes == 60) {
                    minutes = 0;
                    hours += 1;
                    if (hours == 24) {
                        hours = 23;
                        minutes = 59;
                    }
                }
                return LocalTime.of(hours, minutes);
            } else if (cell.getCellType() == CellType.STRING) {
                String[] parts = cell.getStringCellValue().trim().split(":");
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
                    return LocalTime.of(hours, minutes);
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("시간 변환 오류: " + e.getMessage());
            return null;
        }
    }
}