package com.zyc.slideswitchscreen;

import android.os.Handler;
import android.os.SystemClock;
/**
 * Created by zyc on 2016/10/13.
 * 动画速率控制器
 */

public class AccelerantTween {
    /**
     * 频率
     */
    private final int FPS = 30;
    /**
     * 每一帧的时间
     */
    private final int FRAME_TIME = 1000 / FPS;
    /**
     * 时间间隔
     */
    int mDuration;
    /**
     * 动画回调
     */
    TweenAnimationCallback mCallback;
    /**
     * handler
     */
    Handler mHandler;
    /**
     * 基准时间
     */
    long mBaseTime;
    /**
     * 当前值
     */
    float currentValue;
    /**
     * 是否正在运行
     */
    boolean mRunning;
    /**
     * 是否暂停
     */
    private boolean isPause = false;
    /**
     * 下一次的时间
     */
    private long next;
    /**
     * 是否最后一帧
     */
    private boolean isLastFrame;

    public AccelerantTween(TweenAnimationCallback callback, Handler handler) {
        mCallback = callback;
        this.mHandler = handler;
    }

    /**
     * 开始动画
     *
     * @param duration 动画持续时间
     */
    public void start(int duration) {
        if (!mRunning) {
            isLastFrame = false;
            mDuration = duration;
            mBaseTime = SystemClock.uptimeMillis();
            mRunning = true;
            isPause = false;
            mCallback.onTweenStarted();
            long next = SystemClock.uptimeMillis() + FRAME_TIME;
            mHandler.postAtTime(mTick, next);
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        this.isPause = true;
    }

    /**
     * 线程类计算时间间隔
     */
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
}
