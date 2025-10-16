package com.eastonlearning;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;

public class SpeechBubbleView extends View {
    private Paint bubblePaint;
    private Paint strokePaint;
    private Paint textPaint;
    private String currentText = "";
    private int visibleCharCount = 0;
    private ValueAnimator textAnimator;
    private float bubbleScale = 0f;
    
    public SpeechBubbleView(Context context) {
        super(context);
        init();
    }
    
    public SpeechBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(Color.WHITE);
        bubblePaint.setShadowLayer(4, 2, 2, Color.GRAY);
        
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.parseColor("#2196F3"));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#2E7D32"));
        textPaint.setTextSize(48);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    
    public void showText(String text) {
        currentText = text;
        visibleCharCount = 0;
        
        // Animate bubble appearance
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(0f, 1f);
        scaleAnimator.setDuration(300);
        scaleAnimator.setInterpolator(new BounceInterpolator());
        scaleAnimator.addUpdateListener(animation -> {
            bubbleScale = (Float) animation.getAnimatedValue();
            invalidate();
        });
        scaleAnimator.start();
        
        // Animate text typing effect
        if (textAnimator != null) {
            textAnimator.cancel();
        }
        
        textAnimator = ValueAnimator.ofInt(0, text.length());
        textAnimator.setDuration(text.length() * 50); // 50ms per character
        textAnimator.addUpdateListener(animation -> {
            visibleCharCount = (Integer) animation.getAnimatedValue();
            invalidate();
        });
        textAnimator.setStartDelay(300); // Wait for bubble to appear
        textAnimator.start();
    }
    
    public void hideText() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(bubbleScale, 0f);
        scaleAnimator.setDuration(200);
        scaleAnimator.addUpdateListener(animation -> {
            bubbleScale = (Float) animation.getAnimatedValue();
            invalidate();
        });
        scaleAnimator.start();
        
        currentText = "";
        visibleCharCount = 0;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (bubbleScale <= 0 || currentText.isEmpty()) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        
        canvas.save();
        canvas.scale(bubbleScale, bubbleScale, width / 2f, height / 2f);
        
        // Draw speech bubble with tail
        Path bubblePath = new Path();
        RectF bubbleRect = new RectF(40, 20, width - 40, height - 60);
        bubblePath.addRoundRect(bubbleRect, 30, 30, Path.Direction.CW);
        
        // Add tail pointing down-left (towards capybara's mouth)
        float tailX = bubbleRect.left + 60;
        float tailY = bubbleRect.bottom;
        bubblePath.moveTo(tailX, tailY);
        bubblePath.lineTo(tailX - 30, tailY + 40);
        bubblePath.lineTo(tailX + 20, tailY);
        bubblePath.close();
        
        canvas.drawPath(bubblePath, bubblePaint);
        canvas.drawPath(bubblePath, strokePaint);
        
        // Draw visible portion of text
        if (visibleCharCount > 0) {
            String visibleText = currentText.substring(0, Math.min(visibleCharCount, currentText.length()));
            
            // Word wrap text
            String[] words = visibleText.split(" ");
            float textX = width / 2f;
            float textY = bubbleRect.centerY() - 20;
            float lineHeight = textPaint.getTextSize() + 10;
            
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (textPaint.measureText(testLine) > bubbleRect.width() - 40) {
                    if (currentLine.length() > 0) {
                        canvas.drawText(currentLine.toString(), textX, textY, textPaint);
                        textY += lineHeight;
                        currentLine = new StringBuilder(word);
                    } else {
                        canvas.drawText(word, textX, textY, textPaint);
                        textY += lineHeight;
                    }
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            
            if (currentLine.length() > 0) {
                canvas.drawText(currentLine.toString(), textX, textY, textPaint);
            }
        }
        
        canvas.restore();
    }
}