package lionmobi.com.example.uncon.wechat_hongbao;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by uncon on 2017/4/13.
 */

public class AutoLuckyMoneyService extends AccessibilityService {

    //------------------变量声明------------------//
    private AccessibilityNodeInfo node;
    private List<AccessibilityNodeInfo> nodestoFetch;



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        node = event.getSource();

        switch (RobStage.getRobStageInstance().getStage()) {

            case RobStage.STAGE_FETCHING_ALL:
                break;
            case RobStage.STAGE_FETCHED_ALL:
                if (nodestoFetch.size() > 0) {
                    AccessibilityNodeInfo nodeInfo = nodestoFetch.remove(nodestoFetch.size() - 1);
                    if (nodeInfo.getParent() != null) {
                        // ......
                        RobStage.getRobStageInstance().setStage(RobStage.STAGE_OPENING_ENVELOPES);
                        nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    return;
                }
                RobStage.getRobStageInstance().setStage(RobStage.STAGE_FETCHING_ALL);
//                fetchHongbao(nodeInfo);
                RobStage.getRobStageInstance().setStage(RobStage.STAGE_FETCHED_ALL);
                break;
            case RobStage.STAGE_OPENING_ENVELOPES:
                // .....
                RobStage.getRobStageInstance().setStage(RobStage.STAGE_FETCHED_ALL);
                performGlobalAction(GLOBAL_ACTION_BACK);
                break;
            case RobStage.STAGE_OPENED_ENVELOPES:
                RobStage.getRobStageInstance().setStage(RobStage.STAGE_FETCHED_ALL);
                performGlobalAction(GLOBAL_ACTION_BACK);
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }



    /**
     * 如果通知栏中有[微信红包]，取之
     * 具体实现打开微信和点击红包的动作
     * @param event
     */
    private void handleNotification(AccessibilityEvent event) {

    }

    /**
     * 自动抢红包，并开之
     */
    private void robLuckyMoney() {

    }

    /**
     * 关闭红包详情，回到聊天窗口
     */
    private void backToChatWindow() {

    }

    /**
     * 寻找界面中所有红包
     * TODO:防止重复开一个红包。
     */
    private void findAllEnvelope() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }
}
