import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TodoFrame extends JFrame {
    private static final Color BACKGROUND = new Color(242, 246, 249);
    private static final Color NAVY = new Color(24, 47, 67);
    private static final Color TEAL = new Color(0, 115, 105);
    private static final Color MUTED = new Color(82, 101, 116);
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TaskManager manager;
    private final JLabel scoreLabel = new JLabel();
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> taskFilter = new JComboBox<>(new String[]{"전체", "진행 중"});
    private final JPanel cardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));

    // 창 설정
    public TodoFrame(String id, TaskManager manager) {
        super("할 일 관리");
        this.manager = manager;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 440));
        setSize(940, 620);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(id), BorderLayout.WEST);
        root.add(createMainPanel(), BorderLayout.CENTER);
        setContentPane(root);
        refreshCards();

        setupDeadlineTimer();
    }

    //마감 여부 갱신
    private void setupDeadlineTimer() {
        Timer timer = new Timer(30_000, event -> {
            if (isActive()) {refreshAfterSave(manager.applyDeadlinePenalty());}
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {timer.start();}

            @Override
            public void windowClosed(WindowEvent event) {timer.stop();}
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
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 16f));
        actions.add(scoreLabel);
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.addActionListener(event -> returnToLogin());
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

        main.add(createSearchHeader(), BorderLayout.NORTH);
        main.add(createTaskScrollPane(), BorderLayout.CENTER);
        main.add(createRegisterFooter(), BorderLayout.SOUTH);
        return main;
    }

    // 메인 패널 상단 — 제목, 검색창, 필터
    private JPanel createSearchHeader() {
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
        searchLabel.setLabelFor(searchField);
        searchField.setToolTipText("과제명 또는 과목명으로 검색"); //툴 팁 표시(커서 가져다 대면 나옴)
        searchField.setPreferredSize(new Dimension(0, 34));
        // 검색창 내용 수정시 즉각 반응
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshCards();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshCards();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshCards();
            }
        });
        taskFilter.addActionListener(event -> refreshCards()); // 필터 변경 표시

        toolbar.add(searchLabel, BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(taskFilter, BorderLayout.EAST);
        header.add(toolbar, BorderLayout.CENTER);
        return header;
    }

    //과제 카드를 넣을 스크롤 패널
    private JScrollPane createTaskScrollPane() {
        // 중단부 — 스크롤 가능한 과제 카드 목록
        cardsPanel.setOpaque(false);

        JPanel list = new JPanel(new BorderLayout());
        list.setBackground(BACKGROUND);

        // 카드가 화면 위쪽부터 쌓이도록 목록 패널의 NORTH에 배치한다.
        list.add(cardsPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    // 하단부 — 오른쪽에 배치한 등록 버튼
    private JPanel createRegisterFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        JButton addButton = new JButton("+  등록");
        addButton.setPreferredSize(new Dimension(112, 40));
        addButton.setBackground(TEAL);
        addButton.setForeground(Color.WHITE);

        // null을 전달하면 새 과제를 등록하는 빈 입력창을 연다.
        addButton.addActionListener(event -> openEditor(null));

        footer.add(addButton);
        return footer;
    }

    // 과제 카드, 점수 새로고침 및 마감순 정렬
    private void refreshCards() {
        scoreLabel.setText("점수  " + manager.getTaskScore().getScore() + "점");
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        boolean includeCompleted = taskFilter.getSelectedIndex() == 0;
        LocalDateTime now = LocalDateTime.now(SEOUL);
        cardsPanel.removeAll();
        List<Task> visibleTasks = new ArrayList<>();
        for (Task task : manager.getTasks()) {
            if (!includeCompleted && task.getProcess() == 100) {continue;}

            String taskName = task.getTaskName().toLowerCase(Locale.ROOT);
            if (taskName.contains(keyword)
                    || task.getSubName().toLowerCase(Locale.ROOT).contains(keyword)) {
                visibleTasks.add(task);
            }
        }

        visibleTasks.sort(Comparator.comparing(Task::getDeadLine));
        for (Task task : visibleTasks) {
            cardsPanel.add(createCard(task, now));
        }

        if (visibleTasks.isEmpty()) {
            String message = keyword.isEmpty() ? "표시할 과제가 없습니다." : "검색 결과가 없습니다.";
            JLabel empty = new JLabel(message);
            empty.setForeground(MUTED);
            cardsPanel.add(empty);
        }
        // 카드를 다시 배치하고 화면을 다시 그린다.
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // 과제 카드 생성
    private JPanel createCard(Task task, LocalDateTime now) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 227, 233)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));


        String status = statusOf(task, now);
        JLabel badge = new JLabel(status, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(66, 0));
        badge.setOpaque(true);
        if ("만료".equals(status)) {
            badge.setBackground(new Color(232, 232, 232));
            badge.setForeground(new Color(85, 85, 85));}
        else if ("완료".equals(status)) {
            badge.setBackground(new Color(224, 243, 238));
            badge.setForeground(TEAL);}
        else {
            badge.setBackground(new Color(255, 243, 196));
            badge.setForeground(new Color(110, 80, 0));};
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 15f));
        card.add(badge, BorderLayout.WEST);

        JPanel center = new JPanel(new GridLayout(0, 1, 0, 3));
        center.setOpaque(false);
        JLabel name = new JLabel(task.getTaskName());
        name.putClientProperty("html.disable", true);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));
        name.setForeground(NAVY);
        name.setPreferredSize(new Dimension(0, name.getPreferredSize().height));
        center.add(name);
        JLabel subject = new JLabel(task.getSubName() + "  ·  " + task.getDeadLine().format(DISPLAY_DATE));
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
        delete.addActionListener(event -> confirmAndDeleteTask(task));
        actions.add(delete);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    //삭제 확인창
    private void confirmAndDeleteTask(Task task) {
        int answer = JOptionPane.showConfirmDialog(this, "선택한 과제를 삭제할까요?",
                "과제 삭제", JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.OK_OPTION) {
            manager.removeTask(task);
            refreshAfterSave(manager.applyDeadlinePenalty(true));
        }
    }

   // 과제 상태 표시 - D-Day 계산 및 마감 여부
    static String statusOf(Task task, LocalDateTime now) {
        if (task.getProcess() == 100) {
            return "완료";
        }
        if (now.isAfter(task.getDeadLine())) {
            return "만료";
        }
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), task.getDeadLine().toLocalDate());
        return days == 0 ? "D-Day" : "D-" + days;
    }

    //등록 및 수정 창
    private void openEditor(Task task) {
        JTextField name = new JTextField(22);
        JTextField subject = new JTextField(22);
        JTextField deadline = new JTextField(22);
        JTextField memo = new JTextField(22);
        JComboBox<Integer> progress = new JComboBox<>(new Integer[]{0, 25, 50, 75, 100});

        // null이 아니면 기존 과제 수정(입력 값을 기존 값에 덮어씌움)
        if (task != null) {
            name.setText(task.getTaskName());
            subject.setText(task.getSubName());
            deadline.setText(task.getDeadLine().toString());
            memo.setText(task.getMemo());
            progress.setSelectedItem(task.getProcess());}

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
        while (JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String taskName = name.getText();
            String subjectName = subject.getText();
            String memoText = memo.getText();

            if (taskName.isBlank() || subjectName.isBlank()) {
                showError("과제명과 과목명을 입력해 주세요.");
                continue;
            }
            // 저장 파일에 구분자 $는 사용 불가능
            if (taskName.contains("$") || subjectName.contains("$") || memoText.contains("$")) {
                showError("과제명, 과목명, 메모에는 $를 사용할 수 없습니다.");
                continue;
            }
            LocalDateTime due;
            try {due = LocalDateTime.parse(deadline.getText().trim());}
            catch (DateTimeParseException e) {
                showError("마감기한을 2026-12-01T23:59 형식으로 입력해 주세요.");
                continue;}

            int value = (Integer) progress.getSelectedItem();
            String note = memoText.isBlank() ? "메모 없음" : memoText;

            // null이면 과제 신규 등록
            if (task == null) {manager.addTask(new Task(taskName.trim(), due, subjectName.trim(), value, note, false));}
            else {
                task.setTaskName(taskName.trim());
                task.setSubName(subjectName.trim());
                task.setDeadLine(due);
                task.setMemo(note);
                manager.updateProgress(task, value);}
            // 화면 갱신
            refreshAfterSave(manager.applyDeadlinePenalty(true));
            return;
        }
    }

    //저장 실패시 로그인 화면으로 돌아감(버그났을 때 확인용)
    private void refreshAfterSave(boolean saved) {
        if (!saved) {
            // 파일도 다시 읽을 수 없으면 과제 창을 닫고 로그인 화면으로 돌아간다.
            if (!manager.loadTasks()) {
                showError("저장한 데이터를 다시 열 수 없습니다. 다시 로그인해 주세요.");
                returnToLogin();
                return;
            }
            showError("변경 내용을 저장하지 못했습니다.");
        }
        refreshCards();
    }

    //로그인 화면 표시
    private void returnToLogin() {
        dispose();
        GuiMain.showLogin();}

    // 오류 메세지 표시(오류 확인용)
    private void showError(String message) {JOptionPane.showMessageDialog(this, message,
            "할 일 관리", JOptionPane.ERROR_MESSAGE);}
}