package com.siyehua.klinegraph;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KLineView kLineView = (KLineView) findViewById(R.id.v_content);
        CandleView candleView = (CandleView) findViewById(R.id.c_content);
        final TextView timeView = (TextView) findViewById(R.id.tv_time);
        final TextView priceView = (TextView) findViewById(R.id.tv_price);
        final TextView percentView = (TextView) findViewById(R.id.tv_percent);

        kLineView.setTouchMoveListener(new KLineView.TouchMoveListener() {
            @Override
            public void change(String time, String price, String percent, String count) {
                timeView.setText(time);
                priceView.setText(price);
                percentView.setText(percent);

                int color = percent.contains("-") ? Color.parseColor("#008000") : Color.RED;
                priceView.setTextColor(color);
                percentView.setTextColor(color);
            }
        });
        candleView.setTouchMoveListener(new KLineView.TouchMoveListener() {
            @Override
            public void change(String time, String price, String percent, String count) {
                timeView.setText(time);
                priceView.setText(price);
                percentView.setText(percent);
                int color = percent.contains("-") ? Color.parseColor("#008000") : Color.RED;
                priceView.setTextColor(color);
                percentView.setTextColor(color);
            }
        });
    }

}
