package Todo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 달력의 날짜 하나를 표현하는 커스텀 버튼
 * 점(●)과 막대(-)를 그려서 일정을 표시함
 */
public class DayButton extends JButton {
    private List<Color> dotColors = new ArrayList<>();
    private List<Color> barColors = new ArrayList<>();
    
    private boolean isToday = false;
    private boolean isSelected = false;

    public DayButton(String text) {
        super(text);
        // 기본 스타일 설정
        setFont(Theme.FONT_REGULAR_14);
        setFocusPainted(false);
        setBorder(null);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(Theme.TEXT_MAIN);
    }

    public void setTaskColors(List<Color> dots, List<Color> bars) { 
        this.dotColors = dots;
        this.barColors = bars;
    }
    
    public void setIsToday(boolean b) { this.isToday = b; }
    public void setIsSelected(boolean b) { this.isSelected = b; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. 배경 그리기
        if (isToday) {
            g2.setColor(Theme.PRIMARY);
            int diameter = Math.min(w, h) - 4;
            g2.fillOval((w-diameter)/2, (h-diameter)/2, diameter, diameter);
            setForeground(Color.WHITE); // 오늘 날짜는 흰색 글씨
            setFont(Theme.FONT_BOLD_16);
        } else if (isSelected) {
            g2.setColor(new Color(225, 240, 255));
            int diameter = Math.min(w, h) - 4;
            g2.fillOval((w-diameter)/2, (h-diameter)/2, diameter, diameter);
            setForeground(Theme.PRIMARY);
            setFont(Theme.FONT_BOLD_16);
        } else {
            setForeground(Theme.TEXT_MAIN);
            setFont(Theme.FONT_REGULAR_14);
        }

        // 2. 상단 막대 그리기 (최대 3개)
        if (!barColors.isEmpty()) {
            int barHeight = 5; 
            int barGap = 2;    
            int startY = 4;    
            int maxBars = 3; 

            int count = Math.min(barColors.size(), maxBars);
            
            for (int i = 0; i < count; i++) {
                g2.setColor(barColors.get(i));
                g2.fillRoundRect(6, startY + (i * (barHeight + barGap)), w - 12, barHeight, 2, 2);
            }
            
            if (barColors.size() > maxBars) {
                g2.setColor(Theme.TEXT_SUB);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("+", w - 10, startY + (count * (barHeight + barGap)) + 2); 
            }
        }

        // 3. 텍스트 그리기 (기본)
        super.paintComponent(g);

        // 4. 하단 점 그리기 (최대 3개)
        if (!dotColors.isEmpty()) {
            int dotSize = 6;
            int gap = 3;
            int maxDots = 3;
            
            int count = dotColors.size();
            int displayCount = Math.min(count, maxDots);
            boolean hasMore = count > maxDots;

            int totalWidth = (displayCount * dotSize) + ((displayCount - 1) * gap);
            if (hasMore) totalWidth += (gap + 6);

            int startX = (w - totalWidth) / 2;
            int y = h - 12; 

            for (int i = 0; i < displayCount; i++) {
                g2.setColor(dotColors.get(i));
                g2.fillOval(startX + (i * (dotSize + gap)), y, dotSize, dotSize);
            }

            if (hasMore) {
                g2.setColor(Theme.TEXT_SUB);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("+", startX + (displayCount * (dotSize + gap)), y + dotSize); 
            }
        }
    }
}