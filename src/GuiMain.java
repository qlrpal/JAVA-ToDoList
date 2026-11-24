import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GuiMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GuiMain::showLogin);
    }

    private static void showLogin() {
        LoginManager loginManager = new LoginManager();

        while (true) {
            String id = loginManager.loginWithWindow();
            if (id == null) {
                return;
            }

            String fileName = LoginManager.accountFile(id).getPath();
            TaskManager manager = new TaskManager(fileName);

            if (!manager.loadTasks()) {
                JOptionPane.showMessageDialog(
                        null, "과제 파일을 불러오지 못했습니다."
                );
                continue;
            }

            if (!manager.applyDeadlinePenalty()) {
                JOptionPane.showMessageDialog(
                        null, "마감 차감 결과를 저장하지 못했습니다."
                );
                continue;
            }

            TodoFrame frame = new TodoFrame(id, manager, GuiMain::showLogin);
            frame.setVisible(true);
            return;
        }
    }
}