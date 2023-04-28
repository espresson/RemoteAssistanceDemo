package com.example.accessibilitytest.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accessibilitytest.MyApp;
import com.example.accessibilitytest.R;
import com.example.accessibilitytest.data.event.NettyInitEvent;
import com.example.accessibilitytest.service.websocket.SocketService;
import com.example.accessibilitytest.utils.AccessibilityUtil;
import com.example.accessibilitytest.utils.ScreenUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private TextView tvMsg,tv_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        tvMsg = findViewById(R.id.tv_msg);
        tv_status = findViewById(R.id.tv_status);
        tv_status.setText(SocketService.getInstance().getSocketStatus());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!AccessibilityUtil.INSTANCE.isAccessibilitySettingsOn(this)){
            showConfirmDialog("需要开启无障碍权限。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String data = String.format("%s,%s", ScreenUtil.getScreenWidth(MyApp.getApp()), ScreenUtil.getScreenHeight(MyApp.getApp()));
        Log.d(TAG,data);
        data = String.format("%s,%s", ScreenUtil.getScreenWidth(MainActivity.this), ScreenUtil.getScreenHeight(MainActivity.this));
        Log.d(TAG,"横竖屏切换" + data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(NettyInitEvent event) {
        tv_status.setText(event.getMsg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(String message) {
        tvMsg.setText("收到消息：\n" + message);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn1:
                //sendMsg("测试消息");
                break;
            case R.id.btn2:
                break;
        }
    }

    //    private void sendMsg(String msg){
//        LogUtils.e(TAG,msg);
//        if (!NettyTcpServer.getInstance().isServerStart()) {
//            Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
//        } else {
//            NettyTcpServer.getInstance().sendMsgToServer(msg, channelFuture -> runOnUiThread(() -> {
//                if (channelFuture.isSuccess()) {
//                    Toast.makeText(getApplicationContext(), "NettyServer发送消息成功", LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "NettyServer发送消息失败", LENGTH_SHORT).show();
//                }
//            }));
//        }
//    }

    private void showConfirmDialog(String msg, DialogInterface.OnClickListener listener){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("确定",listener)
                .setNegativeButton("取消",null)
                .show();
    }

}