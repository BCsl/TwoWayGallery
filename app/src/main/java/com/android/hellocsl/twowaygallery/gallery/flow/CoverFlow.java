package com.android.hellocsl.twowaygallery.gallery.flow;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.nfc.Tag;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;

import com.android.hellocsl.twowaygallery.gallery.TwoWayGallery;

/**
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/9/28 0028.
 */
public class CoverFlow extends TwoWayGallery {
    private final String TAG = this.getClass().getSimpleName();
    private final float DEFAULT_UNSELECTED_SCALE = 1.0f;

    private final float mUnselectedScale = 0.8f;

    public CoverFlow(Context context) {
        super(context);
        init();
    }

    public CoverFlow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverFlow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CoverFlow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setStaticTransformationsEnabled(true);
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        super.getChildStaticTransformation(child, t);
        Log.d(TAG, "getChildStaticTransformation");
        // Since Jelly Bean childs won't get invalidated automatically, needs to be added for the smooth coverflow animation
        if (getOrientation() == HORIZONTAL) {
            final int coverFlowWidth = this.getWidth();
            final int coverFlowCenter = getHorizontalCenterOfGallery();
            final int childWidth = child.getWidth();
            final int childHeight = child.getHeight();
            final int childCenter = child.getLeft() + childWidth / 2;

            final int actionDistance = (int) ((coverFlowWidth + childWidth) / 2.0f);
            // Calculate the abstract amount for all effects.
            float effectsAmount = Math.min(
                    1.0f,
                    Math.max(-1.0f, (1.0f / actionDistance)
                            * (childCenter - coverFlowCenter)));

            t.clear();
            t.setTransformationType(Transformation.TYPE_BOTH);

            if (getUnselectedAlpha() != 1) {
                // Pass over saturation to the wrapper.
                final float alpha = (getUnselectedAlpha() - 1)
                        * Math.abs(effectsAmount) + 1;
                t.setAlpha(alpha);
            }
//            t.setAlpha(child == getSelectedView() ? 1.0f : getUnselectedAlpha());

            final Matrix scaleMatrix = t.getMatrix();

            if (mUnselectedScale != 1) {
                final float zoomAmount = 1f / 2f * (1 - Math.abs(effectsAmount))
                        * (1 - Math.abs(effectsAmount))
                        * (1 - Math.abs(effectsAmount)) + 0.5f;//缩放系数

                final float translateX = childWidth / 2.0f;
                final float translateY = childHeight / 2.0f;
                scaleMatrix.preTranslate(-translateX, -translateY);//并不改变Pivot
                scaleMatrix.postScale(zoomAmount, zoomAmount);
                scaleMatrix.postTranslate(translateX, translateY);

                if (effectsAmount != 0) {
                    double point = 0.4;
                    boolean rightOfCenter = effectsAmount > 0;
                    int indicator = rightOfCenter ? -1 : 1;
                    double translateFactor = (-1f / (point * point)
                            * (Math.abs(effectsAmount) - point)
                            * (Math.abs(effectsAmount) - point) + 1) * indicator;
                    scaleMatrix
                            .postTranslate(
                                    (float) (Dp2Px(getContext(), 25) * translateFactor),
                                    0);

                }
//                if (effectsAmount != 0 && effectsAmount != 0) {
//                    boolean rightOfCenter = effectsAmount > 0;
//                    int indicator = rightOfCenter ? -1 : 1;
//                    scaleMatrix
//                            .postTranslate(
//                                    indicator * (1 - zoomAmount) / 2 * childWidth,
//                                    0);
//                }

            }

        } else {
//            final int coverFlowHeight = this.getHeight();
//            final int coverFlowCenter = getVerticalCenterOfGallery();
//            final int childWidth = child.getWidth();
//            final int childHeight = child.getHeight();
//            final int childCenter = child.getTop() + childHeight / 2;
//
//            final int actionDistance = (int) ((coverFlowHeight + childHeight) / 2.0f);
//            // Calculate the abstract amount for all effects.
//            float effectsAmount = Math.min(
//                    1.0f,
//                    Math.max(-1.0f, (1.0f / actionDistance)
//                            * (childCenter - coverFlowCenter)));
//
//            t.clear();
//            t.setTransformationType(Transformation.TYPE_BOTH);
//
//
//            final Matrix scaleMatrix = t.getMatrix();
//
//            if (mUnselectedScale != 1) {
//                final float zoomAmount = 1f / 2f * (1 - Math.abs(effectsAmount))
//                        * (1 - Math.abs(effectsAmount))
//                        * (1 - Math.abs(effectsAmount)) + 0.5f;//缩放系数
//
//                final float translateX = childWidth / 2.0f;
//                final float translateY = childHeight / 2.0f;
//                scaleMatrix.preTranslate(-translateX, -translateY);
//                scaleMatrix.postScale(zoomAmount, zoomAmount);
//                scaleMatrix.postTranslate(translateX, translateY);
//            }
            final int coverFlowCenter = getVerticalCenterOfGallery();
            final int coverFlowHeight = this.getHeight();
            final int childWidth = child.getWidth();
            final int childHeight = child.getHeight();
            final int childCenter = child.getTop() + childHeight / 2;
            float w = childHeight * 2;
            boolean rightOfCenter = childCenter - coverFlowCenter >= 0 ? true : false;
            float effectAmount = 1.0f * Math.abs(childCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
            t.clear();
            t.setTransformationType(Transformation.TYPE_BOTH);
            if (getUnselectedAlpha() != 1) {
                // Pass over saturation to the wrapper.
                final float alpha = (getUnselectedAlpha() - 1)
                        * Math.abs(effectAmount) + 1;
                t.setAlpha(alpha);
            }
            final Matrix scaleMatrix = t.getMatrix();
            if (mUnselectedScale != 1) {
                final float translateX = childWidth / 2.0f;
                final float translateY = childHeight / 2.0f;
                scaleMatrix.preTranslate(-translateX, -translateY);
                scaleMatrix.postScale(effectAmount, effectAmount);
                scaleMatrix.postTranslate(translateX, translateY);
            }
            if (rightOfCenter) {
                float previousAmount = 0;
                float preCenter = childCenter - childHeight;
                previousAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                int translateY = 0;
                while (preCenter > coverFlowCenter) {
                    translateY += (1 - previousAmount) / 2 * childHeight;
                    preCenter -= childHeight;
                    previousAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                }
                translateY += (1 - effectAmount) / 2 * childHeight;
                Log.d(TAG, "rightOfCenter:" + -translateY);
                scaleMatrix.postTranslate(0, -translateY);
            } else {
                float nextAmount = 0;
                float nextCenter = childCenter + childHeight;
                nextAmount = 1.0f * Math.abs(nextCenter + coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                int translateY = 0;
                while (nextCenter < coverFlowCenter) {
                    translateY += (1 - nextAmount) / 2 * childHeight;
                    nextCenter += childHeight;
                    nextAmount = 1.0f * Math.abs(nextCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                }
                translateY += (1 - effectAmount) / 2 * childHeight;
                scaleMatrix.postTranslate(0, translateY);
                Log.d(TAG, "leftOfCenter:" + -translateY);
            }

        }

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            child.postInvalidate();
        }
        return true;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {

        int selectedIndex = getSelectedItemPosition()
                - getFirstVisiblePosition();
        if (i < selectedIndex) {
            return i;
        } else if (i >= selectedIndex) {
            return childCount - 1 - i + selectedIndex;
        } else {
            return i;
        }
    }

    /**
     * @param context
     * @param dp
     * @return
     */

    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * @param context
     * @param px
     * @return
     */
    public static int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
