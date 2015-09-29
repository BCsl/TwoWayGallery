package com.android.hellocsl.twowaygallery.gallery.flow;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
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
    private final boolean DEBUG = true;
    private final float DEFAULT_UNSELECTED_SCALE = 1.0f;
    private final float DEFAULT_COVERAGE = 0.1f;
    private Matrix mHorizontalMatrix, mVerticalMatrix;
    private final float mUnselectedScale = 0.8f;
    private float mCoverage = DEFAULT_COVERAGE;//覆盖率

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
        if (getOrientation() == VERTICAL) {
            final int coverFlowCenter = getVerticalCenterOfGallery();
            final int childWidth = child.getWidth();
            final int childHeight = child.getHeight();
            final int childCenter = child.getTop() + childHeight / 2;
            float w = childHeight;
            boolean bottomOfCenter = childCenter - coverFlowCenter >= 0 ? true : false;
            t.clear();
            mVerticalMatrix = t.getMatrix();
            //线性变化
            float effectAmount = 1.0f * Math.abs(childCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
            if (mUnselectedScale != 1) {
                //居中缩放
                final float translateX = childWidth / 2.0f;
                final float translateY = childHeight / 2.0f;
                mVerticalMatrix.preTranslate(-translateX, -translateY);
                mVerticalMatrix.postScale(effectAmount, effectAmount);
                mVerticalMatrix.postTranslate(translateX, translateY);
            }
            if (bottomOfCenter) {
                float preEffectAmount = 0;
                float preCenter = childCenter - childHeight;
                preEffectAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                float translateY = 0;
                translateY = (1 - effectAmount) / 2.0f * childHeight;
                while (preCenter > coverFlowCenter && preEffectAmount < 1) {
                    translateY += (1 - preEffectAmount) * childHeight;
                    preCenter -= childHeight;
                    preEffectAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                }
                mVerticalMatrix.postTranslate(0, -translateY);
            } else {
                float nextEffectAmount = 0;
                float nextCenter = childCenter + childHeight;
                nextEffectAmount = 1.0f * Math.abs(nextCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                float translateY = 0;
                translateY = (1 - effectAmount) / 2.0f * childHeight;
                while (nextCenter < coverFlowCenter && nextEffectAmount < 1) {
                    translateY += (1 - nextEffectAmount) * childHeight;
                    nextCenter += childHeight;
                    nextEffectAmount = 1.0f * Math.abs(nextCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                }
                mVerticalMatrix.postTranslate(0, translateY);
            }
        } else {
            final int coverFlowCenter = getHorizontalCenterOfGallery();
            final int childWidth = child.getWidth();
            final int childHeight = child.getHeight();
            final int childCenter = child.getLeft() + childWidth / 2;
            float w = childWidth;
            boolean rightOfCenter = childCenter - coverFlowCenter >= 0 ? true : false;
            t.clear();
            mHorizontalMatrix = t.getMatrix();
            //线性变化
            float effectAmount = 1.0f * Math.abs(childCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
            if (mUnselectedScale != 1) {
                //居中缩放
                final float translateX = childWidth / 2.0f;
                final float translateY = childHeight / 2.0f;
                mHorizontalMatrix.preTranslate(-translateX, -translateY);
                mHorizontalMatrix.postScale(effectAmount, effectAmount);
                mHorizontalMatrix.postTranslate(translateX, translateY);
            }
            if (rightOfCenter) {
                float preEffectAmount = 0;
                float preCenter = childCenter - childWidth;
                preEffectAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                float translateX = 0;
                translateX = (1 - effectAmount) / 2.0f * childWidth;
                while (preCenter > coverFlowCenter && preEffectAmount < 1) {
                    translateX += (1 - preEffectAmount) * childWidth;
                    preCenter -= childWidth;
                    preEffectAmount = 1.0f * Math.abs(preCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                }
                mHorizontalMatrix.postTranslate(-translateX, 0);
            } else {
                float nextEffectAmount = 0;
                float nextCenter = childCenter + childWidth;
                nextEffectAmount = 1.0f * Math.abs(nextCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                float translateX = 0;
                translateX = (1 - effectAmount) / 2.0f * childWidth;
                while (nextCenter < coverFlowCenter && nextEffectAmount < 1) {
                    if (DEBUG)
                        Log.d(TAG, "1nextAmount:" + nextEffectAmount + ",1nextEffectAmount:" + nextEffectAmount);
                    translateX += (1 - nextEffectAmount) * childWidth;
                    if (DEBUG)
                        Log.d(TAG, "1translateX:" + translateX);
                    nextCenter += childWidth;
                    nextEffectAmount = 1.0f * Math.abs(nextCenter - coverFlowCenter) * (mUnselectedScale - 1) / w + 1;
                    if (DEBUG)
                        Log.d(TAG, "2nextAmount:" + nextEffectAmount + ",2nextEffectAmount:" + nextEffectAmount);
                }
                if (DEBUG)
                    Log.d(TAG, "translateX:" + translateX);
                mHorizontalMatrix.postTranslate(translateX, 0);
            }
        }
        return true;
    }


}
