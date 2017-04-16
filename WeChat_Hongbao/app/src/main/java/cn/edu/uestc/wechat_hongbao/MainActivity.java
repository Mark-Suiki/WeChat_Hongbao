package cn.edu.uestc.wechat_hongbao;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnToSettingsPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToSettingsPage = (Button) findViewById(R.id.start_service_page);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        btnToSettingsPage.setText("前往设置界面");
        btnToSettingsPage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_service_page:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
