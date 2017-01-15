package com.siyehua.klinegraph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * k-line
 * Created by siyehua on 2017/1/1.
 */
public class KLineView extends View {
    /**
     * event action listener
     */
    private GestureDetector detector;
    /**
     * canvas paint
     */
    private Paint mPaint;
    /**
     * base Y
     */
    private float baseData;
    /**
     * max price
     */
    private float maxPrice;
    /**
     * min price
     */
    private float minPrice = Float.MAX_VALUE;
    /**
     * list of time
     */
    private ArrayList<String> times;
    /**
     * list of price
     */
    private ArrayList<Float> prices;

    /**
     * long press flag
     */
    private boolean longPressFlag = false;
    /**
     * touch x
     */
    private int touchIndex;
    private TouchMoveListener touchMoveListener;


    public KLineView(Context context) {
        super(context);
        init();
    }

    public KLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KLineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        detector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                showTouchLine(e.getRawX());
                Log.e("onLongPress", getActionName(e));
            }


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float
                    distanceY) {
                Log.e("onScroll", getActionName(e2) + "  Y: " + distanceY + "  e2: " + e2.getRawY
                        ());
                if (e2.getAction() == MotionEvent.ACTION_MOVE && longPressFlag) {
                    showTouchLine(e2.getRawX());
                }
                return true;
            }


            @Override
            public boolean onDown2(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onUp2(MotionEvent e) {
                return false;
            }


            @Override
            public boolean onUp(MotionEvent e) {
                Log.e("onUp", getActionName(e));
                hideTouchLine();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
//                Log.e("onDown", getActionName(e));
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float
                    velocityY) {
//                Log.e("onFling", getActionName(e2));
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.e("onSingleTapUp", getActionName(e));
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
//                Log.e("onShowPress", getActionName(e));
            }
        });

        mPaint = new Paint();

        createTestData();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }


    /**
     * create the test data
     */
    private void createTestData() {
        baseData = 3120.50f;
        try {
            times = new ArrayList<>();
            prices = new ArrayList<>();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat
                    ("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormat.parse("2017-01-01 09:30:00");
            for (int i = 0; i < 240; i++) {
                if (i == 120) {
                    date = dateFormat.parse("2017-01-01 13:00:00");
                }
                date.setTime(date.getTime() + 60 * 1000);
                times.add(formatTime(dateFormat.format(date)));

                float tmp;
                if (i == 0) tmp = (float) (baseData + 5 - Math.random() * 10);
                else tmp = (float) (prices.get(i - 1) + 5 - Math.random() * 10);
                tmp = formatPrice(tmp);
                if (tmp > maxPrice) {
                    maxPrice = tmp;
                }
                if (tmp < minPrice) {
                    minPrice = tmp;
                }
                prices.add(tmp);
            }
//            for (String str : times) {
//                Log.e("time", str);
//            }
//            for (Float item : prices) {
//                Log.e("time", item + "");
//            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
        return time.substring(11, 16);
    }

    /**
     * get action name
     *
     * @param event event
     * @return event name
     */
    private String getActionName(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            default:
                return "other action";
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        float item = viewHeight / 410f;

        /**
         * draw lines
         */
        drawLines(canvas, viewWidth, item);


        /**
         * draw time
         */
        drawTimes(canvas, viewWidth, item);


        /**
         * draw broken line and shadow graph
         */
        drawBrokenLine(canvas, viewWidth, item, "#504F76DB", Paint.Style.FILL);
        drawBrokenLine(canvas, viewWidth, item, "#4F76DB", Paint.Style.STROKE);

        /**
         * draw max, min price and percent
         */
        drawPriceAndPercent(canvas, viewWidth, item);

        /**
         * draw touch lines and point
         */
        drawTouchLines(canvas, viewWidth, item);

    }


    /**
     * draw lines
     * <p>from top to bottom, it have 5 horizontal lines,
     * <br> 1 vertical line in the horizontal center.
     * </p>
     *
     * @param canvas    canvas
     * @param viewWidth the view's width
     * @param item      the view's height divided into 410
     */
    private void drawLines(Canvas canvas, int viewWidth, float item) {
        mPaint.setColor(Color.parseColor("#AAAAAA"));
        mPaint.setStrokeWidth(0f);
        canvas.drawLine(0, item * 10, viewWidth, item * 10, mPaint);
        canvas.drawLine(0, item * 30, viewWidth, item * 30, mPaint);
        drawDashEffect(canvas, 0, item * 190, viewWidth, item * 190);
        canvas.drawLine(0, item * 360, viewWidth, item * 360, mPaint);
        canvas.drawLine(0, item * 380, viewWidth, item * 380, mPaint);
        canvas.drawLine(viewWidth / 2.0f, item * 10, viewWidth / 2.0f, item * 380, mPaint);
    }

    /**
     * draw a doted line
     *
     * @param canvas canvas
     * @param x      startX
     * @param y      startY
     * @param endX   endX
     * @param endY   endY
     */
    private void drawDashEffect(Canvas canvas, float x, float y, float endX, float endY) {
        PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.parseColor("#AAAAAA"));
        p.setPathEffect(effects);
        p.setStyle(Paint.Style.STROKE);
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(endX, endY);
        canvas.drawPath(path, p);
    }


    /**
     * draw times
     * <br><br>
     * draw text method:
     * <p>params: 1:content, 2:x, 3: the baseline</p>
     * <br><b>Note:the baseline == -mPaint.getFontMetrics().top in default</b>
     * <br><br><b>More information, please
     * <a href="https://github.com/siyehua/StyleTextView">click this</a></b>
     *
     * @param canvas    canvas
     * @param viewWidth view's width
     * @param item      the view's height divided into 410
     */
    private void drawTimes(Canvas canvas, int viewWidth, float item) {
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f,
                getResources().getDisplayMetrics()));
        mPaint.setColor(Color.parseColor("#999999"));
        float textWidth = mPaint.measureText("09:30");
        canvas.drawText("09:30", item * 10, -mPaint.getFontMetrics().top + item * 380, mPaint);
        canvas.drawText("11:30", viewWidth / 2.0f - textWidth / 2.0f, -mPaint.getFontMetrics()
                .top + item * 380, mPaint);
        canvas.drawText("15:00", viewWidth - textWidth - item * 10, -mPaint.getFontMetrics().top
                + item * 380, mPaint);
    }

    /**
     * draw broken line
     *
     * @param canvas    canvas
     * @param viewWidth view's width
     * @param item      the view's height divided into 410
     * @param color     paint color
     * @param style     paint style,FILL: draw shadow, STROKE:draw line
     */
    private void drawBrokenLine(Canvas canvas, int viewWidth, float item, String color, Paint
            .Style style) {
        Path path = new Path();
        Paint paint = new Paint();
        float xItem = viewWidth / 2.0f / 120f;

        // get biggest  difference value, it will be calculated proportion
        float yCount = maxPrice - baseData > baseData - minPrice ? maxPrice - baseData : baseData
                - minPrice;
        //get one item height
        float yItem = 330 * item / yCount / 2.0f;

        //set path start point,item * 195 is baseData's y point.
        path.moveTo(0, item * 195);
        //set other points
        for (int i = 0; i < times.size(); i++) {
            path.lineTo(xItem * (i + 1), item * 195 + yItem * (baseData - prices.get(i)));
        }
        //if draw shadow, we should add 3 points to draw a complete graphics.
        //if draw lines, we should let lines bold.
        if (Paint.Style.FILL == style) {
            path.lineTo(viewWidth, item * 380);
            path.lineTo(0, item * 380);
            path.lineTo(0, item * 195);
            path.close();
        } else {
            paint.setStrokeWidth(2f);
        }
        paint.setColor(Color.parseColor(color));
        paint.setAntiAlias(true);
        paint.setStyle(style);
        canvas.drawPath(path, paint);
    }


    /**
     * draw price and percent
     * <br><br>
     * draw text method:
     * <p>params: 1:content, 2:x, 3: the baseline</p>
     * <br><b>Note:the baseline == -mPaint.getFontMetrics().top in default</b>
     * <br><br><b>More information, please
     * <a href="https://github.com/siyehua/StyleTextView">click this</a></b>
     *
     * @param canvas    canvas
     * @param viewWidth view's width
     * @param item      the view's height divided into 410
     */
    private void drawPriceAndPercent(Canvas canvas, int viewWidth, float item) {
        // get biggest  difference value, it will be calculated proportion
        float yCount = maxPrice - baseData > baseData - minPrice ? maxPrice - baseData : baseData
                - minPrice;
        mPaint.setStrokeWidth(2f);


        mPaint.setColor(Color.RED);
        //draw max price
        canvas.drawText(yCount + baseData + "", item * 10, -mPaint.getFontMetrics().top + item *
                30, mPaint);
        String percentStr = formatPrice(yCount * 100 / baseData) + "%";
        float textWidth = mPaint.measureText(percentStr);
        //draw max percent
        canvas.drawText(percentStr, viewWidth - textWidth - item * 10, -mPaint.getFontMetrics()
                .top + item * 30, mPaint);


        mPaint.setColor(Color.parseColor("#008000"));
        //draw min price
        canvas.drawText(baseData - yCount + "", item * 10, item * 360 - (mPaint.getFontMetrics()
                .descent - mPaint.getFontMetrics().ascent - mPaint.getTextSize() + mPaint
                .getFontMetrics().ascent - mPaint.getFontMetrics().top), mPaint);
        percentStr = "-" + percentStr;
        textWidth = mPaint.measureText(percentStr);
        //draw min percent
        canvas.drawText(percentStr, viewWidth - textWidth - item * 10, item * 360 - (mPaint
                .getFontMetrics().descent - mPaint.getFontMetrics().ascent -
                mPaint.getTextSize() + mPaint.getFontMetrics().ascent - mPaint.getFontMetrics()
                .top), mPaint);
    }


    /**
     * draw touch lines and point
     *
     * @param canvas    canvas
     * @param viewWidth view's width
     * @param item      the view's height divided into 410
     */
    private void drawTouchLines(Canvas canvas, int viewWidth, float item) {
        if (longPressFlag) {
            // get biggest  difference value, it will be calculated proportion
            float yCount = maxPrice - baseData > baseData - minPrice ? maxPrice - baseData :
                    baseData - minPrice;
            float xItem = viewWidth / 2.0f / 120f;
            float yItem = 330 * item / yCount / 2.0f;
            float x = xItem * (touchIndex + 1);
            float y = item * 195 + yItem * (baseData - prices.get(touchIndex));

            //draw the lines
            mPaint.setColor(Color.parseColor("#999999"));
            canvas.drawLine(0, y, viewWidth, y, mPaint);
            canvas.drawLine(x, item * 10, x, item * 380, mPaint);

            //draw the point
            mPaint.setColor(Color.parseColor("#FFC125"));
            mPaint.setStrokeWidth(10f);
            canvas.drawPoint(x, y, mPaint);
        }
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
        longPressFlag = true;
        float itemX = (float) getWidth() / prices.size();
        for (int i = 1; i <= prices.size(); i++) {
            if (itemX * i >= touchX) {
                touchIndex = i - 1;
                break;
            }
        }
        postInvalidate();
        if (touchMoveListener != null && touchIndex >= 0) {
            touchMoveListener.change(times.get(touchIndex), prices.get(touchIndex) + "",
                    formatPrice((prices.get(touchIndex) - baseData) / baseData * 100) + "%",
                    "4613.93万");

        }
    }

    public void setTouchMoveListener(TouchMoveListener touchMoveListener) {
        this.touchMoveListener = touchMoveListener;
    }

    public interface TouchMoveListener {
        void change(String time, String price, String percent, String count);
    }


}
