import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class LoginManager {
    private Set<String> ids = new HashSet<>();
    private Scanner sc = new Scanner(System.in);

    //모든 아이디를 ids에 저장함
    public void loadID(){
        ids.clear();
        File folder = new File("res");
        File[] files = folder.listFiles();

        if (files == null) {return;}

        for (File file : files) {
            String name = file.getName();

            if (file.isFile() && name.endsWith(".txt")) {
                String id = name.substring(0, name.length() - 4);
                ids.add(id);}
        }
    }


    private boolean createAccount(String id){
        //아이디 형식이 올바르지 않은 경우
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("아이디는 영문과 숫자만 사용할 수 있습니다.");
            return false;}

        File folder = new File("res");
        File file = new File(folder, id + ".txt");

        //아이디가 중복인 경우
        if (ids.contains(id) || file.exists()) {
            System.out.println("이미 사용 중인 아이디입니다.");
            return false;
        }


        String password;
        while (true) {
            System.out.print("설정할 비밀번호 >> ");
            password = sc.nextLine();

            if (checkPassword(password)){break;}

            System.out.println("대문자, 소문자, 숫자, 특수문자(!@#$%^&*)를 모두 포함해야 합니다.");
        }

        boolean created = false;

        try {
            if (!file.createNewFile()) {
                System.out.println("이미 사용 중인 아이디입니다.");
                return false;}

            created = true;

            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println(password);
            writer.close();

            ids.add(id);
            System.out.println("계정이 생성되었습니다.");
            return true;}
        catch (IOException e) {
            if (created) {file.delete();}

            System.out.println("계정 파일을 저장하지 못했습니다.");
            return false;
        }
    }

    public String login(){
        loadID();

        while (true) {
            System.out.print("아이디 (Enter만 누르면 종료)>> ");
            String id = sc.nextLine();
            if (id.isEmpty()) {return null;}

            //아이디 제약조건 확인(영문과 숫자만)
            if (!id.matches("[A-Za-z0-9]+")) {
                System.out.println("아이디는 영문과 숫자만 사용할 수 있습니다.");
                continue;}

            //등록되지 않은 아이디의 파일 생성
            if (!ids.contains(id)) {
                System.out.print("등록되지 않은 아이디입니다. 생성하시겠습니까? (Y/N) >> ");
                String answer = sc.nextLine();

                if (answer.equalsIgnoreCase("Y")) {
                    if (createAccount(id)) {return id;}
                }
                continue;
            }
            //아이디를 바탕으로 파일 지정
            File file = new File("res", id + ".txt");

            try (Scanner reader = new Scanner(file, "UTF-8")) {
                String savedPassword = reader.nextLine();

                System.out.print("비밀번호 >> ");
                String password = sc.nextLine();

                if (savedPassword.equals(password)) {
                    System.out.println("로그인되었습니다.");
                    return id;}

                System.out.println("비밀번호가 일치하지 않습니다.");
            }
            catch (IOException e) {System.out.println("계정 파일을 읽지 못했습니다.");}
        }
    }

    //비밀번호 제약 확인
    private boolean checkPassword(String password){
        String pw_limit =
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]+$";
        return password.matches(pw_limit);
    }
}