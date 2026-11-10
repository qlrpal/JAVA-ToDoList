import java.time.LocalDateTime;

public class Task {
    private LocalDateTime deadLine; //마감기한
    private String subName; //과목명
    private String taskName; //과제명
    private int process; //진행도
    private String memo; //메모

    public Task(LocalDateTime deadLine, String subName, String taskName, int process, String memo) {
        this.deadLine = deadLine;
        this.subName = subName;
        this.taskName = taskName;
        this.process = process;
        this.memo = memo;
    }

    public Task(LocalDateTime deadLine, String taskName, String subName) {
        this(deadLine,taskName,subName,0,"메모 없음");
    }

    public Task(LocalDateTime deadLine, String subName, String taskName, String memo) {
        this(deadLine, subName, taskName, 0, memo);
    }

    public Task(LocalDateTime deadLine, String subName, String taskName, int process) {
        this(deadLine, subName, taskName, process, "메모 없음");
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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
