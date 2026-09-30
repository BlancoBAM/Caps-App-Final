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
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Custom Speech Bubble with left-facing tail pointing toward Capybara's mouth.
 */
public class SpeechBubbleView extends View {

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private String currentText = "";
    private int visibleChars = 0;
    private float bubbleScale = 1f;
    private ValueAnimator scaleAnimator;
    private ValueAnimator typingAnimator;

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

        strokePaint.setColor(Color.parseColor("#38BDF8")); // light friendly sky blue
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        textPaint.setColor(Color.parseColor("#14532D")); // readable forest green
        textPaint.setTextSize(40f);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    /**
     * Show new speech text with gentle bounce animation.
     */
    public void showText(String text) {
        if (text == null) text = "";
        this.currentText = text;
        this.visibleChars = text.length();

        if (scaleAnimator != null) scaleAnimator.cancel();
        bubbleScale = 0.88f;
        scaleAnimator = ValueAnimator.ofFloat(0.88f, 1f);
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
        this.visibleChars = 0;
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

        // ── 1. Draw Bubble Body with Left Tail ────────────────────────────────
        float tailTipX = 6f; // points to the left towards Capybara's mouth
        float tailBaseX = 26f;
        float tailY = h * 0.5f;

        Path bubblePath = new Path();
        RectF cardRect = new RectF(tailBaseX, 6f, w - 8f, h - 8f);
        bubblePath.addRoundRect(cardRect, 18f, 18f, Path.Direction.CW);

        // Tail pointing left
        bubblePath.moveTo(tailBaseX + 2f, tailY - 12f);
        bubblePath.lineTo(tailTipX, tailY);
        bubblePath.lineTo(tailBaseX + 2f, tailY + 12f);

        // Draw fill and outline
        canvas.drawPath(bubblePath, bubblePaint);
        canvas.drawPath(bubblePath, strokePaint);

        // ── 2. Draw Text using StaticLayout (Word-Wrap) ───────────────────────
        if (!currentText.isEmpty()) {
            int availableWidth = (int) (cardRect.width() - 32f);
            if (availableWidth > 50) {
                // Adjust text size based on length
                if (currentText.length() > 60) {
                    textPaint.setTextSize(30f);
                } else if (currentText.length() > 35) {
                    textPaint.setTextSize(34f);
                } else {
                    textPaint.setTextSize(38f);
                }

                StaticLayout layout = new StaticLayout(
                        currentText,
                        textPaint,
                        availableWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.15f,
                        0f,
                        false
                );

                canvas.save();
                float textX = cardRect.left + 16f;
                float textY = Math.max(cardRect.top + 8f, cardRect.centerY() - (layout.getHeight() / 2f));
                canvas.translate(textX, textY);
                layout.draw(canvas);
                canvas.restore();
            }
        }

        canvas.restore();
    }
}