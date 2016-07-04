package com.yaea.menu.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.yaea.menu.R;

/**
 * Created by 慧客 on 2016/7/4.
 */
public class MoonMenu extends ViewGroup implements View.OnClickListener {
    /**
     * 菜单位置 默认右下角
     */
    private static final int LEFT_TOP = 0;
    private static final int LEFT_BOTTOM = 1;
    private static final int RIGHT_TOP = 2;
    private static final int RIGHT_BOTTOM = 3;

    private Position mPosition = Position.RIGHT_BOTTOM;

    public enum Position {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    /**
     * 菜单弧度半径
     */
    private int mRadius;
    /**
     * 菜单状态 默认关闭
     */
    private Status mCurrentStatus = Status.CLOSE;

    public enum Status {
        OPEN, CLOSE
    }

    /**
     * 菜单主按钮
     */
    private View mCButton;

    /**
     * 子菜单回调接口
     */
    private OnMenuItemClickListener mMenuItemClickListener;

    public interface OnMenuItemClickListener {
        void onClick(View view, int position);
    }

    public void setMenuItemClickListener(OnMenuItemClickListener mMenuItemClickListener) {
        this.mMenuItemClickListener = mMenuItemClickListener;
    }

    public MoonMenu(Context context) {
        this(context, null);
    }

    public MoonMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoonMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, getResources().getDisplayMetrics());

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MoonMenu,
                defStyleAttr, 0);

        int position = ta.getInt(R.styleable.MoonMenu_position, RIGHT_BOTTOM);
        switch (position) {
            case LEFT_TOP:
                mPosition = Position.LEFT_TOP;
                break;
            case LEFT_BOTTOM:
                mPosition = Position.LEFT_BOTTOM;
                break;
            case RIGHT_TOP:
                mPosition = Position.RIGHT_TOP;
                break;
            case RIGHT_BOTTOM:
                mPosition = Position.RIGHT_BOTTOM;
                break;
        }

        mRadius = (int) ta.getDimension(R.styleable.MoonMenu_radius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                        getResources().getDisplayMetrics()));
        Log.e("TAG", "position = " + mPosition + " , radius =  " + mRadius);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            layoutCButton();
            int childCount = getChildCount();
            for (int i = 0; i < childCount - 1; i++) {
                View child = getChildAt(i + 1);
                child.setVisibility(GONE);
                int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (childCount - 2) * i));
                int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (childCount - 2) * i));
                int cWidht = child.getMeasuredWidth();
                int cHeight = child.getMeasuredHeight();
                if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM) {
                    ct = getMeasuredHeight() - cHeight - ct;
                }
                if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM) {
                    cl = getMeasuredWidth() - cWidht - cl;
                }
                child.layout(cl, ct, cl + cWidht, ct + cHeight);
            }
        }
    }

    @Override
    public void onClick(View view) {
        rotateCButton(view, 0f, 360f, 300);
        toggleMenu(300);
    }

    private void toggleMenu(int duration) {
        int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            final View childView = getChildAt(i + 1);
            childView.setVisibility(VISIBLE);
            int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
            int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));
            int xFlag = 1;
            int yFlag = 1;
            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM) {
                xFlag = -1;
            }
            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP) {
                yFlag = -1;
            }
            AnimationSet animSet = new AnimationSet(true);
            Animation tranAnim = null;
            if (mCurrentStatus == Status.CLOSE) {
                tranAnim = new TranslateAnimation(xFlag * cl, 0, yFlag * ct, 0);
                childView.setClickable(true);
                childView.setFocusable(true);
            } else {
                tranAnim = new TranslateAnimation(0, xFlag * cl, 0, yFlag * ct);
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            tranAnim.setFillAfter(true);
            tranAnim.setDuration(duration);
            tranAnim.setStartOffset((i * 100) / count);
            tranAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCurrentStatus == Status.CLOSE) {
                        childView.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            RotateAnimation rotateAnim = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(duration);
            rotateAnim.setFillAfter(true);
            animSet.addAnimation(rotateAnim);
            animSet.addAnimation(tranAnim);
            childView.startAnimation(animSet);

            final int position = i + 1;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMenuItemClickListener != null) {
                        mMenuItemClickListener.onClick(view, position);
                    }
                    menuItemAnim(position - 1);
                    changeStatus();
                }
            });
        }
        changeStatus();
    }

    private void changeStatus() {
        mCurrentStatus = (mCurrentStatus == Status.CLOSE ? Status.OPEN : Status.CLOSE);
    }

    private void menuItemAnim(int position) {
        for (int i = 0; i < getChildCount() - 1; i++) {
            View childView = getChildAt(i + 1);
            if (i == position) {
                childView.startAnimation(scaleBigAnim(300));
            } else {
                childView.startAnimation(scaleSmallAnim(300));
            }
            childView.setClickable(false);
            childView.setFocusable(false);
        }
    }

    private Animation scaleBigAnim(int duration) {
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 4.0f, 1.0f, 4.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphAnim = new AlphaAnimation(1f, 0.0f);
        animSet.addAnimation(scaleAnim);
        animSet.addAnimation(alphAnim);

        animSet.setDuration(duration);
        animSet.setFillAfter(true);
        return animSet;
    }

    private Animation scaleSmallAnim(int duration) {
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0.0f);
        animSet.addAnimation(scaleAnim);
        animSet.addAnimation(alphaAnim);
        animSet.setDuration(duration);
        animSet.setFillAfter(true);
        return animSet;
    }

    private void rotateCButton(View view, float start, float end, int duration) {
        RotateAnimation anim = new RotateAnimation(start, end, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    private void layoutCButton() {
        mCButton = getChildAt(0);
        mCButton.setOnClickListener(this);

        int left = 0;
        int top = 0;
        int width = mCButton.getMeasuredWidth();
        int height = mCButton.getMeasuredHeight();

        switch (mPosition) {
            case LEFT_TOP:
                left = 0;
                top = 0;
                break;
            case LEFT_BOTTOM:
                left = 0;
                top = getMeasuredHeight() - height;
                break;
            case RIGHT_TOP:
                left = getMeasuredWidth() - width;
                top = 0;
                break;
            case RIGHT_BOTTOM:
                left = getMeasuredWidth() - width;
                top = getMeasuredHeight() - height;
                break;
        }

        mCButton.layout(left, top, left + width, top + height);
    }
}
