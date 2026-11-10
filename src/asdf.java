import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class asdf {

    static void main() throws FileNotFoundException {
        File file = new File("SaveTest");
        Scanner sc = new Scanner(file);
        List<Task> assignment = new LinkedList<>();
        while (sc.hasNextLine())  {
            String str = sc.nextLine();
            String[] temp = str.split("\t");
            assignment.add(new Task());

        }
    }
}
