import java.time.LocalDateTime;

public class Task {
    private String taskName; //과제명
    private LocalDateTime deadLine; //마감기한
    private String subName; //과목명
    private int process; //진행도
    private String memo; //메모

    public Task(String taskName, LocalDateTime deadLine, String subName, int process, String memo) {
        this.taskName = taskName;
        this.deadLine = deadLine;
        this.subName = subName;
        this.process = process;
        this.memo = memo;
    }

    public Task(String taskName, LocalDateTime deadLine, String subName) {
        this(taskName, deadLine, subName, 0, "메모 없음");
    }

    public Task(String taskName, LocalDateTime deadLine, String subName, String memo) {
        this(taskName, deadLine, subName, 0, memo);
    }

    public Task(String taskName, LocalDateTime deadLine, String subName, int process) {
        this(taskName, deadLine, subName, process, "메모 없음");
    }



    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public LocalDateTime getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(LocalDateTime deadLine) {
        this.deadLine = deadLine;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}