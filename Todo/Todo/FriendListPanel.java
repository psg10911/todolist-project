package Todo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FriendListPanel extends JPanel {

    private final FriendService friendService;
    private String currentUserId;

    private JList<String> friendList;
    private DefaultListModel<String> listModel;
    private JTextArea sharedTodoArea;

    public FriendListPanel(FriendService service, String userId) {
        this.friendService = service;
        this.currentUserId = userId;

        setLayout(new BorderLayout());
        initComponents();
        loadFriends(); // 초기 목록 로드
    }

    private void initComponents() {

        // ============ 왼쪽: 친구 목록 =============
        listModel = new DefaultListModel<>();
        friendList = new JList<>(listModel);

        friendList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displayFriendSharedTodos();
            }
        });

        JScrollPane friendScroll = new JScrollPane(friendList);
        friendScroll.setBorder(BorderFactory.createTitledBorder("내 친구 목록"));

        // ============ 오른쪽: 공유된 일정 =============
        sharedTodoArea = new JTextArea();
        sharedTodoArea.setEditable(false);
        sharedTodoArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane todoScroll = new JScrollPane(sharedTodoArea);
        todoScroll.setBorder(BorderFactory.createTitledBorder("나에게 공유된 일정"));

        // 스플릿
        JSplitPane splitPane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, friendScroll, todoScroll);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);

        // ============ 아래쪽: ID 입력 + 친구추가 + 요청목록 버튼 =============
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel label = new JLabel("ID:");
        JTextField idField = new JTextField(12);

        JButton addBtn = new JButton("친구 추가");
        JButton requestListBtn = new JButton("요청 목록");

        bottomPanel.add(label);
        bottomPanel.add(idField);
        bottomPanel.add(addBtn);
        bottomPanel.add(requestListBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // -------- 친구추가 버튼 --------
        addBtn.addActionListener(e -> {
            if (currentUserId == null) {
                JOptionPane.showMessageDialog(this, "로그인 후 이용해주세요.");
                return;
            }

            String friendId = idField.getText().trim();
            if (friendId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "친구 ID를 입력하세요.");
                return;
            }

            boolean result = friendService.requestFriend(currentUserId, friendId);
            if (result) {
                JOptionPane.showMessageDialog(this, friendId + "님에게 친구 요청을 보냈습니다.");
                idField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "친구 요청 실패 (이미 요청했거나 존재하지 않는 ID).",
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // -------- 요청목록 버튼: FriendRequestDialog 띄우기 --------
        requestListBtn.addActionListener(e -> {

            JDialog dialog = new FriendRequestDialog(
                SwingUtilities.getWindowAncestor(this),  // ✔ 부모를 Window로 받음
                friendService,
                currentUserId,
                this
        );


            dialog.setVisible(true);
        });
    }

    // 로그인 후 유저 적용
    public void setUser(String userId) {
        this.currentUserId = userId;
        loadFriends();
    }

    // 친구 목록 로드
    public void loadFriends() {
        if (listModel == null) return;
        listModel.clear();

        if (currentUserId == null) return;

        List<String> friends = friendService.getFriends(currentUserId);
        for (String id : friends) {
            listModel.addElement(id);
        }

        sharedTodoArea.setText("친구를 선택하여 공유된 일정을 확인하세요.");
    }

    // 공유 일정 표시
    private void displayFriendSharedTodos() {
        String selectedFriend = friendList.getSelectedValue();
        if (selectedFriend == null) return;

        List<Task> tasks = friendService.getSharedTodos(currentUserId);

        sharedTodoArea.setText("--- " + selectedFriend + " 님의 공유 일정 ---\n\n");

        if (tasks.isEmpty()) {
            sharedTodoArea.append("공유된 일정이 없습니다.");
            return;
        }

        for (Task t : tasks) {
            sharedTodoArea.append(
                String.format(
                    "ID %d | %s | %s\n",
                    t.getId(),
                    t.isCompleted() ? "[완료]" : "[미완]",
                    t.getTitle()
                )
            );
        }
    }
}
