package cn.edu.uestc.wechat_hongbao;


/**
 * Created by uncon on 2017/4/24.
 */

public class TimeUtil {
    public static long TTD = 5000;

    private long tStart;
    private long tStop;

    private TimeUtil() {}

    public TimeUtil getInstance() {
        TimeUtil newInstance = new TimeUtil();
        newInstance.tStart = 0;
        newInstance.tStop = 0;
        return newInstance;
    }

    public void startTimer() {
        tStart = System.currentTimeMillis();
    }

    public void stopTimer() {
        tStop = System.currentTimeMillis();
    }

    public long getTimeLong() {
        return (tStop - tStart);
    }

    public boolean isTimeOut() {
        return tStart - tStop > TTD;
    }

    public long getTStart() {
        return this.tStart;
    }

    public long getTStop() {
        return this.tStop;
    }
}
