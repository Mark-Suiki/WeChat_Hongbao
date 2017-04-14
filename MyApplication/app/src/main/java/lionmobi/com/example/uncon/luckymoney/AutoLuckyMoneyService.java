package lionmobi.com.example.uncon.luckymoney;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uncon on 2017/4/14.
 */

public class AutoLuckyMoneyService extends AccessibilityService {
    private final String TAG = getClass().getSimpleName();

    private Map<String, AccessibilityNodeInfo> allReceivedPackets = new HashMap<>();
    private Date currentDate;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 这是在I区打出所有监听事件的类型和信息
        printEventLog(event);
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                openAppByNotification(event);
                break;
            default:
                // 在聊天窗口内
                AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
                if (rootInfo != null) {
                    List<AccessibilityNodeInfo> luckyMoneyNodes = rootInfo.findAccessibilityNodeInfosByText("领取红包");
                    for (AccessibilityNodeInfo luckyMoneyNode : luckyMoneyNodes) {
                        // TODO: 加入map当中，以当前时间戳为key
                        currentDate = new Date();

                        allReceivedPackets.put(String.valueOf(currentDate.getTime()), luckyMoneyNode);
                        if (luckyMoneyNode.isClickable()) {
                            AccessibilityNodeInfo parentNode = luckyMoneyNode.getParent();
                            parentNode = findClickable(parentNode);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void openAppByNotification(AccessibilityEvent event) {
        if (event.getParcelableData() != null  && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            try {
                PendingIntent pendingIntent = notification.contentIntent;
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    private AccessibilityNodeInfo findClickable(AccessibilityNodeInfo node) {
        if (!node.isClickable()) node = node.getParent();
        if (node != null) return node;
        else return null;
    }

    private void printEventLog(AccessibilityEvent event) {
        Log.i(TAG, "-------------------------------------------------------------");
        int eventType = event.getEventType(); //事件类型
        Log.i(TAG, "PackageName:" + event.getPackageName() + ""); // 响应事件的包名
        Log.i(TAG, "Source Class:" + event.getClassName() + ""); // 事件源的类名
        Log.i(TAG, "Description:" + event.getContentDescription()+ ""); // 事件源描述
        Log.i(TAG, "Event Type(int):" + eventType + "");

        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                Log.i(TAG, "event type:TYPE_NOTIFICATION_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗体状态改变
                Log.i(TAG, "event type:TYPE_WINDOW_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED://View获取到焦点
                Log.i(TAG, "event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                Log.i(TAG, "event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                Log.i(TAG, "event type:TYPE_GESTURE_DETECTION_END");
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.i(TAG, "event type:TYPE_WINDOW_CONTENT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i(TAG, "event type:TYPE_VIEW_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.i(TAG, "event type:TYPE_VIEW_TEXT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.i(TAG, "event type:TYPE_VIEW_SCROLLED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                Log.i(TAG, "event type:TYPE_VIEW_TEXT_SELECTION_CHANGED");
                break;
            default:
                Log.i(TAG, "no listen event");
        }

        for (CharSequence txt : event.getText()) {
            Log.i(TAG, "text:" + txt);
        }

        Log.i(TAG, "-------------------------------------------------------------");
    }
}
