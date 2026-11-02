package com.arif.smartordermanagement.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import com.arif.smartordermanagement.R;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.arif.smartordermanagement.model.SalesDataPoint;
import com.arif.smartordermanagement.model.SalesTrend;

import java.util.Collections;
import java.util.List;

public class SalesBarChartView extends View {

    private List<SalesDataPoint> points = Collections.emptyList();
    private int selectedIndex = -1;

    private Paint barPaint;
    private Paint barTrackPaint;
    private Paint textPaint;
    private Paint activeTextPaint;

    public SalesBarChartView(Context context) {
        super(context);
        init();
    }

    public SalesBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SalesBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(getContext().getColor(R.color.google_blue));

        barTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barTrackPaint.setColor(getContext().getColor(R.color.google_blue_container));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getContext().getColor(R.color.on_surface_variant_light));
        textPaint.setTextSize(spToPx(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        activeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeTextPaint.setColor(getContext().getColor(R.color.google_blue));
        activeTextPaint.setTextSize(spToPx(12));
        activeTextPaint.setFakeBoldText(true);
        activeTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setSalesTrend(SalesTrend trend) {
        if (trend != null && trend.getPoints() != null) {
            this.points = trend.getPoints();
            this.selectedIndex = -1;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();

        float labelHeight = dpToPx(20);
        float chartTop = dpToPx(18);
        float chartBottom = height - labelHeight;
        float chartHeight = chartBottom - chartTop;

        double maxAmount = 1.0;
        for (SalesDataPoint pt : points) {
            if (pt.getAmount() > maxAmount) {
                maxAmount = pt.getAmount();
            }
        }

        int count = points.size();
        float columnWidth = (float) width / count;
        float barWidth = dpToPx(18);

        for (int i = 0; i < count; i++) {
            SalesDataPoint point = points.get(i);
            float centerX = columnWidth * i + columnWidth / 2f;
            float left = centerX - barWidth / 2f;
            float right = centerX + barWidth / 2f;

            float ratio = (float) (point.getAmount() / maxAmount);
            if (ratio < 0.08f) ratio = 0.08f;

            float barTop = chartBottom - (chartHeight * ratio);

            // Draw track
            RectF trackRect = new RectF(left, chartTop, right, chartBottom);
            canvas.drawRoundRect(trackRect, dpToPx(6), dpToPx(6), barTrackPaint);

            // Draw active bar
            RectF barRect = new RectF(left, barTop, right, chartBottom);
            boolean isSelected = (i == selectedIndex);
            barPaint.setColor(getContext().getColor(
                    isSelected ? R.color.google_blue : R.color.chart_bar_inactive));
            canvas.drawRoundRect(barRect, dpToPx(6), dpToPx(6), barPaint);

            // Draw label
            canvas.drawText(point.getLabel(), centerX, height - dpToPx(4), isSelected ? activeTextPaint : textPaint);

            // Draw value tooltip if selected
            if (isSelected) {
                String valueStr = "৳" + (int) point.getAmount();
                canvas.drawText(valueStr, centerX, barTop - dpToPx(4), activeTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !points.isEmpty()) {
            float x = event.getX();
            float columnWidth = (float) getWidth() / points.size();
            int index = (int) (x / columnWidth);
            if (index >= 0 && index < points.size()) {
                selectedIndex = (selectedIndex == index) ? -1 : index;
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
