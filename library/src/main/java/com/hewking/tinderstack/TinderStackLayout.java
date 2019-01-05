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

    private static final int DEFAULT_SHOW_COUNT = 4;

    private int showCount = DEFAULT_SHOW_COUNT;

    private ViewDragHelper mDragHelper;

    private int mCenterX;
    private int mCenterY;

    private BaseCardAdapter adapter;

    private OnChooseListener chooseListener;

    public OnChooseListener getChooseListener() {
        return chooseListener;
    }

    public void setChooseListener(OnChooseListener chooseListener) {
        this.chooseListener = chooseListener;
    }

    public BaseCardAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BaseCardAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null){
            int count = Math.min(adapter.getItemCount(),showCount);
            if (count <= 0) {
                return ;
            }
            for (int i = 0 ;i < count ; i++) {
                addView(adapter.getView());
            }
        }
    }

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
                // 最top 的view 可滑动
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
                        final float sloap = mReleasedPoint.y / (mReleasedPoint.x * 1.0f);
                        if (Math.abs(mReleasedPoint.x) > getMeasuredWidth() / 3 && Math.abs(sloap) > 0.15) {
                            mDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth(), (int) (getMeasuredWidth() * sloap));

                            onChoosePick(sloap);

                            invalidate();
                            mReleasedPoint.x = 0;
                            mReleasedPoint.y = 0;
                            removeView(releasedChild);
                            onAddView();
                        }
                    }
                }
            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                L.d(TAG, "onViewPositionChanged left : " + left + " top : " + top + " dx : " + dx + "dy : " + dy);
                //斜率，有方向
                float sloap = top * 1.0f / left;
                // top view 滑动的距离超过 宽度的三分之一，并且斜率 大于0.15 可以视为触发选择事件
                if (Math.abs(left) > getMeasuredWidth() / 3 && Math.abs(sloap) > 0.15) {
                    mReleasedPoint.x = left;
                    mReleasedPoint.y = top;
                    isDraging = false;
                } else {
                    // 调整剩下view的大小和位置
                    ensureChildView();
                    isDraging = true;
                    // 剩下的子view，缩放 平移,-2 未考虑只有一个view 的情况，还有visibilty= gone
                    float rate = left * 1.0f / (getMeasuredWidth() / 3);
                    float a = Math.min(1, Math.max(0, Math.abs(rate)));
                    int offset = ViewExKt.dp2px(TinderStackLayout.this, DEFAULT_OFFSET);
                    // 这里为什么会有判断 i = 0，i= 1，是因为如果释放了会把view remove
                    // 所以这里会做判断保证布局底部的显示，从1开始最底部view 不会有变化
                    for (int i = getChildCount() < showCount ? 0 : 1; i < getChildCount() - 1; i++) {
                        View child = getChildAt(i);
                        // ds 代表缩放，分为两部分计算 + 号前面是布局的时候应该缩放多少，后段是跟随滑动
                        // 缩放的变化量
                        float ds = 1 - DEFAULT_SCALE * (getChildCount() - 1 - i) + DEFAULT_SCALE * a;
                        // 同根据布局时固定的的偏移量 - 变化量
                        float doffset = (getChildCount() - 1 - i) * offset - offset * a;
                        // 同布局时缩放的偏移量 - 变化量
                        float yOffset = child.getMeasuredHeight() * DEFAULT_SCALE * (getChildCount() - 1 - i - a) / 2;
                        child.setScaleY(ds);
                        child.setScaleX(ds);
                        child.setTranslationY(doffset + yOffset);

                        L.d(TAG, "ds : " + ds + " doffset : " + doffset + " a : " + a);
                    }

                    //rate > 0  右滑动 else  // 左滑动
                    // 旋转,根据现在的比例
                    L.d(TAG,"onViewPositionChange rate : " + rate);
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
                // 停止滑动的时候，将最后一个view 角度设置为0，因为算斜率的
                // 的方式最后滑动完成会有微小的偏差
                if (state == ViewDragHelper.STATE_IDLE && isDraging) {
                    View childTop = getChildAt(getChildCount() - 1);
                    if (childTop != null) {
                        childTop.setRotation(0);
                    }
                }
            }
        });
    }

    /**
     * 确保有足够的view 符合条件
     */
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

            // i > 1 是因为确保最后两个view是重叠在一起
            if (i > 1 || getChildCount() < showCount) {
                level++;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
//        int margin = ViewExKt.dp2px(this, DEFAULT_MARGIN);

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

    private void onAddView() {
        if (adapter != null) {
            if (adapter.getView() == null) {
                return;
            }
            addView(adapter.getView(),0);
        }
    }

    private void onChoosePick(float sloap) {
        if (chooseListener != null) {
            chooseListener.onPicked(sloap > 0 ? 1 : 0);
        }
    }

    public interface BaseCardAdapter {
        int getItemCount();

        View getView();
    }

    public interface OnChooseListener{
        // 1 为右边滑动 0 为左边滑动
        void onPicked(int directon);
    }
}
