package com.zyc.slideswitchscreen;

/**
 * Created by zyc on 2016/10/13.
 * 创建动画回调接口
 */

public interface TweenAnimationCallback {
    /**
     * 动画值改变
     *
     * @param fastInterpolatedTime 快速改变的时间
     * @param isLastFrame          是否最后一帧
     */
    void onTweenValueChanged(float fastInterpolatedTime, boolean isLastFrame);

    /**
     * 动画开始
     */
    void onTweenStarted();

    /**
     * 动画完成
     *
     * @param time 完成时间
     */
    void onTweenFinished(long time);

    /**
     * 动画停止
     */
    void onTweenStop();
}
