/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.duzi.chartandcalendar.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Scroller;

public abstract class ScrollableChart extends View
        implements GestureDetector.OnGestureListener, ValueAnimator.AnimatorUpdateListener { // 제스쳐, 애니메이션

    private int dataOffset;

    private int scrollerBucketSize = 1;

    private int direction = 1;

    /**
     * 터치이벤트는 down-move-up 과정을 거치는데
     * 이러한 조합된 모션을 쉽게 감지할 수 있도록 만든 인터페이스
     */
    private GestureDetector detector;

    private Scroller scroller;

    private ValueAnimator scrollAnimator;

    private ScrollController scrollController;

    private int maxDataOffset = 10000;


    public ScrollableChart(Context context) {
        super(context);
        init(context);
    }

    public ScrollableChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public int getDataOffset() {
        return dataOffset;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {

        if (!scroller.isFinished()) {
            scroller.computeScrollOffset();
            updateDataOffset();
        }
        else {
            scrollAnimator.cancel();
        }
    }

    // 터치하려고 손을 대는 순간 받는다. true로 반환하여 다음 이벤트가 동작되도록 한다.
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    // 드래그하다가 손을 떼면 발생한다. 스크롤과 비슷하지만 끝에 살짝 튕기는 동작에서 발생
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        scroller.fling(scroller.getCurrX(), scroller.getCurrY(),
                direction * ((int) velocityX) / 2, 0, 0, getMaxX(), 0, 0);
        invalidate();

        scrollAnimator.setDuration(scroller.getDuration());
        scrollAnimator.start();
        return false;
    }

    // 드래그시 발생. ( 최소 30ms 이후부터 )
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        if (scrollerBucketSize == 0) return false;

        if (Math.abs(dx) > Math.abs(dy))
        {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
        }


        dx = - direction * dx;
        dx = Math.min(dx, getMaxX() - scroller.getCurrX());
        scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(), (int) dx,
                (int) dy, 0);

        scroller.computeScrollOffset();
        updateDataOffset();
        return true;
    }

    // 터치하면 발생 ( 100ms 정도 )
    @Override
    public void onShowPress(MotionEvent e) {

    }

    // 한번 터치하고 다시 터치이벤트가 들어오지 않을 경우
    // 확실히 한번 터치한 것이 확실할 경우 발생
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    // 액티비티의 터치 이벤트가 발생하면 제스쳐 이벤트로 던져줌
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    public void setDirection(int direction) {
        if (direction != 1 && direction != -1)
            throw new IllegalArgumentException();
        this.direction = direction;
    }

    // 길게 누르면 발생 ( 590~600ms 정도)
    @Override
    public void onLongPress(MotionEvent e) {

    }

    public void setMaxDataOffset(int maxDataOffset) {
        this.maxDataOffset = maxDataOffset;
        this.dataOffset = Math.min(dataOffset, maxDataOffset);
        scrollController.onDataOffsetChanged(this.dataOffset);
        postInvalidate();
    }

    public void setScrollController(ScrollController scrollController) {
        this.scrollController = scrollController;
    }

    public void setScrollerBucketSize(int scrollerBucketSize) {
        this.scrollerBucketSize = scrollerBucketSize;
    }

    private void init(Context context) {
        detector = new GestureDetector(context, this);
        scroller = new Scroller(context, null, true);
        scrollAnimator = ValueAnimator.ofFloat(0, 1);
        scrollAnimator.addUpdateListener(this);
    }

    private int getMaxX() {
        return maxDataOffset * scrollerBucketSize;
    }

    private void updateDataOffset() {
        int newDataOffset = scroller.getCurrX() / scrollerBucketSize;
        newDataOffset = Math.max(0, newDataOffset);
        newDataOffset = Math.min(maxDataOffset, newDataOffset);

        if (newDataOffset != dataOffset)
        {
            dataOffset = newDataOffset;
            scrollController.onDataOffsetChanged(dataOffset);
            postInvalidate();
        }
    }

    public interface ScrollController {
        void onDataOffsetChanged(int newDataOffset);
    }
}
