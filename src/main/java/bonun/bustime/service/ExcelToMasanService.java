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
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelToMasanService {

    // 엑셀 파일 구조 상수
    private static final int STOP_HEADER_ROW = 4;
    private static final int DATA_START_ROW = 5;
    private static final int DATA_END_ROW = 87;
    private static final int BUS_NUMBER_COL = 0;
    private static final int START_STOP_COL = 1;
    private static final int START_TIME_COL = 2;
    private static final int STOP_START_COL = 3;
    private static final int STOP_END_COL = 27;
    private static final int END_STOP_COL = 28;

    private final BusRepository busRepository;
    private final StopRepository stopRepository;
    private final RouteMasanRepository routeMasanRepository;
    private final BusTimeToMasanRepository busTimeToMasanRepository;

    /**
     * 엑셀 파일에서 마산 버스 시간표 데이터를 읽고 저장합니다.
     */
    @Transactional
    public void saveExcelData(String filePath) {
        log.info("마산 방향 엑셀 데이터 저장 시작: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 정류장 헤더 읽기
            Map<Integer, StopEntity> locationMap = readStopHeaders(sheet);

            // 버스 시간표 데이터 처리
            processTimeTableData(sheet, locationMap);

            log.info("마산 방향 엑셀 데이터 저장 완료");

        } catch (IOException e) {
            log.error("엑셀 파일 읽기 중 오류 발생", e);
            throw new RuntimeException("엑셀 파일 처리 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 엑셀에서 정류장 헤더 정보를 읽어 맵으로 반환
     */
    private Map<Integer, StopEntity> readStopHeaders(Sheet sheet) {
        Row locationRow = sheet.getRow(STOP_HEADER_ROW);
        if (locationRow == null) {
            log.warn("정류장 헤더 행(Row {})이 비어있습니다.", STOP_HEADER_ROW);
            return Collections.emptyMap();
        }

        Map<Integer, StopEntity> locationMap = new HashMap<>();
        for (int colIndex = STOP_START_COL; colIndex <= STOP_END_COL; colIndex++) {
            String stopName = getStringCell(locationRow.getCell(colIndex));
            if (!stopName.isEmpty()) {
                StopEntity stop = findOrCreateStop(stopName);
                locationMap.put(colIndex, stop);
                log.debug("정류장 매핑: 열={}, 정류장={}", colIndex, stopName);
            }
        }

        log.debug("정류장 헤더 읽기 완료: {}개 정류장 정보 추출", locationMap.size());
        return locationMap;
    }

    /**
     * 시간표 데이터를 처리하고 저장
     */
    private void processTimeTableData(Sheet sheet, Map<Integer, StopEntity> locationMap) {
        for (int rowIndex = DATA_START_ROW; rowIndex <= DATA_END_ROW; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            String busNumber = getStringCell(row.getCell(BUS_NUMBER_COL));
            if (busNumber.isEmpty()) continue;

            log.debug("버스 시간표 처리 중: 버스 번호={}, 행={}", busNumber, rowIndex);

            // 버스 엔티티 조회 또는 생성
            BusEntity bus = findOrCreateBus(busNumber);

            // 출발지와 도착지 정류장 찾기
            StopEntity startLocation = findOrCreateStop(getStringCell(row.getCell(START_STOP_COL)));
            StopEntity endLocation = findOrCreateStop(getStringCell(row.getCell(END_STOP_COL)));

            if (endLocation == null) {
                log.warn("버스 {}의 도착지가 확인되지 않아 건너뜁니다. (행={})", busNumber, rowIndex);
                continue;
            }

            // 출발 시간과 마지막 도착 시간 찾기
            LocalTime startTime = parseLocalTime(row.getCell(START_TIME_COL));
            LocalTime lastArrivalTime = findLastArrivalTime(row);

            // 노선 정보 생성 및 저장
            RouteMasanEntity route = createAndSaveRoute(bus, startLocation, endLocation, startTime, lastArrivalTime);

            // 출발 및 종점 시간 저장
            saveEndpointTimes(bus, startLocation, endLocation, route, startTime, lastArrivalTime);

            // 정류장별 도착 시간 저장
            saveStopTimes(row, bus, route, locationMap);
        }
    }

    /**
     * 마지막 도착 시간 찾기
     */
    private LocalTime findLastArrivalTime(Row row) {
        LocalTime lastArrivalTime = null;
        for (int colIndex = STOP_START_COL; colIndex <= STOP_END_COL; colIndex++) {
            LocalTime arrivalTime = parseLocalTime(row.getCell(colIndex));
            if (arrivalTime != null) {
                lastArrivalTime = arrivalTime;
            }
        }
        return lastArrivalTime;
    }

    /**
     * 노선 정보 생성 및 저장
     */
    private RouteMasanEntity createAndSaveRoute(BusEntity bus, StopEntity startLocation,
                                                StopEntity endLocation, LocalTime startTime,
                                                LocalTime lastArrivalTime) {
        RouteMasanEntity route = new RouteMasanEntity(bus, startLocation, endLocation);
        route.setStartLocationTime(startTime);
        route.setEndLocationTime(lastArrivalTime);
        routeMasanRepository.save(route);

        log.debug("노선 저장: 버스={}, 출발={}, 도착={}, 출발시간={}, 도착시간={}",
                bus.getBusNumber(), startLocation.getStopName(),
                endLocation.getStopName(), startTime, lastArrivalTime);

        return route;
    }

    /**
     * 출발지와 종점의 시간 저장
     */
    private void saveEndpointTimes(BusEntity bus, StopEntity startLocation, StopEntity endLocation,
                                   RouteMasanEntity route, LocalTime startTime, LocalTime lastArrivalTime) {
        // 출발 시간 저장
        if (startTime != null) {
            saveBusTime(bus, startLocation, route, startTime);
        }

        // 종점 시간 저장
        if (lastArrivalTime != null) {
            saveBusTime(bus, endLocation, route, lastArrivalTime);
        }
    }

    /**
     * 정류장별 도착 시간 저장
     */
    private void saveStopTimes(Row row, BusEntity bus, RouteMasanEntity route, Map<Integer, StopEntity> locationMap) {
        for (int colIndex = STOP_START_COL; colIndex <= STOP_END_COL; colIndex++) {
            StopEntity stop = locationMap.get(colIndex);
            if (stop == null) continue;

            LocalTime arrivalTime = parseLocalTime(row.getCell(colIndex));
            if (arrivalTime != null) {
                saveBusTime(bus, stop, route, arrivalTime);
                log.trace("시간 저장: 버스={}, 정류장={}, 시간={}",
                        bus.getBusNumber(), stop.getStopName(), arrivalTime);
            }
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
            log.debug("새 정류장 생성: {}", stopName);
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
            log.debug("새 버스 생성: {}", busNumber);
        }
        return bus;
    }

    /**
     * 버스 도착 시간 저장
     */
    private void saveBusTime(BusEntity bus, StopEntity stop, RouteMasanEntity route, LocalTime arrivalTime) {
        BusTimeToMasanEntity busTime = new BusTimeToMasanEntity(bus, stop, route, arrivalTime);
        busTimeToMasanRepository.save(busTime);
    }

    /**
     * 셀 값을 문자열로 변환
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
     * 셀 값을 LocalTime으로 변환
     */
    private LocalTime parseLocalTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double numericVal = cell.getNumericCellValue();
                int hours = (int) Math.floor(numericVal * 24);
                int minutes = (int) Math.round((numericVal * 24 - hours) * 60);

                // 분이 60인 경우 시간 조정
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

                    // 분이 60인 경우 시간 조정
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
            log.error("시간 변환 오류: {}", e.getMessage());
            return null;
        }
    }
}