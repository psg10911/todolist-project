package Todo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 친구 요청 보내기, 나에게 온 요청 목록 확인/처리 기능을 담당하는 GUI 다이얼로그
 * FriendService의 requestFriend, getPendingRequests, acceptFriendRequest, rejectFriendRequest를 사용
 */
public class FriendRequestDialog extends JDialog {

    private final FriendService friendService;
    private final String currentUserId; // 현재 로그인된 사용자 ID

    private JList<String> pendingList;
    private DefaultListModel<String> listModel;
    private JTextField friendIdField;
    
    // 친구 목록 패널 갱신을 위한 참조 (FriendListPanel이 갱신되어야 할 경우 사용)
    private FriendListPanel friendListPanelRef; 

    public FriendRequestDialog(Window parent, FriendService service, String userId, FriendListPanel panelRef) {
        super(parent, "친구 요청 및 관리", ModalityType.APPLICATION_MODAL);
     // 모달 다이얼로그
        this.friendService = service;
        this.currentUserId = userId;
        this.friendListPanelRef = panelRef; // 친구 목록 갱신을 위한 참조 저장
        
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        loadPendingRequests(); // 초기 목록 로드
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // --- 상단: 친구 요청 보내기 (GUI: 친구추가 창) ---
        JPanel requestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        friendIdField = new JTextField(15);
        JButton requestButton = new JButton("요청");

        requestButton.addActionListener(e -> handleRequestSend());
        
        requestPanel.add(new JLabel("친구 ID:"));
        requestPanel.add(friendIdField);
        requestPanel.add(requestButton);
        add(requestPanel, BorderLayout.NORTH);

        // --- 중앙: 나에게 온 친구 요청 목록 (GUI: 친구 요청칸) ---
        listModel = new DefaultListModel<>();
        pendingList = new JList<>(listModel);
        
        // --- 하단: 수락/거절 버튼 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton acceptButton = new JButton("수락");
        JButton rejectButton = new JButton("거절");
        
        acceptButton.addActionListener(e -> handleRequestAction(true));
        rejectButton.addActionListener(e -> handleRequestAction(false));

        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        
        add(new JScrollPane(pendingList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // 친구 요청 보내기 핸들러
    private void handleRequestSend() {
        String targetId = friendIdField.getText().trim();
        if (targetId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "친구 ID를 입력하세요.");
            return;
        }
        if (friendService.requestFriend(currentUserId, targetId)) {
            JOptionPane.showMessageDialog(this, targetId + "에게 친구 요청을 보냈습니다.");
            friendIdField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "요청 실패 (존재하지 않는 ID거나 이미 요청됨).", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 친구 요청 목록을 DB에서 로드
    private void loadPendingRequests() {
        listModel.clear();
        List<String> requests = friendService.getPendingRequests(currentUserId);
        for (String id : requests) {
            listModel.addElement(id);
        }
    }

    // 수락/거절 버튼 액션 핸들러
    private void handleRequestAction(boolean accept) {
        String selectedId = pendingList.getSelectedValue();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "목록에서 요청을 선택하세요.");
            return;
        }

        boolean success = false;
        if (accept) {
            success = friendService.acceptFriendRequest(selectedId, currentUserId);
        } else {
            success = friendService.rejectFriendRequest(selectedId, currentUserId);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, selectedId + "님의 요청을 " + (accept ? "수락" : "거절") + "했습니다.");
            loadPendingRequests(); // 요청 목록 갱신
            
            // ★ 수락/거절 후 메인 창의 친구 목록도 갱신하도록 호출 (추가된 부분)
            if (friendListPanelRef != null && accept) {
                friendListPanelRef.loadFriends(); 
            }
        } else {
            JOptionPane.showMessageDialog(this, "처리 실패.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}