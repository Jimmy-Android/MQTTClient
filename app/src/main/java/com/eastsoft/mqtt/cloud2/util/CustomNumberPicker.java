package com.eastsoft.mqtt.cloud2.util;

import java.util.Locale;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Scroller;
import android.widget.NumberPicker.Formatter;

import com.eastsoft.mqtt.cloud2.R;

public class CustomNumberPicker extends LinearLayout {

	/**
	 * The number of items show in the selector wheel.
	 */
	private int SELECTOR_WHEEL_ITEM_COUNT = 5;

	/**
	 * The index of the middle selector item.
	 */
	private int SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT / 2;

	/**
	 * The the duration for adjusting the selector wheel.
	 */
	private final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;

	/**
	 * The coefficient by which to adjust (divide) the max fling velocity.
	 */
	private final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;

	/**
	 * Cache for the string representation of selector indices.
	 */
	private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<String>();

	/**
	 * The values to be displayed instead the indices.
	 */
	private String[] mDisplayedValues;

	/**
	 * The text for showing the current value.
	 */
	private EditText mInputText;

	/**
	 * The {@link Scroller} responsible for flinging the selector.
	 */
	private final Scroller mFlingScroller;

	/**
	 * The distance between the two selection dividers.
	 */

	private Scroller mAdjustScroller;

	/**
	 * The height of the text.
	 */
	private final int mTextSize;

	/**
	 * The {@link Paint} for drawing the selector.
	 */
	private final Paint mSelectorWheelPaint;

	/**
	 * The {@link Paint} for drawing the selected number.
	 */
	private final Paint mCenterPaint;

	/**
	 * The current offset of the scroll selector.
	 */
	private int mCurrentScrollOffset;

	/**
	 * The initial offset of the scroll selector.
	 */
	private int mInitialScrollOffset = Integer.MIN_VALUE;

	/**
	 * The height of the gap between text elements if the selector wheel.
	 */
	private int mSelectorTextGapHeight;

	/**
	 * The height of a selector element (text + gap).
	 */
	private int mSelectorElementHeight;

	/**
	 * Flag whether the selector should wrap around.
	 */
	private boolean mWrapSelectorWheel = true;

	/**
	 * The previous Y coordinate while scrolling the selector.
	 */
	private int mPreviousScrollerY;

	/**
	 * Determines speed during touch scrolling.
	 */
	private VelocityTracker mVelocityTracker;

	/**
	 * @see ViewConfiguration#getScaledMinimumFlingVelocity()
	 */
	private int mMinimumFlingVelocity;

	/**
	 * @see ViewConfiguration#getScaledMaximumFlingVelocity()
	 */
	private int mMaximumFlingVelocity;

	/**
	 * The selector indices whose value are show by the selector.
	 */
	private int[] mSelectorIndices = new int[SELECTOR_WHEEL_ITEM_COUNT];

	/**
	 * Current value of this NumberPicker
	 */
	private int mValue;

	/**
	 * Formatter for for displaying the current value.
	 */
	private Formatter mFormatter;

	private String mMiddleDisplayedValues;

	private int downY;

	private int currY;

	private int deltaY;

	private int mLeft;

	private int mRight;

	private int mTop;

	private int mBottom;

	private boolean isInit = true;

	private int itemHeight = getMeasuredHeight() / SELECTOR_WHEEL_ITEM_COUNT;

	private int helfHeight;

	private int mMinValue = -21;

	private int mMaxValue = -12;

	private LinearLayout root;
	private Context context;

	public CustomNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.smartsocket_number_layout, this, true);
		LayoutParams ll = new LayoutParams(
				LayoutParams.MATCH_PARENT, 0, 1);
		root = (LinearLayout) findViewById(R.id.root);
		for (int i = 0; i < SELECTOR_MIDDLE_ITEM_INDEX; i++) {
			root.addView(new View(context), i, ll);
			root.addView(new View(context), SELECTOR_MIDDLE_ITEM_INDEX + i, ll);
		}
		mInputText = (EditText) findViewById(R.id.tv);

		// create the fling and adjust scrollers
		mFlingScroller = new Scroller(getContext(), null, true);
		mAdjustScroller = new Scroller(getContext(),
				new DecelerateInterpolator(2.5f));

		mTextSize = (int) mInputText.getTextSize();
		helfHeight = SELECTOR_WHEEL_ITEM_COUNT * itemHeight / 2;
		mCurrentScrollOffset = mInputText.getBaseline();

		// initialize constants
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity()
				/ SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;

		for (int i = 0; i < mSelectorIndices.length; i++) {
			mSelectorIndices[i] = i;
		}

		// create the selector wheel paint
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(mTextSize);
		paint.setTypeface(mInputText.getTypeface());
		ColorStateList colors = mInputText.getTextColors();
		int color = colors.getColorForState(ENABLED_STATE_SET, Color.WHITE);
		paint.setColor(color);
		mSelectorWheelPaint = paint;

		Paint cPaint = new Paint();
		cPaint.setAntiAlias(true);
		cPaint.setTextAlign(Align.CENTER);
		cPaint.setColor(color);
		cPaint.setTextSize(mTextSize);
		mCenterPaint = cPaint;
	}

	/**
	 * 设置显示的数目
	 * 
	 * @author zhangchi
	 * @param itemCount
	 * @throws Exception
	 */
	public void setItemCount(int itemCount) throws Exception {
		if (itemCount <= 0 || itemCount % 2 == 0) {
			throw new Exception(
					"Illegal itemCount Exception: itemCount must bigget than 0 and can not be an even");
		}
		SELECTOR_WHEEL_ITEM_COUNT = itemCount;
		SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT / 2;
		mSelectorIndices = new int[SELECTOR_WHEEL_ITEM_COUNT];
		itemHeight = getMeasuredHeight() / SELECTOR_WHEEL_ITEM_COUNT;
		if (root != null) {
			root.removeAllViews();
			LayoutParams ll = new LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 1);
			for (int i = 0; i < SELECTOR_WHEEL_ITEM_COUNT; i++) {
				if (i != SELECTOR_MIDDLE_ITEM_INDEX) {
					root.addView(new View(context), i, ll);
				} else {
					mInputText = new EditText(context);
					mInputText.setGravity(Gravity.CENTER);
					mInputText.setVisibility(View.INVISIBLE);
					root.addView(mInputText, SELECTOR_MIDDLE_ITEM_INDEX, ll);
				}
			}

		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		super.onLayout(changed, l, t, r, b);
		initializeFadingEdges();
		if (isInit) {
			mLeft = getLeft();
			mRight = getRight();
			mTop = getTop();
			mBottom = getBottom();
		}

		if (changed) {
			// need to do all this when we know our size
			initializeSelectorWheel();
			initializeFadingEdges();
			// mTopSelectionDividerTop = (getHeight() -
			// mSelectionDividersDistance) / 2
			// - mSelectionDividerHeight;
			// mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2 *
			// mSelectionDividerHeight
			// + mSelectionDividersDistance;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		float x = (mRight - mLeft) / 2;
		float y = mCurrentScrollOffset;

		// draw the selector wheel
		int[] selectorIndices = mSelectorIndices;
		for (int i = 0; i < selectorIndices.length; i++) {
			int selectorIndex = selectorIndices[i];
			String scrollSelectorValue = mSelectorIndexToStringCache
					.get(selectorIndex);
			// Do not draw the middle item if input is visible since the input
			// is shown only if the wheel is static and it covers the middle
			// item. Otherwise, if the user starts editing the text via the
			// IME he may see a dimmed version of the old value intermixed
			// with the new one.

			if (i != SELECTOR_MIDDLE_ITEM_INDEX /*
												 * || mInputText.getVisibility()
												 * != VISIBLE
												 */) {
				mSelectorWheelPaint.setAlpha((int) ((helfHeight - Math.abs(y
						- itemHeight * SELECTOR_MIDDLE_ITEM_INDEX
						- mInputText.getBaseline()))
						/ (float) helfHeight * 255f));
				canvas.drawText("" + scrollSelectorValue, x, y,
						mSelectorWheelPaint);
			} else if (i == SELECTOR_MIDDLE_ITEM_INDEX) {

				int centerTextSize = mTextSize
						+ (int) ((mInputText.getBaseline() - Math.abs(y
								- itemHeight * SELECTOR_MIDDLE_ITEM_INDEX
								- mInputText.getBaseline()))
								/ (float) (mInputText.getBaseline()) * 30f);
				mCenterPaint.setTextSize(centerTextSize);
				canvas.drawText("" + mMiddleDisplayedValues, x, y
						+ (centerTextSize - mTextSize) / 2, mCenterPaint);
			}
			y += itemHeight;
		}

		// draw the selection dividers
		// if (mSelectionDivider != null) {
		// // draw the top divider
		// int topOfTopDivider = mTopSelectionDividerTop;
		// int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
		// mSelectionDivider.setBounds(0, topOfTopDivider, mRight,
		// bottomOfTopDivider);
		// mSelectionDivider.draw(canvas);
		//
		// // draw the bottom divider
		// int bottomOfBottomDivider = mBottomSelectionDividerBottom;
		// int topOfBottomDivider = bottomOfBottomDivider -
		// mSelectionDividerHeight;
		// mSelectionDivider.setBounds(0, topOfBottomDivider, mRight,
		// bottomOfBottomDivider);
		// mSelectionDivider.draw(canvas);
		// }
		super.onDraw(canvas);
	}

	@Override
	public void computeScroll() {
		Scroller scroller = mFlingScroller;
		if (scroller.isFinished()) {
			scroller = mAdjustScroller;
			if (scroller.isFinished()) {
				return;
			}
		}
		scroller.computeScrollOffset();
		int currentScrollerY = scroller.getCurrY();
		if (mPreviousScrollerY == 0) {
			mPreviousScrollerY = scroller.getStartY();
		}
		scrollBy(0, currentScrollerY - mPreviousScrollerY);
		mPreviousScrollerY = currentScrollerY;
		if (scroller.isFinished()) {
			onScrollerFinished(scroller);
		} else {
			invalidate();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {

		// if (!mHasSelectorWheel || !isEnabled()) {
		// return false;
		// }
		final int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			if (!mFlingScroller.isFinished()) {
				mFlingScroller.forceFinished(true);
				mAdjustScroller.forceFinished(true);
				// onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
			} else if (!mAdjustScroller.isFinished()) {
				mFlingScroller.forceFinished(true);
				mAdjustScroller.forceFinished(true);
			}
			return true;
		}
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int ea = event.getAction();
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		switch (ea) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			currY = (int) event.getRawY();
			deltaY = currY - downY;
			mCurrentScrollOffset += deltaY;
			scrollBy(0, deltaY);
			downY = currY;
			postInvalidate();
			break;
		case MotionEvent.ACTION_UP:

			VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
			int initialVelocity = (int) velocityTracker.getYVelocity();
			if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
				fling(initialVelocity);
				// onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
			}
			ensureScrollWheelAdjusted();
			mVelocityTracker.recycle();
			mVelocityTracker = null;
			break;
		}
		return true;
	}

	private void initializeFadingEdges() {
		setVerticalFadingEdgeEnabled(true);
		setFadingEdgeLength((mBottom - mTop - 3 * mTextSize) / 2);
	}

	@Override
	public void scrollBy(int x, int y) {
		int[] selectorIndices = mSelectorIndices;
		if (!mWrapSelectorWheel && y > 0
				&& selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
			mCurrentScrollOffset = mInitialScrollOffset;
			return;
		}
		if (!mWrapSelectorWheel && y < 0
				&& selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
			mCurrentScrollOffset = mInitialScrollOffset;
			return;
		}
		mCurrentScrollOffset += y;
		while (mCurrentScrollOffset - mInitialScrollOffset > itemHeight / 2) {
			mCurrentScrollOffset -= mSelectorElementHeight;
			decrementSelectorIndices(selectorIndices);
			setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
			if (!mWrapSelectorWheel
					&& selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
				mCurrentScrollOffset = mInitialScrollOffset;
			}
		}
		while (mCurrentScrollOffset - mInitialScrollOffset < -itemHeight / 2) {
			mCurrentScrollOffset += mSelectorElementHeight;
			incrementSelectorIndices(selectorIndices);
			setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
			if (!mWrapSelectorWheel
					&& selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
				mCurrentScrollOffset = mInitialScrollOffset;
			}
		}
	}

	private void initializeSelectorWheel() {
		initializeSelectorWheelIndices();
		int[] selectorIndices = mSelectorIndices;
		int totalTextHeight = selectorIndices.length * mTextSize;
		float totalTextGapHeight = (mBottom - mTop) - totalTextHeight;
		float textGapCount = selectorIndices.length;
		mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f);
		mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
		itemHeight = (mBottom - mTop) / SELECTOR_WHEEL_ITEM_COUNT;
		helfHeight = SELECTOR_WHEEL_ITEM_COUNT * itemHeight / 2;
		// mInitialScrollOffset = editTextTextPosition
		// - (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
		mInitialScrollOffset = mInputText.getBaseline();
		mCurrentScrollOffset = mInitialScrollOffset;

		updateInputTextView();
	}

	/**
	 * Sets whether the selector wheel shown during flinging/scrolling should
	 * wrap around the {@link NumberPicker#getMinValue()} and
	 * {@link NumberPicker#getMaxValue()} values.
	 * <p>
	 * By default if the range (max - min) is more than the number of items
	 * shown on the selector wheel the selector wheel wrapping is enabled.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> If the number of items, i.e. the range (
	 * {@link #getMaxValue()} - {@link #getMinValue()}) is less than the number
	 * of items shown on the selector wheel, the selector wheel will not wrap.
	 * Hence, in such a case calling this method is a NOP.
	 * </p>
	 * 
	 * @param wrapSelectorWheel
	 *            Whether to wrap.
	 */
	public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
		final boolean wrappingAllowed = (mMaxValue - mMinValue) >= mSelectorIndices.length;
		if ((!wrapSelectorWheel || wrappingAllowed)
				&& wrapSelectorWheel != mWrapSelectorWheel) {
			mWrapSelectorWheel = wrapSelectorWheel;
		}
	}

	/**
	 * Increments the <code>selectorIndices</code> whose string representations
	 * will be displayed in the selector.
	 */
	private void incrementSelectorIndices(int[] selectorIndices) {
		for (int i = 0; i < selectorIndices.length - 1; i++) {
			selectorIndices[i] = selectorIndices[i + 1];
		}
		int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
		if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
			nextScrollSelectorIndex = mMinValue;
		}
		selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
		ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
	}

	/**
	 * Decrements the <code>selectorIndices</code> whose string representations
	 * will be displayed in the selector.
	 */
	private void decrementSelectorIndices(int[] selectorIndices) {
		for (int i = selectorIndices.length - 1; i > 0; i--) {
			selectorIndices[i] = selectorIndices[i - 1];
		}
		int nextScrollSelectorIndex = selectorIndices[1] - 1;
		if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
			nextScrollSelectorIndex = mMaxValue;
		}
		selectorIndices[0] = nextScrollSelectorIndex;
		ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
	}

	/**
	 * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
	 * middle element is in the middle of the widget.
	 * 
	 * @return Whether an adjustment has been made.
	 */
	private boolean ensureScrollWheelAdjusted() {
		// adjust to the closest value
		int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
		if (deltaY != 0) {
			mPreviousScrollerY = 0;
			if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
				deltaY += (deltaY > 0) ? -mSelectorElementHeight
						: mSelectorElementHeight;
			}
			mAdjustScroller.startScroll(0, 0, 0, deltaY,
					SELECTOR_ADJUSTMENT_DURATION_MILLIS);
			invalidate();
			return true;
		}
		return false;
	}

	/**
	 * Flings the selector with the given <code>velocityY</code>.
	 */
	private void fling(int velocityY) {
		mPreviousScrollerY = 0;

		if (velocityY > 0) {
			mFlingScroller
					.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
		} else {
			mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0,
					Integer.MAX_VALUE);
		}

		invalidate();
	}

	/**
	 * Callback invoked upon completion of a given <code>scroller</code>.
	 */
	private void onScrollerFinished(Scroller scroller) {
		if (scroller == mFlingScroller) {
			if (!ensureScrollWheelAdjusted()) {
				updateInputTextView();
			}
			// onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
		} else {
			// if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			updateInputTextView();
			// }
		}
	}

	/**
	 * Sets the values to be displayed.
	 * 
	 * @param displayedValues
	 *            The displayed values.
	 * 
	 *            <strong>Note:</strong> The length of the displayed values
	 *            array must be equal to the range of selectable numbers which
	 *            is equal to {@link #getMaxValue()} - {@link #getMinValue()} +
	 *            1.
	 */
	public void setDisplayedValues(String[] displayedValues) {
		if (mDisplayedValues == displayedValues) {
			return;
		}
		mDisplayedValues = displayedValues;
		if (mDisplayedValues != null) {
			// Allow text entry rather than strictly numeric entry.
			mInputText.setRawInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		} else {
			mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		}
		updateInputTextView();
		initializeSelectorWheelIndices();
		// tryComputeMaxWidth();
	}

	/**
	 * Resets the selector indices and clear the cached string representation of
	 * these indices.
	 */
	private void initializeSelectorWheelIndices() {
		mSelectorIndexToStringCache.clear();
		int[] selectorIndices = mSelectorIndices;
		int current = getValue();
		for (int i = 0; i < mSelectorIndices.length; i++) {
			int selectorIndex = current + (i - SELECTOR_MIDDLE_ITEM_INDEX);
			if (mWrapSelectorWheel) {
				selectorIndex = getWrappedSelectorIndex(selectorIndex);
			}
			selectorIndices[i] = selectorIndex;
			ensureCachedScrollSelectorValue(selectorIndices[i]);
		}
	}

	/**
	 * Ensures we have a cached string representation of the given <code>
	 * selectorIndex</code> to avoid multiple instantiations of the same string.
	 */
	private void ensureCachedScrollSelectorValue(int selectorIndex) {
		SparseArray<String> cache = mSelectorIndexToStringCache;
		String scrollSelectorValue = cache.get(selectorIndex);
		if (scrollSelectorValue != null) {
			return;
		}
		if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
			scrollSelectorValue = "";
		} else {
			if (mDisplayedValues != null) {
				int displayedValueIndex = selectorIndex - mMinValue;
				scrollSelectorValue = mDisplayedValues[displayedValueIndex];
			} else {
				scrollSelectorValue = formatNumber(selectorIndex);
			}
		}
		cache.put(selectorIndex, scrollSelectorValue);
	}

	private String formatNumber(int value) {
		return (mFormatter != null) ? mFormatter.format(value)
				: formatNumberWithLocale(value);
	}

	/**
	 * Set the formatter to be used for formatting the current value.
	 * <p>
	 * Note: If you have provided alternative values for the values this
	 * formatter is never invoked.
	 * </p>
	 * 
	 * @param formatter
	 *            The formatter object. If formatter is <code>null</code>,
	 *            {@link String#valueOf(int)} will be used.
	 * @see #setDisplayedValues(String[])
	 */
	public void setFormatter(Formatter formatter) {
		if (formatter == mFormatter) {
			return;
		}
		mFormatter = formatter;
		initializeSelectorWheelIndices();
		updateInputTextView();
	}

	static private String formatNumberWithLocale(int value) {
		return String.format(Locale.getDefault(), "%d", value);
	}

	/**
	 * @return The wrapped index <code>selectorIndex</code> value.
	 */
	private int getWrappedSelectorIndex(int selectorIndex) {
		if (selectorIndex > mMaxValue) {
			return mMinValue + (selectorIndex - mMaxValue)
					% (mMaxValue - mMinValue) - 1;
		} else if (selectorIndex < mMinValue) {
			return mMaxValue - (mMinValue - selectorIndex)
					% (mMaxValue - mMinValue) + 1;
		}
		return selectorIndex;
	}

	/**
	 * Returns the value of the picker.
	 * 
	 * @return The value.
	 */
	public int getValue() {
		return mValue;
	}

	public int getMinValue() {
		return mMinValue;
	}

	/**
	 * Sets the min value of the picker.
	 * 
	 * @param minValue
	 *            The min value inclusive.
	 * 
	 *            <strong>Note:</strong> The length of the displayed values
	 *            array set via {@link #setDisplayedValues(String[])} must be
	 *            equal to the range of selectable numbers which is equal to
	 *            {@link #getMaxValue()} - {@link #getMinValue()} + 1.
	 */
	public void setMinValue(int minValue) {
		if (mMinValue == minValue) {
			return;
		}
		if (minValue < 0) {
			throw new IllegalArgumentException("minValue must be >= 0");
		}
		mMinValue = minValue;
		if (mMinValue > mValue) {
			mValue = mMinValue;
		}
		boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
		setWrapSelectorWheel(wrapSelectorWheel);
		initializeSelectorWheelIndices();
		updateInputTextView();
		// tryComputeMaxWidth();
		invalidate();
	}

	public int getMaxValue() {
		return mMaxValue;
	}

	/**
	 * Sets the max value of the picker.
	 * 
	 * @param maxValue
	 *            The max value inclusive.
	 * 
	 *            <strong>Note:</strong> The length of the displayed values
	 *            array set via {@link #setDisplayedValues(String[])} must be
	 *            equal to the range of selectable numbers which is equal to
	 *            {@link #getMaxValue()} - {@link #getMinValue()} + 1.
	 */
	public void setMaxValue(int maxValue) {
		if (mMaxValue == maxValue) {
			return;
		}
		if (maxValue < 0) {
			throw new IllegalArgumentException("maxValue must be >= 0");
		}
		mMaxValue = maxValue;
		if (mMaxValue < mValue) {
			mValue = mMaxValue;
		}
		boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
		setWrapSelectorWheel(wrapSelectorWheel);
		initializeSelectorWheelIndices();
		updateInputTextView();
		// tryComputeMaxWidth();
		invalidate();
	}

	/**
	 * Sets the current value of this NumberPicker.
	 * 
	 * @param current
	 *            The new value of the NumberPicker.
	 * @param notifyChange
	 *            Whether to notify if the current value changed.
	 */
	private void setValueInternal(int current, boolean notifyChange) {
		if (mValue == current) {
			return;
		}
		// Wrap around the values if we go past the start or end
		if (mWrapSelectorWheel) {
			current = getWrappedSelectorIndex(current);
		} else {
			current = Math.max(current, mMinValue);
			current = Math.min(current, mMaxValue);
		}
		// int previous = mValue;
		mValue = current;
		updateInputTextView();
		if (notifyChange) {
			// notifyChange(previous, current);
		}
		initializeSelectorWheelIndices();
		invalidate();
	}

	private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

	public static final Formatter getTwoDigitFormatter() {
		return sTwoDigitFormatter;
	}

	/**
	 * Use a custom NumberPicker formatting callback to use two-digit minutes
	 * strings like "01". Keeping a static formatter etc. is the most efficient
	 * way to do this; it avoids creating temporary objects on every call to
	 * format().
	 */
	private static class TwoDigitFormatter implements Formatter {
		final StringBuilder mBuilder = new StringBuilder();

		// char mZeroDigit;
		java.util.Formatter mFmt;

		final Object[] mArgs = new Object[1];

		TwoDigitFormatter() {
			final Locale locale = Locale.getDefault();
			init(locale);
		}

		private void init(Locale locale) {
			mFmt = createFormatter(locale);
		}

		public String format(int value) {
			// final Locale currentLocale = Locale.getDefault();
			mArgs[0] = value;
			mBuilder.delete(0, mBuilder.length());
			mFmt.format("%02d", mArgs);
			return mFmt.toString();
		}

		private java.util.Formatter createFormatter(Locale locale) {
			return new java.util.Formatter(mBuilder, locale);
		}
	}

	/**
	 * Updates the view of this NumberPicker. If displayValues were specified in
	 * the string corresponding to the index specified by the current value will
	 * be returned. Otherwise, the formatter specified in {@link #setFormatter}
	 * will be used to format the number.
	 * 
	 * @return Whether the text was updated.
	 */
	private boolean updateInputTextView() {
		/*
		 * If we don't have displayed values then use the current number else
		 * find the correct value in the displayed values for the current
		 * number.
		 */

		String text = (mDisplayedValues == null) ? formatNumber(mValue)
				: mDisplayedValues[mValue - mMinValue];
		mMiddleDisplayedValues = text;
		if (!TextUtils.isEmpty(text)
				&& !text.equals(mInputText.getText().toString())) {
			mInputText.setText(text);
			return true;
		}

		return false;
	}

	/**
	 * Set the current value for the number picker.
	 * <p>
	 * If the argument is less than the {@link NumberPicker#getMinValue()} and
	 * {@link NumberPicker#getWrapSelectorWheel()} is <code>false</code> the
	 * current value is set to the {@link NumberPicker#getMinValue()} value.
	 * </p>
	 * <p>
	 * If the argument is less than the {@link NumberPicker#getMinValue()} and
	 * {@link NumberPicker#getWrapSelectorWheel()} is <code>true</code> the
	 * current value is set to the {@link NumberPicker#getMaxValue()} value.
	 * </p>
	 * <p>
	 * If the argument is less than the {@link NumberPicker#getMaxValue()} and
	 * {@link NumberPicker#getWrapSelectorWheel()} is <code>false</code> the
	 * current value is set to the {@link NumberPicker#getMaxValue()} value.
	 * </p>
	 * <p>
	 * If the argument is less than the {@link NumberPicker#getMaxValue()} and
	 * {@link NumberPicker#getWrapSelectorWheel()} is <code>true</code> the
	 * current value is set to the {@link NumberPicker#getMinValue()} value.
	 * </p>
	 * 
	 * @param value
	 *            The current value.
	 * @see #setWrapSelectorWheel(boolean)
	 * @see #setMinValue(int)
	 * @see #setMaxValue(int)
	 */
	public void setValue(int value) {
		if (mValue == value) {
			return;
		}
		if (value < mMinValue) {
			value = mWrapSelectorWheel ? mMaxValue : mMinValue;
		}
		if (value > mMaxValue) {
			value = mWrapSelectorWheel ? mMinValue : mMaxValue;
		}
		mValue = value;
		initializeSelectorWheelIndices();
		updateInputTextView();
		invalidate();
	}

}
