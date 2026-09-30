package com.eastonlearning;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Custom animated Capybara mascot for Easton Learning App.
 *
 * Visually angled facing toward the RIGHT (where the speech bubble connects to its mouth).
 * Features:
 * - Syllable-accurate mouth animation: opens and closes in sync with words/syllables
 * - Eyes blink naturally every few seconds
 * - Gentle breathing / idle animation
 * - Signature cute orange/yuzu on head
 */
public class CapybaraView extends View {

    // ── Paints ────────────────────────────────────────────────────────────────
    private final Paint bodyPaint       = makePaint(Color.parseColor("#BA7518"), Paint.Style.FILL);
    private final Paint bodyDarkPaint   = makePaint(Color.parseColor("#92520B"), Paint.Style.FILL);
    private final Paint bellyPaint      = makePaint(Color.parseColor("#E0A352"), Paint.Style.FILL);
    private final Paint snoutPaint      = makePaint(Color.parseColor("#D08D3E"), Paint.Style.FILL);
    private final Paint eyePaint        = makePaint(Color.parseColor("#1E1B18"), Paint.Style.FILL);
    private final Paint eyeShine        = makePaint(Color.WHITE, Paint.Style.FILL);
    private final Paint nosePaint       = makePaint(Color.parseColor("#4A2609"), Paint.Style.FILL);
    private final Paint mouthStroke     = makePaint(Color.parseColor("#4A2609"), Paint.Style.STROKE);
    private final Paint innerMouthPaint = makePaint(Color.parseColor("#881337"), Paint.Style.FILL);
    private final Paint tonguePaint     = makePaint(Color.parseColor("#FB7185"), Paint.Style.FILL);
    private final Paint teethPaint      = makePaint(Color.parseColor("#FFFFFF"), Paint.Style.FILL);
    private final Paint blushPaint      = makePaint(Color.parseColor("#FCA5A5"), Paint.Style.FILL);
    private final Paint orangeFruitPaint= makePaint(Color.parseColor("#F97316"), Paint.Style.FILL);
    private final Paint leafPaint       = makePaint(Color.parseColor("#22C55E"), Paint.Style.FILL);

    // ── State ─────────────────────────────────────────────────────────────────
    /** 0 = mouth fully closed smile, 1 = mouth open */
    private float mouthOpenFraction = 0f;
    /** 0 = eyes wide open, 1 = eyes shut (blink) */
    private float blinkFraction = 0f;
    private float breathScale = 1f;

    private boolean isTalking = false;
    private ValueAnimator talkAnimator;
    private ValueAnimator breathAnimator;
    private ValueAnimator blinkAnimator;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            triggerBlink();
            handler.postDelayed(this, 3500 + (long) (Math.random() * 2000));
        }
    };

    public CapybaraView(Context context) {
        super(context);
        init();
    }

    public CapybaraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CapybaraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mouthStroke.setStrokeWidth(5f);
        mouthStroke.setStrokeCap(Paint.Cap.ROUND);
        mouthStroke.setStrokeJoin(Paint.Join.ROUND);
        blushPaint.setAlpha(120);

        startBreathing();
        handler.postDelayed(blinkRunnable, 2500);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Animate mouth for an exact number of syllables.
     * E.g., for 1 syllable ("A", "dog"), the mouth opens and closes ONCE.
     */
    public void startTalking(int syllables) {
        if (syllables <= 0) syllables = 1;
        isTalking = true;

        if (talkAnimator != null) {
            talkAnimator.cancel();
        }

        talkAnimator = ValueAnimator.ofFloat(0f, 1f);
        talkAnimator.setDuration(200); // 200ms open, 200ms close = 400ms per syllable
        // In reverse mode, 1 cycle (open + close) = repeatCount 1
        talkAnimator.setRepeatCount((syllables * 2) - 1);
        talkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        talkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        talkAnimator.addUpdateListener(anim -> {
            mouthOpenFraction = (float) anim.getAnimatedValue();
            invalidate();
        });
        talkAnimator.start();
    }

    /**
     * Start continuous natural mouth talking for long sentences.
     */
    public void startTalkingContinuous() {
        isTalking = true;
        if (talkAnimator != null && talkAnimator.isRunning()) return;

        talkAnimator = ValueAnimator.ofFloat(0f, 1f);
        talkAnimator.setDuration(220); // natural conversational cadence
        talkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        talkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        talkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        talkAnimator.addUpdateListener(anim -> {
            mouthOpenFraction = (float) anim.getAnimatedValue();
            invalidate();
        });
        talkAnimator.start();
    }

    /**
     * Default start talking (1 syllable).
     */
    public void startTalking() {
        startTalking(1);
    }

    /**
     * Smoothly stop mouth talking and return to smiling.
     */
    public void stopTalking() {
        isTalking = false;
        if (talkAnimator != null) {
            talkAnimator.cancel();
            talkAnimator = null;
        }

        ValueAnimator close = ValueAnimator.ofFloat(mouthOpenFraction, 0f);
        close.setDuration(120);
        close.addUpdateListener(anim -> {
            mouthOpenFraction = (float) anim.getAnimatedValue();
            invalidate();
        });
        close.start();
    }

    public boolean isTalking() {
        return isTalking;
    }

    // ── Breathing & Blinking ──────────────────────────────────────────────────

    private void startBreathing() {
        breathAnimator = ValueAnimator.ofFloat(1f, 1.03f);
        breathAnimator.setDuration(1800);
        breathAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breathAnimator.addUpdateListener(anim -> {
            breathScale = (float) anim.getAnimatedValue();
            invalidate();
        });
        breathAnimator.start();
    }

    private void triggerBlink() {
        if (blinkAnimator != null && blinkAnimator.isRunning()) return;

        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        blinkAnimator.setDuration(220);
        blinkAnimator.addUpdateListener(anim -> {
            blinkFraction = (float) anim.getAnimatedValue();
            invalidate();
        });
        blinkAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(blinkRunnable);
        if (talkAnimator != null) talkAnimator.cancel();
        if (breathAnimator != null) breathAnimator.cancel();
        if (blinkAnimator != null) blinkAnimator.cancel();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w * 0.46f;
        float cy = h * 0.54f;
        float size = Math.min(w, h);

        canvas.save();
        canvas.scale(breathScale, breathScale, cx, cy);

        drawRightFacingCapybara(canvas, cx, cy, size);

        canvas.restore();
    }

    /**
     * Draws the Capybara in 3/4 view facing toward the RIGHT side (facing speech bubble).
     */
    private void drawRightFacingCapybara(Canvas canvas, float cx, float cy, float size) {
        float s = size / 200f; // reference scale

        // ── 1. Back Ear (behind head) ─────────────────────────────────────────
        float backEarX = cx - 18 * s;
        float backEarY = cy - 58 * s;
        canvas.drawOval(new RectF(backEarX - 9 * s, backEarY - 14 * s, backEarX + 9 * s, backEarY + 14 * s), bodyDarkPaint);

        // ── 2. Plump Body ─────────────────────────────────────────────────────
        float bodyL = cx - 64 * s;
        float bodyT = cy - 12 * s;
        float bodyR = cx + 38 * s;
        float bodyB = cy + 70 * s;
        RectF bodyRect = new RectF(bodyL, bodyT, bodyR, bodyB);
        canvas.drawRoundRect(bodyRect, 38 * s, 34 * s, bodyPaint);

        // Tummy patch
        RectF bellyRect = new RectF(cx - 30 * s, cy + 10 * s, cx + 24 * s, cy + 62 * s);
        canvas.drawRoundRect(bellyRect, 24 * s, 24 * s, bellyPaint);

        // ── 3. Feet & Paws ────────────────────────────────────────────────────
        canvas.drawRoundRect(new RectF(cx - 58 * s, cy + 58 * s, cx - 28 * s, cy + 74 * s), 8 * s, 8 * s, bodyDarkPaint);
        canvas.drawRoundRect(new RectF(cx - 14 * s, cy + 58 * s, cx + 14 * s, cy + 74 * s), 8 * s, 8 * s, bodyPaint);
        canvas.drawRoundRect(new RectF(cx + 18 * s, cy + 58 * s, cx + 46 * s, cy + 74 * s), 8 * s, 8 * s, bodyPaint);

        // ── 4. Head & Snout (Facing Right) ────────────────────────────────────
        float headCx = cx + 2 * s;
        float headCy = cy - 32 * s;
        RectF headRect = new RectF(headCx - 36 * s, headCy - 28 * s, headCx + 34 * s, headCy + 28 * s);
        canvas.drawRoundRect(headRect, 24 * s, 24 * s, bodyPaint);

        // Snout extending right
        float snoutL = headCx - 4 * s;
        float snoutT = headCy - 12 * s;
        float snoutR = headCx + 56 * s;
        float snoutB = headCy + 26 * s;
        RectF snoutRect = new RectF(snoutL, snoutT, snoutR, snoutB);
        canvas.drawRoundRect(snoutRect, 18 * s, 18 * s, snoutPaint);

        // Cheek blush
        canvas.drawCircle(headCx + 8 * s, headCy + 8 * s, 10 * s, blushPaint);

        // ── 5. Front Ear ──────────────────────────────────────────────────────
        float frontEarX = headCx - 6 * s;
        float frontEarY = headCy - 28 * s;
        canvas.drawOval(new RectF(frontEarX - 11 * s, frontEarY - 15 * s, frontEarX + 11 * s, frontEarY + 15 * s), bodyPaint);
        canvas.drawOval(new RectF(frontEarX - 6 * s, frontEarY - 10 * s, frontEarX + 6 * s, frontEarY + 10 * s), bodyDarkPaint);

        // ── 6. Eyes (Looking Right towards bubble) ─────────────────────────────
        float eyeY = headCy - 10 * s;
        drawEye(canvas, headCx - 14 * s, eyeY, 5.5f * s, s);
        drawEye(canvas, headCx + 16 * s, eyeY, 7.5f * s, s);

        // ── 7. Nose (Front of snout, facing right) ─────────────────────────────
        float noseX = snoutR - 8 * s;
        float noseY = snoutT + 6 * s;
        RectF noseRect = new RectF(noseX - 6 * s, noseY - 4 * s, noseX + 6 * s, noseY + 5 * s);
        canvas.drawRoundRect(noseRect, 4 * s, 4 * s, nosePaint);

        // ── 8. Mouth (Animated, facing right) ─────────────────────────────────
        float mouthStartX = snoutL + 16 * s;
        float mouthTipX   = snoutR - 4 * s;
        float mouthBaseY  = snoutB - 6 * s;

        drawRightMouth(canvas, mouthStartX, mouthTipX, mouthBaseY, s);

        // ── 9. Cute Orange/Yuzu with Leaf on Head ─────────────────────────────
        float orangeX = headCx - 2 * s;
        float orangeY = headCy - 38 * s;
        float orangeR = 12 * s;
        canvas.drawCircle(orangeX, orangeY, orangeR, orangeFruitPaint);

        // Leaf
        Path leaf = new Path();
        leaf.moveTo(orangeX, orangeY - orangeR);
        leaf.quadTo(orangeX + 8 * s, orangeY - orangeR - 12 * s, orangeX + 14 * s, orangeY - orangeR - 6 * s);
        leaf.quadTo(orangeX + 6 * s, orangeY - orangeR - 2 * s, orangeX, orangeY - orangeR);
        leaf.close();
        canvas.drawPath(leaf, leafPaint);
    }

    private void drawEye(Canvas canvas, float cx, float cy, float r, float s) {
        if (blinkFraction > 0.4f) {
            Path blinkArc = new Path();
            blinkArc.moveTo(cx - r, cy);
            blinkArc.quadTo(cx, cy - r * 0.8f, cx + r, cy);
            Paint p = new Paint(mouthStroke);
            p.setStrokeWidth(3.5f * s);
            canvas.drawPath(blinkArc, p);
        } else {
            canvas.drawCircle(cx, cy, r, eyePaint);
            canvas.drawCircle(cx + r * 0.3f, cy - r * 0.3f, r * 0.35f, eyeShine);
            canvas.drawCircle(cx - r * 0.2f, cy + r * 0.3f, r * 0.16f, eyeShine);
        }
    }

    /**
     * Draws mouth on right side of snout with proportional, natural opening/closing.
     */
    private void drawRightMouth(Canvas canvas, float startX, float endX, float baseY, float s) {
        float maxDrop = 13f * s; // natural, gentle jaw opening
        float open = mouthOpenFraction * maxDrop;

        if (open < 1.2f) {
            // Closed friendly smile
            Path smile = new Path();
            smile.moveTo(startX, baseY - 2 * s);
            smile.quadTo((startX + endX) * 0.5f, baseY + 5 * s, endX, baseY);
            canvas.drawPath(smile, mouthStroke);
        } else {
            // Open mouth: cavity, tongue, teeth
            float mouthTop = baseY;
            float mouthBottom = baseY + open;
            RectF mouthCavity = new RectF(startX + 4 * s, mouthTop, endX + 2 * s, mouthBottom);

            // 1. Dark cavity
            canvas.drawRoundRect(mouthCavity, 6 * s, 6 * s, innerMouthPaint);

            // 2. Cute top teeth
            float toothH = Math.min(5 * s, open * 0.35f);
            RectF teethRect = new RectF(mouthCavity.centerX() - 5 * s, mouthTop, mouthCavity.right - 2 * s, mouthTop + toothH);
            canvas.drawRoundRect(teethRect, 2 * s, 2 * s, teethPaint);

            // 3. Pink tongue at bottom
            float tongueH = open * 0.4f;
            RectF tongueRect = new RectF(mouthCavity.left + 3 * s, mouthBottom - tongueH, mouthCavity.right - 2 * s, mouthBottom);
            canvas.drawRoundRect(tongueRect, 5 * s, 5 * s, tonguePaint);

            // 4. Outer stroke
            canvas.drawRoundRect(mouthCavity, 6 * s, 6 * s, mouthStroke);
        }
    }

    private static Paint makePaint(int color, Paint.Style style) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }
}
