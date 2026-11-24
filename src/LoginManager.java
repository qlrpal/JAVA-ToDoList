import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginManager {
    private final Set<String> ids = new HashSet<>();
    private final Scanner sc = new Scanner(System.in);

    // res 폴더에 있는 계정 파일에서 아이디를 모음
    public void loadID() {
        ids.clear();
        File folder = new File("res");

        if (!folder.exists() && folder.mkdir()) {
            System.out.println("res 폴더가 존재하지 않아 생성했습니다.");
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String name = file.getName();

            if (file.isFile() && name.endsWith(".txt")) {
                String id = name.substring(0, name.length() - 4);
                ids.add(id);
            }
        }
    }

    // 콘솔에서 회원가입 비밀번호를 입력받음
    private boolean createAccount(String id) {
        if (!id.matches("[A-Za-z0-9]+")) {
            System.out.println("아이디는 영문과 숫자만 사용할 수 있습니다.");
            return false;
        }

        if (ids.contains(id) || accountFile(id).exists()) {
            System.out.println("이미 사용 중인 아이디입니다.");
            return false;
        }

        String password;
        while (true) {
            System.out.print("설정할 비밀번호 >> ");
            password = sc.nextLine();

            if (checkPassword(password)) {
                break;
            }

            System.out.println(
                    "대문자, 소문자, 숫자, 특수문자(!@#$%^&*)를 모두 포함해야 합니다."
            );
        }

        try {
            createAccount(id, password);
            ids.add(id);
            System.out.println("계정이 생성되었습니다.");
            return true;
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // 콘솔과 GUI에서 공통으로 쓰는 계정 생성 메서드
    public static void createAccount(String id, String password) throws IOException {
        File file = accountFile(id);

        if (!checkPassword(password)) {
            throw new IllegalArgumentException(
                    "대문자, 소문자, 숫자, 특수문자(!@#$%^&*)를 모두 포함해야 합니다."
            );
        }

        File folder = file.getParentFile();
        if (!folder.isDirectory() && !folder.mkdir() && !folder.isDirectory()) {
            throw new IOException("res 폴더를 만들지 못했습니다.");
        }

        if (!file.createNewFile()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println(password);
            writer.println(0);

            if (writer.checkError()) {
                throw new IOException("계정 파일을 저장하지 못했습니다.");
            }
        } catch (IOException e) {
            file.delete();
            throw e;
        }
    }

    public static File accountFile(String id) {
        if (!id.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("아이디는 영문과 숫자만 사용할 수 있습니다.");
        }

        return new File("res", id + ".txt");
    }

    // 계정 파일 첫 줄의 비밀번호를 입력값과 비교
    public static boolean login(String id, String password) throws IOException {
        File file = accountFile(id);

        if (!file.exists()) {
            return false;
        }

        try (Scanner reader = new Scanner(file, "UTF-8")) {
            return reader.hasNextLine() && reader.nextLine().equals(password);
        }
    }

    // 로그인 창을 띄우고 성공하면 아이디를 반환
    public String loginWithWindow() {
        JTextField idField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("아이디"));
        form.add(idField);
        form.add(new JLabel("비밀번호"));
        form.add(passwordField);

        String[] options = {"로그인", "회원가입", "취소"};

        while (true) {
            int choice = JOptionPane.showOptionDialog(
                    null, form, "할 일 관리 · 로그인",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );

            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                return null;
            }

            String id = idField.getText().trim();
            char[] characters = passwordField.getPassword();
            String password = new String(characters);
            Arrays.fill(characters, '\0');
            passwordField.setText("");

            try {
                if (choice == 1) {
                    createAccount(id, password);
                    return id;
                }

                if (login(id, password)) {
                    return id;
                }

                JOptionPane.showMessageDialog(
                        null, "아이디 또는 비밀번호를 확인해 주세요.",
                        "로그인 실패", JOptionPane.WARNING_MESSAGE
                );
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                        null, e.getMessage(), "입력 확인",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (IOException e) {
                String message = choice == 1
                        ? "계정 파일을 저장하지 못했습니다."
                        : "계정 파일을 읽지 못했습니다.";

                JOptionPane.showMessageDialog(
                        null, message, "계정 파일 오류",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // 콘솔에서 아이디와 비밀번호를 입력받음
    public String login() {
        loadID();

        while (true) {
            System.out.print("아이디 (Enter만 누르면 종료)>> ");
            String id = sc.nextLine();

            if (id.isEmpty()) {
                return null;
            }

            if (!id.matches("[A-Za-z0-9]+")) {
                System.out.println("아이디는 영문과 숫자만 사용할 수 있습니다.");
                continue;
            }

            if (!ids.contains(id)) {
                System.out.print("등록되지 않은 아이디입니다. 생성하시겠습니까? (Y/N) >> ");
                String answer = sc.nextLine();

                if (answer.equalsIgnoreCase("Y") && createAccount(id)) {
                    return id;
                }

                continue;
            }

            try {
                System.out.print("비밀번호 >> ");
                String password = sc.nextLine();

                if (login(id, password)) {
                    System.out.println("로그인되었습니다.");
                    return id;
                }

                System.out.println("비밀번호가 일치하지 않습니다.");
            } catch (IOException e) {
                System.out.println("계정 파일을 읽지 못했습니다.");
            }
        }
    }

    private static boolean checkPassword(String password) {
        String pattern =
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]+$";

        return password.matches(pattern);
    }
}