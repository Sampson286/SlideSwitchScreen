package com.zyc.slideswitchscreen;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
/**
 * Created by zyc on 2016/10/13.
 * 用户切换两个视图的布局
 */

public class ChageScreenFrameLayout extends FrameLayout implements TweenAnimationCallback {
    /**
     * 定义平移和透明度的变化因子
     */
    private float translateFactor;
    private float alphaForctor;
    /**
     * 开始动画的变化值
     */
    private final float START_ALPHA = 0.6f;
    private final float START_TRANSLATE = 0.0f;
    /**
     * 当前动画的变化值
     */
    private float currentAlpha = START_ALPHA;
    private float currentTranslate = START_TRANSLATE;
    /**
     * 定义一下被覆盖的视图的偏移
     */
    private int firstLayoutOffset;
    /**
     * 动画标示
     */
    private final int ANIMATION_CANCEL = 0;
    protected final int ANIMATION_END = 1;
    /**
     * 屏幕宽
     */
    private int widthPixels;
    /**
     * 触摸移动的最小值
     */
    private int mTouchSlop;
    /**
     * 结束时间
     */
    private long endTime;
    /**
     * 手指按下移动的坐标
     */
    private int mFirstMotionX = -1;
    private int mLastMotionX;
    private int mLastMotionY;
    /**
     * 动画平均时间时间
     */
    private int averageDuration = 30;
    /**
     * 以前宽为480的为基准
     */
    private float standardWidth = 480.0f;
    /**
     * 屏幕划分份数
     */
    private static final float SCREEN_DIVID_BY = 10;
    /**
     * 取消切换 动画时间
     */
    private static final int CANCEL_ANIMATION_TIME = 200;
    /**
     * 判断当前视图的状态，是折叠还是打开
     */
    private boolean isCollapse = false;
    /**
     * 是否开始拖动
     */
    private boolean isStartDrag = false;
    /**
     * 是否动画结束结束
     */
    private boolean isFinish = false;
    /**
     * 是否放弃切换
     */
    private boolean isCancel = false;
    /**
     * 是否向右移动
     */
    private boolean isMoveToRight = false;
    /**
     * 手指松开后动画开始
     */
    private boolean isAnimation = false;
    /**
     * 是否开始交换动画
     */
    private boolean isStartSwitchAnim;
    /**
     * 右边阴影
     */
    private Bitmap sideBitmap;
    /**
     * 上次渐变值
     */
    private float lastAlpha;
    /**
     * 上次平移值
     */
    private float lastTranslate;
    /**
     * 手势检查
     */
    private GestureDetector mGestureDetector;
    /**
     * 用于更新的handler
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ANIMATION_END:
                    isAnimation = false;
                    isStartDrag = false;
                    currentTranslate = START_TRANSLATE;
                    if (isCollapse) {
                        getChildAt(1).layout(-widthPixels, 0, 0,
                                getChildAt(1).getMeasuredHeight());
                        getChildAt(0).layout(0, 0, getChildAt(0).getWidth(),
                                getChildAt(0).getMeasuredHeight());
                    } else {
                        getChildAt(1).layout(0, 0, getChildAt(1).getWidth(),
                                getChildAt(1).getMeasuredHeight());
                        getChildAt(0).layout(
                                getChildAt(0).getLeft() + firstLayoutOffset,
                                0,
                                getChildAt(0).getLeft() + getChildAt(0).getWidth()
                                        + firstLayoutOffset,
                                getChildAt(0).getMeasuredHeight());
                    }
                    break;
                case ANIMATION_CANCEL:
                    isAnimation = false;
                    isStartDrag = false;
                    currentAlpha = START_ALPHA;
                    currentTranslate = START_TRANSLATE;
                    break;
            }
        }

        ;
    };
    /**
     * 动画速率控制
     */
    private AccelerantTween accelerantTween = new AccelerantTween(this, handler);

    public ChageScreenFrameLayout(Context context) {
        this(context, null);
    }

    public ChageScreenFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChageScreenFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context);
    }

    /***
     * 初始化一些数据
     * @param context
     */
    private void initData(Context context) {
        // 获取当前屏幕的宽度
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        widthPixels = dm.widthPixels;
        averageDuration = (int) ((widthPixels / standardWidth) * averageDuration);
        // 用于计算被覆盖的移动的距离，产生上下同时完成单速度不同的效果
        firstLayoutOffset = (widthPixels / 5)*4;
        // 手势控制渐变的比例
        int gestrueAlpha = 2;
        translateFactor = ((float) 1) / (widthPixels);
        alphaForctor = ((float) 1) / (widthPixels * gestrueAlpha);
        //得到最小的滑动距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //定义一个手势滑动，处理快速滑动
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        int dx = (int) (e2.getX() - e1.getX()); // 计算滑动的距离
                        if (Math.abs(dx) > mTouchSlop
                                && Math.abs(velocityX) > Math.abs(velocityY)) { // 降噪处理，必须有较大的动作才识别
                            lastAlpha = currentAlpha;
                            lastTranslate = currentTranslate;
                            if (velocityX > 0) {
                                // 向右边
                                if (isCollapse) {
                                    isMoveToRight = true;
                                    accelerantTween.start(300);
                                } else {
                                    cancelAnimation();
                                }
                            } else {
                                // 向左边
                                if (!isCollapse) {
                                    isMoveToRight = false;
                                    accelerantTween.start(300);
                                } else {
                                    cancelAnimation();
                                }
                            }
                            return true;
                        } else {
                            return false; // 当然可以处理velocityY处理向上和向下的动作
                        }
                    }
                });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //进行视图的重新摆放
        if (isCollapse) {
            if (getChildCount() == 2) {
                getChildAt(1).layout(-widthPixels, 0, 0,
                        getChildAt(1).getMeasuredHeight());
            }
        } else {
            if (getChildCount() == 2) {
                getChildAt(0).layout(
                        getChildAt(0).getLeft() + firstLayoutOffset,
                        0,
                        getChildAt(0).getLeft() + getChildAt(0).getWidth()
                                + firstLayoutOffset,
                        getChildAt(0).getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isAnimation) {
            return true;
        }
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isStartSwitchAnim = false;
                mFirstMotionX = mLastMotionX = (int) x;
                mLastMotionY = (int) y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isCollapse && (x - mLastMotionX) > 0) {
                    return false;
                }
                if (isCollapse && (x - mLastMotionX) < 0) {
                    return false;
                }
                float yDiff = Math.abs(y - mLastMotionY);
                float xDiff = Math.abs(x - mLastMotionX);
                boolean yMoved = yDiff > mTouchSlop;
                boolean xMoved = xDiff > mTouchSlop;
                if (yMoved && yDiff > xDiff) {
                    MotionEvent motionEvent = MotionEvent.obtain(ev);
                    motionEvent.setAction(MotionEvent.ACTION_DOWN);
                    return super.onInterceptTouchEvent(motionEvent);
                } else if (xMoved) {
                    isStartSwitchAnim = true;
                    return true;
                }
                break;
        }
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (!isStartSwitchAnim) {
            return false;
        }
        if (isAnimation) {
            return false;
        }
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dx = x - mLastMotionX;
                if (dx != 0) {
                    if ((mLastMotionX - mFirstMotionX > 0 && x - mFirstMotionX < 0)
                            || (mLastMotionX - mFirstMotionX < 0 && x
                            - mFirstMotionX > 0)) {
                        // currentAlpha = START_ALPHA;
                        currentTranslate = START_TRANSLATE;
                        dx = x - mFirstMotionX;
                    }
                    doAnimation(dx, x < mFirstMotionX);
                } else {
                    mLastMotionX = x;
                    break;
                }
                mLastMotionX = x;
                break;
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
        }
        return true;
    }

    @Override
    public void onTweenValueChanged(float interpolatedTime, boolean isLastFrame) {
        if (isMoveToRight) {
            if (isCancel) {
                currentTranslate = lastTranslate - lastTranslate
                        * interpolatedTime;
                currentAlpha = lastAlpha - lastAlpha * interpolatedTime;
            } else {
                currentTranslate = lastTranslate + (1 - lastTranslate)
                        * interpolatedTime;
                currentAlpha = lastAlpha + (START_ALPHA - lastAlpha)
                        * interpolatedTime;
            }
            currentTranslate = currentTranslate > 0.99f ? 1.0f
                    : currentTranslate;
        } else {
            if (isCancel) {
                currentTranslate = lastTranslate - lastTranslate
                        * interpolatedTime;
                currentAlpha = lastAlpha - lastAlpha * interpolatedTime;
            } else {
                currentTranslate = lastTranslate - (1 - lastTranslate)
                        * interpolatedTime;
                currentAlpha = lastAlpha - lastAlpha * interpolatedTime;
            }
            currentTranslate = currentTranslate < -0.99f ? -1.0f
                    : currentTranslate;
        }
        if (currentAlpha <= 0) {
            currentAlpha = 0;
        }
        currentAlpha = currentAlpha < START_ALPHA ? currentAlpha : START_ALPHA;
        if (currentTranslate >= 1.0f) {
            currentAlpha = currentTranslate = 1.0f;
        }
        invalidate();
    }

    @Override
    public void onTweenStarted() {
        isAnimation = true;
        this.isStartDrag = true;
        this.isFinish = false;
    }

    @Override
    public void onTweenFinished(long time) {
        endTime = time;
        isFinish = true;
        if (!isCancel) {
            isCollapse = !isCollapse;
        }

    }

    @Override
    public void onTweenStop() {
        // TODO Auto-generated method stub

    }

    /**
     * 动画过程中的改变
     *
     * @param dx           当前X方向的偏移
     * @param isMoveToLeft 当前移动的方向
     */
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

    /**
     * handler发送消息
     *
     * @param what 消息类型
     */
    private void sendMyEmptyMessage(int what) {
        boolean sendEmptyMessage = handler.sendEmptyMessageDelayed(what, 0);
        while (!sendEmptyMessage) {
            sendEmptyMessage = handler.sendEmptyMessageDelayed(what, 0);
        }
    }

    /**
     * 结束动画
     *
     */
    private void endAnimation() {
        int duration = (int) (((1 - Math.abs(currentTranslate)) * SCREEN_DIVID_BY) * averageDuration);
        accelerantTween.start(duration);
    }

    /**
     * 放弃切换动画
     */
    private void cancelAnimation() {
        this.isCancel = true;
        accelerantTween.start(CANCEL_ANIMATION_TIME);
    }

    /**
     * 进行视图的绘制
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        int childCount = getChildCount();
        if ((childCount == 0 || !this.isStartDrag) && !isFinish) {
            super.dispatchDraw(canvas);
        } else {
            if (this.isStartDrag) {
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child != null) {
                        if (i == 0) {
                            canvas.save();
                            canvas.translate(firstLayoutOffset
                                    * currentTranslate, 0);
                            drawChild(canvas, child, getDrawingTime());
                            if (currentAlpha < 0.99f) {
                                Paint paint = new Paint();
                                paint.setAlpha((int) (currentAlpha * 255));
                                canvas.drawPaint(paint);
                            }
                            canvas.restore();
                        }
                        if (i == 1) {
                            canvas.save();
                            // 先绘制阴影
                            if (Math.abs(currentTranslate) < 1.0) {

                                if (currentTranslate > 0) {
                                    if (sideBitmap == null) {
                                        sideBitmap = BitmapFactory
                                                .decodeResource(getResources(),
                                                        R.mipmap.td_bg_right);
                                    }
                                    // 进行屏幕的移动
                                    canvas.translate(
                                            widthPixels * currentTranslate
                                                    - sideBitmap.getWidth(), 0);
                                    // 绘制阴影
                                    canvas.drawBitmap(sideBitmap,
                                            sideBitmap.getWidth(), 0, null);
                                    canvas.translate(-(widthPixels
                                            * currentTranslate - sideBitmap
                                            .getWidth()), 0);
                                } else {
                                    if (sideBitmap == null) {
                                        sideBitmap = BitmapFactory
                                                .decodeResource(getResources(),
                                                        R.mipmap.td_bg_right);
                                    }
                                    canvas.translate(widthPixels
                                            * (1 + currentTranslate), 0);
                                    canvas.drawBitmap(sideBitmap, 0, 0, null);
                                    canvas.translate(
                                            -(widthPixels * (1 + currentTranslate)),
                                            0);
                                }
                            }
                            canvas.translate(widthPixels * currentTranslate, 0);
                            drawChild(canvas, child, getDrawingTime());
                            canvas.restore();
                        }
                    }
                }
            }
            if (this.isFinish) {
                this.isFinish = false;
                if (this.isCancel) {
                    this.isCancel = false;
                    super.dispatchDraw(canvas);
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            sendMyEmptyMessage(ANIMATION_CANCEL);
                        }
                    }, endTime);
                } else {
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            sendMyEmptyMessage(ANIMATION_END);
                        }
                    }, endTime);
                }
            }
        }

    }
}
