package com.mythread.diablo.threaddelyintime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mythread.diablo.threaddelyintime.http.RequestUtil;

public class MainActivity extends Activity {

    private String url = "http://mp.weixin.qq.com/s?__biz=MzA3NTYzODYzMg==&mid=2653577496&idx=2&sn=0744a914379dc21d52824bce980d04c3&scene=4#wechat_redirect";
    long endTime;
    long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = (Button) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 doDelay();
            }
        });

        Button startOnThread = (Button) findViewById(R.id.btn_OnThread);
        startOnThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHttpTime(1000);
            }
        });
    }

    private void doDelay(){
        DelayInThreadOnUI delayInTimeThread = new DelayInThreadOnUI(1000);
        delayInTimeThread.setDelayThreadInterface(new DelayInThreadOnUI.DelayThreadInterface() {
            @Override
            public void initOnUI() {
                for (int i = 0;i<1000000;i++){
                    new DelayInThreadOnUI(0);
                }
            }

            @Override
            public void doNext() {
                Intent intent = new Intent(MainActivity.this,SecondAct.class);
                startActivity(intent);
            }
        });
        delayInTimeThread.start();
    }
    private void getHttpTime(final long time){
        startTime = System.currentTimeMillis();
        RequestUtil requestUtil = new RequestUtil(getBaseContext()) {

            @Override
            public void OnHttpsGetAndPostSucceed(final String result) {
                endTime = System.currentTimeMillis();
                long cost = endTime-startTime;
                System.out.println("cost Time ==="+cost);
                final long tmp = time - cost;
                System.out.println("sleep Time ==="+tmp);
                if (tmp > 0){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(tmp);
                                //// TODO:  do next 
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else {
                    //// TODO: do next
                }
                
            }

            @Override
            public void OnHttpsGetAndPostError(String errorInfo) {
            }
        };
        requestUtil.doHttpPost(url, null, null);
    }

}
