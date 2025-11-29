package Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {


    public boolean checkRecordExists(String userA, String userB) {
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



    public boolean requestFriend(String myUserId, String friendId) {
  
        if (checkRecordExists(myUserId, friendId)) {
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


    public List<String> getPendingRequests(String myId) {
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

    public boolean acceptFriend(String requesterId, String myId) {
        String sql = "UPDATE friends SET status = 'accepted' " +
                     "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        
        try (Connection conn = DBUtill.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requesterId);
            pstmt.setString(2, myId);
            pstmt.setString(3, myId);
            pstmt.setString(4, requesterId);

            int updatedRows = pstmt.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean rejectFriend(String requesterId, String myId) {
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

    public List<String> getFriends(String myId) {
        List<String> friends = new ArrayList<>();
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


    public boolean shareTodo(int todoId, String myId, String friendId) {
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
