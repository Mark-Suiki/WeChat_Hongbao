package lionmobi.com.example.uncon.wechat_hongbao;

/**
 * Created by uncon on 2017/4/13.
 */

public class RobStage {

    //------------------常量表示状态------------------//
//    public static final int STAGE_FIND_NOTIFICATION = 1001;

    // 当前正在搜集红包并加入队列，此时不应该拆或关闭
    public static final int STAGE_FETCHING_ALL = 1002;
    // 加入队列的一个阶段结束，可以开拆
    public static final int STAGE_FETCHED_ALL = 1003;
    // 正在拆红包，不应有其他操作
    public static final int STAGE_OPENING_ENVELOPES = 1004;
    // 拆好了一个红包，进入红包详情
    public static final int STAGE_OPENED_ENVELOPES = 1005;

    public static boolean mutex = false;



    //------------------使用单例模式------------------//
    private static RobStage robStageInstance;
    private int currentStage = STAGE_FETCHING_ALL;

    //------------------空构造器------------------//
    private RobStage() {
    }



    /**
     * 获取单例实例
     */
    public static RobStage getRobStageInstance() {
        if (robStageInstance == null) {
            robStageInstance = new RobStage();
        }
        return robStageInstance;
    }

    /**
     * getter
     */
    public int getStage() {
        return currentStage;
    }

    public void setStage(int _Stage) {
        currentStage = _Stage;
    }
}
