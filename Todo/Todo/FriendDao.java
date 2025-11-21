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
        // ... (기존 코드)
        String sql = "INSERT  INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, myUserId);
            pstmt.setString(2, friendId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 나에게 온 친구 요청 목록 확인 (대기중인 것만)
    public List<String> getPendingRequests(String myId) {
        // ... (기존 코드)
        List<String> requestors = new ArrayList<>();
        String sql = "SELECT user_id FROM friends WHERE friend_id = ? AND status = 'pending'";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, myId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                requestors.add(rs.getString("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requestors;
    }

    // 3. 친구 요청 수락하기
    public boolean acceptFriend(String requesterId, String myId) {
        // ... (기존 코드)
        String sql = "UPDATE friends SET status = 'accepted' WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requesterId);
            pstmt.setString(2, myId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // 4. 친구 요청 거절하기
    public boolean rejectFriend(String requesterId, String myId) {
        // ... (기존 코드)
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requesterId);
            pstmt.setString(2, myId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. 친구 목록 조회 (수락된 친구만) - ★ 최종본에 추가된 부분
    /**
     * 나의 수락된 친구 ID 목록을 조회합니다.
     * @param myId 사용자 ID
     * @return 수락된 친구들의 ID 목록
     */
    public List<String> getFriends(String myId) {
        List<String> friends = new ArrayList<>();
        // 쌍방 친구 관계를 조회 (UNION 사용)
        String sql = "SELECT friend_id AS friend FROM friends WHERE user_id = ? AND status = 'accepted' " +
                     "UNION " +
                     "SELECT user_id AS friend FROM friends WHERE friend_id = ? AND status = 'accepted'";

        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, myId);
            pstmt.setString(2, myId);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                friends.add(rs.getString("friend"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }


    // [추가 기능] 친구에게 할 일 공유하기
    public boolean shareTodo(int todoId, String myId, String friendId) {
        // ... (기존 코드)
        String sql = "INSERT INTO shared_todos (todo_id, sender_id, receiver_id) VALUES (?, ?, ?)";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, todoId);
            pstmt.setString(2, myId);
            pstmt.setString(3, friendId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // [추가 기능] 나에게 공유된 할 일 목록(ID) 보기
    public List<Integer> getSharedTodoIds(String myId) {
        // ... (기존 코드)
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