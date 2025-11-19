package Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    // 1. 친구 요청 보내기
    public boolean requestFriend(String myUserId, String friendId) {
        
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, myUserId);   
            pstmt.setString(2, friendId);   
            
            int rowsAffected = pstmt.executeUpdate();
            
            return rowsAffected > 0; // 성공시 true
            
        } catch (SQLException e) {
            e.printStackTrace(); 
            return false; 
        }
    }
    
    // 2. 나에게 온 친구 요청 목록 확인 (대기중인 것만)
    public List<String> getPendingRequests(String myId) {
        List<String> requestors = new ArrayList<>();
        // 나(friend_id)에게 요청을 보냈고, 아직 수락 안 된(pending) 목록 조회
        String sql = "SELECT user_id FROM friends WHERE friend_id = ? AND status = 'pending'";

        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, myId);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                requestors.add(rs.getString("user_id")); // 요청 보낸 사람 ID 담기
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requestors;
    }
    
    // 3. 친구 요청 수락하기
    public boolean acceptFriend(String requesterId, String myId) {
        // requesterId: 요청 보낸 사람, myId: 나 (수락하는 사람)
        // 상태를 'pending' -> 'accepted'로 변경
        String sql = "UPDATE friends SET status = 'accepted' WHERE user_id = ? AND friend_id = ?";

        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, requesterId);
            pstmt.setString(2, myId);

            return pstmt.executeUpdate() > 0; // 성공하면 true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------
    // [추가 기능] 친구에게 할 일 공유하기
    // 설명: 내 투두리스트의 번호(todoId)를 친구(friendId)에게 공유함
    // ---------------------------------------------------------
    public boolean shareTodo(int todoId, String myId, String friendId) {
        
        String sql = "INSERT INTO shared_todos (todo_id, sender_id, receiver_id) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, todoId);      // 공유할 할 일 번호
            pstmt.setString(2, myId);     // 보내는 사람 (나)
            pstmt.setString(3, friendId); // 받는 사람 (친구)
            
            int rows = pstmt.executeUpdate();
            return rows > 0; // 성공하면 true
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // [추가 기능] 나에게 공유된 할 일 목록(ID) 보기
    // 설명: 친구들이 나에게 공유해준 할 일들의 번호만 가져옴 (나중에 조회용)
    public List<Integer> getSharedTodoIds(String myId) {
        List<Integer> sharedList = new ArrayList<>();
        String sql = "SELECT todo_id FROM shared_todos WHERE receiver_id = ?";
        
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, myId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                sharedList.add(rs.getInt("todo_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sharedList;
    }
}