package com.assistance.client.ui;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.assistance.client.R;
import com.assistance.client.data.bean.OperationModel;
import com.assistance.client.data.bean.RemoteAssistanceBean;
import com.assistance.client.live.Decode264;
import com.assistance.client.utils.ImageUtil;
import com.assistance.client.utils.LogUtils;
import com.assistance.client.utils.ScreenUtil;
import com.assistance.client.websocket.OnSocketMessage;
import com.assistance.client.websocket.SocketServer;

import java.util.Date;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/1
 */
public class RemoteAssistanceActivity extends AppCompatActivity {
    private final String TAG = RemoteAssistanceActivity.class.getSimpleName();

    private ImageView ivScreen;
    private View lay_function;
    private SurfaceView mSurfaceView;
    private Surface mSurface;

    private Decode264 mDecode264;

    private RemoteAssistanceBean remoteAssistanceBean = new RemoteAssistanceBean();
    private OperationModel operationModel;
    private long dateStart, dateEnd, during;
    private int screenWidth,screenHeight;
    private SocketServer socketServer;
    private boolean isCreate = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_assistance);

        ivScreen = findViewById(R.id.image);
        lay_function = findViewById(R.id.lay_function);
        ivScreen.setOnTouchListener(onTouchListener);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mSurface = holder.getSurface();
                mDecode264 = new Decode264(mSurface);
//                mDecode265 = new Decode265(mSurface);
                initSocket();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
        new Handler().postDelayed(() -> resetSurfaceViewSize(),500);

    }

    private void initSocket(){
        String host = getIntent().getStringExtra("ipAddress");
        socketServer = new SocketServer(String.format("ws://%s:11006/?type=Accessibility",host));
        socketServer.setOnSocketMessage(onSocketMessage);
        socketServer.initAndStart();
    }

    private void resetSurfaceViewSize(){
        if(screenWidth == 0) return;
        int width = ScreenUtil.getScreenWidth(this);
        int height = ScreenUtil.getScreenHeight(this);
        int sw = width;
        int sh = width * screenHeight / screenWidth;
        if(sh > height){
            sh = height;
            sw = height * screenWidth / screenHeight;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        layoutParams.width = sw;
        layoutParams.height = sh;
        mSurfaceView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        socketServer.closeConnect();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetSurfaceViewSize();
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            //如果是横屏了，在这里设置横屏的UI
        }else{
            //否则，在这里设置竖屏的UI
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.iv_open:
                if(lay_function.getVisibility() == View.VISIBLE){
                    lay_function.setVisibility(View.GONE);
                }else {
                    lay_function.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_connect:
                finish();
                break;
            case R.id.iv_switch_screen:
                switchScreen();
                break;
            case R.id.btn_back:
                RemoteAssistanceBean bean = new RemoteAssistanceBean(2, "BACK");
                sendMessage("RemoteAssistance"+JSON.toJSONString(bean));
                break;
            case R.id.btn_home:
                bean = new RemoteAssistanceBean(3, "HOME");
                sendMessage("RemoteAssistance"+JSON.toJSONString(bean));
                break;
            case R.id.btn_reboot:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("确定重启吗？")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RemoteAssistanceBean bean = new RemoteAssistanceBean(7, "reboot");
                                sendMessage("RemoteAssistance"+JSON.toJSONString(bean));
                            }
                        }).show();
                break;
            case R.id.btn1:
                Toast.makeText(this,"发送测试数据- 我是客户端",Toast.LENGTH_LONG).show();
                sendMessage("我是客户端！");
                break;
        }
    }

    /**
     * 横竖屏切换
     */
    private void switchScreen(){
        if(ScreenUtil.isLandscape(this)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void sendMessage(String msg){
        if (!socketServer.isConnecting()) {
            Toast.makeText(getApplicationContext(), "未连接,请先连接", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(msg.trim())) {
                return;
            }
            socketServer.sendMsg(msg);
        }
    }

    private OnSocketMessage onSocketMessage = new OnSocketMessage() {
        @Override
        public void onMessage(String message) {
            runOnUiThread(() -> {
                try {
                    RemoteAssistanceBean bean = JSON.parseObject(message,RemoteAssistanceBean.class);
                    switch (bean.getCode()){
                        case 1:
                            Bitmap bitmap = ImageUtil.base64ToBitmap(bean.getData());
                            ivScreen.setImageBitmap(bitmap);
                            if(screenWidth == 0){
                                screenWidth = bitmap.getWidth();
                                screenHeight = bitmap.getHeight();
                            }
                            break;
                        case 8:
                            String[] ss = bean.getData().split(",");
                            screenWidth = Integer.parseInt(ss[0]);
                            screenHeight = Integer.parseInt(ss[1]);
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onMessage(byte[] bytes) {
            if(mDecode264 != null){
                mDecode264.setData(bytes);
            }
        }
    };

    boolean isEffective = true; //此次手势是否有效的
    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Date date = new Date();
            if (view.getId() == R.id.image) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        LogUtils.d(TAG, "onTouchEvent: ACTION_DOWN = " + motionEvent.getAction());
                        if (operationModel == null) {
                            operationModel = new OperationModel();
                        }
                        operationModel.clear();
                        Float[] point = getScreenPoint(motionEvent.getX(), motionEvent.getY());
                        if(point != null){
                            isEffective = true;
                            dateStart = date.getTime();
                            operationModel.setDownPoint(point);
                        }else {
                            isEffective = false;
                            operationModel.setDownPoint(null);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        LogUtils.d(TAG, "onTouchEvent: ACTION_MOVE = " + motionEvent.getAction());
                        if(!isEffective) break;
                        point = getScreenPoint(motionEvent.getX(), motionEvent.getY());
                        if(point != null){
                            operationModel.addLocationModel(point);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        LogUtils.d(TAG, "onTouchEvent: ACTION_UP = " + motionEvent.getAction());
                        if(!isEffective) break;
                        point = getScreenPoint(motionEvent.getX(), motionEvent.getY());
                        if(point != null){
                            dateEnd = date.getTime();
                            during = dateEnd - dateStart;
                            operationModel.addLocationModel(point);
                            operationModel.setDelayTime(0);
                            operationModel.setDurationTime((int) during);
                            remoteAssistanceBean.setCode(6);
                            remoteAssistanceBean.setData(JSON.toJSONString(operationModel));
                            sendMessage("RemoteAssistance"+JSON.toJSONString(remoteAssistanceBean));
                            LogUtils.d(TAG, "onTouch: during:" + during);
//                            if(during <= 100){
//                                RemoteAssistanceBean bean = new RemoteAssistanceBean(5, JSON.toJSONString(operationModel));
//                                sendMessage(JSON.toJSONString(bean));
//                            }else{
//                                if(operationModel.getPointList().size() > 3){
//                                    RemoteAssistanceBean bean = new RemoteAssistanceBean(6, JSON.toJSONString(operationModel));
//                                    sendMessage(JSON.toJSONString(bean));
//                                }
//                            }
                        }
                        break;
                }
            }
            return false;
        }
    };

    private void sendAction(int during){
        operationModel.setDurationTime(during);
        remoteAssistanceBean.setCode(6);
        remoteAssistanceBean.setData(JSON.toJSONString(operationModel));
        String str = JSON.toJSONString(remoteAssistanceBean);
        operationModel.setDownPoint(operationModel.getPointList().get(operationModel.getPointList().size() - 1));
        operationModel.clear();
        sendMessage("RemoteAssistance"+str);
    }

    /**
     * 获取远程屏幕的实际坐标，
     * 显示方式 ： scaleType="fitCenter"
     */
    private Float[] getScreenPoint(float x, float y){
        LogUtils.d(TAG, "onTouchEvent: getX:" + x);
        LogUtils.d(TAG, "onTouchEvent: getY:" + y);
        if(screenWidth == 0) return null;
        float screenX;
        float screenY;
        int imgWidth = ivScreen.getWidth();
        int imgHeight = ivScreen.getHeight();
        LogUtils.d(TAG, "\n\n");
        LogUtils.d(TAG, String.format("test----------------------: 远程屏幕，w = %s，h = %s",screenWidth,screenHeight));
        LogUtils.d(TAG, String.format("test----------------------: imageView，w = %s，h = %s",imgWidth,imgHeight));
        if(1.0f * imgWidth/imgHeight > 1.0f * screenWidth/screenHeight){
            //imgWidth两边会多出空隙
            LogUtils.d(TAG, "test----------------------: 两边多出空隙");
            int sw = imgHeight * screenWidth / screenHeight; //客户端显示远程屏幕的宽度
            LogUtils.d(TAG, String.format("test----------------------: 远程屏幕在图片上宽高 ，w = %s, h = %s" , sw, imgHeight));
            float sx = x - ((imgWidth - sw) >> 1); //客户端显示远程屏幕的宽度上的x坐标
            if(sx < 0 || sx > sw) return null; //点击了远程屏幕的外面
            float scale = 1.0f * screenWidth / sw;
            screenX = sx * scale;
            screenY = y * scale;
            LogUtils.d(TAG, "test----------------------: 远程屏幕宽高/实际显示图片上的宽高 scale = " + scale);
            LogUtils.d(TAG, String.format("test----------------------: 点击事件在图片上的坐标，x = %s，y = %s",sx,y));
        }else {
            LogUtils.d(TAG, "test----------------------: 上下多出空隙");
            int sh = imgWidth * screenHeight / screenWidth;
            LogUtils.d(TAG, String.format("test----------------------: 远程屏幕在图片上宽高 ，w = %s, h = %s" , imgWidth, sh));
            float sy = y - ((imgHeight - sh) >> 1);
            if(sy < 0 || sy > sh) return null; //点击了远程屏幕的外面
            float scale = 1.0f * screenWidth / imgWidth;
            screenX = x * scale;
            screenY = sy * scale;
            LogUtils.d(TAG, "test----------------------: 远程屏幕宽高/实际显示图片上宽高 scale = " + scale);
            LogUtils.d(TAG, String.format("test----------------------: 点击事件在图片上的坐标，x = %s，y = %s",x,sy));
        }
        LogUtils.d(TAG, String.format("test----------------------: 转换成实际屏幕上的坐标，x = %s，y = %s",screenX,screenY));
        return new Float[]{Math.round(screenX * 100) / 100.0f, Math.round(screenY * 100) / 100.0f};
    }

    private void showConfirmDialog(String msg){
        if(isFinishing()) return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }
}
