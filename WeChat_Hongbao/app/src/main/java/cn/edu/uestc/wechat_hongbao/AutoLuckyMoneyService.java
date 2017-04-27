package cn.edu.uestc.wechat_hongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2017/4/16.
 */

public class AutoLuckyMoneyService extends AccessibilityService {
    private final String TAG = getClass().getSimpleName();

    public static int serviceMode;
    public static boolean flagOnGet = true;

    private ExecutorService pool = Executors.newSingleThreadExecutor();
    private TimerUtil instance = TimerUtil.getInstance();

    private List<AccessibilityNodeInfo> nodeList;
    private List<String> nodeHashName;
    private MyHandler handler;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        // 0 是普通模式， 1 是全取模式
        serviceMode = 0;
        nodeList = new ArrayList<>();
        nodeHashName = new ArrayList<>();
        handler = new MyHandler(this);
        Log.i(TAG, "Service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        printEventLog(accessibilityEvent);

        switch (accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:

                if (!flagOnGet) break;
                List<CharSequence> texts = accessibilityEvent.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        if (text.toString().contains("[微信红包]")) {
                            wakeUnlock(this);
                            openAppByNotification(accessibilityEvent);
                            break;
                        }
                    }
                }

            default:
                String className = accessibilityEvent.getClassName().toString();

                // TODO: 未来版本更新后报名可能会改变
                // 在红包详情页之中，退出
                switch (className) {
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                        goBackPage();
                        break;

                    // 在红包已开待拆的界面
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                    case "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f":
                        openPacket();
                        break;

                    // 在红包完全没有拆开的情况
                    case "com.tencent.mm.ui.LauncherUI":
//                    default:
                        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                        getAllPackets(rootNode);
                        break;
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        nodeHashName = null;
        nodeList = null;
        handler = null;
        Log.i(TAG, "Service destroyed");
        super.onDestroy();
    }
    // -------------------------------------------------------------------------- //

    private void wakeUnlock(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    private void goBackPage() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            // 当红包已经拆完，停留在最后一个红包界面
            if (serviceMode == 0 || MyHandler.index == -1) {
                flagOnGet = true;
            }
            else {
                instance.updateStartTime();
                performGlobalAction(GLOBAL_ACTION_BACK);
                nodeInfo.recycle();
            }
        }
    }

    private void openPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            // TODO: 找到buttonNode对应的Button控件
            // TODO: 在将来更新版本后必须修改id
            List<AccessibilityNodeInfo> buttonNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bi3");
            if (!buttonNodes.isEmpty()) {
                AccessibilityNodeInfo buttonNode = buttonNodes.get(0);
                if (isButton(buttonNode)) {
                    buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } else {
                goBackPage();
            }
        }
    }

    private boolean isButton(AccessibilityNodeInfo node) {
        return node.getClassName().toString().equals("android.widget.Button");
    }

    // -------------------------------------------------------------------------- //

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

    // -------------------------------------------------------------------------- //

    private void getAllPackets(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return;

        switch (serviceMode) {
            case 1:
                // 不监听最新的红包，将屏幕上所有红包都过一遍
                if (flagOnGet) {
                    nodeList = null;
                    nodeList = rootNode.findAccessibilityNodeInfosByText("领取红包");

                    // 去掉已经点击过的红包
//                    removeRobbedNodes(nodeList);
                    Log.i(TAG, nodeList.size() + "\t检测到红包数目");
                    MyHandler.index = nodeList.size() - 1;
                    flagOnGet = false;      // 阻塞，不让表更新
                }
                handler.sendEmptyMessage(MyHandler.FLAG_CLICK_PACKETS);

                break;
            default:
                pool.shutdown();
                nodeList = rootNode.findAccessibilityNodeInfosByText("领取红包");
                if (!nodeList.isEmpty()) {
//                    Toast.makeText(this, "We've got " + nodeList.size() + " packets", Toast.LENGTH_SHORT).show();

                    // 始终获取最后一个
                    clickOpenPacket(nodeList.get(nodeList.size() - 1));
                }
                break;
        }
    }

//    private void removeRobbedNodes(List<AccessibilityNodeInfo> list) {
//        if (list.isEmpty()) return;
//        for (int i = list.size() - 1; i >= 0; i--) {
//            if (nodeHashName.contains(getHongbaoHash(list.get(i)))) {
//                list.remove(i);
//                Log.i(TAG, nodeHashName.get(i));
//            }
//        }
//    }

    private void clickOpenPacket(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo clickableNode = findClickable(node);

        pool.execute(instance.timerRunnable);
        if (clickableNode == null) return;
        clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private AccessibilityNodeInfo findClickable(AccessibilityNodeInfo node) {

        AccessibilityNodeInfo nodeOut = node;
        while (nodeOut != null) {
            if (!nodeOut.isClickable()) {
                nodeOut = nodeOut.getParent();
            } else
                break;
        }
        return nodeOut;
    }

    // -------------------------------------------------------------------------- //

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

    /**
     * 将节点对象的id和红包上的内容合并 Content@Id
     * 用于表示一个唯一的红包
     *
     * @param node 任意对象
     * @return 红包标识字符串
     */
    private String getHongbaoHash(AccessibilityNodeInfo node) {
        /* 获取红包上的文本 */
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException npr) {
            return null;
        }

        return content + "@" + getNodeId(node);
    }

    /**
     * 获取节点对象唯一的id，通过正则表达式匹配
     * AccessibilityNodeInfo@后的十六进制数字
     *
     * @param node AccessibilityNodeInfo对象
     * @return id字符串
     */
    private String getNodeId(AccessibilityNodeInfo node) {
        /* 用正则表达式匹配节点Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
        objHashMatcher.find();

        return objHashMatcher.group(0);
    }

//    /**
//     * 要重新给listview赋值的话 需要把listViewNode重新置为null
//     */
//    private void findListViewNode(AccessibilityNodeInfo info) {
//        if (info.getClassName().toString().contains("ListView")) {
//            listViewNode = info;
//        } else {
//            for (int i = 0; i < info.getChildCount(); i++) {
//                if (listViewNode != null)
//                    return;
//                if (info.getChild(i) != null) {
//                    findListViewNode(info.getChild(i));
//                }
//            }
//
//        }
//    }

    private static class MyHandler extends Handler {
        private WeakReference<AutoLuckyMoneyService> reference;
        private static final int FLAG_CLICK_PACKETS = 0x1001;
        private static final int FLAG_RELEASE_MUTEX = 0x1002;

        private static int index;

        private MyHandler(AutoLuckyMoneyService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MyHandler.FLAG_CLICK_PACKETS:
                    Log.i(reference.get().TAG, "FLAG_CLICK_PACKETS running");

                    if (index == -1)
                        sendEmptyMessage(FLAG_RELEASE_MUTEX);
                    else if (index >= 0) {
                        String id = reference.get().getHongbaoHash(reference.get().nodeList.get(index));
                        Log.i(reference.get().TAG, "此节点的时间+标志位\t\t" + System.currentTimeMillis() + "\t" + id);

                        if (id != null) {
//                            for (int j = reference.get().nodeHashName.size(); j > 0; j--) {
//                                Log.i(reference.get().TAG, "Name in ArrayList" + id);
//                                if (!id.equals(reference.get().nodeHashName.get(j))) {
//                                    reference.get().nodeHashName.add(id);
//                                    reference.get().clickOpenPacket(reference.get().nodeList.get(index));
//                                }
//                            }
                            reference.get().nodeHashName.add(id);
                            reference.get().clickOpenPacket(reference.get().nodeList.get(index));
                        }

//                Toast.makeText(reference.get(), "Packet index is " + index, Toast.LENGTH_SHORT).show();
                        index--;
                    }
                    break;
                case FLAG_RELEASE_MUTEX:
                    Log.i(reference.get().TAG, "FLAG_RELEASE_MUTEX running");
                    reference.get().performGlobalAction(GLOBAL_ACTION_HOME);
                    AutoLuckyMoneyService.flagOnGet = true;
                    break;
            }
        }
    }
}
