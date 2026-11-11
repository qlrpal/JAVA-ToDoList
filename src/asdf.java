import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class asdf {
    List<Task> assignment = new LinkedList<>();

    public void searchFile(String userName) {
        File file = new File("res/" + userName + ".txt");
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine())  {
                String str = sc.nextLine();
                String[] temp = str.split("\\$");
                assignment.add(new Task(temp[0], LocalDateTime.parse(temp[1]), temp[2], Integer.parseInt(temp[3]), temp[4]));
            }
            System.out.println(userName + "의 ToDo리스트 조회");
        } catch (FileNotFoundException e) {
            System.out.println("파일이 존재하지 않음");
        }
    }
}
