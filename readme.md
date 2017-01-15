#股票图,K线图,蜡烛图,高仿雪球股票,教你一步步实现股票图

点击播放效果图

[<img src="/img/_004.jpg" height="40%" width="40%" />](http://v.youku.com/v_show/id_XMjQxMzAwMjkzMg==.html?spm=a2h0k.8191407.0.0.7SyXhZ&from=s1.8-1-1.2)

讲K线图之前,先来一个引言.

前两天听了[朱凯](https://www.zhihu.com/people/rengwuxian/answers)大神的知乎live,其中说到一点,作为Android开发者需要立即提升的三项技能:
分别是:<b>UI,网络,线程,而UI又分:布局,绘制,以及触摸事件的反馈</b>.凯哥强调:其实UI确实只有这么几个最主要的东西,但是很多人却没能搞明白.

其实UI的这三个方面,说容易也容易,说难也难.有同学当时也问到:怎么样才能算是掌握了这三个方面呢?
凯哥当时的回答是:给出一个不算是为难的界面,能布局出来,绘制好,并掌握相应的触摸反馈,就算是基本上掌握UI了.

实际上UI容易也是在这里,初步上手,对大部分比较认真的同学来说都是可以做到的,但是深入了解的,却比较少.例如,触摸Touch事件是如何分发的,
它的原理是什么,这就需要大家更加深入的学习了.

前面说到的UI的三个方面,其实在股票图里面都有比较好的体现,下面就这三个方法,讲解一下我实现股票图的思路

##股票图基本知识
了解股票图如何绘制,首先应该了解股票图的业务逻辑是怎样的,我这篇文章是仿雪球股票写的,建议大家下载雪球股票软件体验一下.在写这个股票图之前,我对股票是一无所知(原谅我穷买不起),
所以花了一点时间了解了一下股票图的基本信息,如果知道股票图是如何解读的,可以跳过这节.

股票图的种类特别多,不同的种类的股票图也不一样,例如股票有港股,美股,上证,深圳,创业板等等.然后上证又有:分时,日K,月K等等.
复杂程度完全可以直接绕晕人,没错,我就是看不懂所以不敢买.

股票图的种类之多,所以我也没有一一编写,这里主要是仿照了雪球股票之上证指数的:分时图,以及日K图.
也就是股票的两大图种:<b>分时图,以及蜡烛图.</b>

####分时图
<img src="/img/_001.jpg" height="40%" width="40%" />

分时图有股票当天的涨跌情况,以及一些最高点,最低点,比分比,长按分时图,可以定位当时手指按下的时间所对应的股票点是多少点,并且可以左右滑动


股票的开盘时间是早上09:30~11:30,下午是13:00~15:00.


####蜡烛图
<img src="/img/_002.jpg" height="40%" width="40%" />

蜡烛图和分时图类似,先除去那三条折线.分时图是把涨跌情况用折线表示,而蜡烛图是用一个矩形加一条竖线表示,日K图每个月一个间隔.

其中竖线的最高点代表当日最高涨到了多少点,最低表示最低跌到了多少点.

矩形的顶端,表示当日开盘是多少点,底端,表示收盘是多少点.

颜色红,代表收盘后,相对于昨天,涨了,颜色绿,则表示跌了.


<b>下面就分时图,蜡烛图,分别讲解其布局,绘制,已经触摸反馈</b>


##分时图
###布局
布局无论是xml引用layout编写,亦或是java直接new出来,或者是使用canvas直接绘制,最重要的不是应该使用
RelativeLayout还是LinearLayout,而是应该剖析它的层次与结构.

####层次
根据上面的基本介绍,分时图的可以分为以下几个层次:

* 第1层:横线,竖线,以及底部时间(底部时间没有其他的元素,可以处于任意一层)

* 第2层:折线,以及阴影部分

* 第3层:文字,包括最高点,最低点,百分比

####结构
分时图的结构相对简单,在基本介绍上已经说明其基本信息.

股票的开盘时间是早上09:30~11:30,下午是13:00~15:00,所以其分上午,下午两部分.
中间的虚线是昨天收盘的股票点,以此为基准线,计算折线图的位置.

###绘制
布局分析好之后,就开始绘制这些基本信息.普通View的绘制,是写好xml或者java代码,然后交给每个view自己绘制,这里我们自己控制其绘制.

绘制的步骤,其实就是布局中所说的层次,绘制的规则,则是布局中的结构.换句话说,这个结构,规则,就是数学中的公式,步骤就是我们解题的思路.

####详细绘制步骤
#####开始.
自定义一个View,覆写其四个构造方法(注意最好四个构造方法都覆写,这样就可以通过多种途径新建这个View),覆写onDraw()方法,画图的时候就是在这个方法进行绘制的.

```java
public class KLineView extends View {
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

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
}
```

  一般还需要初始化一些信息.为了让自己能看到每一步的绘制效果,我写了一个添加测试数据方法,初始化的时候执行该方法即可.

```java
/**
 * canvas paint
 */
private Paint mPaint;

private void init() {
    mPaint = new Paint();
    createTestData();
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
```





#####绘制线.
使用MarkMan量取,分时图在720*1280分辨率下,高度是是410,则我们可以把其高度分成410份.
   它一共有5条横线,从上到下,每条线距离顶部的距离依次为:10,30,190,360,380.其中第3条为虚线.还有一条竖线,水平居中.

   依次画出每一条线.

```java
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
```

#####绘制时间.
时间的最简单,三个时间是固定的,位置也是固定的.

   需要注意的是,绘制文字的x,y坐标,x=文字的左边,y=文字的baseline,文字的baseline默认等于-mPaint.getFontMetrics().top

   想了解更多关于文字绘制的细节,请移步到我这篇文章[StyleTextView](https://github.com/siyehua/StyleTextView)

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int viewHeight = getHeight();
    int viewWidth = getWidth();
    float item = viewHeight / 410f;

    /**
     * draw time
     */
    drawTimes(canvas, viewWidth, item);
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
```

#####绘制折线,以及折线的阴影面积.
转到canvas上来说,其实就是绘制路径,在前面绘制横线的时候,绘制虚线其实就是绘制路径.
   注意绘制阴影的时候,要把画笔设置为实心的,这样才会有阴影的效果,同时路径path要多连接几个点,包括右下角,左下角,表明折线下方,第五条横线上方,就是阴影部分.

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int viewHeight = getHeight();
    int viewWidth = getWidth();
    float item = viewHeight / 410f;

    /**
     * draw broken line and shadow graph
     */
    drawBrokenLine(canvas, viewWidth, item, "#504F76DB", Paint.Style.FILL);
    drawBrokenLine(canvas, viewWidth, item, "#4F76DB", Paint.Style.STROKE);
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
```


#####绘制最高点,最低点,以及百分比.
有了绘制时间的经验,我们知道x,y分别代表的是文字的左下角,baseline,直接绘制即可.

   绘制最低点的时候需要注意,最低点距离第四条横线的距离,应该与第二条线距离最高点的距离一致.放大雪球股票的图,发现其K线图,以及后面要绘制的蜡烛图,这
   两个距离都不相等,虽然无伤大雅.但是如果我们能做到,那就更好不过.

   凯哥live中说到,<b>设计或者产品出来一个交互,一个需求,你做不到,没什么关系,因为别人也做不到.但是假设别人做不到,但是你做到了,那么很明显,你就强于别人</b>

   在前面绘制文字的时候提到过我的这篇文章[StyleTextView](https://github.com/siyehua/StyleTextView),发布到郭霖的公众号后,有部分同学说,为什么这么麻烦搞这么多,感觉不需要这么复杂.
   实际上如果只是单纯做一个需求,确实不需要多复杂的代码,直接绘制是最简单的,但是绘制也涉及到留白的问题,在一个要求不是特别精确的View,一两个像素的差距,确实可有可无,甚至有同学直接根据
   实际运行出来的效果图,调整空白大小.

   但是你为什么调整空白大小,为什么要这么调,调了以后其他的机型适配吗?如果在一个很大的View上,字体大小很大,此时能保证也能满足正常视觉吗?

   故有时候追求一些细节,对自己的代码,以及技术,都是一种负责任的态度.

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int viewHeight = getHeight();
    int viewWidth = getWidth();
    float item = viewHeight / 410f;

    /**
     * draw max, min price and percent
     */
    drawPriceAndPercent(canvas, viewWidth, item);
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
```




至此,绘制基本已经结束了,直接运行,就能看到一个基本K线图,但是还差K线图的交互,也就是长按K线图的交互,这其实就是一个触摸反馈的过程

<img src="/img/_003.jpg" height="50%" width="50%" />


###触摸
网上有很多的触摸文章教程,我这里就不展开篇幅讲解了,这里直接使用手势识别类:GestureDetector

但是实际使用发现,假设手指长按了,就不能再接收到
手指的移动事件,看GestureDetector发现,如果它判断是长按就直接break了,同时发现它也没有发送手指离开屏幕的事件,这都不是我想要的,所以我就把它源码直接复制出来了,删掉了一些用不到的事件,并
添加了手指离开事件.第二个手指按下,离开事件.

```java
boolean onDown2(MotionEvent e);
boolean onUp2(MotionEvent e);
boolean onUp(MotionEvent e);
```
更多详情,请点击这个改造过的[GestureDetector](/app/src/main/java/com/siyehua/klinegraph/GestureDetector.java)


添加手势触摸监听,首先在init初始化GestureDetector,并在onTouch中拦截触摸事件

```java
//初始化
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
}


//拦截触摸事件
@Override
   public boolean onTouchEvent(MotionEvent event) {
       return detector.onTouchEvent(event);
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
    //根据触摸的坐标,计算当前被触摸的indext
    float itemX = (float) getWidth() / prices.size();
    for (int i = 1; i <= prices.size(); i++) {
        if (itemX * i >= touchX) {
            touchIndex = i - 1;
            break;
        }
    }
    //绘制触摸线
    postInvalidate();
    //交给外部的触摸回调监听
    if (touchMoveListener != null && touchIndex >= 0) {
        touchMoveListener.change(times.get(touchIndex), prices.get(touchIndex) + "",
                formatPrice((prices.get(touchIndex) - baseData) / baseData * 100) + "%",
                "4613.93万");
    }
}


//onDraw处理触摸事件
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int viewHeight = getHeight();
    int viewWidth = getWidth();
    float item = viewHeight / 410f;

    /**
     * draw touch lines and point
     */
    drawTouchLines(canvas, viewWidth, item);
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
```


###分时图总结
至此,分时图的布局,绘制,触摸反馈都已经完整,如果再加上设置数据的方法,就可以作为一个基本的分时图使用了.
详细代码请点击:[KLineView](/app/src/main/java/com/siyehua/klinegraph/KLineView.java)
