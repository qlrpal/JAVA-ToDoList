public class Main {
    public static void main(String[] args) throws InterruptedException {
        TaskManager manager = new TaskManager("res/SaveTest.txt"); //수정 필요

        Ascii ascii = new Ascii();

        for(int i = 0; i < 6; i++)  {
            for (int j = 0; j < 20; j++)    {
                System.out.println("");
            }
            System.out.println(ascii.Loading(i));
            Thread.sleep(100);
        }

        Menu menu = new Menu(manager);
        menu.run();
    }
}