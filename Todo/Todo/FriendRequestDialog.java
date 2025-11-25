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
        super(parent, "친구 요청 및 관리", ModalityType.APPLICATION_MODAL); // 모달 다이얼로그
        this.friendService = service;
        this.currentUserId = userId;
        this.friendListPanelRef = panelRef; // 친구 목록 갱신을 위한 참조 저장
        
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Theme.CARD_BG); // 배경색 적용
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 전체 여백

        initComponents();
        loadPendingRequests(); // 초기 목록 로드
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // --- 상단: 친구 요청 보내기 (GUI: 친구추가 창) ---
        JPanel requestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        requestPanel.setBackground(Theme.CARD_BG); // 배경색
        
        JLabel label = new JLabel("친구 ID:");
        label.setFont(Theme.FONT_BOLD_16);
        
        friendIdField = new JTextField(12);
        Theme.styleTextField(friendIdField); // 입력창 스타일
        
        JButton requestButton = new JButton("요청");
        Theme.styleButton(requestButton); // 버튼 스타일

        requestButton.addActionListener(e -> handleRequestSend());
        
        requestPanel.add(label);
        requestPanel.add(friendIdField);
        requestPanel.add(requestButton);
        
        // 타이틀 보더 추가
        requestPanel.setBorder(Theme.createTitledBorder(" 친구 요청 보내기 "));
        
        add(requestPanel, BorderLayout.NORTH);

        // --- 중앙: 나에게 온 친구 요청 목록 (GUI: 친구 요청칸) ---
        listModel = new DefaultListModel<>();
        pendingList = new JList<>(listModel);
        Theme.styleList(pendingList); // 리스트 스타일 적용
        
        JScrollPane scrollPane = new JScrollPane(pendingList);
        scrollPane.setBorder(Theme.createTitledBorder(" 받은 친구 요청 ")); // 타이틀 보더
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // --- 하단: 수락/거절 버튼 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Theme.CARD_BG);
        
        JButton acceptButton = new JButton("수락");
        Theme.styleButton(acceptButton);
        acceptButton.setPreferredSize(new Dimension(80, 35));
        
        JButton rejectButton = new JButton("거절");
        Theme.styleDangerButton(rejectButton); // 거절은 빨간색 스타일
        rejectButton.setPreferredSize(new Dimension(80, 35));
        
        acceptButton.addActionListener(e -> handleRequestAction(true));
        rejectButton.addActionListener(e -> handleRequestAction(false));

        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // 친구 요청 보내기 핸들러
    private void handleRequestSend() {
        String targetId = friendIdField.getText().trim();
        if (targetId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "친구 ID를 입력하세요.");
            return;
        }
        // 자기 자신에게 요청 방지 (Service에서도 체크하지만 UI에서도 한번 더)
        if (targetId.equals(currentUserId)) {
            JOptionPane.showMessageDialog(this, "자기 자신에게는 요청할 수 없습니다.");
            return;
        }

        if (friendService.requestFriend(currentUserId, targetId)) {
            JOptionPane.showMessageDialog(this, targetId + "님에게 친구 요청을 보냈습니다.");
            friendIdField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "요청 실패.\n(존재하지 않는 ID이거나, 이미 친구/요청 상태입니다.)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 친구 요청 목록을 DB에서 로드
    private void loadPendingRequests() {
        listModel.clear();
        List<String> requests = friendService.getPendingRequests(currentUserId);
        if (requests.isEmpty()) {
            listModel.addElement("받은 요청이 없습니다.");
            pendingList.setEnabled(false); // 선택 불가하게
        } else {
            pendingList.setEnabled(true);
            for (String id : requests) {
                listModel.addElement(id);
            }
        }
    }

    // 수락/거절 버튼 액션 핸들러
    private void handleRequestAction(boolean accept) {
        // "받은 요청이 없습니다." 같은 메시지가 선택되었을 때 처리 방지
        if (!pendingList.isEnabled() || pendingList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "목록에서 처리할 요청을 선택하세요.");
            return;
        }

        String selectedId = pendingList.getSelectedValue();
        
        boolean success = false;
        if (accept) {
            success = friendService.acceptFriendRequest(selectedId, currentUserId);
        } else {
            success = friendService.rejectFriendRequest(selectedId, currentUserId);
        }

        if (success) {
            String msg = selectedId + "님의 요청을 " + (accept ? "수락" : "거절") + "했습니다.";
            JOptionPane.showMessageDialog(this, msg);
            
            loadPendingRequests(); // 요청 목록 갱신
            
            // ★ 수락/거절 후 메인 창의 친구 목록도 갱신하도록 호출
            if (friendListPanelRef != null && accept) {
                friendListPanelRef.loadFriends(); 
            }
        } else {
            JOptionPane.showMessageDialog(this, "처리 실패. 다시 시도해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}