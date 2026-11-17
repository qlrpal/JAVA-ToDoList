public class Main {
    public static void main(String[] args) throws InterruptedException {
        LoginManager loginManager = new LoginManager();
        String id = loginManager.login();

        // 아이디 없이 Enter를 누르면 종료
        if (id == null) {return;}

        TaskManager manager = new TaskManager("res/" + id + ".txt");

        Menu menu = new Menu(manager);
        menu.run();
    }
}