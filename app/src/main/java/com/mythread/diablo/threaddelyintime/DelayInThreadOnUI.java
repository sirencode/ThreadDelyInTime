package com.mythread.diablo.threaddelyintime;

/**
 * Created by Diablo on 16/7/22.
 */
public class DelayInThreadOnUI extends Thread{

    private long delayTime;
    private DelayThreadInterface delayThreadInterface;

    public DelayInThreadOnUI(long delayTime){
        this.delayTime = delayTime;
    }

    public void setDelayThreadInterface(DelayThreadInterface delayThreadInterface){
        this.delayThreadInterface = delayThreadInterface;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        delayThreadInterface.initOnUI();
        long endTime = System.currentTimeMillis();
        long cost = endTime-startTime;
        long realDelayTime = delayTime - cost;
        System.out.println("init cost time:"+cost+",sleep time:"+realDelayTime);
        if (realDelayTime > 0){
            try {
                this.sleep(realDelayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        delayThreadInterface.doNext();
    }

    public interface DelayThreadInterface{
        public void initOnUI();
        public void doNext();
    }
}
