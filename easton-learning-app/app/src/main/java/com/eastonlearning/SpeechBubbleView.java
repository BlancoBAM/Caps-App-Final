package com.eastonlearning;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Custom Speech Bubble with left-facing tail pointing toward Capybara's mouth.
 * Fully optimized with pre-allocated paths and density-scaled typography (sp/dp).
 */
public class SpeechBubbleView extends View {

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    // Reusable drawing objects (Zero-allocation onDraw)
    private final RectF cardRectTemp = new RectF();
    private final Path bubblePathTemp = new Path();

    private String currentText = "";
    private float bubbleScale = 1f;
    private ValueAnimator scaleAnimator;

    // Layout cache to prevent re-allocating StaticLayout every frame
    private StaticLayout cachedLayout = null;
    private String lastLayoutText = "";
    private int lastLayoutWidth = 0;
    private float lastTextSize = 0f;

    public SpeechBubbleView(Context context) {
        super(context);
        init();
    }

    public SpeechBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeechBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bubblePaint.setColor(Color.WHITE);
        bubblePaint.setStyle(Paint.Style.FILL);

        strokePaint.setColor(Color.parseColor("#38BDF8")); // friendly sky blue
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpToPx(2.5f));
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        textPaint.setColor(Color.parseColor("#14532D")); // readable forest green
        textPaint.setTextSize(spToPx(16f));
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * Show new speech text with gentle bounce animation.
     */
    public void showText(String text) {
        if (text == null) text = "";
        this.currentText = text;
        this.cachedLayout = null; // Invalidate cached layout

        if (scaleAnimator != null) scaleAnimator.cancel();
        bubbleScale = 0.90f;
        scaleAnimator = ValueAnimator.ofFloat(0.90f, 1f);
        scaleAnimator.setDuration(220);
        scaleAnimator.setInterpolator(new OvershootInterpolator(1.8f));
        scaleAnimator.addUpdateListener(anim -> {
            bubbleScale = (float) anim.getAnimatedValue();
            invalidate();
        });
        scaleAnimator.start();

        invalidate();
    }

    public void hideText() {
        this.currentText = "";
        this.cachedLayout = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.save();
        canvas.scale(bubbleScale, bubbleScale, w * 0.5f, h * 0.5f);

        // ── 1. Bubble Body & Left Pointer Tail ────────────────────────────────
        float tailTipX = dpToPx(3f); // points to the left towards Capybara's mouth
        float tailBaseX = dpToPx(14f);
        float tailY = h * 0.5f;

        cardRectTemp.set(tailBaseX, dpToPx(3f), w - dpToPx(4f), h - dpToPx(4f));

        bubblePathTemp.reset();
        bubblePathTemp.addRoundRect(cardRectTemp, dpToPx(12f), dpToPx(12f), Path.Direction.CW);

        // Tail pointing left
        bubblePathTemp.moveTo(tailBaseX + dpToPx(1.5f), tailY - dpToPx(7f));
        bubblePathTemp.lineTo(tailTipX, tailY);
        bubblePathTemp.lineTo(tailBaseX + dpToPx(1.5f), tailY + dpToPx(7f));

        // Draw fill and outline
        canvas.drawPath(bubblePathTemp, bubblePaint);
        canvas.drawPath(bubblePathTemp, strokePaint);

        // ── 2. Word-Wrapped Text ──────────────────────────────────────────────
        if (!currentText.isEmpty()) {
            int availableWidth = (int) (cardRectTemp.width() - dpToPx(20f));
            if (availableWidth > 40) {
                // Determine responsive text size in sp
                float targetSp;
                if (currentText.length() > 65) {
                    targetSp = 13.5f;
                } else if (currentText.length() > 35) {
                    targetSp = 15f;
                } else {
                    targetSp = 17f;
                }
                float targetPx = spToPx(targetSp);

                if (cachedLayout == null || !currentText.equals(lastLayoutText)
                        || availableWidth != lastLayoutWidth || Math.abs(targetPx - lastTextSize) > 0.5f) {
                    textPaint.setTextSize(targetPx);
                    lastLayoutText = currentText;
                    lastLayoutWidth = availableWidth;
                    lastTextSize = targetPx;

                    cachedLayout = new StaticLayout(
                            currentText,
                            textPaint,
                            availableWidth,
                            Layout.Alignment.ALIGN_NORMAL,
                            1.15f,
                            0f,
                            false
                    );
                }

                canvas.save();
                float textX = cardRectTemp.left + dpToPx(10f);
                float textY = Math.max(cardRectTemp.top + dpToPx(4f), cardRectTemp.centerY() - (cachedLayout.getHeight() / 2f));
                canvas.translate(textX, textY);
                cachedLayout.draw(canvas);
                canvas.restore();
            }
        }

        canvas.restore();
    }
}