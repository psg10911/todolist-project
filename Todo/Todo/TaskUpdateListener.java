package Todo;

/**
 * 일정이 추가/수정/삭제되었을 때 알림을 받기 위한 인터페이스 (Observer)
 */
public interface TaskUpdateListener {
    void onTaskUpdated(); // "일정 변했으니 갱신하세요" 라는 신호
}