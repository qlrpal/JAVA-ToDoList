import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskManager {
    private String fileName;
    private List<Task> taskList = new ArrayList<>(); //Task객체를 담을 리스트
    private String password;
    private TaskScore taskScore = new TaskScore(0);

    public TaskManager(String fileName) {
        this.fileName = fileName;
    }

    public TaskScore getTaskScore() {
        return taskScore;
    }

    // 파일 불러오기
    public boolean loadTasks() {
        try (Scanner reader = new Scanner(new File(fileName), "UTF-8")) {
            if (!reader.hasNextLine()) {throw new IllegalArgumentException("계정 파일에 비밀번호가 없습니다.");}
            String savedPassword = reader.nextLine();

            if (!reader.hasNextLine()) {throw new IllegalArgumentException("계정 파일에 저장된 점수가 없습니다.");}
            int savedScore = Integer.parseInt(reader.nextLine());
            List<Task> loaded = new ArrayList<>();

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.isBlank()) {continue;}

                String[] data = line.split("\\$", -1);
                if (data.length < 5 || data.length > 6) {
                    throw new IllegalArgumentException("과제 데이터 형식이 잘못되었습니다.");
                }

                loaded.add(new Task(
                        data[0],
                        LocalDateTime.parse(data[1]),
                        data[2],
                        Integer.parseInt(data[3]),
                        data[4],
                        data.length == 6 && Boolean.parseBoolean(data[5])
                ));
            }

            if (reader.ioException() != null) {
                throw reader.ioException();
            }

            password = savedPassword;
            taskScore.setScore(savedScore);
            taskList = loaded;
            return true;
        } catch (IOException | IllegalArgumentException | DateTimeParseException e) {
            System.out.println("파일을 불러오지 못했습니다. " + e.getMessage());
            return false;
        }
    }

    // 파일 저장하기
    public boolean saveTasks() {
        if (password == null) {
            System.out.println("계정 파일을 먼저 불러와야 합니다.");
            return false;}

        try (PrintWriter output = new PrintWriter(fileName, "UTF-8")) {
            output.println(password);
            output.println(taskScore.getScore());

            for (Task task : taskList) {
                output.println(task.getTaskName() + "$" + task.getDeadLine()
                        + "$" + task.getSubName() + "$" + task.getProcess()
                        + "$" + task.getMemo() + "$" + task.getPenalty());}

            if (output.checkError()) {
                System.out.println("파일을 저장하지 못했습니다.");
                return false;}

            return true;}
            catch (IOException e) {
            System.out.println("파일을 저장하지 못했습니다.");
            return false;}
    }

    public List<Task> getTasks() {return new ArrayList<>(taskList);}

    public void addTask(Task task) {
        taskList.add(task);
        taskScore.addScore(1);}

    public boolean updateProgress(Task task, int progress) {
        boolean completed = task.getProcess() != 100 && progress == 100;
        task.setProcess(progress);

        if (completed) {taskScore.addScore(3);}
        return completed;
    }

    public void removeTask(Task task) {taskList.remove(task);}

    // 등록 : 사용자에게 정보를 입력받아 할 일 추가
    public void addTask(Scanner scanner) {
        String taskName;
        while (true) {
            System.out.print("과제명 : ");
            taskName = scanner.nextLine();

            if (taskName.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;}
            break;}

        LocalDateTime deadLine;
        while (true) {
            try {System.out.print("마감기한 (예: 2026-11-25T23:59) : ");
                deadLine = LocalDateTime.parse(scanner.nextLine());
                break;} catch (DateTimeParseException e) {System.out.println("날짜형식이 잘못되었습니다. 다시 입력해주세요.");
            }
        }

        String subName;
        while (true) {
            System.out.print("과목명 : ");
            subName = scanner.nextLine();

            if (subName.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;}
            break;
        }

        int process;
        while (true) {
            try {
                System.out.print("진행도 (0/25/50/75/100, 엔터 입력 시 0) : ");
                String input = scanner.nextLine();
                process = input.isBlank() ? 0 : Integer.parseInt(input);

                if (process < 0 || process > 100 || process % 25 != 0) {
                    System.out.println("0, 25, 50, 75, 100 중 입력해주세요.");
                    continue;}
                break;} catch (NumberFormatException e) {System.out.println("진행도는 정수로 입력해주세요.");}
        }

        String memo;
        while (true) {
            System.out.print("메모 (엔터 입력 시 메모 없음) : ");
            memo = scanner.nextLine();

            if (memo.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;
            }
            break;
        }

        if (memo.isBlank()) {memo = "메모 없음";}

        Task task = new Task(taskName, deadLine, subName, process, memo, false);
        addTask(task);
        System.out.println("할 일이 등록되었습니다.");
    }

    // 조회 : includeCompleted가 true면 완료 포함, false면 진행중인 것만 조회
    public void showTasks(boolean includeCompleted) {
        int count = 0;

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (!includeCompleted && task.getProcess() == 100) {
                continue;
            }

            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), task.getDeadLine().toLocalDate());

            System.out.println("과제명 : " + task.getTaskName());
            System.out.println("마감기한 : " + task.getDeadLine());
            System.out.println("과목명 : " + task.getSubName());
            System.out.println("진행도 : "
                    + (task.getProcess() == 100 ? "완료" : task.getProcess() + "% 진행 중"));
            System.out.println("메모 : " + task.getMemo());
            System.out.println("남은 기한 : "
                    + (daysLeft > 0 ? "D-" + daysLeft : daysLeft == 0 ? "D-Day" : "D+" + (-daysLeft)));
            System.out.println("--------------------");

            count++;
        }

        if (count == 0) {System.out.println("조회할 할 일이 없습니다.");}
    }

    // 검색 : 과제명이나 과목명에 검색어가 포함된 할 일을 출력
    public void searchTasks(String keyword) {
        int count = 0; // 검색 조건에 일치한 할 일의 개수

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (task.getSubName().contains(keyword) || task.getTaskName().contains(keyword)) {
                System.out.println("과제명: " + task.getTaskName());
                System.out.println("마감기한: " + task.getDeadLine());
                System.out.println("과목명: " + task.getSubName());
                System.out.println("진행도: "
                        + (task.getProcess() == 100 ? "완료" : task.getProcess() + "% 진행 중"));
                System.out.println("메모: " + task.getMemo());
                System.out.println("--------------------");

                count++;
            }
        }

        if (count == 0) {System.out.println("검색 결과가 없습니다.");}
    }

    // 수정 : 과제명을 입력받아 내용을 변경
    public void updateTask(Scanner scanner) {
        System.out.print("수정할 과제명 : ");
        String name = scanner.nextLine();
        Task selectedTask = null;
        Boolean real = false;

        for (Task task1 : taskList) { // 입력받은 과제를 검색
            if (task1.getTaskName().equals(name)) {
                selectedTask = task1;
                real = true;
                break;}
        }

        if (!real) {
            int count = 0; // 검색 조건에 일치한 할 일의 개수

            for (int i = 0; i < taskList.size(); i++) {
                Task task = taskList.get(i);

                if (task.getSubName().contains(name) || task.getTaskName().contains(name)) {
                    System.out.println("과제 : " + task.getTaskName() + " | "
                            + task.getSubName() + " | " + task.getDeadLine() + " | "
                            + (task.getProcess() == 100 ? "완료" : "(" + task.getProcess() + "/100%)"));
                    count++;
                }
            }

            if (count == 0) {
                System.out.println("검색 결과가 없습니다.");
                System.out.println("--------------------");
                return;}

            System.out.println("--------------------");
            System.out.print("수정할 과제명 : ");
            String realname = scanner.nextLine();

            for (Task task1 : taskList) {
                if (task1.getTaskName().equals(realname)) {
                    selectedTask = task1;
                    break;}
            }
        }

        if (selectedTask == null) {
            System.out.println("해당 과제명이 없습니다.");
            return;}

        String taskName;
        while (true) {
            System.out.print("새 과제명 (엔터: 유지) : ");
            taskName = scanner.nextLine();

            if (taskName.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;}
            if (!taskName.isBlank()) {
                selectedTask.setTaskName(taskName);}
            break;
        }

        while (true) {
            try {
                System.out.print("새 마감기한 (예: 2026-11-25T23:59, 엔터: 유지) : ");
                String input = scanner.nextLine();

                if (!input.isBlank()) {selectedTask.setDeadLine(LocalDateTime.parse(input));}
                break;} catch (DateTimeParseException e) {System.out.println("날짜형식이 잘못되었습니다. 다시 입력해주세요.");}
        }

        String subName;
        while (true) {
            System.out.print("새 과목명 (엔터: 유지) : ");
            subName = scanner.nextLine();

            if (subName.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;}
            if (!subName.isBlank()) {selectedTask.setSubName(subName);}
            break;
        }

        boolean completed = false; // 진행도 100%를 채웠는지 여부(점수 부여용)

        while (true) {
            try {
                System.out.print("새 진행도 (0/25/50/75/100, 엔터: 유지) : ");
                String input = scanner.nextLine();

                if (input.isBlank()) {break;}

                int process = Integer.parseInt(input);
                if (process < 0 || process > 100 || process % 25 != 0) {
                    System.out.println("0, 25, 50, 75, 100 중 입력해주세요.");
                    continue;}

                completed = updateProgress(selectedTask, process);
                break;
            } catch (NumberFormatException e) {System.out.println("진행도는 정수로 입력해주세요.");}
        }

        String memo;
        while (true) {
            System.out.print("새 메모 (엔터: 유지) : ");
            memo = scanner.nextLine();

            if (memo.contains("$")) {
                System.out.println("입력에 $는 작성할 수 없습니다.");
                continue;}
            if (!memo.isBlank()) {selectedTask.setMemo(memo);}
            break;
        }

        System.out.println("할 일이 수정되었습니다.");
        if (completed) {System.out.println("과제 완료! (+3점)");}
    }

    // 선택 삭제 : 과제명을 입력받아 해당 과제를 삭제
    public void deleteTask(Scanner scanner, boolean includeCompleted) {
        if (taskList.isEmpty()) {
            System.out.println("삭제할 할 일이 없습니다.");
            return;}

        int count = 0;

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (!includeCompleted && task.getProcess() == 100) {continue;}

            System.out.println("과제명 : " + task.getTaskName() + " | (" + task.getProcess() + "/100%)");
            count++;
        }

        if (count == 0) {System.out.println("조회할 할 일이 없습니다.");}

        System.out.print("삭제할 과제명 : ");
        String taskName = scanner.nextLine();

        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getTaskName().equals(taskName)) {
                taskList.remove(i);
                System.out.println("할 일이 삭제되었습니다.");
                return;
            }
        }

        System.out.println("해당 과제명이 없습니다.");
    }

    // 완료 삭제 : 진행도가 100%인 모든 할 일을 삭제
    public void deleteCompletedTasks() {
        int count = 0;

        for (int i = taskList.size() - 1; i >= 0; i--) {
            if (taskList.get(i).getProcess() == 100) {
                taskList.remove(i);
                count++;
            }
        }

        if (count == 0) {System.out.println("완료된 할 일이 없습니다.");}
        else {System.out.println("완료된 할 일 " + count + "개를 삭제했습니다.");}
    }

    // 추가 기능 : 마감기한이 지난 과제가 있으면 점수 3점 차감(메뉴 호출 시 자동 실행)
    public boolean applyDeadlinePenalty() {return applyDeadlinePenalty(false);}

    // 과제 등록·수정·삭제 후에는 차감 여부와 관계없이 저장한다.
    public boolean applyDeadlinePenalty(boolean saveChanges) {
        boolean changed = updateDeadlinePenalties();

        if (changed || saveChanges) {return saveTasks();}

        return true;
    }

    private boolean updateDeadlinePenalties() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        boolean changed = false;

        for (Task task : taskList) {
            if (task.getProcess() < 100
                    && now.isAfter(task.getDeadLine())
                    && !task.getPenalty()) {
                taskScore.addScore(-3);
                task.setPenalty(true);
                changed = true;
            }
        }

        return changed;
    }
}