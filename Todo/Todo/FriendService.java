package Todo;

import java.util.ArrayList;
import java.util.List;


public class FriendService {

    private final FriendDao friendDAO;
    private final TodoDao todoDAO;

    public FriendService() {
        this.friendDAO = new FriendDao();
        this.todoDAO = new TodoDao();
    }


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

 
    public List<String> getFriends(String myId) {
        return friendDAO.getFriends(myId);
    }
    
 
    public List<Task> getSharedTodos(String myId) {
    
        List<Integer> sharedTodoIds = friendDAO.getSharedTodoIds(myId);
        List<Task> sharedTasks = new ArrayList<>();
        for (int todoId : sharedTodoIds) {
            Task task = todoDAO.findById(todoId); //?
            if (task != null) {
                sharedTasks.add(task);
            }
        }
        
        return sharedTasks;
    }
}
