---
title: 仿掌阅书城书架切换
date: 2016-10-13
tags: Android自定义控件
---

### 首先我们先分析一下掌阅在书城书架的切换效果： ###
- 首先书架、书城两个视图都会移动，速度不同所以大概得出在摆放的时候会有一定的偏移。
- 再有就是透明度、和移动位置随着手指的滑动而变化。
- 书架视图的右边界有一个阴影来达到层叠的效果。

### 在模仿别人的视图中，一般都会使用到系统的显示布局边界的功能。 ###
![](http://i.imgur.com/HqKFuoq.png)
- 在这个图片中可以看到右边有一条边界线应该是视图的初始位置，可以证明第一个特点，下层视图有一定的偏移，大概的偏移量为屏幕的4/5。所以在onLayout()就需要对布局重新排布一下。
<!-- more -->
```
DisplayMetrics dm = context.getResources().getDisplayMetrics();
widthPixels = dm.widthPixels;
// 用于计算被覆盖的移动的距离，产生上下同时完成单速度不同的效果
firstLayoutOffset = (widthPixels / 5)*4;
getChildAt(0).layout(getChildAt(0).getLeft() + firstLayoutOffset,
                        0,getChildAt(0).getLeft() + getChildAt(0).getWidth()
                                + firstLayoutOffset,
                        getChildAt(0).getMeasuredHeight()); ```
- 重写onTouchEvent()方法，在MotionEvent.ACTION_MOVE:中计算X轴的偏移距离，从而根据偏移的大小来变化透明度和偏移量。通过X滑动大小和方向来进行变化。
```
private void doAnimation(int dx, boolean isMoveToLeft) {
        isStartDrag = true;
        // 计算移动中当前透明度的值，通过变化因子和变化值计算
        currentAlpha += (float) alphaForctor * dx;
        // 计算当前的偏移值
        currentTranslate += translateFactor * dx;
        // 防止超过透明度的开始值也为最大值
        currentAlpha = currentAlpha < START_ALPHA ? currentAlpha : START_ALPHA;
        if (currentAlpha <= 0) {
            currentAlpha = 0;
        }
        if ((currentTranslate <= 0 && !isCollapse)
                || (currentTranslate >= 0 && isCollapse)) {
            this.invalidate();
        } else {
            currentTranslate = START_TRANSLATE;
        }
    }
```
- 在MotionEvent.ACTION_UP:中根据当前移动的偏移距离来判断是取消动画还是完成动画。
```
case MotionEvent.ACTION_UP:
                if (x >= mFirstMotionX) {
                    isMoveToRight = true;
                } else {
                    isMoveToRight = false;
                }
                lastAlpha = currentAlpha;
                lastTranslate = currentTranslate;
                if (isMoveToRight) {
                    if (x - mFirstMotionX > widthPixels * 0.5f) {
                        endAnimation();
                    } else {
                        cancelAnimation();
                    }
                } else {
                    if (x + (widthPixels - mFirstMotionX) < widthPixels * 0.5f) {
                        endAnimation();
                    } else {
                        cancelAnimation();
                    }
                }
                break;
```
- 结束动画、和取消动画。
```
private void endAnimation() {
        //根据已经完成的距离，来计算一下还需要多长时间来完成后续的动画
        int duration = (int) (((1 - Math.abs(currentTranslate)) * SCREEN_DIVID_BY) * averageDuration);
        accelerantTween.start(duration);
    }
private void cancelAnimation() {
        this.isCancel = true;
        accelerantTween.start(CANCEL_ANIMATION_TIME);
    }
```
- AccelerantTween类：创建一个线程，根据需要完成的时间，和每一帧的时间来进行事件的回调，完成相应的动作。
```
Runnable mTick = new Runnable() {

        public void run() {
            long baseTime = mBaseTime;
            long now = SystemClock.uptimeMillis();
            long diff = now - baseTime;
            int duration = mDuration;
            float val = diff / (float) duration;
            val = Math.max(Math.min(val, 1.0f), 0.0f);
            currentValue = val;
            if (!isPause) {
                if (diff >= duration) {
                    mCallback.onTweenFinished(next);
                    isLastFrame = true;
                }
                mCallback.onTweenValueChanged(currentValue, isLastFrame);
            } else {
                mCallback.onTweenStop();
            }
            int frame = (int) (diff / FRAME_TIME);
            next = baseTime + ((frame + 1) * FRAME_TIME);
            if (diff < duration && !isPause) {
                mHandler.postAtTime(this, next);
            }
            if (diff >= duration) {
                mRunning = false;
            }
        }
    };
```
- 在根据计算出的偏移距离和透明度进行绘制。
- 大概逻辑详细可以看源码：https://github.com/zhaoyongchao/SlideSwitchScreen
