package Todo;

import java.util.ArrayList;
import java.util.List;
// TodoDAO, Todo 모델에 대한 import는 실제 경로에 따라 변경 필요

public class FriendService {

    private final FriendDao friendDAO;
    private final TodoDao todoDAO;

    public FriendService() {
        this.friendDAO = new FriendDao();
        this.todoDAO = new TodoDao();
    }

    // --- 친구 추가 및 요청 관리 기능 (기존 코드 유지) ---

    public boolean requestFriend(String myUserId, String friendId) {
        if (myUserId.equals(friendId)) {
            System.out.println("⚠️ 자기 자신에게 친구 요청을 보낼 수 없습니다.");
            return false;
        }
        return friendDAO.requestFriend(myUserId, friendId);
    }

    public List<String> getPendingRequests(String myId) {
        return friendDAO.getPendingRequests(myId);
    }

    public boolean acceptFriendRequest(String requesterId, String myId) {
        return friendDAO.acceptFriend(requesterId, myId);
    }

    public boolean rejectFriendRequest(String requesterId, String myId) {
        return friendDAO.rejectFriend(requesterId, myId);
    }

    // ------------------------------------------------------------------
    // 2. 추가된 기능: 친구 목록 및 공유 투두 조회
    // ------------------------------------------------------------------
    
    /**
     * 현재 사용자의 수락된 친구 목록을 조회합니다.
     */
    public List<String> getFriends(String myId) {
        return friendDAO.getFriends(myId);
    }
    
    /**
     * 친구가 나에게 공유한 투두 항목의 전체 정보를 조회합니다.
     * (TodoDao의 findById를 호출하여 완성됨)
     */
    public List<Task> getSharedTodos(String myId) {
        // 1. FriendDAO를 통해 나에게 공유된 todo_id 목록을 가져옵니다.
        List<Integer> sharedTodoIds = friendDAO.getSharedTodoIds(myId);
        
        List<Task> sharedTasks = new ArrayList<>();
        
        // 2. 이 ID 목록을 사용하여 TodoDao에서 실제 Task(할 일) 정보를 조회합니다.
        for (int todoId : sharedTodoIds) {
            Task task = todoDAO.findById(todoId);
            if (task != null) {
                sharedTasks.add(task);
            }
        }
        
        return sharedTasks;
    }
}