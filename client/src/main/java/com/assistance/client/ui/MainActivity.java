package com.assistance.client.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.assistance.client.R;
import com.assistance.client.data.bean.DeviceHost;
import com.assistance.client.utils.NetworkSniffTask;
import com.assistance.client.utils.SPUtils;

import java.util.List;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/1
 */
public class MainActivity extends AppCompatActivity{
    private final String TAG = MainActivity.class.getSimpleName();

    private ImageView ivScreen;
    private EditText et_input;
    private Button btn_ips;

    private  String[] ips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) { // 如果不是栈内第一个activity则直接关闭
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

        et_input = findViewById(R.id.et_input);
        btn_ips = findViewById(R.id.btn_ips);
        String saveIP = SPUtils.getString(this,"serverIP","192.168.8.106");
        et_input.setText(saveIP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ips == null || ips.length == 0){
            btn_ips.setVisibility(View.GONE);
        }else {
            btn_ips.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_connect:
                String ip = et_input.getText().toString().trim();
                if(ip == null || ip.length() < 11){
                    Toast.makeText(this,"请输入正确的IP地址",Toast.LENGTH_LONG).show();
                    return;
                }
                SPUtils.putString(this,"serverIP",ip);
                Intent intent = new Intent(this, RemoteAssistanceActivity.class);
                intent.putExtra("ipAddress",ip);
                startActivity(intent);
                break;
            case R.id.iv_refresh:
                //查找局域网内的所有设备ip和设备名称
                new NetworkSniffTask(this, new NetworkSniffTask.HostsIPCallback() {
                    @Override
                    public void hostsIP(List<DeviceHost> deviceHosts) {
                        if(deviceHosts == null || deviceHosts.size() == 0){
                            btn_ips.setVisibility(View.GONE);
                        }else {
                            btn_ips.setVisibility(View.VISIBLE);
                            ips = new String[deviceHosts.size()];
                            for(int i = 0; i<deviceHosts.size(); i++){
                                ips[i] = deviceHosts.get(i).getIp();
                            }
                        }
                    }
                }).execute();
                break;
            case R.id.btn_ips:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setItems(ips, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        et_input.setText(ips[which]);
                    }
                }).show();
                break;
        }
    }
}
