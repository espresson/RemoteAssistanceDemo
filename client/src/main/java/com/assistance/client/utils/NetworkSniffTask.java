package com.assistance.client.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.assistance.client.data.bean.DeviceHost;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取wifi局域网的所有设备名称和ip
 * Author: HuangYuGuang
 * Date: 2022/12/7
 */
public class NetworkSniffTask extends AsyncTask<Void, Void, List<DeviceHost>> {
    private static final String TAG = NetworkSniffTask.class.getSimpleName();
    private WeakReference<Context> mContextRef;

    private HostsIPCallback hostsIPCallback;

    private ProgressDialog progressDialog;
    private int progress = 0,reachableNumber;
    private boolean isCancel = false;

    public NetworkSniffTask(Context context,HostsIPCallback hostsIPCallback) {
        mContextRef = new WeakReference<Context>(context);
        this.hostsIPCallback = hostsIPCallback;
        progressDialog = new ProgressDialog(mContextRef.get());
        progressDialog.setOnCancelListener(dialog -> cancel());
    }

    private void cancel(){
        isCancel = true;
        if(!isCancelled()) cancel(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
        isCancel = false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(progress);
        progressDialog.setMessage(String.format("正在检测第%s/255个IP（%s）",progress, reachableNumber));
    }

    @Override
    protected List<DeviceHost> doInBackground(Void... voids) {
        Log.d(TAG, "Let's sniff the network");
        List<DeviceHost> deviceHostList = new ArrayList<>();
        try {
            Context context = mContextRef.get();
            if (context != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);
                Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
                Log.d(TAG, "ipString: " + String.valueOf(ipString));
                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: " + prefix);
                for (int i = 0; i < 255; i++) {
                    if(isCancel) break;
                    progress = i + 1;
                    reachableNumber = deviceHostList.size();
                    publishProgress();
                    String testIp = prefix + String.valueOf(i);
                    if(ipString.equals(testIp)) continue;
                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = address.isReachable(1000);
                    String hostName = address.getCanonicalHostName();
                    if (reachable){
                        Log.i(TAG, "DeviceHost: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
                        deviceHostList.add(new DeviceHost(hostName,testIp));
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Well that's not good.", t);
        }
        return deviceHostList;
    }

    @Override
    protected void onCancelled(List<DeviceHost> deviceHosts) {
        super.onCancelled(deviceHosts);
        if(hostsIPCallback != null){
            hostsIPCallback.hostsIP(deviceHosts);
        }
    }

    @Override
    protected void onPostExecute(List<DeviceHost> deviceHostList) {
        progressDialog.dismiss();
        if(hostsIPCallback != null){
            hostsIPCallback.hostsIP(deviceHostList);
        }
    }

    public interface HostsIPCallback{
        void hostsIP(List<DeviceHost> deviceHostList);
    }
}