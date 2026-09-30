package com.eastonlearning;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

/**
 * Kid-friendly on-screen QWERTY keyboard.
 *
 * Supports touch interactions as well as visual highlighting when keys
 * are typed on an external/hardware keyboard.
 */
public class OnScreenKeyboard extends LinearLayout {

    public interface OnKeyPressListener {
        void onKeyPress(char key);
    }

    private OnKeyPressListener keyPressListener;
    private final Map<Character, TextView> keyViews = new HashMap<>();
    private final Map<Character, Integer> defaultColors = new HashMap<>();

    // Vibrant, child-friendly color palette for keyboard rows
    private final int[] ROW1_COLORS = {
            0xFF3B82F6, // Q - Blue
            0xFF10B981, // W - Green
            0xFFF59E0B, // E - Amber
            0xFF8B5CF6, // R - Purple
            0xFFEC4899, // T - Pink
            0xFF06B6D4, // Y - Cyan
            0xFFF97316, // U - Orange
            0xFF6366F1, // I - Indigo
            0xFF14B8A6, // O - Teal
            0xFFEF4444  // P - Coral Red
    };

    private final int[] ROW2_COLORS = {
            0xFF10B981, // A
            0xFF3B82F6, // S
            0xFFEC4899, // D
            0xFFF59E0B, // F
            0xFF8B5CF6, // G
            0xFF14B8A6, // H
            0xFFF97316, // J
            0xFF06B6D4, // K
            0xFF6366F1  // L
    };

    private final int[] ROW3_COLORS = {
            0xFF8B5CF6, // Z
            0xFFEC4899, // X
            0xFF3B82F6, // C
            0xFF10B981, // V
            0xFFF59E0B, // B
            0xFF14B8A6, // N
            0xFFF97316, // M
            0xFF64748B  // ⌫ DEL
    };

    public OnScreenKeyboard(Context context) {
        super(context);
        init();
    }

    public OnScreenKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnScreenKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dpToPx(2);
        setPadding(pad, pad, pad, pad);

        buildKeyboard();
    }

    private void buildKeyboard() {
        removeAllViews();
        keyViews.clear();
        defaultColors.clear();

        // Row 1: QWERTYUIOP (10 keys)
        addRow("QWERTYUIOP", ROW1_COLORS, 0f, 0f);

        // Row 2: ASDFGHJKL (9 keys, slightly indented for natural staggered look)
        addRow("ASDFGHJKL", ROW2_COLORS, 0.05f, 0.05f);

        // Row 3: ZXCVBNM + Backspace (8 keys)
        addRow("ZXCVBNM\b", ROW3_COLORS, 0.1f, 0f);
    }

    private void addRow(String keys, int[] colors, float leftWeight, float rightWeight) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dpToPx(2), 0, dpToPx(2));
        row.setLayoutParams(rowParams);

        if (leftWeight > 0f) {
            View spacer = new View(getContext());
            LayoutParams sp = new LayoutParams(0, 1, leftWeight);
            row.addView(spacer, sp);
        }

        for (int i = 0; i < keys.length(); i++) {
            final char c = keys.charAt(i);
            int color = colors[i % colors.length];

            TextView keyView = createKeyView(c, color);
            keyViews.put(Character.toUpperCase(c), keyView);
            defaultColors.put(Character.toUpperCase(c), color);

            float weight = (c == '\b') ? 1.4f : 1.0f;
            LayoutParams kp = new LayoutParams(0, dpToPx(38), weight);
            kp.setMargins(dpToPx(2), 0, dpToPx(2), 0);
            row.addView(keyView, kp);
        }

        if (rightWeight > 0f) {
            View spacer = new View(getContext());
            LayoutParams sp = new LayoutParams(0, 1, rightWeight);
            row.addView(spacer, sp);
        }

        addView(row);
    }

    private TextView createKeyView(final char c, final int color) {
        final TextView tv = new TextView(getContext());
        tv.setText(c == '\b' ? "⌫" : String.valueOf(c));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, c == '\b' ? 16 : 18);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setBackground(makeKeyDrawable(color, false));
        tv.setElevation(dpToPx(3));

        tv.setOnClickListener(v -> {
            animateTouchPress(tv);
            if (keyPressListener != null) {
                keyPressListener.onKeyPress(Character.toLowerCase(c));
            }
        });

        return tv;
    }

    /**
     * Visually highlight an on-screen key when pressed on physical/external keyboard.
     */
    public void highlightKey(char key) {
        char upper = Character.toUpperCase(key);
        final TextView keyView = keyViews.get(upper);
        if (keyView == null) return;

        final Integer baseColor = defaultColors.get(upper);
        int normalColor = baseColor != null ? baseColor : 0xFF3B82F6;

        // Flash key with gold accent and scale bounce
        keyView.setBackground(makeKeyDrawable(0xFFFACC15, true)); // Gold flash
        keyView.setTextColor(0xFF78350F); // Warm dark text

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(keyView, "scaleX", 1f, 1.25f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(keyView, "scaleY", 1f, 1.25f, 1f);
        set.playTogether(scaleX, scaleY);
        set.setDuration(190);
        set.start();

        // Restore normal style after flash
        keyView.postDelayed(() -> {
            keyView.setBackground(makeKeyDrawable(normalColor, false));
            keyView.setTextColor(Color.WHITE);
        }, 180);
    }

    private void animateTouchPress(View keyView) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(keyView, "scaleX", 1f, 0.88f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(keyView, "scaleY", 1f, 0.88f, 1f);
        set.playTogether(scaleX, scaleY);
        set.setDuration(140);
        set.start();
    }

    private GradientDrawable makeKeyDrawable(int color, boolean isHighlighted) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dpToPx(8));
        if (isHighlighted) {
            gd.setStroke(dpToPx(3), 0xFFFFFFFF);
        } else {
            gd.setStroke(dpToPx(1.5f), 0x40FFFFFF);
        }
        return gd;
    }

    public void setOnKeyPressListener(OnKeyPressListener listener) {
        this.keyPressListener = listener;
    }

    private int dpToPx(float dp) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }
}