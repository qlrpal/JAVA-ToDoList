import java.util.Scanner;

public class Menu {
    private TaskManager manager;
    private Scanner scanner = new Scanner(System.in);

    public Menu(TaskManager manager) {this.manager = manager;}

    public void run() {

        manager.loadTasks();
        while (true) {
            manager.applyDeadlinePenalty();
            System.out.println("\n===== 할 일 관리 =====");
            System.out.println("1. 등록\t2. 조회\t3. 검색\t4. 수정\t5. 삭제\t6. 나무\t0. 저장 및 종료");
            System.out.print("선택 : ");

            int choice;

            try {choice = Integer.parseInt(scanner.nextLine());}
            catch (NumberFormatException e) {
                System.out.println("번호는 정수로 입력해주세요.");
                continue;}

            switch (choice) {
                case 1:
                    manager.addTask(scanner);
                    break;
                case 2:
                    while (true) {
                        System.out.println("\n1. 진행 중인 할 일 조회");
                        System.out.println("2. 전체 조회");
                        System.out.println("0. 이전 메뉴");
                        System.out.print("선택 : ");

                        try {
                            int showChoice = Integer.parseInt(scanner.nextLine());

                            if (showChoice == 1) {
                                manager.showTasks(false);
                                break;}
                            else if (showChoice == 2) {
                                manager.showTasks(true);
                                break;}
                            else if (showChoice == 0) {break;}
                            else {System.out.println("메뉴에 있는 번호를 입력해주세요.");}
                        } catch (NumberFormatException e) {System.out.println("번호는 정수로 입력해주세요.");}
                    }
                    break;
                case 3:
                    System.out.print("검색어 : ");
                    manager.searchTasks(scanner.nextLine());
                    break;
                case 4:
                    manager.updateTask(scanner);
                    break;
                case 5:
                    while (true) {
                        System.out.println("\n1. 선택 삭제");
                        System.out.println("2. 완료된 할 일 전체 삭제");
                        System.out.println("0. 이전 메뉴");
                        System.out.print("선택 : ");

                        try {
                            int deleteChoice = Integer.parseInt(scanner.nextLine());

                            if (deleteChoice == 1) {
                                manager.deleteTask(scanner, false);
                                break;}

                            else if (deleteChoice == 2) {
                                manager.deleteCompletedTasks();
                                break;}

                            else if (deleteChoice == 0) {break;}

                            else {System.out.println("메뉴에 있는 번호를 입력해주세요.");}

                        } catch (NumberFormatException e) {System.out.println("번호는 정수로 입력해주세요.");}
                    }
                    break;
                case 6:
                    TaskScore taskScore = manager.getTaskScore();
                    taskScore.treeGrow(taskScore.getScore());
                    break;

                case 0:
                    manager.saveTasks();
                    System.out.println("프로그램을 저장 및 종료합니다.");
                    return;
                default:
                    System.out.println("메뉴에 있는 번호를 입력해주세요.");
            }
        }
    }
}