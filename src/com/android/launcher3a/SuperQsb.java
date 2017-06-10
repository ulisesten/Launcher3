package com.android.launcher3a;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

public abstract class SuperQsb //b
        extends FrameLayout
        implements View.OnClickListener
{
    private static final String TEXT_ASSIST = "com.google.android.googlequicksearchbox.TEXT_ASSIST"; //bB
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator(); //mArgbEvaluator
    private ObjectAnimator mObjectAnimator; //getInstance
    protected View mQsbView; //bF
    private float qsbButtonElevation; //bG
    protected View qsbConnector; //bH
    private final Interpolator mADInterpolator = new AccelerateDecelerateInterpolator(); //bI
    private ObjectAnimator elevationAnimator; //bJ
    protected boolean micEnabled; //bL
    protected boolean qsbHidden; //bM
    private int mQsbViewId = 0; //bN
    protected final Launcher mLauncher; //getView
    private boolean windowHasFocus; //bP

    protected abstract int getQsbView(boolean withMic);

    public SuperQsb(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        this.mLauncher = Launcher.getLauncher(paramContext);
    }

    public void applyOpaPreference() { //bm
        int qsbView = getQsbView(false);
        if (qsbView != mQsbViewId) {
            mQsbViewId = qsbView;
            if (mQsbView != null) {
                removeView(mQsbView);
            }
            this.mQsbView = LayoutInflater.from(getContext()).inflate(mQsbViewId, this, false);
            this.qsbButtonElevation = (float) getResources().getDimensionPixelSize(R.dimen.qsb_button_elevation);
            this.addView(this.mQsbView);
            mObjectAnimator = ObjectAnimator.ofFloat(mQsbView, "elevation", 0.0f, this.qsbButtonElevation).setDuration(200L);
            mObjectAnimator.setInterpolator(this.mADInterpolator);
            if (qsbHidden) {
                hideQsb();
            }
            mQsbView.setOnClickListener(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyOpaPreference();
        applyMinusOnePreference();
        applyVisibility();
    }

    private void applyMinusOnePreference() { //bh
        if (this.qsbConnector != null) {
            removeView(this.qsbConnector);
            this.qsbConnector = null;
        }

        if (!mLauncher.useVerticalBarLayout()) {
            addView(qsbConnector = mLauncher.getLayoutInflater().inflate(R.layout.qsb_connector, this, false), 0);
            final int color = this.getResources().getColor(R.color.qsb_connector);
            final int color2 = this.getResources().getColor(R.color.qsb_background);
            final ColorDrawable background = new ColorDrawable(color);
            qsbConnector.setBackground(background);
            (elevationAnimator = ObjectAnimator.ofObject(background, "color", this.mArgbEvaluator, color2, color)).setDuration(200L);
            elevationAnimator.setInterpolator(this.mADInterpolator);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onClick(View paramView)
    {
        getContext().sendOrderedBroadcast(bm("com.google.nexuslauncher.FAST_TEXT_SEARCH"), null, new C0287l(this), null, 0, null, null);
    }

    private Intent bm(String str) {
        int[] iArr = new int[2];
        mQsbView.getLocationOnScreen(iArr);
        Rect rect = new Rect(iArr[0], iArr[1], iArr[0] + mQsbView.getWidth(), iArr[1] + mQsbView.getHeight());
        Intent intent = new Intent(str);
        bi(rect, intent);
        intent.setSourceBounds(rect);
        return intent.putExtra("source_round_left", true).putExtra("source_round_right", true).putExtra("source_logo_offset", MidLocation(findViewById(R.id.g_icon), rect)).setPackage("com.google.android.googlequicksearchbox").addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private Point MidLocation(View view, Rect rect) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        Point point = new Point();
        point.x = (iArr[0] - rect.left) + (view.getWidth() / 2);
        point.y = (iArr[1] - rect.top) + (view.getHeight() / 2);
        return point;
    }

    protected void bi(Rect rect, Intent intent) {
    }

    private void loadWindowFocus() { //bj
        if (hasWindowFocus()) {
            windowHasFocus = true;
        } else {
            hideQsb();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean newWindowHasFocus) {
        super.onWindowFocusChanged(newWindowHasFocus);
        if (!newWindowHasFocus && windowHasFocus) {
            hideQsb();
        } else if (newWindowHasFocus && !windowHasFocus) {
            showQsb(true);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int paramInt)
    {
        super.onWindowVisibilityChanged(paramInt);
        showQsb(false);
    }

    private void hideQsb() { //bb
        windowHasFocus = false;
        qsbHidden = true;
        if (mQsbView != null) {
            mQsbView.setAlpha(0.0f);
            if (elevationAnimator != null && elevationAnimator.isRunning()) {
                elevationAnimator.end();
            }
        }
        if (qsbConnector != null) {
            if (this.mObjectAnimator != null && this.mObjectAnimator.isRunning()) {
                this.mObjectAnimator.end();
            }
            qsbConnector.setAlpha(0.0f);
        }
    }

    private void showQsb(boolean animated) { //bc
        windowHasFocus = false;
        if (qsbHidden) {
            qsbHidden = false;
            if (mQsbView != null) {
                mQsbView.setAlpha(1.0f);
                if (elevationAnimator != null) {
                    elevationAnimator.start();
                    if (!animated) {
                        elevationAnimator.end();
                    }
                }
            }
            if (qsbConnector != null) {
                qsbConnector.setAlpha(1.0f);
                if (this.mObjectAnimator != null) {
                    this.mObjectAnimator.start();
                    if (!animated) {
                        this.mObjectAnimator.end();
                    }
                }
            }
        }
    }

    private void startQsbActivity(String str) { //bl
        try {
            getContext().startActivity(new Intent(str).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).setPackage("com.google.android.googlequicksearchbox"));
        } catch (ActivityNotFoundException e) {
            Log.e("SuperQsb", "ActivityNotFound");
        }
    }

    private void applyVisibility() { //bg
        if (mQsbView != null) {
            mQsbView.setVisibility(View.VISIBLE);
        }
        if (qsbConnector != null) {
            qsbConnector.setVisibility(View.VISIBLE);
        }
    }

    final class C0287l extends BroadcastReceiver {
        final /* synthetic */ SuperQsb cq;

        C0287l(SuperQsb qsbView) {
            cq = qsbView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == 0) { //why 0..?
                cq.startQsbActivity(SuperQsb.TEXT_ASSIST);
            } else {
                cq.loadWindowFocus();
            }
        }
    }
}