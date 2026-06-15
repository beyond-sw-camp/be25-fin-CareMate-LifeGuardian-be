package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.report.dto.internal.data.GrowthStandardDto;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

/**
 * 성장 기준 백분위와 고객 측정값을 리포트용 PNG 그래프로 생성한다.
 */
@Service
public class GrowthChartService {

    private static final int WIDTH = 1100;
    private static final int COMBINED_HEIGHT = 660;
    private static final int COMBINED_PANEL_HEIGHT = 235;
    private static final int COMBINED_X_AXIS_Y = 561;
    private static final int COMBINED_LEGEND_Y = 635;
    private static final int HEIGHT = 470;
    private static final int LEFT = 138;
    private static final int RIGHT = 64;
    private static final int TOP = 58;
    private static final int BOTTOM = 82;

    /**
     * 키와 몸무게 백분위 곡선을 하나의 이미지로 구성해 Base64 데이터 URI로 반환한다.
     */
    public String createCombinedChart(List<GrowthStandardDto> standards) {
        if (standards == null || standards.size() < 2) {
            return null;
        }

        BufferedImage image = new BufferedImage(WIDTH, COMBINED_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            configure(graphics);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, COMBINED_HEIGHT);

            graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
            graphics.setColor(new Color(37, 48, 71));
            graphics.drawString("월령별 성장 백분위 곡선", LEFT, 32);

            drawCombinedPanel(
                    graphics, standards, 58, "키", "cm",
                    GrowthStandardDto::getHeightP5,
                    GrowthStandardDto::getHeightP50,
                    GrowthStandardDto::getHeightP95,
                    GrowthStandardDto::getChildHeight
            );
            drawCombinedPanel(
                    graphics, standards, 326, "몸무게", "kg",
                    GrowthStandardDto::getWeightP5,
                    GrowthStandardDto::getWeightP50,
                    GrowthStandardDto::getWeightP95,
                    GrowthStandardDto::getChildWeight
            );
            drawCombinedXAxis(graphics, standards);
            drawLegendAt(graphics);

            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", output);
                return "data:image/png;base64,"
                        + Base64.getEncoder().encodeToString(output.toByteArray());
            }
        } catch (Exception e) {
            throw new BaseException(500, "통합 성장 그래프 생성에 실패했습니다.");
        } finally {
            graphics.dispose();
        }
    }

    private void drawCombinedPanel(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            int top,
            String label,
            String unit,
            Function<GrowthStandardDto, BigDecimal> p5Value,
            Function<GrowthStandardDto, BigDecimal> p50Value,
            Function<GrowthStandardDto, BigDecimal> p95Value,
            Function<GrowthStandardDto, BigDecimal> childValue
    ) {
        int panelHeight = COMBINED_PANEL_HEIGHT;
        double minValue = standards.stream().map(p5Value)
                .mapToDouble(BigDecimal::doubleValue).min().orElse(0);
        double maxValue = standards.stream().map(p95Value)
                .mapToDouble(BigDecimal::doubleValue).max().orElse(1);
        for (GrowthStandardDto standard : standards) {
            BigDecimal child = childValue.apply(standard);
            if (child != null) {
                minValue = Math.min(minValue, child.doubleValue());
                maxValue = Math.max(maxValue, child.doubleValue());
            }
        }
        double padding = Math.max((maxValue - minValue) * 0.12, 1);
        minValue = Math.max(0, minValue - padding);
        maxValue += padding;

        int chartWidth = WIDTH - LEFT - RIGHT;
        graphics.setColor(new Color(248, 250, 253));
        graphics.fillRoundRect(LEFT, top, chartWidth, panelHeight, 14, 14);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 19));
        graphics.setColor(new Color(44, 61, 89));
        graphics.drawString(label, 22, top + panelHeight / 2);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 14));
        graphics.drawString(unit, 24, top + panelHeight / 2 + 21);

        for (int index = 0; index <= 4; index++) {
            int gridY = top + panelHeight * index / 4;
            graphics.setColor(new Color(226, 232, 240));
            graphics.drawLine(LEFT, gridY, LEFT + chartWidth, gridY);
            double tickValue = maxValue - (maxValue - minValue) * index / 4;
            graphics.setColor(new Color(112, 121, 138));
            String tick = formatValue(tickValue);
            graphics.drawString(tick, LEFT - graphics.getFontMetrics().stringWidth(tick) - 12, gridY + 5);
        }

        for (int index = 0; index < standards.size(); index++) {
            if (!shouldShowAgeTick(standards, index)) {
                continue;
            }
            int gridX = combinedX(index, standards.size());
            graphics.setColor(new Color(235, 239, 244));
            graphics.drawLine(gridX, top, gridX, top + panelHeight);
        }

        drawCombinedArea(graphics, standards, p5Value, p95Value, top, panelHeight, minValue, maxValue);
        drawCombinedLine(graphics, standards, p5Value, top, panelHeight, minValue, maxValue,
                new Color(236, 167, 50), dashedStroke());
        drawCombinedLine(graphics, standards, p50Value, top, panelHeight, minValue, maxValue,
                new Color(82, 113, 225), solidStroke());
        drawCombinedLine(graphics, standards, p95Value, top, panelHeight, minValue, maxValue,
                new Color(65, 177, 132), dashedStroke());
        drawCombinedChildPoint(
                graphics, standards, childValue, top, panelHeight, minValue, maxValue, unit);
    }

    private void drawCombinedArea(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> lowerValue,
            Function<GrowthStandardDto, BigDecimal> upperValue,
            int top,
            int panelHeight,
            double minValue,
            double maxValue
    ) {
        Path2D area = new Path2D.Double();
        for (int index = 0; index < standards.size(); index++) {
            double pointX = combinedX(index, standards.size());
            double pointY = combinedY(
                    upperValue.apply(standards.get(index)).doubleValue(),
                    top, panelHeight, minValue, maxValue);
            if (index == 0) {
                area.moveTo(pointX, pointY);
            } else {
                area.lineTo(pointX, pointY);
            }
        }
        for (int index = standards.size() - 1; index >= 0; index--) {
            area.lineTo(
                    combinedX(index, standards.size()),
                    combinedY(lowerValue.apply(standards.get(index)).doubleValue(),
                            top, panelHeight, minValue, maxValue)
            );
        }
        area.closePath();
        graphics.setColor(new Color(224, 234, 249));
        graphics.fill(area);
    }

    private void drawCombinedLine(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> value,
            int top,
            int panelHeight,
            double minValue,
            double maxValue,
            Color color,
            BasicStroke stroke
    ) {
        Path2D line = new Path2D.Double();
        for (int index = 0; index < standards.size(); index++) {
            double pointX = combinedX(index, standards.size());
            double pointY = combinedY(value.apply(standards.get(index)).doubleValue(),
                    top, panelHeight, minValue, maxValue);
            if (index == 0) {
                line.moveTo(pointX, pointY);
            } else {
                line.lineTo(pointX, pointY);
            }
        }
        graphics.setColor(color);
        graphics.setStroke(stroke);
        graphics.draw(line);
    }

    private void drawCombinedChildPoint(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> value,
            int top,
            int panelHeight,
            double minValue,
            double maxValue,
            String unit
    ) {
        for (int index = 0; index < standards.size(); index++) {
            BigDecimal child = value.apply(standards.get(index));
            if (child == null) {
                continue;
            }
            int pointX = combinedX(index, standards.size());
            int pointY = combinedY(child.doubleValue(), top, panelHeight, minValue, maxValue);
            graphics.setColor(new Color(225, 72, 77, 50));
            graphics.fillRect(pointX - 1, top, 3, panelHeight);
            graphics.setColor(new Color(225, 72, 77));
            graphics.fillOval(pointX - 7, pointY - 7, 14, 14);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
            graphics.drawString(child.stripTrailingZeros().toPlainString() + unit,
                    Math.min(pointX + 9, WIDTH - RIGHT - 65), Math.max(top + 17, pointY - 9));
        }
    }

    private void drawCombinedXAxis(
            Graphics2D graphics,
            List<GrowthStandardDto> standards
    ) {
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 13));
        graphics.setColor(new Color(92, 101, 118));
        for (int index = 0; index < standards.size(); index++) {
            if (shouldShowAgeTick(standards, index)) {
                int ageMonth = standards.get(index).getAgeMonth();
                String label = formatCompactAgeMonth(ageMonth);
                int pointX = combinedX(index, standards.size());
                graphics.drawString(label,
                        pointX - graphics.getFontMetrics().stringWidth(label) / 2,
                        COMBINED_X_AXIS_Y + 22);
            }
        }
        graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
        String axisTitle = "월령";
        graphics.drawString(
                axisTitle,
                LEFT + (WIDTH - LEFT - RIGHT - graphics.getFontMetrics().stringWidth(axisTitle)) / 2,
                COMBINED_X_AXIS_Y + 48
        );
    }

    private boolean shouldShowAgeTick(List<GrowthStandardDto> standards, int index) {
        if (index == 0 || index == standards.size() - 1) {
            return true;
        }

        GrowthStandardDto standard = standards.get(index);
        if (standard.getChildHeight() != null || standard.getChildWeight() != null) {
            return true;
        }

        int currentAgeMonth = standards.stream()
                .filter(item -> item.getChildHeight() != null || item.getChildWeight() != null)
                .mapToInt(GrowthStandardDto::getAgeMonth)
                .findFirst()
                .orElse(Integer.MIN_VALUE);

        int ageMonth = standard.getAgeMonth();
        int firstAgeMonth = standards.getFirst().getAgeMonth();
        int lastAgeMonth = standards.getLast().getAgeMonth();

        return ageMonth % 6 == 0
                && Math.abs(ageMonth - currentAgeMonth) >= 4
                && ageMonth - firstAgeMonth >= 4
                && lastAgeMonth - ageMonth >= 4;
    }

    private void drawLegendAt(Graphics2D graphics) {
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 13));
        drawLegendItem(graphics, 285, COMBINED_LEGEND_Y, new Color(65, 177, 132), "95백분위");
        drawLegendItem(graphics, 430, COMBINED_LEGEND_Y, new Color(82, 113, 225), "50백분위");
        drawLegendItem(graphics, 575, COMBINED_LEGEND_Y, new Color(236, 167, 50), "5백분위");
        drawLegendItem(graphics, 710, COMBINED_LEGEND_Y, new Color(225, 72, 77), "고객 측정값");
    }

    private int combinedX(int index, int size) {
        int chartWidth = WIDTH - LEFT - RIGHT;
        return size == 1 ? LEFT : LEFT + chartWidth * index / (size - 1);
    }

    private int combinedY(
            double value,
            int top,
            int panelHeight,
            double minValue,
            double maxValue
    ) {
        double ratio = (value - minValue) / (maxValue - minValue);
        return top + panelHeight - (int) Math.round(panelHeight * ratio);
    }

    private String formatCompactAgeMonth(int ageMonth) {
        int years = ageMonth / 12;
        int months = ageMonth % 12;
        if (years == 0) {
            return ageMonth + "개월";
        }
        if (months == 0) {
            return years + "세";
        }
        return years + "세" + months + "개월";
    }

    @SuppressWarnings("unused")
    public String createHeightChart(List<GrowthStandardDto> standards) {
        return createChart(
                standards,
                "키 성장 백분위 곡선",
                "cm",
                GrowthStandardDto::getHeightP5,
                GrowthStandardDto::getHeightP50,
                GrowthStandardDto::getHeightP95,
                GrowthStandardDto::getChildHeight
        );
    }

    @SuppressWarnings("unused")
    public String createWeightChart(List<GrowthStandardDto> standards) {
        return createChart(
                standards,
                "몸무게 성장 백분위 곡선",
                "kg",
                GrowthStandardDto::getWeightP5,
                GrowthStandardDto::getWeightP50,
                GrowthStandardDto::getWeightP95,
                GrowthStandardDto::getChildWeight
        );
    }

    private String createChart(
            List<GrowthStandardDto> standards,
            String title,
            String unit,
            Function<GrowthStandardDto, BigDecimal> p5Value,
            Function<GrowthStandardDto, BigDecimal> p50Value,
            Function<GrowthStandardDto, BigDecimal> p95Value,
            Function<GrowthStandardDto, BigDecimal> childValue
    ) {
        if (standards == null || standards.size() < 2) {
            return null;
        }

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            configure(graphics);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            double minValue = standards.stream()
                    .map(p5Value)
                    .mapToDouble(BigDecimal::doubleValue)
                    .min()
                    .orElse(0);
            double maxValue = standards.stream()
                    .map(p95Value)
                    .mapToDouble(BigDecimal::doubleValue)
                    .max()
                    .orElse(1);

            for (GrowthStandardDto standard : standards) {
                BigDecimal child = childValue.apply(standard);
                if (child != null) {
                    minValue = Math.min(minValue, child.doubleValue());
                    maxValue = Math.max(maxValue, child.doubleValue());
                }
            }

            double padding = Math.max((maxValue - minValue) * 0.15, 2);
            minValue = Math.max(0, minValue - padding);
            maxValue += padding;

            drawTitleAndAxes(graphics, title, unit, standards, minValue, maxValue);
            drawReferenceArea(graphics, standards, p5Value, p95Value, minValue, maxValue);
            drawLine(graphics, standards, p5Value, minValue, maxValue,
                    new Color(246, 184, 74), dashedStroke());
            drawLine(graphics, standards, p50Value, minValue, maxValue,
                    new Color(93, 123, 239), solidStroke());
            drawLine(graphics, standards, p95Value, minValue, maxValue,
                    new Color(78, 190, 145), dashedStroke());
            drawCurveLabels(graphics, standards, p5Value, p50Value, p95Value, minValue, maxValue);
            drawChildPoint(graphics, standards, childValue, minValue, maxValue, unit);
            drawLegend(graphics);

            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", output);
                return "data:image/png;base64,"
                        + Base64.getEncoder().encodeToString(output.toByteArray());
            }
        } catch (Exception e) {
            throw new BaseException(500, "성장 그래프 생성에 실패했습니다.");
        } finally {
            graphics.dispose();
        }
    }

    private void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
    }

    private void drawTitleAndAxes(
            Graphics2D graphics,
            String title,
            String unit,
            List<GrowthStandardDto> standards,
            double minValue,
            double maxValue
    ) {
        int chartWidth = WIDTH - LEFT - RIGHT;
        int chartHeight = HEIGHT - TOP - BOTTOM;

        graphics.setFont(new Font("SansSerif", Font.BOLD, 23));
        graphics.setColor(new Color(37, 48, 71));
        graphics.drawString(title, LEFT, 31);

        graphics.setColor(new Color(239, 246, 255));
        graphics.fillRoundRect(WIDTH - RIGHT - 58, 10, 58, 30, 14, 14);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 15));
        graphics.setColor(new Color(49, 86, 152));
        graphics.drawString(unit, WIDTH - RIGHT - 39, 31);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 14));
        graphics.setColor(new Color(105, 114, 132));
        graphics.drawString("연령", WIDTH - RIGHT - 2, HEIGHT - 42);

        graphics.setStroke(new BasicStroke(1f));
        for (int index = 0; index <= 4; index++) {
            int y = TOP + chartHeight * index / 4;
            graphics.setColor(new Color(229, 234, 241));
            graphics.drawLine(LEFT, y, LEFT + chartWidth, y);

            double value = maxValue - (maxValue - minValue) * index / 4;
            graphics.setColor(new Color(112, 121, 138));
            String tick = formatValue(value);
            int tickWidth = graphics.getFontMetrics().stringWidth(tick);
            graphics.drawString(tick, LEFT - tickWidth - 14, y + 5);
        }

        for (int index = 0; index < standards.size(); index++) {
            int x = x(index, standards.size());
            graphics.setColor(new Color(237, 240, 245));
            graphics.drawLine(x, TOP, x, TOP + chartHeight);
            graphics.setColor(new Color(112, 121, 138));
            String age = formatAgeMonth(standards.get(index).getAgeMonth());
            graphics.drawString(age, x - graphics.getFontMetrics().stringWidth(age) / 2, HEIGHT - 42);
        }

        graphics.setColor(new Color(152, 160, 174));
        graphics.drawLine(LEFT, TOP, LEFT, TOP + chartHeight);
        graphics.drawLine(LEFT, TOP + chartHeight, LEFT + chartWidth, TOP + chartHeight);
    }

    private void drawReferenceArea(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> lowerValue,
            Function<GrowthStandardDto, BigDecimal> upperValue,
            double minValue,
            double maxValue
    ) {
        Path2D area = new Path2D.Double();
        for (int index = 0; index < standards.size(); index++) {
            double x = x(index, standards.size());
            double y = y(upperValue.apply(standards.get(index)).doubleValue(), minValue, maxValue);
            if (index == 0) {
                area.moveTo(x, y);
            } else {
                area.lineTo(x, y);
            }
        }
        for (int index = standards.size() - 1; index >= 0; index--) {
            double x = x(index, standards.size());
            double y = y(lowerValue.apply(standards.get(index)).doubleValue(), minValue, maxValue);
            area.lineTo(x, y);
        }
        area.closePath();

        graphics.setColor(new Color(229, 237, 250));
        graphics.fill(area);
    }

    private void drawLine(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> value,
            double minValue,
            double maxValue,
            Color color,
            BasicStroke stroke
    ) {
        Path2D line = new Path2D.Double();
        for (int index = 0; index < standards.size(); index++) {
            double pointX = x(index, standards.size());
            double pointY = y(value.apply(standards.get(index)).doubleValue(), minValue, maxValue);
            if (index == 0) {
                line.moveTo(pointX, pointY);
            } else {
                line.lineTo(pointX, pointY);
            }
        }
        graphics.setColor(color);
        graphics.setStroke(stroke);
        graphics.draw(line);

        if (!(stroke.getDashArray() == null)) {
            return;
        }

        for (int index = 0; index < standards.size(); index++) {
            int pointX = x(index, standards.size());
            int pointY = y(value.apply(standards.get(index)).doubleValue(), minValue, maxValue);
            graphics.fillOval(pointX - 3, pointY - 3, 6, 6);
        }
    }

    private void drawCurveLabels(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> p5Value,
            Function<GrowthStandardDto, BigDecimal> p50Value,
            Function<GrowthStandardDto, BigDecimal> p95Value,
            double minValue,
            double maxValue
    ) {
        GrowthStandardDto last = standards.getLast();
        int labelX = WIDTH - RIGHT + 9;
        graphics.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawCurveLabel(graphics, "P95", labelX,
                y(p95Value.apply(last).doubleValue(), minValue, maxValue) + 4,
                new Color(54, 164, 120));
        drawCurveLabel(graphics, "P50", labelX,
                y(p50Value.apply(last).doubleValue(), minValue, maxValue) + 4,
                new Color(75, 105, 220));
        drawCurveLabel(graphics, "P5", labelX,
                y(p5Value.apply(last).doubleValue(), minValue, maxValue) + 4,
                new Color(211, 143, 23));
    }

    private void drawCurveLabel(Graphics2D graphics, String label, int x, int y, Color color) {
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }

    private void drawChildPoint(
            Graphics2D graphics,
            List<GrowthStandardDto> standards,
            Function<GrowthStandardDto, BigDecimal> value,
            double minValue,
            double maxValue,
            String unit
    ) {
        for (int index = 0; index < standards.size(); index++) {
            BigDecimal child = value.apply(standards.get(index));
            if (child == null) {
                continue;
            }

            int pointX = x(index, standards.size());
            int pointY = y(child.doubleValue(), minValue, maxValue);
            int chartHeight = HEIGHT - TOP - BOTTOM;

            graphics.setColor(new Color(225, 72, 77, 55));
            graphics.fillRect(pointX - 1, TOP, 3, chartHeight);
            graphics.setColor(new Color(225, 72, 77));
            graphics.fillOval(pointX - 8, pointY - 8, 16, 16);

            String label = child.stripTrailingZeros().toPlainString() + unit;
            graphics.setFont(new Font("SansSerif", Font.BOLD, 15));
            int labelWidth = graphics.getFontMetrics().stringWidth(label);
            int boxX = Math.min(pointX + 12, WIDTH - RIGHT - labelWidth - 18);
            int boxY = Math.max(TOP + 5, pointY - 31);
            graphics.setColor(new Color(255, 242, 243));
            graphics.fillRoundRect(boxX, boxY, labelWidth + 14, 25, 10, 10);
            graphics.setColor(new Color(194, 48, 54));
            graphics.drawString(label, boxX + 7, boxY + 18);
        }
    }

    private void drawLegend(Graphics2D graphics) {
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 14));
        int y = HEIGHT - 12;
        drawLegendItem(graphics, 265, y, new Color(78, 190, 145), "95백분위");
        drawLegendItem(graphics, 415, y, new Color(93, 123, 239), "50백분위");
        drawLegendItem(graphics, 565, y, new Color(246, 184, 74), "5백분위");
        drawLegendItem(graphics, 705, y, new Color(225, 72, 77), "고객 측정값");
    }

    private void drawLegendItem(Graphics2D graphics, int x, int y, Color color, String label) {
        graphics.setColor(color);
        graphics.fillRect(x, y - 10, 22, 3);
        graphics.setColor(new Color(92, 101, 118));
        graphics.drawString(label, x + 28, y - 3);
    }

    private int x(int index, int size) {
        int chartWidth = WIDTH - LEFT - RIGHT;
        return size == 1 ? LEFT : LEFT + chartWidth * index / (size - 1);
    }

    private int y(double value, double minValue, double maxValue) {
        int chartHeight = HEIGHT - TOP - BOTTOM;
        double ratio = (value - minValue) / (maxValue - minValue);
        return TOP + chartHeight - (int) Math.round(chartHeight * ratio);
    }

    private String formatValue(double value) {
        return Math.abs(value - Math.rint(value)) < 0.05
                ? String.format("%.0f", value)
                : String.format("%.1f", value);
    }

    private String formatAgeMonth(int ageMonth) {
        int years = ageMonth / 12;
        int months = ageMonth % 12;
        if (months == 0) {
            return years + "세";
        }
        return years + "세" + months + "개월";
    }

    private BasicStroke solidStroke() {
        return new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private BasicStroke dashedStroke() {
        return new BasicStroke(
                2f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                1f,
                new float[]{8f, 6f},
                0f
        );
    }
}
