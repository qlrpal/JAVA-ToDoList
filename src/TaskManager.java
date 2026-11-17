import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskManager {
    private String fileName;
    private List<Task> taskList = new ArrayList<>(); //Task객체를 담을 리스트

    public TaskManager(String fileName) {
        this.fileName = fileName;
    }

    // 파일 불러오기
    public void loadTasks() {
        try (Scanner scanner = new Scanner(new File(fileName), "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\$", -1);

                Task task = new Task(data[0], LocalDateTime.parse(data[1]), data[2], Integer.parseInt(data[3]), data[4]);
                taskList.add(task);
            }
        }
        catch (FileNotFoundException e) {System.out.println("파일을 찾을 수 없습니다.");}
        catch (ArrayIndexOutOfBoundsException e) {System.out.println("누락된 데이터가 있습니다.");}
        catch (DateTimeParseException e) {System.out.println("날짜형식이 잘못되었습니다.");}
        catch (	NumberFormatException e) {System.out.println("데이터의 형식이 올바르지 않습니다.");}
    }

    // 파일 저장하기
    public void saveTasks() {
        try (PrintWriter output = new PrintWriter(fileName)) {
            for (Task task : taskList) {
                output.println(
                        task.getTaskName()+"$"+task.getDeadLine()+"$"+task.getSubName()+"$"+task.getProcess()+"$"+task.getMemo());
            }
        } catch (FileNotFoundException e) {System.out.println("파일을 열 수 없습니다.");}
    }

    // 등록 : 사용자에게 정보를 입력받아 할 일 추가
    public void addTask(Scanner scanner) {
        System.out.print("과제명 : ");
        String taskName = scanner.nextLine();

        LocalDateTime deadLine;
        while (true) {
            try {System.out.print("마감기한 (예: 2026-11-25T23:59) : ");
                deadLine = LocalDateTime.parse(scanner.nextLine());
                break;}
            catch (DateTimeParseException e) {System.out.println("날짜형식이 잘못되었습니다. 다시 입력해주세요.");}
        }

        System.out.print("과목명 : ");
        String subName = scanner.nextLine();

        int process;
        while (true) {
            try {System.out.print("진행도 (0/25/50/75/100, 엔터 입력 시 0) : ");
                String input = scanner.nextLine();
                process = input.isBlank() ? 0 : Integer.parseInt(input);

                if (process < 0 || process > 100 || process % 25 != 0) {
                    System.out.println("0, 25, 50, 75, 100 중 입력해주세요.");
                    continue;}
                break;}
            catch (NumberFormatException e) {System.out.println("진행도는 정수로 입력해주세요.");}
        }

        System.out.print("메모 (엔터 입력 시 메모 없음) : ");
        String memo = scanner.nextLine();

        if (memo.isBlank()) {memo = "메모 없음";}

        Task task = new Task(taskName, deadLine, subName, process, memo);

        taskList.add(task);
        System.out.println("할 일이 등록되었습니다.");
    }

    // 조회 : includeCompleted가 true면 완료 포함, false면 진행중인 것만 조회
    public void showTasks(boolean includeCompleted) {
        int count = 0;

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (!includeCompleted && task.getProcess() == 100) {continue;}

            System.out.println("과제명 : " + task.getTaskName());
            System.out.println("마감기한 : " + task.getDeadLine());
            System.out.println("과목명 : " + task.getSubName());
            System.out.println("진행도 : " + (task.getProcess() == 100 ? "완료" : task.getProcess()+"% 진행 중"));
            System.out.println("메모 : " + task.getMemo());
            System.out.println("--------------------");

            count++;
        }
        if (count == 0) {
            System.out.println("조회할 할 일이 없습니다.");
        }
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
                System.out.println("진행도: " + (task.getProcess() == 100 ? "완료" : task.getProcess() + "% 진행 중"));
                System.out.println("메모: " + task.getMemo());
                System.out.println("--------------------");

                count++;
            }
        } if (count == 0) {System.out.println("검색 결과가 없습니다.");}
    }

    // 수정 : 과제명을 입력받아 내용을 변경
    public void updateTask(Scanner scanner) {
        System.out.print("수정할 과제명 : ");
        String name = scanner.nextLine();
        Task selectedTask = null;

        for (Task task : taskList) { // 입력받은 과제를 검색
            if (task.getTaskName().equals(name)) {
                selectedTask = task;
                break;}}

        if (selectedTask == null) {
            System.out.println("해당 과제명이 없습니다.");
            return;}

        System.out.print("새 과제명 (엔터: 유지) : ");
        String taskName = scanner.nextLine();
        if (!taskName.isBlank()) {selectedTask.setTaskName(taskName);}

        while (true) {
            try {
                System.out.print("새 마감기한 (예: 2026-11-25T23:59, 엔터: 유지) : ");
                String input = scanner.nextLine();

                if (!input.isBlank()) {selectedTask.setDeadLine(LocalDateTime.parse(input));}
                break;
            }
            catch (DateTimeParseException e) {System.out.println("날짜형식이 잘못되었습니다. 다시 입력해주세요.");
            }
        }

        System.out.print("새 과목명 (엔터: 유지) : ");
        String subName = scanner.nextLine();

        if (!subName.isBlank()) {selectedTask.setSubName(subName);}

        while (true) {
            try {
                System.out.print("새 진행도 (0/25/50/75/100, 엔터: 유지) : ");
                String input = scanner.nextLine();

                if (input.isBlank()) {break;}

                int process = Integer.parseInt(input);

                if (process < 0 || process > 100 || process % 25 != 0) {
                    System.out.println("0, 25, 50, 75, 100 중 입력해주세요.");
                    continue;
                }

                selectedTask.setProcess(process);
                break;

            } catch (NumberFormatException e) {System.out.println("진행도는 정수로 입력해주세요.");}
        }

        System.out.print("새 메모 (엔터: 유지) : ");
        String memo = scanner.nextLine();

        if (!memo.isBlank()) {selectedTask.setMemo(memo);}

        System.out.println("할 일이 수정되었습니다.");
    }

    // 선택 삭제 : 과제명을 입력받아 해당 과제를 삭제
    public void deleteTask(Scanner scanner, boolean includeCompleted) {
        if (taskList.isEmpty()) {
            System.out.println("삭제할 할 일이 없습니다.");
            return;
        }

        int count = 0;

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);

            if (!includeCompleted && task.getProcess() == 100) {continue;}

            System.out.println("과제명 : " + task.getTaskName());

            count++;
        }
        if (count == 0) {
            System.out.println("조회할 할 일이 없습니다.");
        }

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
}