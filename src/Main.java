public class Main {
    public static void main(String[] args) throws InterruptedException {
        TaskManager manager = new TaskManager("res/SaveTest.txt"); //수정 필요


        Menu menu = new Menu(manager);
        menu.run();
        TaskScore taskScore = new TaskScore(40);
        taskScore.treeGrow(40);
    }
}