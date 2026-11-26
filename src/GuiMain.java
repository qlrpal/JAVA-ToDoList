import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GuiMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GuiMain::showLogin);
    }

    static void showLogin() {
        LoginManager loginManager = new LoginManager();

        while (true) {
            String id = loginManager.loginWithWindow(); //로그인 창으로 아이디 받기
            if (id == null) {return;} // 창 취소시 반환(종료)

            // 파일 불러오기
            String fileName = LoginManager.accountFile(id).getPath();
            TaskManager manager = new TaskManager(fileName);
            if (!manager.loadTasks()) {
                JOptionPane.showMessageDialog(null, "과제 파일을 불러오지 못했습니다.");
                continue;
            }

            if (!manager.applyDeadlinePenalty()) {
                JOptionPane.showMessageDialog(null, "마감 차감 결과를 저장하지 못했습니다.");
                continue;
            }

            TodoFrame frame = new TodoFrame(id, manager);
            frame.setVisible(true);
            return;
        }
    }
}