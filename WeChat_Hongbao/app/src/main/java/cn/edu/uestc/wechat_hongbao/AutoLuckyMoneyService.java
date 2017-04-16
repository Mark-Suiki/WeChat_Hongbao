package cn.edu.uestc.wechat_hongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * Created by admin on 2017/4/16.
 */

public class AutoLuckyMoneyService extends AccessibilityService {
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        printEventLog(accessibilityEvent);

        switch (accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = accessibilityEvent.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        if (text.toString().contains("[微信红包]")) {
                            openAppByNotification(accessibilityEvent);
                            break;
                        }
                    }
                }
                break;
            default:
                String className = accessibilityEvent.getClassName().toString();

                // 在红包详情页之中，退出
                if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"))
                    goBackPage();

                // 在红包未拆待拆的界面
                else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f"))
                    openPacket(accessibilityEvent);

                // 在红包完全没有拆开的情况
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void getAllPackets() {

    }

    private void openPacket(AccessibilityEvent _Event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText("领取红包");
            if (!nodes.isEmpty()) {
                for (AccessibilityNodeInfo infoNode : nodes) {
                    AccessibilityNodeInfo clickableNode = findClickable(infoNode);
                    clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private void openAppByNotification(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            try {
                PendingIntent pendingIntent = notification.contentIntent;
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private AccessibilityNodeInfo findClickable(@NonNull AccessibilityNodeInfo node) {
        while (node != null) {
            if (!node.isClickable())
                node = node.getParent();
            else
                break;
        }
        return node;
    }

    private void goBackPage() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            nodeInfo.recycle();
        }

    }

    private void printEventLog(AccessibilityEvent event) {
        Log.i(TAG, "-------------------------------------------------------------");
        int eventType = event.getEventType(); //事件类型
        Log.i(TAG, "PackageName:" + event.getPackageName() + ""); // 响应事件的包名
        Log.i(TAG, "Source Class:" + event.getClassName() + ""); // 事件源的类名
        Log.i(TAG, "Description:" + event.getContentDescription() + ""); // 事件源描述
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
