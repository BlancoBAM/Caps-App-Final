package com.eastonlearning;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Expanded animated Capybara mascot for Easton Learning App — "Caps".
 *
 * Implements the 3-step specification from the animation expansion blueprint:
 *
 * 1. Animation State Machine (matching sample animation clip):
 *    IDLE            — Gentle breathing, subtle lateral sway, occasional blink, hands loosely clasped (loop: true)
 *    SPEAKING        — Expressive talking gestures, open hands, syllable/phoneme synchronized visemes (interruptible: true)
 *    PRAISE          — Clapping/rubbing hands (from sample clip), joyful body bounce, wide smile (loop: false, returnTo: IDLE)
 *    QUESTION        — Inquisitive head tilt, one hand raised, raised eyebrow, curious expression (loop: false, returnTo: IDLE)
 *    GENTLE_GUIDANCE — Reassuring open-palm gesture, sympathetic smile, comforting tilt (loop: false, returnTo: IDLE)
 *
 * 2. Viseme Breakdown for Phoneme Sync:
 *    REST            — Neutral closed lips (M, B, P sounds)
 *    WIDE_OPEN       — Low jaw drop for broad vowels (Ah, A, O)
 *    ROUND_PUCKERED  — Tight rounded lips (Oh/Oo, U, W)
 *    WIDE_SMILE      — Stretched lips (Ee, E, I, S, Z)
 *    TEETH_ON_LIP    — Upper teeth touching lower lip (F, V)
 *    TONGUE_ALVEOLAR — Tongue tip hitting behind upper teeth (L, T, D, N)
 *
 * 3. Performance & Zero-GC onDraw Optimization:
 *    All Path, RectF, and auxiliary Paint instances are pre-allocated to guarantee
 *    smooth 60 FPS animation on budget tablets and prevent GC pauses.
 */
public class CapybaraView extends View {

    // ── Animation States ──────────────────────────────────────────────────────
    public enum AnimState {
        IDLE,
        SPEAKING,
        PRAISE,
        QUESTION,
        GENTLE_GUIDANCE
    }

    // ── 6 Foundational Visemes (Mouth Shapes for Phoneme Sync) ────────────────
    public enum Viseme {
        REST,           // Neutral closed lips (M, B, P)
        WIDE_OPEN,      // Broad open jaw (Ah, A, O)
        ROUND_PUCKERED, // Rounded circular mouth (Oh/Oo, U, W)
        WIDE_SMILE,     // Stretched smile mouth (Ee, E, I, S, Z)
        TEETH_ON_LIP,   // Upper teeth contacting lower lip (F, V)
        TONGUE_ALVEOLAR // Tongue tip raised to upper palate (L, T, D, N)
    }

    // ── Pre-allocated Paints ──────────────────────────────────────────────────
    private final Paint bodyPaint          = makePaint(Color.parseColor("#C8781A"), Paint.Style.FILL);
    private final Paint bodyDarkPaint      = makePaint(Color.parseColor("#9A5510"), Paint.Style.FILL);
    private final Paint bellyPaint         = makePaint(Color.parseColor("#ECC47A"), Paint.Style.FILL);
    private final Paint snoutPaint         = makePaint(Color.parseColor("#D9A05A"), Paint.Style.FILL);
    private final Paint eyePaint           = makePaint(Color.parseColor("#1E1B18"), Paint.Style.FILL);
    private final Paint eyeShine           = makePaint(Color.WHITE, Paint.Style.FILL);
    private final Paint eyeHighlight       = makePaint(Color.parseColor("#AADDFF"), Paint.Style.FILL);
    private final Paint nosePaint          = makePaint(Color.parseColor("#3D1F06"), Paint.Style.FILL);
    private final Paint mouthStroke        = makePaint(Color.parseColor("#3D1F06"), Paint.Style.STROKE);
    private final Paint innerMouthPaint    = makePaint(Color.parseColor("#881337"), Paint.Style.FILL);
    private final Paint tonguePaint        = makePaint(Color.parseColor("#FB7185"), Paint.Style.FILL);
    private final Paint teethPaint         = makePaint(Color.parseColor("#FFFFFF"), Paint.Style.FILL);
    private final Paint blushPaint         = makePaint(Color.parseColor("#FCA5A5"), Paint.Style.FILL);
    private final Paint orangeFruitPaint   = makePaint(Color.parseColor("#F97316"), Paint.Style.FILL);
    private final Paint leafPaint          = makePaint(Color.parseColor("#22C55E"), Paint.Style.FILL);
    private final Paint browPaint          = makePaint(Color.parseColor("#3D1F06"), Paint.Style.FILL);
    private final Paint handPaint          = makePaint(Color.parseColor("#7A3D0A"), Paint.Style.FILL);
    private final Paint handDarkPaint      = makePaint(Color.parseColor("#5A2A05"), Paint.Style.FILL);
    private final Paint armPaint           = makePaint(Color.parseColor("#C8781A"), Paint.Style.FILL);
    private final Paint fingerDetailPaint  = makePaint(Color.parseColor("#5A2A05"), Paint.Style.FILL);
    private final Paint dimplePaint        = makePaint(Color.parseColor("#FCA5A5"), Paint.Style.FILL);
    private final Paint eyeArcPaint        = makePaint(Color.parseColor("#3D1F06"), Paint.Style.STROKE);

    // ── Pre-allocated Reusable Drawing Buffers (Zero-Allocation onDraw) ──────
    private final RectF bodyRectTemp       = new RectF();
    private final RectF bellyRectTemp      = new RectF();
    private final RectF headRectTemp       = new RectF();
    private final RectF snoutRectTemp      = new RectF();
    private final RectF blushRectTemp      = new RectF();
    private final RectF noseRectTemp       = new RectF();
    private final RectF earRectTemp        = new RectF();
    private final RectF footRectTemp       = new RectF();
    private final RectF armRectTemp        = new RectF();
    private final RectF cavityRectTemp     = new RectF();
    private final RectF teethRectTemp      = new RectF();
    private final RectF tongueRectTemp     = new RectF();

    private final Path leftBrowPathTemp    = new Path();
    private final Path rightBrowPathTemp   = new Path();
    private final Path eyeArcPathTemp      = new Path();
    private final Path mouthPathTemp       = new Path();
    private final Path leafPathTemp        = new Path();

    // ── State Variables ───────────────────────────────────────────────────────
    /** 0 = mouth closed, 1 = mouth fully open */
    private float mouthOpenFraction  = 0f;
    /** Current active phoneme viseme */
    private Viseme currentViseme     = Viseme.REST;
    /** 0 = eyes open, 1 = eyes shut (blink) */
    private float blinkFraction      = 0f;
    /** 1.0 = normal, 1.025 = inhale peak */
    private float breathScale        = 1f;
    /** Lateral body sway for idle, -1 to 1 */
    private float swaySide           = 0f;
    /** Body vertical bounce (for PRAISE) */
    private float bounceY            = 0f;
    /** Head tilt angle (radians, for QUESTION/GUIDANCE) */
    private float headTiltAngle      = 0f;
    /** Arm raise fraction: 0 = at side/down, 1 = fully raised */
    private float leftArmRaise       = 0f;
    private float rightArmRaise      = 0f;
    /** Hand open: 0 = clasped/closed, 1 = open/spread */
    private float handOpenFraction   = 0f;
    /** Clapping/rubbing phase oscillation [0..1] used in PRAISE state */
    private float clapPhase          = 0f;
    /** Eyebrow raise: 0 = neutral, 1 = fully raised (curious/excited) */
    private float browRaiseFraction  = 0f;
    /** Eyebrow furrow: 0 = neutral, 1 = furrowed (worry/sympathy) */
    private float browFurrowFraction = 0f;
    /** Smile intensity: 0 = neutral, 1 = wide joyful smile */
    private float smileExtra         = 0f;

    private AnimState currentState = AnimState.IDLE;
    private boolean isTalking = false;

    // ── Animators ─────────────────────────────────────────────────────────────
    private ValueAnimator talkAnimator;
    private ValueAnimator breathAnimator;
    private ValueAnimator blinkAnimator;
    private ValueAnimator swayAnimator;
    private ValueAnimator bounceAnimator;
    private ValueAnimator tiltAnimator;
    private ValueAnimator armAnimator;
    private ValueAnimator clapAnimator;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Blink scheduler
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            triggerBlink();
            handler.postDelayed(this, 3000 + (long) (Math.random() * 2500));
        }
    };

    // Auto-return to IDLE state scheduler
    private final Runnable autoReturnRunnable = () -> {
        if (!isTalking && currentState != AnimState.IDLE) {
            setState(AnimState.IDLE);
        }
    };

    // ── Constructors ──────────────────────────────────────────────────────────
    public CapybaraView(Context context) {
        super(context); init();
    }
    public CapybaraView(Context context, AttributeSet attrs) {
        super(context, attrs); init();
    }
    public CapybaraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        mouthStroke.setStrokeWidth(4f);
        mouthStroke.setStrokeCap(Paint.Cap.ROUND);
        mouthStroke.setStrokeJoin(Paint.Join.ROUND);

        eyeArcPaint.setStrokeWidth(3.5f);
        eyeArcPaint.setStrokeCap(Paint.Cap.ROUND);

        browPaint.setStyle(Paint.Style.STROKE);
        browPaint.setStrokeWidth(3.5f);
        browPaint.setStrokeCap(Paint.Cap.ROUND);

        blushPaint.setAlpha(115);
        fingerDetailPaint.setAlpha(200);

        startIdleAnimations();
        handler.postDelayed(blinkRunnable, 2000);
    }

    // ── Public API: State Machine ─────────────────────────────────────────────

    /**
     * Transition to a specified animation state.
     * Non-looping states (PRAISE, QUESTION, GENTLE_GUIDANCE) will automatically
     * return to IDLE after completing their showcase gesture, unless interrupted.
     */
    public void setState(AnimState state) {
        if (currentState == state) return;
        currentState = state;

        cancelStateAnimators();
        handler.removeCallbacks(autoReturnRunnable);

        switch (state) {
            case IDLE:
                enterIdleState();
                break;
            case SPEAKING:
                enterSpeakingState();
                break;
            case PRAISE:
                enterPraiseState();
                scheduleReturnToIdle(2800);
                break;
            case QUESTION:
                enterQuestionState();
                scheduleReturnToIdle(4000);
                break;
            case GENTLE_GUIDANCE:
                enterGuidanceState();
                scheduleReturnToIdle(3200);
                break;
        }
    }

    public AnimState getState() { return currentState; }

    private void scheduleReturnToIdle(long delayMs) {
        handler.removeCallbacks(autoReturnRunnable);
        handler.postDelayed(autoReturnRunnable, delayMs);
    }

    // ── Viseme & Phoneme Synchronization ──────────────────────────────────────

    /**
     * Maps an alphabet character to its corresponding foundational viseme.
     */
    public static Viseme getVisemeForChar(char c) {
        char lower = Character.toLowerCase(c);
        switch (lower) {
            case 'a':
            case 'o':
                return Viseme.WIDE_OPEN;
            case 'u':
            case 'w':
                return Viseme.ROUND_PUCKERED;
            case 'e':
            case 'i':
            case 's':
            case 'z':
            case 'c':
            case 'k':
            case 'g':
            case 'y':
            case 'x':
                return Viseme.WIDE_SMILE;
            case 'f':
            case 'v':
                return Viseme.TEETH_ON_LIP;
            case 'l':
            case 't':
            case 'd':
            case 'n':
            case 'r':
            case 'j':
                return Viseme.TONGUE_ALVEOLAR;
            case 'm':
            case 'b':
            case 'p':
            default:
                return Viseme.REST;
        }
    }

    /**
     * Sets the active viseme for speech rendering.
     */
    public void setViseme(Viseme viseme) {
        this.currentViseme = viseme;
        invalidate();
    }

    public Viseme getViseme() {
        return currentViseme;
    }

    /**
     * Animates Caps speaking an individual letter with accurate viseme shape.
     */
    public void speakLetter(char letter) {
        setViseme(getVisemeForChar(letter));
        startTalking(1);
    }

    // ── Talking Control ───────────────────────────────────────────────────────

    /**
     * Syllable-accurate mouth open/close animation, N cycles.
     */
    public void startTalking(int syllables) {
        if (syllables <= 0) syllables = 1;
        isTalking = true;
        handler.removeCallbacks(autoReturnRunnable);
        if (talkAnimator != null) talkAnimator.cancel();

        if (currentState == AnimState.IDLE) {
            setState(AnimState.SPEAKING);
        }

        talkAnimator = ValueAnimator.ofFloat(0f, 1f);
        talkAnimator.setDuration(190);
        talkAnimator.setRepeatCount((syllables * 2) - 1);
        talkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        talkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        talkAnimator.addUpdateListener(a -> {
            mouthOpenFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        talkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                smoothCloseMouth();
            }
        });
        talkAnimator.start();
    }

    /**
     * Continuous natural talking for longer dialogue or intro phrases.
     * Cycles through natural viseme variations dynamically.
     */
    public void startTalkingContinuous() {
        isTalking = true;
        handler.removeCallbacks(autoReturnRunnable);
        if (talkAnimator != null && talkAnimator.isRunning()) return;

        if (currentState == AnimState.IDLE) {
            setState(AnimState.SPEAKING);
        }

        final Viseme[] cycle = new Viseme[] {
                Viseme.WIDE_OPEN, Viseme.WIDE_SMILE, Viseme.ROUND_PUCKERED,
                Viseme.TONGUE_ALVEOLAR, Viseme.WIDE_OPEN, Viseme.REST
        };

        talkAnimator = ValueAnimator.ofFloat(0f, 1f);
        talkAnimator.setDuration(210);
        talkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        talkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        talkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        talkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int cycleIdx = 0;
            private boolean wasZero = true;

            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                mouthOpenFraction = (float) a.getAnimatedValue();
                if (mouthOpenFraction < 0.15f && !wasZero) {
                    wasZero = true;
                    cycleIdx = (cycleIdx + 1) % cycle.length;
                    currentViseme = cycle[cycleIdx];
                } else if (mouthOpenFraction > 0.5f) {
                    wasZero = false;
                }
                invalidate();
            }
        });
        talkAnimator.start();
    }

    public void startTalking() {
        startTalking(1);
    }

    /**
     * Stops mouth animation with smooth mouth closing, and returns to IDLE if appropriate.
     */
    public void stopTalking() {
        isTalking = false;
        if (talkAnimator != null) {
            talkAnimator.cancel();
            talkAnimator = null;
        }
        smoothCloseMouth();

        // Return to IDLE body state if in SPEAKING
        if (currentState == AnimState.SPEAKING) {
            scheduleReturnToIdle(500);
        }
    }

    public boolean isTalking() {
        return isTalking;
    }

    // ── Lifecycle Support: Pause / Resume ─────────────────────────────────────

    public void pauseAnimations() {
        if (breathAnimator != null && breathAnimator.isRunning()) breathAnimator.pause();
        if (swayAnimator != null && swayAnimator.isRunning()) swayAnimator.pause();
        if (talkAnimator != null && talkAnimator.isRunning()) talkAnimator.pause();
        if (bounceAnimator != null && bounceAnimator.isRunning()) bounceAnimator.pause();
        if (clapAnimator != null && clapAnimator.isRunning()) clapAnimator.pause();
        handler.removeCallbacks(blinkRunnable);
        handler.removeCallbacks(autoReturnRunnable);
    }

    public void resumeAnimations() {
        if (breathAnimator != null && breathAnimator.isPaused()) breathAnimator.resume();
        if (swayAnimator != null && swayAnimator.isPaused()) swayAnimator.resume();
        if (talkAnimator != null && talkAnimator.isPaused()) talkAnimator.resume();
        if (bounceAnimator != null && bounceAnimator.isPaused()) bounceAnimator.resume();
        if (clapAnimator != null && clapAnimator.isPaused()) clapAnimator.resume();
        handler.postDelayed(blinkRunnable, 2000);
    }

    // ── State Transition Handlers ─────────────────────────────────────────────

    private void enterIdleState() {
        currentViseme = Viseme.REST;
        // Relax arms to center clasp
        animateTo(leftArmRaise, 0f, v -> { leftArmRaise = v; invalidate(); }, 400);
        animateTo(rightArmRaise, 0f, v -> { rightArmRaise = v; invalidate(); }, 400);
        animateTo(handOpenFraction, 0f, v -> { handOpenFraction = v; invalidate(); }, 400);
        animateTo(browRaiseFraction, 0f, v -> { browRaiseFraction = v; invalidate(); }, 300);
        animateTo(browFurrowFraction, 0f, v -> { browFurrowFraction = v; invalidate(); }, 300);
        animateTo(headTiltAngle, 0f, v -> { headTiltAngle = v; invalidate(); }, 350);
        animateTo(smileExtra, 0f, v -> { smileExtra = v; invalidate(); }, 400);
        animateTo(clapPhase, 0f, v -> { clapPhase = v; invalidate(); }, 300);
    }

    private void enterSpeakingState() {
        // Expressive hand gesturing while talking
        animateTo(leftArmRaise, 0.32f, v -> { leftArmRaise = v; invalidate(); }, 300);
        animateTo(rightArmRaise, 0.32f, v -> { rightArmRaise = v; invalidate(); }, 300);
        animateTo(handOpenFraction, 0.75f, v -> { handOpenFraction = v; invalidate(); }, 350);
        animateTo(browRaiseFraction, 0.25f, v -> { browRaiseFraction = v; invalidate(); }, 250);
        animateTo(browFurrowFraction, 0f, v -> { browFurrowFraction = v; invalidate(); }, 250);
        animateTo(headTiltAngle, 0f, v -> { headTiltAngle = v; invalidate(); }, 300);
        animateTo(smileExtra, 0.35f, v -> { smileExtra = v; invalidate(); }, 400);

        // Gentle arm sway while speaking
        armAnimator = ValueAnimator.ofFloat(0.25f, 0.45f);
        armAnimator.setDuration(580);
        armAnimator.setRepeatCount(ValueAnimator.INFINITE);
        armAnimator.setRepeatMode(ValueAnimator.REVERSE);
        armAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        armAnimator.addUpdateListener(a -> {
            float v = (float) a.getAnimatedValue();
            leftArmRaise = v;
            rightArmRaise = v * 0.82f;
            invalidate();
        });
        armAnimator.start();
    }

    private void enterPraiseState() {
        // Joyful celebration: clapping/rubbing hands + body bounce + wide smile
        animateTo(smileExtra, 1f, v -> { smileExtra = v; invalidate(); }, 280);
        animateTo(browRaiseFraction, 0.85f, v -> { browRaiseFraction = v; invalidate(); }, 200);
        animateTo(browFurrowFraction, 0f, v -> { browFurrowFraction = v; invalidate(); }, 200);
        animateTo(headTiltAngle, 0f, v -> { headTiltAngle = v; invalidate(); }, 250);

        // Arms raised into clapping position
        animateTo(leftArmRaise, 0.85f, v -> { leftArmRaise = v; invalidate(); }, 320, new OvershootInterpolator());
        animateTo(rightArmRaise, 0.85f, v -> { rightArmRaise = v; invalidate(); }, 320, new OvershootInterpolator());

        // Body vertical bounce (matching sample animation)
        bounceAnimator = ValueAnimator.ofFloat(0f, -20f, 0f);
        bounceAnimator.setDuration(480);
        bounceAnimator.setRepeatCount(4);
        bounceAnimator.setRepeatMode(ValueAnimator.RESTART);
        bounceAnimator.setInterpolator(new BounceInterpolator());
        bounceAnimator.addUpdateListener(a -> {
            bounceY = (float) a.getAnimatedValue();
            invalidate();
        });
        bounceAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bounceY = 0;
                invalidate();
            }
        });
        bounceAnimator.start();

        // Clapping/rubbing hands oscillation (key gesture from sample video)
        clapAnimator = ValueAnimator.ofFloat(0f, 1f);
        clapAnimator.setDuration(170);
        clapAnimator.setRepeatCount(ValueAnimator.INFINITE);
        clapAnimator.setRepeatMode(ValueAnimator.REVERSE);
        clapAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        clapAnimator.addUpdateListener(a -> {
            clapPhase = (float) a.getAnimatedValue();
            handOpenFraction = 0.35f + clapPhase * 0.45f;
            invalidate();
        });
        clapAnimator.start();
    }

    private void enterQuestionState() {
        // Inquisitive posture: head tilt, right hand up/pointing, raised eyebrow
        animateTo(headTiltAngle, 0.22f, v -> { headTiltAngle = v; invalidate(); }, 400, new OvershootInterpolator(1.4f));
        animateTo(browRaiseFraction, 0.95f, v -> { browRaiseFraction = v; invalidate(); }, 300);
        animateTo(browFurrowFraction, 0f, v -> { browFurrowFraction = v; invalidate(); }, 300);
        animateTo(smileExtra, 0.2f, v -> { smileExtra = v; invalidate(); }, 350);

        // Right arm raised high pointing/inquisitive, left arm relaxed
        animateTo(rightArmRaise, 0.8f, v -> { rightArmRaise = v; invalidate(); }, 360, new OvershootInterpolator());
        animateTo(leftArmRaise, 0.12f, v -> { leftArmRaise = v; invalidate(); }, 360);
        animateTo(handOpenFraction, 0.55f, v -> { handOpenFraction = v; invalidate(); }, 300);

        // Subtle head tilt oscillation
        tiltAnimator = ValueAnimator.ofFloat(0.16f, 0.25f);
        tiltAnimator.setDuration(1100);
        tiltAnimator.setRepeatCount(ValueAnimator.INFINITE);
        tiltAnimator.setRepeatMode(ValueAnimator.REVERSE);
        tiltAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        tiltAnimator.addUpdateListener(a -> {
            headTiltAngle = (float) a.getAnimatedValue();
            invalidate();
        });
        tiltAnimator.start();
    }

    private void enterGuidanceState() {
        // Comforting posture: open palms, sympathetic expression, comforting slight head tilt
        animateTo(handOpenFraction, 1f, v -> { handOpenFraction = v; invalidate(); }, 420);
        animateTo(leftArmRaise, 0.46f, v -> { leftArmRaise = v; invalidate(); }, 380);
        animateTo(rightArmRaise, 0.46f, v -> { rightArmRaise = v; invalidate(); }, 380);
        animateTo(headTiltAngle, 0.09f, v -> { headTiltAngle = v; invalidate(); }, 450);
        animateTo(browRaiseFraction, 0.35f, v -> { browRaiseFraction = v; invalidate(); }, 300);
        animateTo(browFurrowFraction, 0.3f, v -> { browFurrowFraction = v; invalidate(); }, 300);
        animateTo(smileExtra, 0.25f, v -> { smileExtra = v; invalidate(); }, 380);
    }

    // ── Idle Background Loops ─────────────────────────────────────────────────

    private void startIdleAnimations() {
        // Continuous gentle breathing
        breathAnimator = ValueAnimator.ofFloat(1f, 1.025f);
        breathAnimator.setDuration(1900);
        breathAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breathAnimator.addUpdateListener(a -> {
            breathScale = (float) a.getAnimatedValue();
            invalidate();
        });
        breathAnimator.start();

        // Subtle lateral sway (weight shift from side to side)
        swayAnimator = ValueAnimator.ofFloat(-1f, 1f);
        swayAnimator.setDuration(3200);
        swayAnimator.setRepeatCount(ValueAnimator.INFINITE);
        swayAnimator.setRepeatMode(ValueAnimator.REVERSE);
        swayAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        swayAnimator.addUpdateListener(a -> {
            swaySide = (float) a.getAnimatedValue();
            invalidate();
        });
        swayAnimator.start();
    }

    private void cancelStateAnimators() {
        if (bounceAnimator != null) { bounceAnimator.cancel(); bounceAnimator = null; bounceY = 0; }
        if (tiltAnimator   != null) { tiltAnimator.cancel();   tiltAnimator   = null; }
        if (armAnimator    != null) { armAnimator.cancel();    armAnimator    = null; }
        if (clapAnimator   != null) { clapAnimator.cancel();   clapAnimator   = null; }
    }

    private void triggerBlink() {
        if (blinkAnimator != null && blinkAnimator.isRunning()) return;
        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        blinkAnimator.setDuration(210);
        blinkAnimator.addUpdateListener(a -> {
            blinkFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        blinkAnimator.start();
    }

    private void smoothCloseMouth() {
        ValueAnimator close = ValueAnimator.ofFloat(mouthOpenFraction, 0f);
        close.setDuration(120);
        close.addUpdateListener(a -> {
            mouthOpenFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        close.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(blinkRunnable);
        handler.removeCallbacks(autoReturnRunnable);
        cancelAll();
    }

    private void cancelAll() {
        if (talkAnimator   != null) talkAnimator.cancel();
        if (breathAnimator != null) breathAnimator.cancel();
        if (blinkAnimator  != null) blinkAnimator.cancel();
        if (swayAnimator   != null) swayAnimator.cancel();
        cancelStateAnimators();
    }

    // ── Drawing Methods (Zero Allocation) ─────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w * 0.5f;
        float cy = h * 0.52f + bounceY;
        float size = Math.min(w, h);

        canvas.save();
        // Breathing scale from center
        canvas.scale(breathScale, breathScale, cx, cy);
        // Subtle lateral sway translation
        canvas.translate(swaySide * 2.5f, 0);

        drawFrontFacingCapybara(canvas, cx, cy, size);

        canvas.restore();
    }

    private void drawFrontFacingCapybara(Canvas canvas, float cx, float cy, float size) {
        float s = size / 220f;

        // 1. Torso & Belly (Reusing pre-allocated RectFs)
        bodyRectTemp.set(cx - 55 * s, cy - 10 * s, cx + 55 * s, cy + 80 * s);
        canvas.drawRoundRect(bodyRectTemp, 42 * s, 42 * s, bodyPaint);

        bellyRectTemp.set(cx - 28 * s, cy + 5 * s, cx + 28 * s, cy + 72 * s);
        canvas.drawRoundRect(bellyRectTemp, 26 * s, 26 * s, bellyPaint);

        // 2. Arms & Hands
        drawArms(canvas, cx, cy, s);

        // 3. Head & Face
        float headCy = cy - 52 * s;

        canvas.save();
        // Head tilt
        canvas.rotate((float) Math.toDegrees(headTiltAngle), cx, headCy);

        // Back ears
        drawEar(canvas, cx - 34 * s, headCy - 22 * s, s, true);
        drawEar(canvas, cx + 34 * s, headCy - 22 * s, s, true);

        // Head shape
        headRectTemp.set(cx - 42 * s, headCy - 36 * s, cx + 42 * s, headCy + 32 * s);
        canvas.drawRoundRect(headRectTemp, 28 * s, 28 * s, bodyPaint);

        // Front ears
        drawEar(canvas, cx - 34 * s, headCy - 26 * s, s, false);
        drawEar(canvas, cx + 34 * s, headCy - 26 * s, s, false);

        // Snout
        snoutRectTemp.set(cx - 26 * s, headCy + 2 * s, cx + 26 * s, headCy + 36 * s);
        canvas.drawRoundRect(snoutRectTemp, 18 * s, 18 * s, snoutPaint);

        // Cheek blush
        blushRectTemp.set(cx - 36 * s, headCy + 6 * s, cx - 20 * s, headCy + 18 * s);
        canvas.drawOval(blushRectTemp, blushPaint);
        blushRectTemp.set(cx + 20 * s, headCy + 6 * s, cx + 36 * s, headCy + 18 * s);
        canvas.drawOval(blushRectTemp, blushPaint);

        // Nose
        noseRectTemp.set(cx - 10 * s, headCy + 2 * s, cx + 10 * s, headCy + 12 * s);
        canvas.drawRoundRect(noseRectTemp, 6 * s, 6 * s, nosePaint);

        // Eyebrows
        drawEyebrows(canvas, cx, headCy, s);

        // Eyes
        float eyeY = headCy - 12 * s;
        drawEye(canvas, cx - 16 * s, eyeY, 9f * s, s, false);
        drawEye(canvas, cx + 16 * s, eyeY, 9f * s, s, false);

        // Viseme-aware Mouth
        drawFrontMouth(canvas, cx, headCy, s);

        // Orange Fruit on Head
        drawOrangeFruit(canvas, cx, headCy - 42 * s, s);

        canvas.restore();

        // 4. Feet
        footRectTemp.set(cx - 46 * s, cy + 74 * s, cx - 18 * s, cy + 90 * s);
        canvas.drawRoundRect(footRectTemp, 9 * s, 9 * s, bodyDarkPaint);
        footRectTemp.set(cx + 18 * s, cy + 74 * s, cx + 46 * s, cy + 90 * s);
        canvas.drawRoundRect(footRectTemp, 9 * s, 9 * s, bodyDarkPaint);
    }

    private void drawArms(Canvas canvas, float cx, float cy, float s) {
        // Left arm
        float leftShoulderX = cx - 52 * s;
        float leftShoulderY = cy + 10 * s;
        float leftArmAngleDeg = -28f - leftArmRaise * 56f;
        drawArm(canvas, leftShoulderX, leftShoulderY, leftArmAngleDeg, s, true);

        // Right arm
        float rightShoulderX = cx + 52 * s;
        float rightShoulderY = cy + 10 * s;
        float rightArmAngleDeg = 28f + rightArmRaise * 56f;
        drawArm(canvas, rightShoulderX, rightShoulderY, rightArmAngleDeg, s, false);

        // Draw clasped / rubbing / clapping hands at center when in IDLE or PRAISE
        if (currentState == AnimState.IDLE || currentState == AnimState.PRAISE) {
            drawClaspedHands(canvas, cx, cy, s);
        }
    }

    private void drawArm(Canvas canvas, float sx, float sy, float angleDeg, float s, boolean isLeft) {
        canvas.save();
        canvas.translate(sx, sy);
        canvas.rotate(angleDeg);

        if (isLeft) {
            armRectTemp.set(-14 * s, -10 * s, 0, 32 * s);
        } else {
            armRectTemp.set(0, -10 * s, 14 * s, 32 * s);
        }
        canvas.drawRoundRect(armRectTemp, 7 * s, 7 * s, armPaint);

        float handCx = isLeft ? -7 * s : 7 * s;
        float handCy = 30 * s;
        drawHand(canvas, handCx, handCy, s, isLeft);

        canvas.restore();
    }

    private void drawHand(Canvas canvas, float hx, float hy, float s, boolean isLeft) {
        canvas.drawCircle(hx, hy, 9 * s, handPaint);

        if (handOpenFraction > 0.3f) {
            // Open palms / spread fingers
            float spread = handOpenFraction * 6 * s;
            int dir = isLeft ? -1 : 1;
            for (int i = -1; i <= 1; i++) {
                canvas.drawCircle(hx + i * spread, hy - 7 * s, 3.5f * s, handDarkPaint);
            }
            canvas.drawCircle(hx + dir * 10 * s, hy - 2 * s, 3f * s, handDarkPaint);
        } else {
            // Loosely closed paws
            canvas.drawCircle(hx - 2.5f * s, hy - 5.5f * s, 2.8f * s, handDarkPaint);
            canvas.drawCircle(hx + 2.5f * s, hy - 5.5f * s, 2.8f * s, handDarkPaint);
        }
    }

    private void drawClaspedHands(Canvas canvas, float cx, float cy, float s) {
        float claspY = cy + 38 * s;
        float claspGap = clapPhase * 8 * s;

        canvas.drawCircle(cx - 6 * s - claspGap, claspY, 10 * s, handPaint);
        canvas.drawCircle(cx + 6 * s + claspGap, claspY, 10 * s, handPaint);

        canvas.drawCircle(cx - 10 * s - claspGap, claspY - 5 * s, 3 * s, fingerDetailPaint);
        canvas.drawCircle(cx + 10 * s + claspGap, claspY - 5 * s, 3 * s, fingerDetailPaint);
        canvas.drawCircle(cx - 3 * s - claspGap, claspY - 8 * s, 3 * s, fingerDetailPaint);
        canvas.drawCircle(cx + 3 * s + claspGap, claspY - 8 * s, 3 * s, fingerDetailPaint);
    }

    private void drawEar(Canvas canvas, float ex, float ey, float s, boolean back) {
        Paint p = back ? bodyDarkPaint : bodyPaint;
        earRectTemp.set(ex - 13 * s, ey - 17 * s, ex + 13 * s, ey + 8 * s);
        canvas.drawOval(earRectTemp, p);
        if (!back) {
            earRectTemp.set(ex - 7 * s, ey - 11 * s, ex + 7 * s, ey + 2 * s);
            canvas.drawOval(earRectTemp, bodyDarkPaint);
        }
    }

    private void drawEye(Canvas canvas, float ex, float ey, float r, float s, boolean isLeft) {
        if (blinkFraction > 0.35f) {
            eyeArcPathTemp.reset();
            eyeArcPathTemp.moveTo(ex - r, ey);
            eyeArcPathTemp.quadTo(ex, ey - r * 0.7f, ex + r, ey);
            eyeArcPaint.setStrokeWidth(3f * s);
            canvas.drawPath(eyeArcPathTemp, eyeArcPaint);
        } else {
            canvas.drawCircle(ex, ey, r, eyePaint);
            // Main highlight (top-right)
            canvas.drawCircle(ex + r * 0.28f, ey - r * 0.3f, r * 0.38f, eyeShine);
            // Secondary highlight (bottom-left)
            canvas.drawCircle(ex - r * 0.22f, ey + r * 0.28f, r * 0.18f, eyeHighlight);
        }
    }

    private void drawEyebrows(Canvas canvas, float cx, float headCy, float s) {
        float baseY = headCy - 22 * s;
        float raiseOff = browRaiseFraction * 7 * s;
        float furrow = browFurrowFraction * 5 * s;

        browPaint.setStrokeWidth(3.5f * s);

        // Left brow
        leftBrowPathTemp.reset();
        leftBrowPathTemp.moveTo(cx - 30 * s, baseY - raiseOff + furrow);
        leftBrowPathTemp.quadTo(cx - 18 * s, baseY - raiseOff - 3 * s, cx - 8 * s, baseY - raiseOff - furrow);
        canvas.drawPath(leftBrowPathTemp, browPaint);

        // Right brow
        rightBrowPathTemp.reset();
        rightBrowPathTemp.moveTo(cx + 8 * s, baseY - raiseOff - furrow);
        rightBrowPathTemp.quadTo(cx + 18 * s, baseY - raiseOff - 3 * s, cx + 30 * s, baseY - raiseOff + furrow);
        canvas.drawPath(rightBrowPathTemp, browPaint);
    }

    /**
     * Renders mouth shapes based on the 6 foundational visemes:
     * REST, WIDE_OPEN, ROUND_PUCKERED, WIDE_SMILE, TEETH_ON_LIP, TONGUE_ALVEOLAR.
     */
    private void drawFrontMouth(Canvas canvas, float cx, float headCy, float s) {
        float baseY = headCy + 28 * s;
        float maxDrop = 14f * s;
        float open = mouthOpenFraction * maxDrop;

        float smileW = 16 * s + smileExtra * 10 * s;
        float smileDip = 5 * s + smileExtra * 5 * s;

        if (open < 1.2f || currentViseme == Viseme.REST) {
            // Closed smile (Rest viseme for M, B, P)
            mouthPathTemp.reset();
            mouthPathTemp.moveTo(cx - smileW, baseY);
            mouthPathTemp.quadTo(cx, baseY + smileDip, cx + smileW, baseY);
            canvas.drawPath(mouthPathTemp, mouthStroke);

            if (smileExtra > 0.45f) {
                float alpha = (smileExtra - 0.45f) * 1.8f * 180f;
                dimplePaint.setAlpha((int) Math.min(200, Math.max(0, alpha)));
                canvas.drawCircle(cx - smileW - 3 * s, baseY - 2 * s, 3 * s, dimplePaint);
                canvas.drawCircle(cx + smileW + 3 * s, baseY - 2 * s, 3 * s, dimplePaint);
            }
            return;
        }

        // Open mouth visemes
        switch (currentViseme) {
            case ROUND_PUCKERED: {
                // Tight rounded puckered "O" (Oh, Oo, U, W)
                float rW = 9 * s;
                float rH = open * 1.05f;
                cavityRectTemp.set(cx - rW, baseY - 2 * s, cx + rW, baseY + rH);
                canvas.drawRoundRect(cavityRectTemp, 8 * s, 8 * s, innerMouthPaint);
                canvas.drawRoundRect(cavityRectTemp, 8 * s, 8 * s, mouthStroke);
                break;
            }

            case WIDE_SMILE: {
                // Stretched broad smile (Ee, E, I, S, Z)
                float extW = smileW + 5 * s;
                float extH = open * 0.72f;
                cavityRectTemp.set(cx - extW, baseY - 2 * s, cx + extW, baseY + extH);
                canvas.drawRoundRect(cavityRectTemp, 6 * s, 6 * s, innerMouthPaint);

                // Full upper teeth row
                teethRectTemp.set(cavityRectTemp.left + 3 * s, baseY - 2 * s, cavityRectTemp.right - 3 * s, baseY + extH * 0.45f);
                canvas.drawRoundRect(teethRectTemp, 2.5f * s, 2.5f * s, teethPaint);

                canvas.drawRoundRect(cavityRectTemp, 6 * s, 6 * s, mouthStroke);
                break;
            }

            case TEETH_ON_LIP: {
                // Upper teeth touching lower lip (F, V)
                cavityRectTemp.set(cx - smileW * 0.9f, baseY - 2 * s, cx + smileW * 0.9f, baseY + open * 0.65f);
                canvas.drawRoundRect(cavityRectTemp, 5 * s, 5 * s, innerMouthPaint);

                // Teeth extending distinctly downward
                teethRectTemp.set(cx - 8 * s, baseY - 2 * s, cx + 8 * s, baseY + open * 0.6f);
                canvas.drawRoundRect(teethRectTemp, 2f * s, 2f * s, teethPaint);

                canvas.drawRoundRect(cavityRectTemp, 5 * s, 5 * s, mouthStroke);
                break;
            }

            case TONGUE_ALVEOLAR: {
                // Tongue tip hitting behind upper teeth (L, T, D, N)
                cavityRectTemp.set(cx - smileW, baseY - 2 * s, cx + smileW, baseY + open);
                canvas.drawRoundRect(cavityRectTemp, 7 * s, 7 * s, innerMouthPaint);

                // Upper teeth
                float toothH = Math.min(5 * s, open * 0.32f);
                teethRectTemp.set(cavityRectTemp.left + 4 * s, baseY - 2 * s, cavityRectTemp.right - 4 * s, baseY - 2 * s + toothH);
                canvas.drawRoundRect(teethRectTemp, 2.5f * s, 2.5f * s, teethPaint);

                // Tongue tip raised upward toward teeth
                float tongueH = open * 0.65f;
                tongueRectTemp.set(cx - 7 * s, baseY + open - tongueH, cx + 7 * s, baseY + open);
                canvas.drawRoundRect(tongueRectTemp, 5 * s, 5 * s, tonguePaint);

                canvas.drawRoundRect(cavityRectTemp, 7 * s, 7 * s, mouthStroke);
                break;
            }

            case WIDE_OPEN:
            default: {
                // Wide open jaw (Ah, A, O)
                cavityRectTemp.set(cx - smileW, baseY - 2 * s, cx + smileW, baseY + open);
                canvas.drawRoundRect(cavityRectTemp, 7 * s, 7 * s, innerMouthPaint);

                // Top teeth
                float toothH = Math.min(5 * s, open * 0.32f);
                teethRectTemp.set(cavityRectTemp.left + 4 * s, baseY - 2 * s, cavityRectTemp.right - 4 * s, baseY - 2 * s + toothH);
                canvas.drawRoundRect(teethRectTemp, 2.5f * s, 2.5f * s, teethPaint);

                // Bottom tongue
                float tongueH = open * 0.38f;
                tongueRectTemp.set(cavityRectTemp.left + 5 * s, baseY + open - tongueH, cavityRectTemp.right - 5 * s, baseY + open);
                canvas.drawRoundRect(tongueRectTemp, 6 * s, 6 * s, tonguePaint);

                canvas.drawRoundRect(cavityRectTemp, 7 * s, 7 * s, mouthStroke);
                break;
            }
        }
    }

    private void drawOrangeFruit(Canvas canvas, float ox, float oy, float s) {
        float r = 11 * s;
        canvas.drawCircle(ox, oy, r, orangeFruitPaint);

        // Leaf (reusing leafPathTemp)
        leafPathTemp.reset();
        leafPathTemp.moveTo(ox, oy - r);
        leafPathTemp.quadTo(ox + 9 * s, oy - r - 13 * s, ox + 16 * s, oy - r - 5 * s);
        leafPathTemp.quadTo(ox + 7 * s, oy - r - 1 * s, ox, oy - r);
        leafPathTemp.close();
        canvas.drawPath(leafPathTemp, leafPaint);

        // Highlight
        canvas.drawCircle(ox - 3 * s, oy - 4 * s, 3.5f * s, eyeShine);
    }

    // ── Helper: Value Animator ────────────────────────────────────────────────

    interface FloatSetter { void set(float v); }

    private void animateTo(float from, float to, FloatSetter setter, long duration) {
        animateTo(from, to, setter, duration, new AccelerateDecelerateInterpolator());
    }

    private void animateTo(float from, float to, FloatSetter setter, long duration,
                           android.view.animation.Interpolator interpolator) {
        ValueAnimator va = ValueAnimator.ofFloat(from, to);
        va.setDuration(duration);
        va.setInterpolator(interpolator);
        va.addUpdateListener(a -> setter.set((float) a.getAnimatedValue()));
        va.start();
    }

    private static Paint makePaint(int color, Paint.Style style) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }
}
