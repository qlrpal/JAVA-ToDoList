import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Locale;

public class TodoFrame extends JFrame {
    // 화면에서 공통으로 사용하는 색상과 날짜 표시 형식, 시간대
    private static final Color BACKGROUND = new Color(242, 246, 249);
    private static final Color NAVY = new Color(24, 47, 67);
    private static final Color TEAL = new Color(0, 115, 105);
    private static final Color MUTED = new Color(82, 101, 116);
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 과제 데이터는 TaskManager가 관리하고, logout은 로그인 화면으로 돌아가는 동작을 담는다.
    private final TaskManager manager;
    private final Runnable logout;
    private final JLabel score = new JLabel();
    private final JTextField search = new JTextField();
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"전체", "진행 중"});
    private final JPanel cards = new JPanel(new GridLayout(0, 1, 0, 10));

    public TodoFrame(String id, TaskManager manager, Runnable logout) {
        super("할 일 관리");
        this.manager = manager;
        this.logout = logout;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 440));
        setSize(940, 620);
        setLocationRelativeTo(null);


        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(id), BorderLayout.WEST);
        root.add(createMainPanel(), BorderLayout.CENTER);
        setContentPane(root);
        refreshCards();

        // 30초마다 과제 마감 여부 갱신
        Timer timer = new Timer(30_000, event -> {
            if (!isActive()) {return;}
            refreshAfterSave(manager.applyDeadlinePenalty());
        });
        // 타이머 열기 닫기
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent event) {timer.start();}
            @Override public void windowClosed(java.awt.event.WindowEvent event) {timer.stop();}
        });
    }

    // 좌측 패널 : 사용자 정보 표시(상단), 종료 및 로그아웃 버튼(하단)
    private JPanel createSidebar(String id) {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(175, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));

        //좌측 패널 상단 : 사용자 정보 표시
        JPanel account = new JPanel(new GridLayout(0, 1, 0, 6));
        account.setOpaque(false);
        JLabel accountLabel = new JLabel("사용자");
        accountLabel.setForeground(new Color(189, 208, 220));
        JLabel name = new JLabel(id);
        name.setToolTipText(id);
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 19f));
        account.add(accountLabel);
        account.add(name);
        sidebar.add(account, BorderLayout.NORTH);

        //좌측 패널 하단 : 현재 점수, 로그아웃/종료 버튼
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 8));
        actions.setOpaque(false);
        score.setForeground(Color.WHITE);
        score.setFont(score.getFont().deriveFont(Font.BOLD, 16f));
        actions.add(score);
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.addActionListener(event -> {
            dispose();
            logout.run();
        });
        actions.add(logoutButton);
        JButton exitButton = new JButton("종료");
        exitButton.addActionListener(event -> dispose());
        actions.add(exitButton);
        sidebar.add(actions, BorderLayout.SOUTH);
        return sidebar;
    }

    // 메인 패널 : 검색 영역(상단), 스크롤 가능한 과제 목록(중단), 등록 버튼(하단)
    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));
        main.setBackground(BACKGROUND);

        // 메인 패널 상단 — 제목, 검색창, 필터
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);
        JLabel title = new JLabel("과제 목록");
        title.setForeground(NAVY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        header.add(title, BorderLayout.NORTH); //제목 쓰기

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        // 검색창
        JLabel searchLabel = new JLabel("검색");
        searchLabel.setLabelFor(search);
        search.setToolTipText("과제명 또는 과목명으로 검색"); //툴 팁 표시(커서 가져다 대면 나옴)
        search.setPreferredSize(new Dimension(0, 34));
        // 검색창 내용 수정시 즉각 반응
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { refreshCards(); }
            @Override public void removeUpdate(DocumentEvent event) { refreshCards(); }
            @Override public void changedUpdate(DocumentEvent event) { refreshCards(); }});
        filter.addActionListener(event -> refreshCards()); // 필터 변경 표시

        toolbar.add(searchLabel, BorderLayout.WEST);
        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(filter, BorderLayout.EAST);
        header.add(toolbar, BorderLayout.CENTER);
        main.add(header, BorderLayout.NORTH);

        // 중단부 — 스크롤 가능한 과제 카드 목록
        cards.setOpaque(false);

        JPanel list = new JPanel(new BorderLayout());
        list.setBackground(BACKGROUND);

        // 카드가 화면 위쪽부터 쌓이도록 목록 패널의 NORTH에 배치한다.
        list.add(cards, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        main.add(scroll, BorderLayout.CENTER);

        // 하단부 — 오른쪽에 배치한 등록 버튼
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        JButton add = new JButton("+  등록");
        add.setPreferredSize(new Dimension(112, 40));
        add.setBackground(TEAL);
        add.setForeground(Color.WHITE);

        // null을 전달하면 새 과제를 등록하는 빈 입력창을 연다.
        add.addActionListener(event -> openEditor(null));

        footer.add(add);
        main.add(footer, BorderLayout.SOUTH);

        return main;
    }

    // 현재 점수와 검색 및 필터 조건에 맞는 과제 목록을 다시 표시한다.
    private void refreshCards() {
        score.setText("점수  " + manager.getTaskScore().getScore() + "점");
        String keyword = search.getText().trim().toLowerCase(Locale.ROOT);
        boolean includeCompleted = filter.getSelectedIndex() == 0;
        LocalDateTime now = LocalDateTime.now(SEOUL);
        cards.removeAll();
        // 완료 여부와 검색어로 과제를 걸러낸 뒤 마감이 빠른 순서대로 카드로 만든다.
        manager.getTasks().stream()
                .filter(task -> includeCompleted || task.getProcess() != 100)
                .filter(task -> task.getTaskName().toLowerCase(Locale.ROOT).contains(keyword)
                        || task.getSubName().toLowerCase(Locale.ROOT).contains(keyword))
                .sorted(Comparator.comparing(Task::getDeadLine))
                .forEach(task -> cards.add(createCard(task, now)));
        // 목록이 비어 있으면 검색어 유무에 맞춰 안내 문구를 표시한다.
        if (cards.getComponentCount() == 0) {
            JLabel empty = new JLabel(keyword.isEmpty() ? "표시할 과제가 없습니다." : "검색 결과가 없습니다.");
            empty.setForeground(MUTED);
            cards.add(empty);
        }
        // 카드를 다시 배치하고 화면을 다시 그린다.
        cards.revalidate();
        cards.repaint();
    }

    // 과제 한 건의 상태, 기본 정보, 진행도, 수정 및 삭제 버튼을 카드에 담는다.
    private JPanel createCard(Task task, LocalDateTime now) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 227, 233)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        // 마감 상태를 카드 왼쪽에 표시하고 만료된 과제에는 다른 색을 사용한다.
        String status = statusOf(task, now);
        JLabel badge = new JLabel(status, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(66, 0));
        badge.setOpaque(true);
        badge.setBackground(status.equals("만료") ? new Color(253, 235, 230) : new Color(224, 243, 238));
        badge.setForeground(status.equals("만료") ? new Color(146, 58, 44) : TEAL);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 15f));
        card.add(badge, BorderLayout.WEST);

        JPanel center = new JPanel(new GridLayout(0, 1, 0, 3));
        center.setOpaque(false);
        JLabel name = new JLabel(task.getTaskName());
        // 사용자가 입력한 과제명을 HTML로 해석하지 않고 그대로 표시한다.
        name.putClientProperty("html.disable", true);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));
        name.setForeground(NAVY);
        name.setPreferredSize(new Dimension(0, name.getPreferredSize().height));
        center.add(name);
        JLabel subject = new JLabel(task.getSubName() + "  ·  " + task.getDeadLine().format(DISPLAY_DATE));
        // 과목명도 일반 텍스트로 표시한다.
        subject.putClientProperty("html.disable", true);
        subject.setForeground(MUTED);
        subject.setPreferredSize(new Dimension(0, subject.getPreferredSize().height));
        center.add(subject);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(task.getProcess());
        progress.setStringPainted(true);
        progress.setForeground(TEAL);
        center.add(progress);
        card.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 6));
        actions.setOpaque(false);
        JButton edit = new JButton("수정");
        edit.addActionListener(event -> openEditor(task));
        actions.add(edit);
        JButton delete = new JButton("삭제");
        delete.addActionListener(event -> {
            // 삭제를 확인한 경우에만 목록에서 과제를 제거하고 저장한다.
            int answer = JOptionPane.showConfirmDialog(this, "선택한 과제를 삭제할까요?",
                    "과제 삭제", JOptionPane.OK_CANCEL_OPTION);
            if (answer == JOptionPane.OK_OPTION) {
                manager.removeTask(task);
                refreshAfterSave(manager.applyDeadlinePenalty(true));
            }
        });
        actions.add(delete);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // 완료를 먼저 확인하고, 미완료 과제는 마감 시각과 남은 날짜로 상태를 결정한다.
    static String statusOf(Task task, LocalDateTime now) {
        if (task.getProcess() == 100) {return "완료";}
        if (now.isAfter(task.getDeadLine())) {return "만료";}
        // 마감이 지나지 않았다면 날짜만 비교해 당일은 D-Day로 표시한다.
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), task.getDeadLine().toLocalDate());
        return days == 0 ? "D-Day" : "D-" + days;
    }

    // 등록할 때는 빈 입력칸을 열고, 수정할 때는 기존 과제 값으로 채운다.
    private void openEditor(Task task) {
        JTextField name = new JTextField(task == null ? "" : task.getTaskName(), 22);
        JTextField subject = new JTextField(task == null ? "" : task.getSubName(), 22);
        JTextField deadline = new JTextField(task == null ? "" : task.getDeadLine().toString(), 22);
        JTextField memo = new JTextField(task == null ? "" : task.getMemo(), 22);
        JComboBox<Integer> progress = new JComboBox<>(new Integer[]{0, 25, 50, 75, 100});
        if (task != null) {progress.setSelectedItem(task.getProcess());}

        // 입력 항목의 이름과 입력칸을 위에서부터 순서대로 배치한다.
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(new JLabel("과제명"));
        form.add(name);
        form.add(new JLabel("과목명"));
        form.add(subject);
        form.add(new JLabel("마감기한 (예: 2026-12-01T23:59)"));
        form.add(deadline);
        form.add(new JLabel("진행도 (%)"));
        form.add(progress);
        form.add(new JLabel("메모"));
        form.add(memo);

        String title = task == null ? "과제 등록" : "과제 수정";
        // 확인을 누르면 입력을 검사한다. 오류가 있으면 입력값을 유지한 채 창을 다시 연다.
        while (JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            if (name.getText().isBlank() || subject.getText().isBlank()) {
                showError("과제명과 과목명을 입력해 주세요.");
                continue;
            }
            // 저장 파일에서 필드를 구분하는 문자 $는 입력값에 사용할 수 없다.
            if ((name.getText() + subject.getText() + memo.getText()).contains("$")) {
                showError("과제명, 과목명, 메모에는 $를 사용할 수 없습니다.");
                continue;
            }
            // 마감기한 문자열을 날짜와 시각으로 변환한다.
            LocalDateTime due;
            try {
                due = LocalDateTime.parse(deadline.getText().trim());
            } catch (DateTimeParseException e) {
                showError("마감기한을 2026-12-01T23:59 형식으로 입력해 주세요.");
                continue;
            }

            int value = (Integer) progress.getSelectedItem();
            String note = memo.getText().isBlank() ? "메모 없음" : memo.getText();
            // 등록은 새 Task를 추가하고, 수정은 기존 Task의 정보와 진행도를 갱신한다.
            if (task == null) {
                manager.addTask(new Task(name.getText().trim(), due, subject.getText().trim(), value, note, false));
            } else {
                task.setTaskName(name.getText().trim());
                task.setSubName(subject.getText().trim());
                task.setDeadLine(due);
                task.setMemo(note);
                manager.updateProgress(task, value);
            }
            // 변경 사항을 저장하는 과정에서 마감 감점을 확인하고 화면을 갱신한다.
            refreshAfterSave(manager.applyDeadlinePenalty(true));
            return;
        }
    }

    // 저장에 실패하면 파일을 다시 읽어 저장된 데이터로 화면을 복구한다.
    private void refreshAfterSave(boolean saved) {
        if (!saved) {
            // 파일도 다시 읽을 수 없으면 과제 창을 닫고 로그인 화면으로 돌아간다.
            if (!manager.loadTasks()) {
                showError("저장한 데이터를 다시 열 수 없습니다. 다시 로그인해 주세요.");
                dispose();
                logout.run();
                return;
            }
            showError("변경 내용을 저장하지 못했습니다.");
        }
        refreshCards();
    }

    // 오류 메시지를 현재 과제 창에 대화상자로 표시한다.
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "할 일 관리", JOptionPane.ERROR_MESSAGE);
    }
}
