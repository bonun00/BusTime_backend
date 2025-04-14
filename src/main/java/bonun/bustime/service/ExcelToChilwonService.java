package bonun.bustime.service;

import bonun.bustime.entity.BusEntity;
import bonun.bustime.entity.StopEntity;
import bonun.bustime.entity.tochilwon.BusTimeToChilwonEntity;
import bonun.bustime.entity.tochilwon.RouteChilwonEntity;
import bonun.bustime.repository.BusRepository;
import bonun.bustime.repository.StopRepository;
import bonun.bustime.repository.tochilwon.BusTimeToChilwonRepository;
import bonun.bustime.repository.tochilwon.RouteChilwonRepository;
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
public class ExcelToChilwonService {

    private static final int STOP_HEADER_ROW = 5;
    private static final int DATA_START_ROW = 7;
    private static final int BUS_NUMBER_COL = 0;
    private static final int END_STOP_COL = 23;
    private static final int FIRST_STOP_COL = 1;
    private static final int LAST_STOP_COL = 18;
    private static final int ADDITIONAL_STOPS_START_COL = 19;

    private final StopRepository stopRepository;
    private final BusRepository busRepository;
    private final BusTimeToChilwonRepository busTimeToChilwonRepository;
    private final RouteChilwonRepository routeChilwonRepository;

    /**
     * 엑셀 파일에서 칠원 버스 시간표 데이터를 읽고 저장합니다.
     */
    @Transactional
    public void saveExcelData(String filePath) {
        log.info("칠원 방향 엑셀 데이터 저장 시작: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 정류장 헤더 읽기
            Map<Integer, StopEntity> stopMap = readStopHeaders(sheet);

            // 버스 시간표 데이터 처리
            processTimeTableData(sheet, stopMap);

            log.info("칠원 방향 엑셀 데이터 저장 완료");

        } catch (IOException e) {
            log.error("엑셀 파일 읽기 중 오류 발생", e);
            throw new RuntimeException("엑셀 파일 처리 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 엑셀 시트에서 정류장 헤더 정보를 읽고 맵으로 반환
     */
    private Map<Integer, StopEntity> readStopHeaders(Sheet sheet) {
        Row stopRow = sheet.getRow(STOP_HEADER_ROW);
        if (stopRow == null) {
            log.warn("정류장 헤더 행(Row {})이 비어있습니다.", STOP_HEADER_ROW);
            return Collections.emptyMap();
        }

        Map<Integer, StopEntity> stopMap = new HashMap<>();
        for (int colIndex = FIRST_STOP_COL; colIndex <= LAST_STOP_COL; colIndex++) {
            String stopName = extractCellValue(stopRow.getCell(colIndex));
            if (!stopName.isEmpty()) {
                StopEntity stop = findOrCreateStop(stopName);
                stopMap.put(colIndex, stop);
            }
        }

        log.debug("정류장 헤더 읽기 완료: {}개 정류장 정보 추출", stopMap.size());
        return stopMap;
    }

    /**
     * 버스 시간표 데이터를 처리하고 DB에 저장
     */
    private void processTimeTableData(Sheet sheet, Map<Integer, StopEntity> stopMap) {
        for (int rowIndex = DATA_START_ROW; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            // 버스 번호 읽기
            String busNumber = extractCellValue(row.getCell(BUS_NUMBER_COL));
            if (busNumber.isEmpty()) {
                continue;
            }

            log.debug("버스 시간표 처리 중: 버스 번호={}, 행={}", busNumber, rowIndex);

            // 버스 엔티티 조회 또는 생성
            BusEntity bus = findOrCreateBus(busNumber);

            // 출발지와 도착지 정류장 찾기
            StopEntity startLocation = findStartLocationByFirstTime(row, stopMap);
            StopEntity endLocation = findStopInCell(row, END_STOP_COL);

            if (startLocation == null || endLocation == null) {
                log.warn("버스 {}의 출발지 또는 도착지가 확인되지 않아 건너뜁니다. (행={})", busNumber, rowIndex);
                continue;
            }

            // 노선 정보 생성 및 저장
            RouteChilwonEntity route = new RouteChilwonEntity(bus, startLocation, endLocation);

            routeChilwonRepository.save(route);

            // 시간표 처리
            List<LocalTime> allTimes = new ArrayList<>();
            allTimes.addAll(processStopTimes(row, bus, route, stopMap));
            allTimes.addAll(processAdditionalStopTimes(row, bus, route));

            // 출발 및 종점 시각 설정
            updateRouteStartEndTimes(route, allTimes);
        }
    }

    /**
     * 정류장 이름으로 StopEntity를 조회하거나 생성
     */
    private StopEntity findOrCreateStop(String stopName) {
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
     * 버스 번호로 BusEntity를 조회하거나 생성
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
     * 처음 발견한 시간이 있는 정류장을 출발점으로 사용
     */
    private StopEntity findStartLocationByFirstTime(Row row, Map<Integer, StopEntity> stopMap) {
        for (int colIndex = FIRST_STOP_COL; colIndex <= LAST_STOP_COL; colIndex++) {
            String value = extractCellValue(row.getCell(colIndex));
            if (value.matches("\\d{1,2}:\\d{2}")) {
                return stopMap.get(colIndex);
            }
        }
        return null;
    }

    /**
     * 특정 셀에서 정류장 이름을 읽어 StopEntity 반환
     */
    private StopEntity findStopInCell(Row row, int colIndex) {
        String val = extractCellValue(row.getCell(colIndex));
        if (val.isEmpty() || val.matches("\\d{1,2}:\\d{2}")) {
            return null; // 비어있거나 시간이면 종점으로 사용 불가
        }
        return findOrCreateStop(val);
    }

    /**
     * 기본 정류장별 시간 처리 (col 1-18)
     */
    private List<LocalTime> processStopTimes(Row row, BusEntity bus, RouteChilwonEntity route, Map<Integer, StopEntity> stopMap) {
        List<LocalTime> times = new ArrayList<>();

        for (int colIndex = FIRST_STOP_COL; colIndex <= LAST_STOP_COL; colIndex++) {
            StopEntity stop = stopMap.get(colIndex);
            if (stop == null) continue;

            String timeValue = extractCellValue(row.getCell(colIndex));
            if (timeValue.matches("\\d{1,2}:\\d{2}")) {
                LocalTime arrivalTime = LocalTime.parse(timeValue);
                times.add(arrivalTime);

                saveTimeIfNotExists(bus, stop, route, arrivalTime);
            }
        }
        return times;
    }

    /**
     * 추가 정류장 시간 처리 (col 19+)
     */
    private List<LocalTime> processAdditionalStopTimes(Row row, BusEntity bus, RouteChilwonEntity route) {
        List<LocalTime> times = new ArrayList<>();
        int lastCol = row.getLastCellNum() - 1;
        StopEntity currentStop = null;

        for (int colIndex = ADDITIONAL_STOPS_START_COL; colIndex <= lastCol; colIndex++) {
            String val = extractCellValue(row.getCell(colIndex));
            if (val.isEmpty()) continue;

            if (val.matches("\\d{1,2}:\\d{2}")) {
                // 시간인 경우
                if (currentStop != null) {
                    LocalTime arrivalTime = LocalTime.parse(val);
                    times.add(arrivalTime);
                    saveTimeIfNotExists(bus, currentStop, route, arrivalTime);
                }
            } else {
                // 정류장 이름인 경우
                currentStop = findOrCreateStop(val);
            }
        }
        return times;
    }

    /**
     * 중복 시간표 데이터 체크 후 저장
     */
    private void saveTimeIfNotExists(BusEntity bus, StopEntity stop, RouteChilwonEntity route, LocalTime arrivalTime) {
        BusTimeToChilwonEntity existing = busTimeToChilwonRepository.findByBusAndStopAndRouteAndArrivalTime(
                bus, stop, route, arrivalTime);

        if (existing == null) {
            BusTimeToChilwonEntity busTime = new BusTimeToChilwonEntity(bus, stop, route, arrivalTime);
            busTimeToChilwonRepository.save(busTime);
            log.trace("버스 시간 저장: 버스={}, 정류장={}, 시간={}", bus.getBusNumber(), stop.getStopName(), arrivalTime);
        }
    }

    /**
     * 노선의 출발/종점 시간 업데이트
     */
    private void updateRouteStartEndTimes(RouteChilwonEntity route, List<LocalTime> allTimes) {
        LocalTime startTime = findMinTime(allTimes);
        LocalTime endTime = findMaxTime(allTimes);

        route.setStartLocationTime(startTime);
        route.setEndLocationTime(endTime);

        log.debug("노선 시간 설정: 버스={}, 출발={}, 종점={}",
                route.getBus().getBusNumber(),
                startTime,
                endTime);
    }

    /**
     * 리스트에서 최소 시간 찾기
     */
    private LocalTime findMinTime(List<LocalTime> times) {
        if (times == null || times.isEmpty()) return null;
        return Collections.min(times);
    }

    /**
     * 리스트에서 최대 시간 찾기
     */
    private LocalTime findMaxTime(List<LocalTime> times) {
        if (times == null || times.isEmpty()) return null;
        return Collections.max(times);
    }

    /**
     * 셀 값 추출 (다양한 형식 처리)
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