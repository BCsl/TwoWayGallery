package com.android.hellocsl.twowaygallery.gallery;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.android.hellocsl.twowaygallery.R;

/**
 * A view that shows items in a center-locked, horizontally or vertical scrolling list.
 * <p>
 * The default values for the Gallery assume you will be using
 * {@link android.R.styleable#Theme_galleryItemBackground} as the background for
 * each View given to the Gallery from the Adapter. If you are not doing this,
 * you may need to adjust some Gallery properties, such as the spacing.
 * <p>
 * Views given to the Gallery should use {@link TwoWayGallery.LayoutParams} as their
 * layout parameters type.
 *
 * @attr ref android.R.styleable#TwoWayGallery_animationDuration
 * @attr ref android.R.styleable#TwoWayGallery_spacing
 * @attr ref android.R.styleable#TwoWayGallery_gravity
 * <p>
 * <p>
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/9/24 0024.
 */
public class TwoWayGallery extends TwoWaySpinner implements GestureDetector.OnGestureListener {

    private static final String TAG = "TwoWayGallery";


    private static final boolean localLOGV = false;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    /**
     * Duration in milliseconds from the start of a scroll during which we're
     * unsure whether the user is scrolling or flinging.
     */
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

    /**
     * Horizontal spacing between items.
     */
    private int mSpacing = 0;

    /**
     * How long the transition animation should run when a child view changes
     * position, measured in milliseconds.
     */
    private int mAnimationDuration = 400;

    /**
     * The alpha of items that are not selected.
     */
    private float mUnselectedAlpha;

    /**
     * Left most edge of a child seen so far during layout.
     */
    private int mLeftMost;

    /**
     * Right most edge of a child seen so far during layout.
     */
    private int mRightMost;

    /**
     * Top most edge of a child seen so far during layout.
     */
    private int mTopMost;

    /**
     * Bottom most edge of a child seen so far during layout.
     */
    private int mBottomMost;

    private int mGravity;

    /**
     * Helper for detecting touch gestures.
     */
    private GestureDetector mGestureDetector;

    /**
     * The position of the item that received the user's down touch.
     */
    private int mDownTouchPosition;

    /**
     * The view of the item that received the user's down touch.
     */
    private View mDownTouchView;

    /**
     * Executes the delta scrolls from a fling or scroll movement.
     */
    private FlingRunnable mFlingRunnable = new FlingRunnable();

    /**
     * Sets mSuppressSelectionChanged = false. This is used to set it to false
     * in the future. It will also trigger a selection changed.
     */
    private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
        @Override
        public void run() {
            mSuppressSelectionChanged = false;
            selectionChanged();
        }
    };

    /**
     * When fling runnable runs, it resets this to false. Any method along the
     * path until the end of its run() can set this to true to abort any
     * remaining fling. For example, if we've reached either the leftmost or
     * rightmost item, we will set this to true.
     */
    private boolean mShouldStopFling;

    /**
     * The currently selected item's child.
     */
    private View mSelectedChild;

    /**
     * Whether to continuously callback on the item selected listener during a
     * fling.
     */
    private boolean mShouldCallbackDuringFling = true;

    /**
     * Whether to callback when an item that is not selected is clicked.
     */
    private boolean mShouldCallbackOnUnselectedItemClick = true;

    /**
     * If true, do not callback to item selected listener.
     */
    private boolean mSuppressSelectionChanged;

    /**
     * If true, we have received the "invoke" (center or enter buttons) key
     * down. This is checked before we action on the "invoke" key up, and is
     * subsequently cleared.
     */
    private boolean mReceivedInvokeKeyDown;

    private AdapterContextMenuInfo mContextMenuInfo;

    /**
     * If true, this onScroll is the first for this user's drag (remember, a
     * drag sends many onScrolls).
     */
    private boolean mIsFirstScroll;

    /**
     * If true, mFirstPosition is the position of the rightmost child, and
     * the children are ordered right to left.
     */
    private boolean mIsRtl = false;

    /**
     * Offset between the center of the selected child view and the center of the Gallery.
     * Used to reset position correctly during layout.
     */
    private int mSelectedCenterOffset;
    /**
     *
     */
    private int mOrientation = HORIZONTAL;


    public TwoWayGallery(Context context) {
        this(context, null);
    }

    public TwoWayGallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.galleryStyle);
    }

    public TwoWayGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TwoWayGallery(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
        // We draw the selected item last (because otherwise the item to the
        // right overlaps it)
//        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
//
//        mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(true);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.TwoWayGalleryGallery, defStyleAttr, defStyleRes);

        int index = a.getInt(R.styleable.TwoWayGalleryGallery_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }
        int animationDuration =
                a.getInt(R.styleable.TwoWayGalleryGallery_animationDuration, -1);
        if (animationDuration > 0) {
            setAnimationDuration(animationDuration);
        }

        int orientation = a.getInt(R.styleable.TwoWayGalleryGallery_orientation, HORIZONTAL);
        if (orientation >= 0) {
            setOrientation(orientation);
        }
        int spacing =
                a.getDimensionPixelOffset(R.styleable.TwoWayGalleryGallery_spacing, 0);
        setSpacing(spacing);

        float unselectedAlpha = a.getFloat(
                R.styleable.TwoWayGalleryGallery_unselectedAlpha, 0.5f);
        setUnselectedAlpha(unselectedAlpha);

        a.recycle();
    }

    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public float getUnselectedAlpha() {
        return mUnselectedAlpha;
    }

    /**
     * Whether or not to callback on any {@link #getOnItemSelectedListener()}
     * while the items are being flinged. If false, only the final selected item
     * will cause the callback. If true, all items between the first and the
     * final will cause callbacks.
     *
     * @param shouldCallback Whether or not to callback on the listener while
     *                       the items are being flinged.
     */
    public void setCallbackDuringFling(boolean shouldCallback) {
        mShouldCallbackDuringFling = shouldCallback;
    }

    /**
     * Whether or not to callback when an item that is not selected is clicked.
     * If false, the item will become selected (and re-centered). If true, the
     * {@link #getOnItemClickListener()} will get the callback.
     *
     * @param shouldCallback Whether or not to callback on the listener when a
     *                       item that is not selected is clicked.
     * @hide
     */
    public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
        mShouldCallbackOnUnselectedItemClick = shouldCallback;
    }

    /**
     * Sets how long the transition animation should run when a child view
     * changes position. Only relevant if animation is turned on.
     *
     * @param animationDurationMillis The duration of the transition, in
     *                                milliseconds.
     * @attr ref android.R.styleable#Gallery_animationDuration
     */
    public void setAnimationDuration(int animationDurationMillis) {
        mAnimationDuration = animationDurationMillis;
    }

    /**
     * Sets the spacing between items in a Gallery
     *
     * @param spacing The spacing in pixels between items in the Gallery
     * @attr ref android.R.styleable#Gallery_spacing
     */
    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }

    /**
     * Sets the alpha of items that are not selected in the Gallery.
     *
     * @param unselectedAlpha the alpha for the items that are not selected.
     * @attr ref android.R.styleable#Gallery_unselectedAlpha
     */
    public void setUnselectedAlpha(float unselectedAlpha) {
        mUnselectedAlpha = unselectedAlpha;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {

        t.clear();
        t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

        return true;
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        // Only 1 item is considered to be selected
        return 1;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        // Current scroll position is the same as the selected position
        return mSelectedPosition;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        // Scroll range is the same as the item count
        return mItemCount;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        /*
         * Gallery expects Gallery.LayoutParams.
         */
        return new TwoWayGallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        /*
         * Remember that we are in layout to prevent more layout request from
         * being generated.
         */
        mInLayout = true;
        layout(0, false);
        mInLayout = false;
    }

    @Override
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /**
     * Tracks a motion scroll. In reality, this is used to do just about any
     * movement to items (touch scroll, arrow-key scroll, set an item as selected).
     *
     * @param deltaX Change in X from the previous event.
     */
    void trackHorizontalMotionScroll(int deltaX) {

        if (getChildCount() == 0) {
            return;
        }

        boolean toLeft = deltaX < 0;//show right

        int limitedDeltaX = getHorizontalLimitedMotionScrollAmount(toLeft, deltaX);
        if (limitedDeltaX != deltaX) {
            // The above call returned a limited amount, so stop any scrolls/flings
            mFlingRunnable.endFling(false);
            onFinishedMovement();
        }

        offsetChildrenLeftAndRight(limitedDeltaX);

        detachHorizontalOffScreenChildren(toLeft);

        if (toLeft) {
            // If moved left, there will be empty space on the right
            fillToGalleryRight();
        } else {
            // Similarly, empty space on the left
            fillToGalleryLeft();
        }

        // Clear unused views
        mRecycler.clear();

        setHorizontalSelectionToCenterChild();

        final View selChild = mSelectedChild;
        if (selChild != null) {
            final int childLeft = selChild.getLeft();
            final int childCenter = selChild.getWidth() / 2;
            final int galleryCenter = getWidth() / 2;
            mSelectedCenterOffset = childLeft + childCenter - galleryCenter;
        }

        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.

        invalidate();
    }

    /**
     * Tracks a motion scroll. In reality, this is used to do just about any
     * movement to items (touch scroll, arrow-key scroll, set an item as selected).
     *
     * @param deltaY Change in Y from the previous event.
     */
    void trackVerticalMotionScroll(int deltaY) {

        if (getChildCount() == 0) {
            return;
        }

        boolean toTop = deltaY < 0;//when toTop==true then show bottom

        int limitedDeltaY = getVerticalLimitedMotionScrollAmount(toTop, deltaY);


        if (limitedDeltaY != deltaY) {
            // The above call returned a limited amount, so stop any scrolls/flings
            mFlingRunnable.endFling(false);
            onFinishedMovement();
        }
//        Log.d(TAG, "trackVerticalMotionScroll==>" + (toTop ? "showBottom" : "showTop") + ",deltaY:" + deltaY + ",limitedDeltaY：" + limitedDeltaY);
        offsetChildrenTopAndBottom(limitedDeltaY);

        detachVerticalOffScreenChildren(toTop);

        if (toTop) {
            // If moved top, there will be empty space on the bottom
            fillToGalleryBottom();
        } else {
            // Similarly, empty space on the top
            fillToGalleryTop();
        }
        // Clear unused views
        mRecycler.clear();

        setVerticalSelectionToCenterChild();

        final View selChild = mSelectedChild;
        if (selChild != null) {
            final int childTop = selChild.getTop();
            final int childCenter = selChild.getHeight() / 2;
            final int galleryCenter = getHeight() / 2;
            mSelectedCenterOffset = childTop + childCenter - galleryCenter;//use for animation
        }
        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.

        invalidate();
    }

    /**
     * @param motionToLeft true:show right,false:show left
     * @param deltaX
     * @return
     */
    int getHorizontalLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

        if (extremeChild == null) {
            return deltaX;//can not scroll to the bound,just scroll what it want
        }

        int extremeChildCenter = getHorizontalCenterOfView(extremeChild);
        int galleryCenter = getHorizontalCenterOfGallery();

        if (motionToLeft) {
            if (extremeChildCenter <= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        } else {
            if (extremeChildCenter >= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        }

        int centerDifference = galleryCenter - extremeChildCenter;

        return motionToLeft
                ? Math.max(centerDifference, deltaX)
                : Math.min(centerDifference, deltaX);
    }

    /**
     * @param motionToTop true:show bottom,false:show top
     * @param deltaY
     * @return
     */
    int getVerticalLimitedMotionScrollAmount(boolean motionToTop, int deltaY) {
        //when motionToTop==true,deltalY<0,showBottom
        //compare with the last item when action showing bottom of gallery
        int extremeItemPosition = motionToTop ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

        if (extremeChild == null) {//can not scroll to the bound,just scroll what it want
            return deltaY;
        }

        int extremeChildCenter = getVerticalCenterOfView(extremeChild);
        int galleryCenter = getVerticalCenterOfGallery();

        if (motionToTop) {
            if (extremeChildCenter <= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        } else {
            if (extremeChildCenter >= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        }

        int centerDifference = galleryCenter - extremeChildCenter;

        return motionToTop
                ? Math.max(centerDifference, deltaY)
                : Math.min(centerDifference, deltaY);
    }

    /**
     * Offset the horizontal location of all children of this view by the
     * specified number of pixels.
     *
     * @param offset the number of pixels to offset
     */
    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }

    /**
     * Offset the Vertial location of all children of this view by the
     * specified number of pixels.
     *
     * @param offset the number of pixels to offset
     */
    private void offsetChildrenTopAndBottom(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetTopAndBottom(offset);
        }
    }

    /**
     * @return The center of this Gallery.
     */
    protected final int getHorizontalCenterOfGallery() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /**
     * @return The center of this Gallery.
     */
    protected final int getVerticalCenterOfGallery() {
        return (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
    }

    /**
     * @return The center of the given view.
     */
    protected final int getHorizontalCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }

    protected final int getVerticalCenterOfView(View v) {
        return v.getTop() + v.getHeight() / 2;
    }

    /**
     * Detaches children that are off the screen (i.e.: Gallery bounds).
     *
     * @param toLeft Whether to detach children to the left of the Gallery, or
     *               to the right.
     */
    private void detachHorizontalOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;//index of first view showed in window
        int count = 0;//unshowed views that befort the first showed 's count

        if (toLeft) {//show left,
            final int galleryLeft = getPaddingLeft();
            for (int i = 0; i < numChildren; i++) {
                int n = mIsRtl ? (numChildren - 1 - i) : i;
                final View child = getChildAt(n);
                //find the first view showed in window
                if (child.getRight() >= galleryLeft) {
                    break;
                } else {
                    //put the unshowed views to Recycler
                    start = n;
                    count++;
                    mRecycler.put(firstPosition + n, child);
                }
            }
            if (!mIsRtl) {
                start = 0;
            }
        } else {
            final int galleryRight = getWidth() - getPaddingRight();
            for (int i = numChildren - 1; i >= 0; i--) {
                int n = mIsRtl ? numChildren - 1 - i : i;
                final View child = getChildAt(n);
                if (child.getLeft() <= galleryRight) {
                    break;
                } else {
                    start = n;
                    count++;
                    mRecycler.put(firstPosition + n, child);
                }
            }
            if (mIsRtl) {
                start = 0;
            }
        }

        detachViewsFromParent(start, count);

        if (toLeft != mIsRtl) {
            mFirstPosition += count;
        }
    }

    /**
     * Detaches children that are off the screen (i.e.: Gallery bounds).
     *
     * @param toTop Whether to detach children to the top of the Gallery, or
     *              to the bottom.
     */
    private void detachVerticalOffScreenChildren(boolean toTop) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;//index of first view showed in window
        int count = 0;//unshowed views that befort the first showed 's count

        if (toTop) {//show bottom of gallery and detach top of gallery
            final int galleryTop = getPaddingTop();
            for (int i = 0; i < numChildren; i++) {
                int n = i;
                final View child = getChildAt(n);
                //find the first view showed in window
                if (child.getBottom() >= galleryTop) {
                    break;
                } else {
                    //put the unshowed views to Recycler
                    count++;
                    mRecycler.put(firstPosition + n, child);
                }
            }
        } else {
            //show top of gallery and detach bottom of gallery
            final int galleryBottom = getHeight() - getPaddingBottom();
            for (int i = numChildren - 1; i >= 0; i--) {
                int n = i;
                final View child = getChildAt(n);
                if (child.getTop() <= galleryBottom) {
                    break;
                } else {
                    start = n;
                    count++;
                    mRecycler.put(firstPosition + n, child);
                }
            }
        }

        detachViewsFromParent(start, count);
        if (toTop) {
            //show bottom of gallery
            mFirstPosition += count;
        }
    }

    /**
     * Scrolls the items so that the selected item is in its 'slot' (its center
     * is the gallery's center).
     */
    private void scrollIntoSlots() {
        Log.d(TAG, "scrollIntoSlots");
        if (getChildCount() == 0 || mSelectedChild == null) return;
        int selectedCenter = 0;
        int targetCenter = 0;
        if (mOrientation == HORIZONTAL) {
            selectedCenter = getHorizontalCenterOfView(mSelectedChild);
            targetCenter = getHorizontalCenterOfGallery();
        } else {
            selectedCenter = getVerticalCenterOfView(mSelectedChild);
            targetCenter = getVerticalCenterOfGallery();
        }
        int scrollAmount = targetCenter - selectedCenter;
        if (scrollAmount != 0) {
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            onFinishedMovement();
        }
    }

    private void onFinishedMovement() {
        Log.d(TAG, "onFinishedMovement");
        if (mSuppressSelectionChanged) {
            Log.d(TAG, "callback selection changed");
            mSuppressSelectionChanged = false;
            // We haven't been callbacking during the fling, so do it now
            super.selectionChanged();
        }
        mSelectedCenterOffset = 0;
        invalidate();
    }

    @Override
    void selectionChanged() {
        if (!mSuppressSelectionChanged) {
            Log.d(TAG, "selectionChanged");
            super.selectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the
     * selected child.
     */
    private void setHorizontalSelectionToCenterChild() {

        View selView = mSelectedChild;
        if (mSelectedChild == null) return;

        int galleryCenter = getHorizontalCenterOfGallery();

        // Common case where the current selected position is correct
        if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
            return;
        }

        // TODO better search
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {

            View child = getChildAt(i);

            if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
                // This child is in the center
                newSelectedChildIndex = i;
                break;
            }

            int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
                    Math.abs(child.getRight() - galleryCenter));
            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }

        int newPos = mFirstPosition + newSelectedChildIndex;

        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the
     * selected child.
     */
    private void setVerticalSelectionToCenterChild() {

        View selView = mSelectedChild;
        if (mSelectedChild == null) return;

        int galleryCenter = getVerticalCenterOfGallery();

        // Common case where the current selected position is correct
        if (selView.getTop() <= galleryCenter && selView.getBottom() >= galleryCenter) {
            return;
        }

        // TODO better search
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {

            View child = getChildAt(i);

            if (child.getTop() <= galleryCenter && child.getBottom() >= galleryCenter) {
                // This child is in the center
                newSelectedChildIndex = i;
                break;
            }

            int childClosestEdgeDistance = Math.min(Math.abs(child.getTop() - galleryCenter),
                    Math.abs(child.getBottom() - galleryCenter));

            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }

        int newPos = mFirstPosition + newSelectedChildIndex;

        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }

    /**
     * Creates and positions all views for this Gallery.
     * <p>
     * We layout rarely, most of the time {@link #trackHorizontalMotionScroll(int)} takes
     * care of repositioning, adding, and removing children.
     *
     * @param delta Change in the selected position. +1 means the selection is
     *              moving to the right, so views are scrolling to the left. -1
     *              means the selection is moving to the left.
     */
    @Override
    void layout(int delta, boolean animate) {
        if (mOrientation == HORIZONTAL) {
            layoutHorizontal(delta, animate);
        } else {
            layoutVertical(delta, animate);
        }
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #VERTICAL}.
     */
    private void layoutVertical(int delta, boolean animate) {
        int childrenTop = mSpinnerPadding.top;
        int childrenHeight = getBottom() - getTop() - mSpinnerPadding.top - mSpinnerPadding.bottom;

        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty gallery by removing all views.
        if (mItemCount == 0) {
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        //removeAllViewsInLayout();
        detachAllViewsFromParent();

        /*
         * These will be used to give initial positions to views entering the
         * gallery as we scroll
         */
        mTopMost = 0;
        mBottomMost = 0;

        // Make selected view and center it

        /*
         * mFirstPosition will be decreased as we add views to the top later
         * on. The 0 for y will be offset in a couple lines down.
         */
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddVerticalView(mSelectedPosition, 0, 0, true);

        // Put the selected child in the center
        int selectedOffset = childrenTop + (childrenHeight / 2) - (sel.getHeight() / 2) +
                mSelectedCenterOffset;
        sel.offsetTopAndBottom(selectedOffset);
        fillToGalleryBottom();
        fillToGalleryTop();

        // Flush any cached views that did not get reused above
        mRecycler.clear();

        invalidate();
        checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);
        updateSelectedItemMetadata();
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #HORIZONTAL}.
     */
    private void layoutHorizontal(int delta, boolean animate) {
//        mIsRtl = isLayoutRtl();
        mIsRtl = false;
        int childrenLeft = mSpinnerPadding.left;
        int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;

        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty gallery by removing all views.
        if (mItemCount == 0) {
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        //removeAllViewsInLayout();
        detachAllViewsFromParent();

        /*
         * These will be used to give initial positions to views entering the
         * gallery as we scroll
         */
        mRightMost = 0;
        mLeftMost = 0;

        // Make selected view and center it

        /*
         * mFirstPosition will be decreased as we add views to the left later
         * on. The 0 for x will be offset in a couple lines down.
         */
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddHorizontalView(mSelectedPosition, 0, 0, true);

        // Put the selected child in the center
        int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2) +
                mSelectedCenterOffset;
        sel.offsetLeftAndRight(selectedOffset);

        fillToGalleryRight();
        fillToGalleryLeft();

        // Flush any cached views that did not get reused above
        mRecycler.clear();

        invalidate();
        checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);
        updateSelectedItemMetadata();
    }


    private void fillToGalleryLeft() {
        if (mIsRtl) {
            fillToGalleryLeftRtl();
        } else {
            fillToGalleryLeftLtr();
        }
    }

    private void fillToGalleryLeftRtl() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();
        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            mFirstPosition = curPosition = mItemCount - 1;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition < mItemCount) {
            prevIterationView = makeAndAddHorizontalView(curPosition, curPosition - mSelectedPosition,
                    curRightEdge, false);

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition++;
        }
    }

    private void fillToGalleryLeftLtr() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddHorizontalView(curPosition, curPosition - mSelectedPosition,
                    curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }

    /**
     * position child from seleted view to Bottom most
     */
    private void fillToGalleryBottom() {
        int itemSpacing = mSpacing;
        int galleryBottom = getBottom() - getTop() - getPaddingBottom();
        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curTopEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curTopEdge = prevIterationView.getBottom() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curTopEdge = getPaddingTop();
            mShouldStopFling = true;
        }

        while (curTopEdge < galleryBottom && curPosition < numItems) {
            prevIterationView = makeAndAddVerticalView(curPosition, curPosition - mSelectedPosition,
                    curTopEdge, true);

            // Set state for next iteration
            curTopEdge = prevIterationView.getBottom() + itemSpacing;
            curPosition++;
        }
    }

    /**
     * position child from seleted view to top most
     */
    private void fillToGalleryTop() {

        int itemSpacing = mSpacing;
        int galleryTop = getPaddingTop();

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curBottomEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curBottomEdge = prevIterationView.getTop() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            curBottomEdge = getBottom() - getTop() - getPaddingBottom();
            mShouldStopFling = true;
        }

        while (curBottomEdge > galleryTop && curPosition >= 0) {
            prevIterationView = makeAndAddVerticalView(curPosition, curPosition - mSelectedPosition,
                    curBottomEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curBottomEdge = prevIterationView.getTop() - itemSpacing;
            curPosition--;
        }
    }


    private void fillToGalleryRight() {
        if (mIsRtl) {
            fillToGalleryRightRtl();
        } else {
            fillToGalleryRightLtr();
        }
    }

    private void fillToGalleryRightRtl() {
        int itemSpacing = mSpacing;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            curPosition = 0;
            curLeftEdge = getPaddingLeft();
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition >= 0) {
            prevIterationView = makeAndAddHorizontalView(curPosition, curPosition - mSelectedPosition,
                    curLeftEdge, true);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRightLtr() {
        int itemSpacing = mSpacing;
        int galleryRight = getRight() - getLeft() - getPaddingRight();
        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {
            prevIterationView = makeAndAddHorizontalView(curPosition, curPosition - mSelectedPosition,
                    curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
    }

    /**
     * Obtain a view, either by pulling an existing view from the recycler or by
     * getting a new one from the adapter. If we are animating, make sure there
     * is enough information in the view's layout parameters to animate from the
     * old to new positions.
     *
     * @param position Position in the gallery for the view to obtain
     * @param offset   Offset from the selected position
     * @param x        X-coordinate indicating where this view should be placed. This
     *                 will either be the left or right edge of the view, depending on
     *                 the fromLeft parameter
     * @param fromLeft Are we positioning views based on the left edge? (i.e.,
     *                 building from left to right)?
     * @return A view that has been added to the gallery
     */
    private View makeAndAddHorizontalView(int position, int offset, int x, boolean fromLeft) {
        View child;
        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {
                // Can reuse an existing view
                int childLeft = child.getLeft();

                // Remember left and right edges of where views have been placed
                mRightMost = Math.max(mRightMost, childLeft
                        + child.getMeasuredWidth());
                mLeftMost = Math.min(mLeftMost, childLeft);

                // Position the view
                setUpHorizontalChild(child, offset, x, fromLeft);

                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpHorizontalChild(child, offset, x, fromLeft);

        return child;
    }

    /**
     * Obtain a view, either by pulling an existing view from the recycler or by
     * getting a new one from the adapter. If we are animating, make sure there
     * is enough information in the view's layout parameters to animate from the
     * old to new positions.
     *
     * @param position Position in the gallery for the view to obtain
     * @param offset   Offset from the selected position
     * @param y        Y-coordinate indicating where this view should be placed. This
     *                 will either be the top or bottom edge of the view, depending on
     *                 the fromTop parameter
     * @param fromTop  Are we positioning views based on the top edge? (i.e.,
     *                 building from top to bottom)?
     * @return A view that has been added to the gallery
     */
    private View makeAndAddVerticalView(int position, int offset, int y, boolean fromTop) {
        View child;
        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {
                // Can reuse an existing view
                int childTop = child.getTop();

                // Remember(update) top and bottom edges of where views have been placed
                mBottomMost = Math.max(mBottomMost, childTop
                        + child.getMeasuredHeight());
                mTopMost = Math.min(mTopMost, childTop);

                // Position the view
                setUpVerticalChild(child, offset, y, fromTop);

                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpVerticalChild(child, offset, y, fromTop);

        return child;
    }

    /**
     * Helper for makeAndAddHorizontalView to set the position of a view and fill out its
     * layout parameters.
     *
     * @param child    The view to position
     * @param offset   Offset from the selected position
     * @param x        X-coordinate indicating where this view should be placed. This
     *                 will either be the left or right edge of the view, depending on
     *                 the fromLeft parameter
     * @param fromLeft Are we positioning views based on the left edge? (i.e.,
     *                 building from left to right)?
     */
    private void setUpHorizontalChild(View child, int offset, int x, boolean fromLeft) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        TwoWayGallery.LayoutParams lp = (TwoWayGallery.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (TwoWayGallery.LayoutParams) generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromLeft != mIsRtl ? -1 : 0, lp, true);

        child.setSelected(offset == 0);

        // Get measure specs
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childLeft;
        int childRight;

        // Position vertically based on gravity setting
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();

        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }

        child.layout(childLeft, childTop, childRight, childBottom);
    }

    /**
     * Helper for makeAndAddHorizontalView to set the position of a view and fill out its
     * layout parameters.
     *
     * @param child   The view to position
     * @param offset  Offset from the selected position
     * @param y       X-coordinate indicating where this view should be placed. This
     *                will either be the left or right edge of the view, depending on
     *                the fromLeft parameter
     * @param fromTop Are we positioning views based on the left edge? (i.e.,
     *                building from left to right)?
     */
    private void setUpVerticalChild(View child, int offset, int y, boolean fromTop) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        TwoWayGallery.LayoutParams lp = (TwoWayGallery.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (TwoWayGallery.LayoutParams) generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromTop ? -1 : 0, lp, true);

        child.setSelected(offset == 0);

        // Get measure specs
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childBottom;
        int childTop;

        // Position vertically based on gravity setting
        int childLeft = calculateLeft(child, true);
        int childRight = childLeft + child.getMeasuredWidth();

        int height = child.getMeasuredHeight();
        if (fromTop) {
            childTop = y;
            childBottom = childTop + height;
        } else {
            childTop = y - height;
            childBottom = y;
        }

        child.layout(childLeft, childTop, childRight, childBottom);
    }


    /**
     * Figure out vertical placement based on mGravity
     *
     * @param child Child to place
     * @return Where the top of the child should be
     */
    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

        int childTop = 0;

        switch (mGravity) {
            case Gravity.TOP:
                childTop = mSpinnerPadding.top;
                break;
            case Gravity.CENTER_VERTICAL:
                int availableSpace = myHeight - mSpinnerPadding.bottom
                        - mSpinnerPadding.top - childHeight;
                childTop = mSpinnerPadding.top + (availableSpace / 2);
                break;
            case Gravity.BOTTOM:
                childTop = myHeight - mSpinnerPadding.bottom - childHeight;
                break;
        }
        return childTop;
    }

    /**
     * Figure out horizontal placement based on mGravity
     *
     * @param child Child to place
     * @return Where the top of the child should be
     */
    private int calculateLeft(View child, boolean duringLayout) {
        int mWidth = duringLayout ? getMeasuredWidth() : getWidth();
        int childWidth = duringLayout ? child.getMeasuredWidth() : child.getWidth();

        int childLeft = 0;

        switch (mGravity) {
            case Gravity.LEFT:
                childLeft = mSpinnerPadding.left;
                break;
            case Gravity.CENTER_HORIZONTAL:
                int availableSpace = mWidth - mSpinnerPadding.right
                        - mSpinnerPadding.left - childWidth;
                childLeft = mSpinnerPadding.left + (availableSpace / 2);
                break;
            case Gravity.RIGHT:
                childLeft = mWidth - mSpinnerPadding.right - childWidth;
                break;
        }
        return childLeft;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Give everything to the gesture detector
        boolean retValue = mGestureDetector.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
            onUp();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onCancel();
        }

        return retValue;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        if (mDownTouchPosition >= 0) {

            // An item tap should make it selected, so scroll to this child.
            scrollToChild(mDownTouchPosition - mFirstPosition);

            // Also pass the click so the client knows, if it wants to.
            if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
                performItemClick(mDownTouchView, mDownTouchPosition, mAdapter
                        .getItemId(mDownTouchPosition));
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (!mShouldCallbackDuringFling) {
            // We want to suppress selection changes

            // Remove any future code to set mSuppressSelectionChanged = false
            removeCallbacks(mDisableSuppressSelectionChangedRunnable);

            // This will get reset once we scroll into slots
            if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
        }

        // Fling the gallery!
        if (mOrientation == HORIZONTAL) {
            Log.d(TAG, "velocityX:" + velocityX);
            //velocityX < 0 ,finger fling from right  to left ,show right
            mFlingRunnable.startUsingVelocity((int) -velocityX);
        } else {
            Log.d(TAG, "velocityY:" + velocityY);
            //velocityY < 0 ,finger fling from bottom  to top ,show bottom
            mFlingRunnable.startUsingVelocity((int) -velocityY);
        }

        return true;
    }

    /**
     * @param e1
     * @param e2
     * @param distanceX (startX-endX)
     * @param distanceY （startY-endY）
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        if (localLOGV) Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));

        /*
         * Now's a good time to tell our parent to stop intercepting our events!
         * The user has moved more than the slop amount, since GestureDetector
         * ensures this before calling this method. Also, if a parent is more
         * interested in this touch's events than we are, it would have
         * intercepted them by now (for example, we can assume when a Gallery is
         * in the ListView, a vertical scroll would not end up in this method
         * since a ListView would have intercepted it by now).
         */
        getParent().requestDisallowInterceptTouchEvent(true);

        // As the user scrolls, we want to callback selection changes so related-
        // info on the screen is up-to-date with the gallery's selection
        if (!mShouldCallbackDuringFling) {
            if (mIsFirstScroll) {
                /*
                 * We're not notifying the client of selection changes during
                 * the fling, and this scroll could possibly be a fling. Don't
                 * do selection changes until we're sure it is not a fling.
                 */
                if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
                postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
            }
        } else {
            if (mSuppressSelectionChanged) mSuppressSelectionChanged = false;
        }

        // Track the motion
        if (mOrientation == HORIZONTAL) {
            trackHorizontalMotionScroll(-1 * (int) distanceX);//when distanceX>0,fling from right to left,the right of the selected view is appearing.
        } else {
            trackVerticalMotionScroll(-1 * (int) distanceY);//when distanceY>0,fling from bottom to top ,the bottom of the selected view is appearing.
        }

        mIsFirstScroll = false;
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {

        // Kill any existing fling/scroll
        mFlingRunnable.stop(false);

        // Get the item's view that was touched
        mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
            mDownTouchView.setPressed(true);
        }

        // Reset the multiple-scroll tracking state
        mIsFirstScroll = true;

        // Must return true to get matching events for this down event.
        return true;
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_UP.
     */
    void onUp() {

        if (mFlingRunnable.mScroller.isFinished()) {
            Log.d(TAG, "onUp-->scrollINtoSlots");
            scrollIntoSlots();
        }

        dispatchUnpress();
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
     */
    void onCancel() {
        onUp();
    }

    @Override
    public void onLongPress(MotionEvent e) {

        if (mDownTouchPosition < 0) {
            return;
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        long id = getItemIdAtPosition(mDownTouchPosition);
        dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
    }

    // Unused methods from GestureDetector.OnGestureListener below

    @Override
    public void onShowPress(MotionEvent e) {
    }

    // Unused methods from GestureDetector.OnGestureListener above

    private void dispatchPress(View child) {

        if (child != null) {
            child.setPressed(true);
        }

        setPressed(true);
    }

    private void dispatchUnpress() {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }

        setPressed(false);
    }

    @Override
    public void dispatchSetSelected(boolean selected) {
        /*
         * We don't want to pass the selected state given from its parent to its
         * children since this widget itself has a selected state to give to its
         * children.
         */
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

        // Show the pressed state on the selected child
        if (mSelectedChild != null) {
            mSelectedChild.setPressed(pressed);
        }
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {

        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }

        final long longPressId = mAdapter.getItemId(longPressPosition);
        return dispatchLongPress(originalView, longPressPosition, longPressId);
    }

    @Override
    public boolean showContextMenu() {

        if (isPressed() && mSelectedPosition >= 0) {
            int index = mSelectedPosition - mFirstPosition;
            View v = getChildAt(index);
            return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
        }

        return false;
    }

    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;

        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView,
                    mDownTouchPosition, id);
        }

        if (!handled) {
            mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }

        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }

        return handled;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Gallery steals all key events
        return event.dispatch(this, null, null);
    }

    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);

        if (child != null) {
            if (mOrientation == HORIZONTAL) {
                int distance = getHorizontalCenterOfGallery() - getHorizontalCenterOfView(child);
                mFlingRunnable.startUsingDistance(distance);
            } else {
                int distance = getVerticalCenterOfGallery() - getVerticalCenterOfView(child);
                mFlingRunnable.startUsingDistance(distance);
            }
            return true;
        }

        return false;
    }

    @Override
    void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);

        // Updates any metadata we keep about the selected item.
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {

        View oldSelectedChild = mSelectedChild;

        View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
        if (child == null) {
            return;
        }

        child.setSelected(true);
        child.setFocusable(true);

        if (hasFocus()) {
            child.requestFocus();
        }

        // We unfocus the old child down here so the above hasFocus check
        // returns true
        if (oldSelectedChild != null && oldSelectedChild != child) {

            // Make sure its drawable state doesn't contain 'selected'
            oldSelectedChild.setSelected(false);

            // Make sure it is not focusable anymore, since otherwise arrow keys
            // can make this one be focused
            oldSelectedChild.setFocusable(false);
        }

    }

    /**
     * Describes how the child views are aligned.
     *
     * @param gravity
     * @attr ref android.R.styleable#Gallery_gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = mSelectedPosition - mFirstPosition;

        // Just to be safe
        if (selectedIndex < 0) return i;

        if (i == childCount - 1) {
            // Draw the selected child last
            return selectedIndex;
        } else if (i >= selectedIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return i;
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        /*
         * The gallery shows focus by focusing the selected item. So, give
         * focus to our selected item instead. We steal keys from our
         * selected item elsewhere.
         */
        if (gainFocus && mSelectedChild != null) {
            mSelectedChild.requestFocus(direction);
            mSelectedChild.setSelected(true);
        }

    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return TwoWayGallery.class.getName();
    }

    /**
     * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;


        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0) return;

            startCommon();
            if (mOrientation == HORIZONTAL) {
                //when initialVelocity >0 show right of gallery,else show left
                int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;// Starting point of the scroll (X)
                Log.d(TAG, "initialX:" + initialX);
                mLastFlingX = initialX;
                mScroller.fling(initialX, 0, initialVelocity, 0,
                        0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            } else {
                //when initialVelocity >0 show bottom of gallery,else show top
                int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;// Starting point of the scroll (Y)
                mLastFlingY = initialY;
                Log.d(TAG, "initialY:" + initialY);
                mScroller.fling(0, initialY, 0, initialVelocity,
                        0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            }
            post(this);
        }

        public void startUsingDistance(int distance) {
            Log.d(TAG, "start using dis:" + distance);
            if (distance == 0) return;

            startCommon();
            if (mOrientation == HORIZONTAL) {
                mLastFlingX = 0;
                mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            } else {
                mLastFlingY = 0;
                mScroller.startScroll(0, 0, 0, -distance, mAnimationDuration);
            }
            post(this);
        }

        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);

            if (scrollIntoSlots) scrollIntoSlots();
        }

        @Override
        public void run() {

            if (mItemCount == 0) {
                endFling(true);
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            if (mOrientation == HORIZONTAL) {
                final int x = scroller.getCurrX();

                // Flip sign to convert finger direction to list items direction
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingX - x;

                // Pretend that each frame of a fling scroll is a touch scroll
                if (delta > 0) {
                    // Moving towards the left. Use leftmost view as mDownTouchPosition
                    mDownTouchPosition = mIsRtl ? (mFirstPosition + getChildCount() - 1) :
                            mFirstPosition;

                    // Don't fling more than 1 screen
                    delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
                } else {
                    // Moving towards the right. Use rightmost view as mDownTouchPosition
                    int offsetToLast = getChildCount() - 1;
                    mDownTouchPosition = mIsRtl ? mFirstPosition :
                            (mFirstPosition + getChildCount() - 1);

                    // Don't fling more than 1 screen
                    delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
                }

                trackHorizontalMotionScroll(delta);

                if (more && !mShouldStopFling) {
                    mLastFlingX = x;
                    post(this);
                } else {
                    endFling(true);
                }
            } else {
                final int y = scroller.getCurrY();
                // Flip sign to convert finger direction to list items direction
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingY - y;
                // Pretend that each frame of a fling scroll is a touch scroll
                if (delta > 0) {
                    //show top
                    // Moving towards the top. Use topmost view as mDownTouchPosition
                    mDownTouchPosition = mFirstPosition;
                    // Don't fling more than 1 screen
                    delta = Math.min(getHeight() - getPaddingTop() - getPaddingBottom() - 1, delta);
                } else {
                    //show bottom
                    // Moving towards the bottom. Use bottommost view as mDownTouchPosition
                    int offsetToLast = getChildCount() - 1;
                    mDownTouchPosition = (mFirstPosition + getChildCount() - 1);
                    // Don't fling more than 1 screen
                    delta = Math.min(getHeight() - getPaddingTop() - getPaddingBottom() - 1, delta);
                }
                trackVerticalMotionScroll(delta);

                if (more && !mShouldStopFling) {
                    mLastFlingY = y;
                    post(this);
                } else {
                    endFling(true);
                }
            }
        }


    }

    /**
     * Gallery extends LayoutParams to provide a place to hold current
     * Transformation information along with previous position/transformation
     * info.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}

