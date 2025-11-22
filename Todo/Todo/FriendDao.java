package Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    // ------------------------------------------------------------------
    // ★ 헬퍼 메서드: 두 사용자 사이에 이미 레코드가 존재하는지 확인 (요청 상태와 무관)
    // ------------------------------------------------------------------
    /**
     * 두 사용자 ID(A, B)를 받아 A->B 또는 B->A 레코드가 존재하는지 확인합니다.
     * @return 레코드가 존재하면 true (친구, 요청 중 무엇이든), 없으면 false
     */
    public boolean checkRecordExists(String userA, String userB) {
        // A->B 또는 B->A 인 레코드를 모두 조회
        String sql = "SELECT COUNT(*) FROM friends " +
                     "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userA);
            pstmt.setString(2, userB);
            pstmt.setString(3, userB);
            pstmt.setString(4, userA);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // 1. 친구 요청 보내기 (수정됨)
    public boolean requestFriend(String myUserId, String friendId) {
        // [수정된 로직] 이미 관계가 존재하는지 확인 (중복 요청 방지)
        if (checkRecordExists(myUserId, friendId)) {
            // 이미 친구이거나, 요청을 보냈거나, 상대방이 나에게 요청을 보낸 상태
            System.out.println("⚠️ 이미 친구이거나 요청이 존재합니다: " + myUserId + " -> " + friendId);
            return false;
        }

        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')";
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

    // 2. 나에게 온 친구 요청 목록 확인 (대기중인 것만) (수정 없음)
    public List<String> getPendingRequests(String myId) {
        // 기존 코드 유지
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

    // 3. 친구 요청 수락하기 (수정됨 - 핵심)
    /**
     * 요청자(A)의 요청을 수락자(B)가 수락합니다. 
     * DB에 존재하는 A->B 레코드와 B->A 레코드(존재한다면)를 모두 'accepted'로 변경합니다.
     */
    public boolean acceptFriend(String requesterId, String myId) {
        String sql = "UPDATE friends SET status = 'accepted' " +
                     "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 1. A->B (requesterId -> myId) 요청을 'accepted'로 변경
            pstmt.setString(1, requesterId);
            pstmt.setString(2, myId);
            
            // 2. B->A (myId -> requesterId) 요청도 혹시 있다면 함께 'accepted'로 변경 (상태 비대칭 해결)
            pstmt.setString(3, myId);
            pstmt.setString(4, requesterId);

            int updatedRows = pstmt.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // 4. 친구 요청 거절하기 (수정 없음)
    public boolean rejectFriend(String requesterId, String myId) {
        // 기존 코드 유지 (DELETE는 요청자 -> 수락자 레코드만 지워도 됨)
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

    // 5. 친구 목록 조회 (수정 없음 - UNION 사용으로 이미 양방향 조회)
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


    // [추가 기능] 친구에게 할 일 공유하기 (수정 없음)
    public boolean shareTodo(int todoId, String myId, String friendId) {
        // 기존 코드 유지
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

    // [추가 기능] 나에게 공유된 할 일 목록(ID) 보기 (수정 없음)
    public List<Integer> getSharedTodoIds(String myId) {
        // 기존 코드 유지
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
