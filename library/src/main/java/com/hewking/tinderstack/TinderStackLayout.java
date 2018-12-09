package com.hewking.tinderstack;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by hewking on 2017/3/30.
 */

public class TinderStackLayout extends ViewGroup {

    public static final String TAG = "TinderStackLayout";

    private static final float DEFAULT_SCALE = 0.05f;
    private static final int DEFAULT_OFFSET = 10;//dp
    private static final int DEFAULT_MARGIN = 10;//dp
    private static final int DEFAULT_DEGRESS = 20;//旋转的度数

    private ViewDragHelper mDragHelper;

    private int mCenterX;
    private int mCenterY;

    private Point mReleasedPoint = new Point();
    private boolean isDraging = false;

    public TinderStackLayout(Context context) {
        this(context, null);
    }

    public TinderStackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TinderStackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return indexOfChild(child) == getChildCount() - 1;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                return top;
            }

            @Override
            public void onViewReleased(@NonNull final View releasedChild, float xvel, float yvel) {
                L.d(TAG, "onViewReleased xvel : " + xvel + " yvel : " + yvel);

                if (isDraging) {
                    mDragHelper.settleCapturedViewAt(mCenterX - releasedChild.getMeasuredWidth() / 2
                            , mCenterY - releasedChild.getMeasuredHeight() / 2);
                    invalidate();
                } else {
                    if (mReleasedPoint.x != 0 && mReleasedPoint.y != 0) {
                        final float sloap = mReleasedPoint.x / mReleasedPoint.y;
                        if (Math.abs(mReleasedPoint.x) > getMeasuredWidth() / 3 && Math.abs(sloap) > 0.15) {
                            mDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth(), (int) (getMeasuredWidth() * sloap));
                            invalidate();
                            mReleasedPoint.x = 0;
                            mReleasedPoint.y = 0;
                            removeView(releasedChild);
                        }
                    }
                }
            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                L.d(TAG, "onViewPositionChanged left : " + left + " top : " + top + " dx : " + dx + "dy : " + dy);
                //斜率，有方向
                float sloap = top * 1.0f / left;
                if (Math.abs(left) > getMeasuredWidth() / 3 && Math.abs(sloap) > 0.15) {
                    mReleasedPoint.x = left;
                    mReleasedPoint.y = top;
                    isDraging = false;
                } else {
                    // 调整剩下view的大小和位置
                    ensureChildView();
                    isDraging = true;
                    // 旋转,根据现在的比例
//                    changedView.setRotation(DEFAULT_DEGRESS * );
                    // 剩下的子view，缩放 平移,-2 未考虑只有一个view 的情况，还有visibilty= gone
                    float rate = left * 1.0f / (getMeasuredWidth() / 3);
                    float a = Math.min(1, Math.max(0, Math.abs(rate)));
                    int offset = ViewExKt.dp2px(TinderStackLayout.this, DEFAULT_OFFSET);
                    for (int i = getChildCount() < 4 ? 0 : 1; i < getChildCount() - 1; i++) {
                        View child = getChildAt(i);
                        float ds = 1 - DEFAULT_SCALE * (getChildCount() - 1 - i) + DEFAULT_SCALE * a;
                        float doffset = (getChildCount() - 1 - i) * offset - offset * a;
                        float yOffset = child.getMeasuredHeight() * DEFAULT_SCALE * (getChildCount() - 1 - i - a) / 2;
                        child.setScaleY(ds);
                        child.setScaleX(ds);
                        child.setTranslationY(doffset + yOffset);

                        L.d(TAG, "ds : " + ds + " doffset : " + doffset + " a : " + a);
                    }

                    //rate > 0  右滑动 else  // 左滑动
                        changedView.setRotation(rate * DEFAULT_DEGRESS);
                }
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                super.onEdgeDragStarted(edgeFlags, pointerId);
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
            }
        });
    }

    private void ensureChildView() {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        float scale = 1f;
        int level = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            float scaleValue = scale - DEFAULT_SCALE * (level);

            int offset = ViewExKt.dp2px(this, DEFAULT_OFFSET);
            int offsetValue = offset * (level);

            child.layout(mCenterX - child.getMeasuredWidth() / 2
                    , mCenterY - child.getMeasuredHeight() / 2
                    , mCenterX + child.getMeasuredWidth() / 2
                    , mCenterY + child.getMeasuredHeight() / 2);

            float yOffset = child.getMeasuredHeight() * DEFAULT_SCALE * (level) / 2;

            child.setTranslationY(yOffset + offsetValue);
            child.setScaleX(scaleValue);
            child.setScaleY(scaleValue);

            if (i > 1 || getChildCount() < 4) {
                level++;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int margin = ViewExKt.dp2px(this, DEFAULT_MARGIN);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            postInvalidate();
        }
    }
}
