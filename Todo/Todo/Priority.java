package Todo;

public enum Priority {
    HIGH(1, "높음"),
    MEDIUM(2, "보통"),
    LOW(3, "낮음");

    private final int dbValue;
    private final String label;

    Priority(int dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public int getDbValue() { return dbValue; }
    public String getLabel() { return label; }

    // DB 값(int)으로 Enum을 찾는 헬퍼 메서드
    public static Priority fromDbValue(int value) {
        for (Priority p : values()) {
            if (p.dbValue == value) return p;
        }
        return MEDIUM; // 기본값
    }
}