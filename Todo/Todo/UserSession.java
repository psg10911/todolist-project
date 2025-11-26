package Todo;

/**
 * 현재 로그인한 사용자 정보를 관리하는 싱글톤 클래스.
 * (강의 13주차: 싱글톤 패턴 적용)
 */
public class UserSession {
    // 1. 자기 자신의 인스턴스를 static으로 가짐
    private static UserSession instance;
    
    // 2. 현재 로그인한 사용자 ID
    private String userId;

    // 3. 외부에서 생성하지 못하도록 생성자를 private으로 막음
    private UserSession() {
    }

    // 4. 오직 이 메서드를 통해서만 인스턴스에 접근 (Lazy Initialization)
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 로그인 처리
    public void login(String userId) {
        this.userId = userId;
    }

    // 현재 사용자 ID 가져오기
    public String getUserId() {
        return userId;
    }

    // 로그아웃 처리
    public void logout() {
        this.userId = null;
    }

    // 로그인 여부 확인
    public boolean isLoggedIn() {
        return userId != null && !userId.isEmpty();
    }
}