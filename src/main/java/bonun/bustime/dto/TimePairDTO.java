package bonun.bustime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimePairDTO {
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String busNumber;  // 버스 번호 추가
}