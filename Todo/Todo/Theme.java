package Todo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 앱 전체의 색상, 폰트, 컴포넌트 스타일을 관리하는 클래스
 */
public class Theme {
    // ===== 색상 팔레트 (Flat Design) =====
    public static final Color PRIMARY = new Color(52, 152, 219);    // 메인 블루
    public static final Color SECONDARY = new Color(41, 128, 185);  // 진한 블루 (버튼 호버용)
    public static final Color ACCENT = new Color(231, 76, 60);      // 포인트/삭제 레드
    public static final Color BACKGROUND = new Color(245, 246, 250); // 앱 전체 배경
    public static final Color CARD_BG = Color.WHITE;                // 카드/패널 배경
    public static final Color TEXT_MAIN = new Color(44, 62, 80);    // 진한 텍스트
    public static final Color TEXT_SUB = new Color(149, 165, 166);  // 연한 텍스트
    public static final Color BORDER = new Color(223, 230, 233);    // 테두리 색상

    // ===== 폰트 (한글 깨짐 방지: 맑은 고딕) =====
    public static final Font FONT_BOLD_26 = new Font("맑은 고딕", Font.BOLD, 26);
    public static final Font FONT_BOLD_24 = new Font("맑은 고딕", Font.BOLD, 24);
    public static final Font FONT_BOLD_16 = new Font("맑은 고딕", Font.BOLD, 16);
    public static final Font FONT_REGULAR_14 = new Font("맑은 고딕", Font.PLAIN, 14);
    public static final Font FONT_REGULAR_12 = new Font("맑은 고딕", Font.PLAIN, 12);

    // ===== 컴포넌트 스타일링 유틸리티 =====

    /** 기본 버튼 스타일 */
    public static void styleButton(JButton btn) {
        btn.setFont(FONT_REGULAR_14);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // 입체감 제거
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 마우스 호버 효과
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if(btn.isEnabled()) btn.setBackground(SECONDARY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if(btn.isEnabled()) btn.setBackground(PRIMARY);
            }
        });
    }

    /** 삭제/위험 버튼 스타일 */
    public static void styleDangerButton(JButton btn) {
        styleButton(btn); 
        btn.setBackground(ACCENT);
        
        for (java.awt.event.MouseListener ml : btn.getMouseListeners()) { /* 기존 리스너 유지 */ }
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT);
            }
        });
    }

    /** 텍스트 입력창 스타일 */
    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_REGULAR_14);
        tf.setForeground(TEXT_MAIN);
        Border line = BorderFactory.createLineBorder(BORDER);
        Border empty = BorderFactory.createEmptyBorder(5, 10, 5, 10);
        tf.setBorder(BorderFactory.createCompoundBorder(line, empty));
        tf.setBackground(Color.WHITE);
    }

    /** 테이블 스타일 */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR_14);
        table.setRowHeight(35); // 행 높이 시원하게
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(214, 234, 248)); // 선택 시 연한 파랑
        table.setSelectionForeground(TEXT_MAIN);
        
        // 헤더 스타일
        table.getTableHeader().setFont(FONT_BOLD_16);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        
        DefaultTableCellRenderer centerRenderer = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }

    // ★ [추가됨] 리스트(JList) 스타일링
    public static void styleList(JList<?> list) {
        list.setFont(FONT_REGULAR_14);
        list.setSelectionBackground(new Color(214, 234, 248)); // 테이블과 동일한 연한 파랑 선택색
        list.setSelectionForeground(TEXT_MAIN);
        list.setFixedCellHeight(35); // 행 높이 넉넉하게
        list.setBackground(Color.WHITE);
    }

    // ★ [추가됨] 텍스트 영역(JTextArea) 스타일링
    public static void styleTextArea(JTextArea area) {
        area.setFont(FONT_REGULAR_14);
        area.setForeground(TEXT_MAIN);
        area.setBackground(Color.WHITE);
        area.setLineWrap(true);     // 줄바꿈 자동
        area.setWrapStyleWord(true); // 단어 단위 줄바꿈
    }

    // ★ [추가됨] 타이틀 보더(그룹박스 제목) 스타일링 헬퍼
    public static TitledBorder createTitledBorder(String title) {
        Border line = BorderFactory.createLineBorder(BORDER);
        TitledBorder border = BorderFactory.createTitledBorder(line, title);
        border.setTitleFont(FONT_BOLD_16);
        border.setTitleColor(PRIMARY);
        return border;
    }
}