package bonun.bustime;

import org.springframework.stereotype.Component;

@Component
public class ExecutionStatus {
    private boolean hasExecuted = false;

    // Getter 메서드 (public 접근 제한자)
    public boolean hasExecuted() {
        return hasExecuted;
    }

    // Setter 메서드
    public void setExecuted(boolean executed) {
        this.hasExecuted = executed;
    }
}