package lionmobi.com.example.uncon.wechat_hongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uncon on 2017/4/13.
 */

public class AutoLuckyMoneyService extends AccessibilityService {
    private final String TAG = this.getClass().getSimpleName();
    // 最大尝试次数
    private final int MAXTTL = 10;

    //------------------变量声明------------------//
    // 成功收到的红包列表
    private List<String> fetchedIdentifiers = new ArrayList<>();
    // 待拆的红包列表
    private List<AccessibilityNodeInfo> packetsToOpen = new ArrayList<>();
    // 当前尝试次数
    private int ttl = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 阻塞标志为真，不可执行操作
        if (RobStage.mutex) return;

        RobStage.mutex = true;
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                try {
                    handleWindowChanged(event.getSource());
                } finally {
                    // 释放阻塞锁
                    RobStage.mutex = false;
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                try {
                    handleNotification(event);
                } finally {
                    // 释放阻塞锁
                    RobStage.mutex = false;
                }
                break;
        }
    }

    /**
     * 在这个函数中，我们分别判断4种状态
     * 1.窗口中出现红包，应该加入待抢队列       [此时什么都不能做] FETCHING_ALL
     * 2.窗口下的红包已全部加入队列              [可以开始抢] FETCHED_ALL
     * 3.模拟打开一个红包，成功则开下一个       [失败重试10次] OPENING_ENVELOPES
     * 4.已开启一个红包，根据红包过期/红包已空/红包可拆分为三种状态 [此时别的操作不可执行] OPENED_ENVELOPES
     *
     * @param source
     */
    private void handleWindowChanged(AccessibilityNodeInfo source) {
        switch (RobStage.getRobStageInstance().getStage()) {
            case RobStage.STAGE_FETCHED_ALL:
                break;
            case RobStage.STAGE_FETCHING_ALL:
                break;
            case RobStage.STAGE_OPENED_ENVELOPES:
                // Version6.5.7不再显示红包详情
                List<AccessibilityNodeInfo> successNodeList = source.findAccessibilityNodeInfosByText("给你发了一个红包");

                break;
            case RobStage.STAGE_OPENING_ENVELOPES:
                // 失败，没有超过MAXTTL;注意，拆红包在判断中执行了
                if (openLuckyPacket() && ttl < MAXTTL) return;

                // 尝试次数清零，并且从开红包状态中退出，返回聊天窗口
                // 如果红包列表已经空了，则重新获取
                ttl = 0;
                RobStage.getRobStageInstance().setStage(RobStage.STAGE_FETCHED_ALL);
                goBackPage();
                if (packetsToOpen.size() == 0) handleWindowChanged(source);
                break;
        }
    }

    private void handleNotification(AccessibilityEvent _Event) {
        // 通知栏列表中的各个项，找出微信红包的提示
        List<CharSequence> texts = _Event.getText();
        Log.d(TAG, texts.toString());
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                if (content.contains("[微信红包]")) {
                    Log.d(TAG, "find a pocket");
                    // 获取的序列化的对象存在，且是通知的实例
                    if (_Event.getParcelableData() != null && _Event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) _Event.getParcelableData();
                        Log.d(TAG, "notification intent done");
                        PendingIntent intent = notification.contentIntent;
                        // 尝试触发通知栏中的事件，通过通知项点开微信窗口
                        try {
                            intent.send();
                            Log.d(TAG, "intent has been sent");
                        } catch (PendingIntent.CanceledException ce) {
                            ce.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private boolean openLuckyPacket() {
        if (ttl > MAXTTL) return false;
//        else if (state)
        else return true;
    }

    /**
     * 关闭红包详情页
     * 自动返回到聊天窗口
     */
    private void goBackPage() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @Override
    public void onInterrupt() {

    }
}
