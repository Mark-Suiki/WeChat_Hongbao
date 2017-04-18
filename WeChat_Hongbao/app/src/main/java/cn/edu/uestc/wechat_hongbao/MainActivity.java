package cn.edu.uestc.wechat_hongbao;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnToSettingsPage;
    private Button btnModifyMode;

    String currentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentText = "普通模式";

        btnToSettingsPage = (Button) findViewById(R.id.start_service_page);
        btnToSettingsPage.setOnClickListener(this);
        btnModifyMode = (Button) findViewById(R.id.mode_change);
        btnModifyMode.setOnClickListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        btnToSettingsPage.setText("前往设置界面");
        btnModifyMode.setText(currentText);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_service_page:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
            case R.id.mode_change:
                switch (btnModifyMode.getText().toString()) {
                    case "全取模式":
                        currentText = "普通模式";
                        btnModifyMode.setText("普通模式");
                        AutoLuckyMoneyService.serviceMode = 0;
                        Log.i(this.getClass().getSimpleName(), AutoLuckyMoneyService.serviceMode+"");
                        break;
                    default:
                        currentText = "全取模式";
                        btnModifyMode.setText("全取模式");
                        AutoLuckyMoneyService.serviceMode = 1;
                        Log.i(this.getClass().getSimpleName(), AutoLuckyMoneyService.serviceMode+"");
                        break;
                }
                break;
            default:
                break;
        }
    }
}
