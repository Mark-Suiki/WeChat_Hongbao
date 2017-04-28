package cn.edu.uestc.wechat_hongbao;


import android.accessibilityservice.AccessibilityService;
import android.util.Log;

/**
 * Created by uncon on 2017/4/24.
 * 针对列表全取的模式用
 * 我们举例说明，在获取聊天窗口N个红包以后
 * 我们需要从第N个红包往上依次点开
 * 1.那么注意，在点击一个红包后会触发ListView往上推一点
 * 2.而且在群中会有其他人发消息，将红包推出聊天窗口
 *
 * 所以在列表全取模式下，不可避免的会遇到最顶上的0号红包被顶出的情况
 * 1.此时，画面会留在聊天窗口，不会接收notification和WINDOW_CHANGED的Event
 *
 * 这是因为此时的mutex不会自己解开，插件就会失效，因此采取如下措施
 * 1.获取到n个红包node以后，instance实例获取当时的时间tStart
 * 2.到达HongbaoDetail的页面的时候，子线程中获取结束时间tStop，与tStart相减
 * 3.若超过TTD，就马上释放锁
 */

public class TimerUtil {
    private static long TTD = 5000;

    private long tStart = 0;
    private long tStop = 0;

    private TimerUtil() {
    }

    public static TimerUtil getInstance() {
        TimerUtil newInstance = new TimerUtil();
        newInstance.updateStartTime();
        newInstance.updateStopTime();
        return newInstance;
    }

    public boolean isInitialed() {
        return tStart != tStop;
    }

    public void updateStartTime() {
        tStart = System.currentTimeMillis();
    }

    public void updateStopTime() {
        tStop = System.currentTimeMillis();
    }

    public boolean isTimeOut() {
        return tStop - tStart > TTD;
    }

    public long getTStart() {
        return this.tStart;
    }

    public long getTStop() {
        return this.tStop;
    }
}
