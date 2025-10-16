package com.eastonlearning;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

public class OnScreenKeyboard extends GridLayout {
    private OnKeyPressListener keyPressListener;
    
    public interface OnKeyPressListener {
        void onKeyPress(char key);
    }
    
    public OnScreenKeyboard(Context context) {
        super(context);
        init();
    }
    
    public OnScreenKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setColumnCount(10);
        setRowCount(3);
        setPadding(20, 20, 20, 20);
        
        createKeyboard();
    }
    
    private void createKeyboard() {
        String[] rows = {
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
        };
        
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                Button key = createKey(c);
                addView(key);
            }
        }
    }
    
    private Button createKey(char letter) {
        Button key = new Button(getContext());
        key.setText(String.valueOf(letter));
        key.setTextSize(28);
        key.setTextColor(Color.WHITE);
        key.setTypeface(key.getTypeface(), android.graphics.Typeface.BOLD);
        
        // Use colorful backgrounds for different letters
        int[] colors = {0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFF9C27B0, 0xFFF44336};
        int colorIndex = (letter - 'A') % colors.length;
        key.setBackgroundColor(colors[colorIndex]);
        
        key.setPadding(12, 12, 12, 12);
        key.setElevation(8);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 100;
        params.height = 100;
        params.setMargins(6, 6, 6, 6);
        key.setLayoutParams(params);
        
        key.setOnClickListener(v -> {
            if (keyPressListener != null) {
                keyPressListener.onKeyPress(Character.toLowerCase(letter));
            }
            animateKeyPress(key);
        });
        
        return key;
    }
    
    private void animateKeyPress(Button key) {
        key.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction(() -> 
                key.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
            );
    }
    
    public void setOnKeyPressListener(OnKeyPressListener listener) {
        this.keyPressListener = listener;
    }
}