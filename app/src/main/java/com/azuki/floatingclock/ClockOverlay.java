package com.azuki.floatingclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockOverlay {
    private final Context context;
    private final WindowManager windowManager;
    private final SharedPreferences prefs;
    private WindowManager.LayoutParams params;
    private LinearLayout panel;
    private TextView timeText;
    private TextView dateText;
    private boolean compact;
    private boolean attached;
    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            updateTime();
            handler.postDelayed(this, 1000);
        }
    };

    private float downX, downY;
    private int startX, startY;
    private long downAt;
    private boolean moved;

    public ClockOverlay(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        prefs = context.getSharedPreferences("clock_position", Context.MODE_PRIVATE);
    }

    public void show() {
        if (attached) return;
        compact = prefs.getBoolean("compact", false);
        buildView();

        int type = android.os.Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = prefs.getInt("x", 30);
        params.y = prefs.getInt("y", 180);

        applyMode();
        windowManager.addView(panel, params);
        attached = true;
        handler.post(ticker);
    }

    private void buildView() {
        panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(12), dp(8), dp(12), dp(8));

        timeText = new TextView(context);
        timeText.setTextColor(Color.WHITE);
        timeText.setTextSize(18);
        timeText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        timeText.setGravity(Gravity.CENTER);

        dateText = new TextView(context);
        dateText.setTextColor(0xFFD7D7D7);
        dateText.setTextSize(10);
        dateText.setGravity(Gravity.CENTER);

        panel.addView(timeText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        panel.addView(dateText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        panel.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startX = params.x;
                        startY = params.y;
                        downAt = System.currentTimeMillis();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                        if (moved) {
                            params.x = startX + (int) dx;
                            params.y = startY + (int) dy;
                            windowManager.updateViewLayout(panel, params);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (moved) {
                            prefs.edit().putInt("x", params.x).putInt("y", params.y).apply();
                        } else if (System.currentTimeMillis() - downAt < 500) {
                            compact = !compact;
                            prefs.edit().putBoolean("compact", compact).apply();
                            applyMode();
                            windowManager.updateViewLayout(panel, params);
                        }
                        return true;
                }
                return true;
            }
        });
        updateTime();
    }

    private void applyMode() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xE6222222);
        bg.setCornerRadius(compact ? dp(48) : dp(14));
        bg.setStroke(dp(1), 0x66777777);
        panel.setBackground(bg);

        if (compact) {
            panel.setPadding(dp(9), dp(9), dp(9), dp(9));
            timeText.setTextSize(12);
            dateText.setVisibility(View.GONE);
            panel.setMinimumWidth(dp(68));
            panel.setMinimumHeight(dp(68));
        } else {
            panel.setPadding(dp(14), dp(9), dp(14), dp(9));
            timeText.setTextSize(20);
            dateText.setVisibility(View.VISIBLE);
            panel.setMinimumWidth(0);
            panel.setMinimumHeight(0);
        }
        updateTime();
    }

    private void updateTime() {
        if (timeText == null) return;
        Date now = new Date();
        timeText.setText(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now));
        dateText.setText(new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now));
    }

    public void remove() {
        handler.removeCallbacks(ticker);
        if (attached && panel != null) {
            try { windowManager.removeView(panel); } catch (Exception ignored) {}
            attached = false;
        }
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
