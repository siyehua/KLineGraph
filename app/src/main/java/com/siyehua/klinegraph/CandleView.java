package com.siyehua.klinegraph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * candle-view
 * Created by siyehua on 2017/1/2.
 */
public class CandleView extends View {
    private class Candle {
        String time;
        Float start;
        Float end;
        Float max;
        Float min;
        Float ma5;
        Float ma10;
        Float ma20;

        @Override
        public String toString() {
            return "Candle{" +
                    "time='" + time + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    ", max=" + max +
                    ", min=" + min +
                    ", ma5=" + ma5 +
                    ", ma10=" + ma10 +
                    ", ma20=" + ma20 +
                    '}';
        }
    }


    /**
     * touch listener
     */
    private GestureDetector detector;
    private Paint mPaint;
    /**
     * data
     */
    private ArrayList<Candle> candles;
    /**
     * max price
     */
    private float maxPrice;
    /**
     * min price
     */
    private float minPrice = Float.MAX_VALUE;
    /**
     * min y
     */
    private int minY;

    /**
     * max y
     */
    private int maxY;
    /**
     * y scale
     */
    private int yScale;

    /**
     * long press flag
     */
    private boolean longPressFlag = false;
    /**
     * touch x
     */
    private int touchIndex;
    private KLineView.TouchMoveListener touchMoveListener;
    /**
     * data start index
     */
    private int startIndex = 0;
    /**
     * the candles count
     */
    private int count = 60;
    /**
     * down index
     */
    private int downIndex;
    /**
     * finger down x
     */
    private float downX;
    /**
     * second down x
     */
    private float down2X;
    /**
     * type 1:one,2 two
     */
    private int type = 1;


    public CandleView(Context context) {
        super(context);
        init();
    }

    public CandleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CandleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CandleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private boolean flag1 = false;
    private boolean flag2 = false;

    private void init() {
        detector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown2(MotionEvent e) {
                Log.e("onDown2", e.getX(1) + "");
                flag1 = flag2 = false;
                type = 2;
                down2X = e.getX(1);
                return true;
            }

            @Override
            public boolean onUp2(MotionEvent e) {
                flag2 = true;
                if (flag1) type = 1;
                return true;
            }

            @Override
            public boolean onUp(MotionEvent e) {
                if (type == 2) {
                    flag1 = true;
                    if (flag2) type = 1;
                } else hideTouchLine();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                Log.e("onDown", e.getRawX() + "");

                type = 1;
                downIndex = startIndex;
                downX = e.getX(0);
                return true;
            }


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float
                    distanceY) {

                //16ms refresh one time,because man resolution 16ms only.
                if (refreshFlag) {
                    refreshFlag = false;
                    handler.sendEmptyMessageDelayed(10086, 15);
                    if (type == 1) {

                        showTouchLine(e2.getRawX());
                    } else {
                        if (e2.getPointerCount() >= 2) {
                            float moveDistance = Math.abs(e2.getX(0) - e2.getX(1)) - Math.abs
                                    (downX - down2X);
                            Log.e("onScroll", Math.abs(e2.getX(0) - e2.getX(1)) + "   " + Math
                                    .abs(downX - down2X) + "   " + moveDistance + "    " + getWidth()
                                    / 30);
                            scaleCandle(moveDistance);
                        }
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (type == 1) {
                    longPressFlag = true;
                    showTouchLine(e.getRawX());
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float
                    velocityY) {
                return true;
            }
        });

        candles = new ArrayList<>();
        mPaint = new Paint();
        createTestData();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        float itemW = (float) viewWidth / count;
        float itemH = viewHeight / 410f;
        drawLinesAndText(canvas, viewWidth, viewHeight, itemW, itemH);


        drawCandles(canvas, viewWidth, viewHeight, itemW, itemH);

        drawTouchLines(canvas, viewWidth, viewHeight, itemW, itemH);
    }

    /**
     * draw candles
     *
     * @param canvas     canvas
     * @param viewWidth  the view's width
     * @param viewHeight the view's height
     * @param itemW      the view's wight divided into count
     * @param itemH      the view's height divided into 410
     */
    private void drawCandles(Canvas canvas, int viewWidth, int viewHeight, float itemW, float
            itemH) {
        mPaint.setStrokeWidth(2f);
        mPaint.setStyle(Paint.Style.FILL);
        String tmpMonth = candles.get(startIndex).time.substring(5, 7);

        for (int i = startIndex; i < startIndex + count; i++) {
            //set paint color
            if (candles.get(i).end > candles.get(i + 1).end) {
                mPaint.setColor(Color.RED);
            } else mPaint.setColor(Color.GREEN);

            if (!tmpMonth.equals(candles.get(i + 1).time.substring(5, 7))) {
                tmpMonth = candles.get(i + 1).time.substring(5, 7);
                mPaint.setColor(Color.DKGRAY);
            }

            float left, top, right, bottom;

            //draw line
            float tmp = itemW * (count + startIndex - i) - itemW / 2;
            left = tmp;
            top = ((maxY - candles.get(i).max) / (maxY - minY) * 370 + 10) * itemH;
            right = tmp;
            bottom = ((maxY - candles.get(i).min) / (maxY - minY) * 370 + 10) * itemH;
            if (top > bottom) {
                float a = top;
                top = bottom;
                bottom = a;
            }
            canvas.drawLine(left, top, right, bottom, mPaint);
//            Log.e("siyehua", tmp + ", " + (((maxY - candles.get(i).start) / (maxY - minY) * 370 +
//                    10) * itemH) + ", " + tmp + ", " + (((maxY - candles.get(i).end) / (maxY -
//                    minY) * 370 + 10) * itemH));

            //draw candles
            left = itemW * (count - 1 + startIndex - i) + 2f;
            top = ((maxY - candles.get(i).start) / (maxY - minY) * 370 + 10) * itemH;
            right = itemW * (count + startIndex - i) - 2f;
            bottom = ((maxY - candles.get(i).end) / (maxY - minY) * 370 + 10) * itemH;
            if (top > bottom) {
                float a = top;
                top = bottom;
                bottom = a;
            }
            canvas.drawRect(left, top, right, bottom, mPaint);
        }

    }

    /**
     * draw lines and text
     *
     * @param canvas     canvas
     * @param viewWidth  the view's width
     * @param viewHeight the view's height
     * @param itemW      the view's wight divided into count
     * @param itemH      the view's height divided into 410
     */
    private void drawLinesAndText(Canvas canvas, int viewWidth, int viewHeight, float itemW,
                                  float itemH) {
        mPaint.setColor(Color.parseColor("#AAAAAA"));
        mPaint.setStrokeWidth(0f);
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f,
                getResources().getDisplayMetrics()));
        /**
         * draw x lines and price text
         */
        getYData();
        int lineCount = (maxY - minY) / yScale;
        if (lineCount > 5) {
            yScale *= 2;
            lineCount = (maxY - minY) / yScale;
        }
        //draw first line
        canvas.drawLine(0, itemH * 10, viewWidth, itemH * 10, mPaint);

        float percent = 370 / (float) lineCount;
        for (int i = 1; i < lineCount; i++) {
            //draw prices
            String content = minY + (lineCount - i) * yScale + "";
            canvas.drawText(content, itemH * 10, itemH * (10 + percent * i) - mPaint
                    .getFontMetrics().bottom, mPaint);

            //draw middle lines
            canvas.drawLine(0, itemH * (10 + percent * i), viewWidth, itemH * (10 + percent * i),
                    mPaint);
        }
        //draw last line
        canvas.drawLine(0, itemH * 380, viewWidth, itemH * 380, mPaint);


        /**
         * draw y lines and time
         */
        String tmpMonth = candles.get(startIndex).time.substring(5, 7);
        for (int i = startIndex + 1; i < startIndex + count; i++) {
            if (!tmpMonth.equals(candles.get(i + 1).time.substring(5, 7))) {
                tmpMonth = candles.get(i + 1).time.substring(5, 7);
                String timeStr = candles.get(i).time.substring(0, 7);
                float tmp = itemW * (count + startIndex - i) - itemW / 2;

                //draw times
                float timeWidth = mPaint.measureText(timeStr);
                canvas.drawText(timeStr, tmp - timeWidth / 2, itemH * 380 + -mPaint
                        .getFontMetrics().top, mPaint);

                //draw liens
                canvas.drawLine(tmp, itemH * 10, tmp, itemH * 380, mPaint);
            }
        }

    }

    /**
     * calculate min and max y,the scale y.
     */
    private void getYData() {
        maxPrice = 0;
        minPrice = Float.MAX_VALUE;
        for (int i = startIndex; i < startIndex + count; i++) {
            if (candles.get(i).start > maxPrice) maxPrice = candles.get(i).start;
            if (candles.get(i).start < minPrice) minPrice = candles.get(i).start;
            if (candles.get(i).end > maxPrice) maxPrice = candles.get(i).end;
            if (candles.get(i).end < minPrice) minPrice = candles.get(i).end;
            if (candles.get(i).max > maxPrice) maxPrice = candles.get(i).max;
            if (candles.get(i).max < minPrice) minPrice = candles.get(i).max;
            if (candles.get(i).min > maxPrice) maxPrice = candles.get(i).min;
            if (candles.get(i).min < minPrice) minPrice = candles.get(i).min;

        }
        yScale = 1;
        int diff = (int) (maxPrice - minPrice);
        if (diff / 100000 >= 1) {
            yScale = 100000;
            minY = (int) minPrice / 100000 * 100000;
            maxY = ((int) maxPrice / 100000 + 1) * 100000;
        } else if (diff / 10000 >= 1) {
            yScale = 10000;
            minY = (int) minPrice / 10000 * 10000;
            maxY = ((int) maxPrice / 10000 + 1) * 10000;
        } else if (diff / 1000 >= 1) {
            yScale = 1000;
            minY = (int) minPrice / 1000 * 1000;
            maxY = ((int) maxPrice / 1000 + 1) * 1000;
        } else if (diff / 100 >= 1) {
            yScale = 100;
            minY = (int) minPrice / 100 * 100;
            maxY = ((int) maxPrice / 100 + 1) * 100;
        } else if (diff / 10 >= 1) {
            yScale = 10;
            minY = (int) minPrice / 10 * 10;
            maxY = ((int) maxPrice / 10 + 1) * 10;
        }
//        Log.e("siyehua", maxPrice + "  " + minPrice + "  " + maxY + "  " + minY + "  " +
//                yScale + "  " + "  ");
    }

    /**
     * draw lines and text
     *
     * @param canvas     canvas
     * @param viewWidth  the view's width
     * @param viewHeight the view's height
     * @param itemW      the view's wight divided into count
     * @param itemH      the view's height divided into 410
     */
    private void drawTouchLines(Canvas canvas, int viewWidth, int viewHeight, float itemW, float
            itemH) {
        if (longPressFlag) {
            float x = itemW * touchIndex - itemW / 2;
            float y;
            float a = ((maxY - candles.get(count + startIndex - touchIndex).start) / (maxY -
                    minY) * 370 + 10) * itemH;
            float b = ((maxY - candles.get(count + startIndex - touchIndex).end) / (maxY - minY)
                    * 370 + 10) * itemH;
            if (candles.get(count + startIndex - touchIndex).end < candles.get(count + startIndex
                    - touchIndex + 1).end) {
                y = a > b ? a : b;
            } else y = a < b ? a : b;


            //draw the lines
            mPaint.setColor(Color.parseColor("#999999"));
            canvas.drawLine(0, y, viewWidth, y, mPaint);
            canvas.drawLine(x, itemH * 10, x, itemH * 380, mPaint);

            //draw the point
//            mPaint.setColor(Color.parseColor("#FFC125"));
//            mPaint.setStrokeWidth(10f);
//            canvas.drawPoint(x, y, mPaint);
        }
    }

    private void scaleCandle(float moveDistance) {
        if (moveDistance > getWidth() / 30) {

            if (count == 20) count = 10;
            else if (count == 10) return;
            else count -= 20;
        } else if (moveDistance < -getWidth() / 30) {
            if (count == 240) return;
            else count += 20;
        }
        postInvalidate();
    }

    /**
     * hide touch line
     */
    private void hideTouchLine() {
        touchIndex = -1;
        longPressFlag = false;
        if (touchMoveListener != null) {
            touchMoveListener.change("", "", "", "");
        }
        postInvalidate();
    }

    /**
     * show touch line
     */
    private void showTouchLine(float touchX) {
        float itemX = (float) getWidth() / count;

        if (longPressFlag) {
            for (int i = 1; i <= count; i++) {
                if (itemX * i >= touchX) {
                    touchIndex = i + 1;
                    break;
                }
            }
            if (touchMoveListener != null && touchIndex >= 0) {
                touchMoveListener.change(candles.get(count + startIndex - touchIndex).time
                        .substring(0, 10), candles.get(count + startIndex - touchIndex).end + "",
                        formatPrice((candles.get(count + startIndex - touchIndex).end - candles
                                .get(count + startIndex - touchIndex + 1).end) / candles.get
                                (count + startIndex - touchIndex + 1).end * 100) + "%", "4613" +
                                ".93万");

            }
        } else {
            int number = (int) ((touchX - downX) / itemX);
//            Log.e("number", number + "");
            startIndex = downIndex + number;
            if (startIndex < 0) startIndex = 0;
            if (startIndex > candles.size() - count - 1) startIndex = candles.size() - count - 1;
        }
        postInvalidate();
    }

    private boolean refreshFlag = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            refreshFlag = true;
            super.handleMessage(msg);
        }
    };

    public void setTouchMoveListener(KLineView.TouchMoveListener touchMoveListener) {
        this.touchMoveListener = touchMoveListener;
    }

//    public interface TouchMoveListener {
//        void change(String time, String price, String percent, String count);
//    }

    /**
     * create test data
     */
    private void createTestData() {
        //create 4 months data
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat
                ("yyyy-MM-dd HH:mm:ss");
        Float todayStart = 3150.10f;
        for (int i = 0; i < 1200; i++) {
            Candle candle = new Candle();
            date.setTime(date.getTime() - 24L * 60L * 60L * 1000L);
//            candle.time = formatTime(dateFormat.format(date));
            candle.time = dateFormat.format(date);
            if (i == 0) candle.start = todayStart;
            else
                candle.start = formatPrice((float) (candles.get(i - 1).end + 100 - Math.random()
                        * 200));
            candle.end = formatPrice((float) (candle.start + candle.start * 0.05 - Math.random()
                    * candle.start * 0.1));
            float tmp = formatPrice((float) (candle.start * 0.05 - Math.random() * candle.start *
                    0.1));
            candle.max = formatPrice(candle.start + (tmp < 0 ? 0 : tmp));
            tmp = formatPrice((float) (candle.start * 0.05 - Math.random() * candle.start * 0.1));
            candle.min = formatPrice(candle.start + (tmp > 0 ? 0 : tmp));
            candles.add(candle);
        }
        for (int i = 0; i < candles.size(); i++) {
            float total = 0f;
            if (i < candles.size() - 5) {
                for (int j = i; j < i + 5; j++) {
                    total += candles.get(j).end;
                }
                candles.get(i).ma5 = total / 5;
            } else {
                candles.get(i).ma5 = candles.get(i).end;
            }
            total = 0f;
            if (i < candles.size() - 10) {
                for (int j = i; j < i + 10; j++) {
                    total += candles.get(j).end;
                }
                candles.get(i).ma10 = total / 10;
            } else {
                candles.get(i).ma10 = candles.get(i).end;
            }
            total = 0f;
            if (i < candles.size() - 20) {
                for (int j = i; j < i + 20; j++) {
                    total += candles.get(j).end;
                }
                candles.get(i).ma20 = total / 20;
            } else {
                candles.get(i).ma20 = candles.get(i).end;
            }
        }

//        for (Candle candle : candles) {
//            Log.e("item", candle.toString());
//        }
    }

    /**
     * format the prices data
     *
     * @param price price
     * @return format price
     */
    private float formatPrice(float price) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return Float.parseFloat(df.format(price));
    }

    /**
     * format time
     *
     * @param time timeStr
     * @return format time
     */
    private String formatTime(String time) {
        return time.substring(0, 7);
    }

}
