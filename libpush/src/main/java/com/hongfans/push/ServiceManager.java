/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hongfans.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.hongfans.push.iq.SetAliasIQ;
import com.hongfans.push.iq.SetTagsIQ;
import com.hongfans.push.util.CommonUtil;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.LogUtil;

import java.util.Properties;

/**
 * This class is to manage the notificatin service and to load the configuration.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public final class ServiceManager{

    private Context context;

    private SharedPreferences sharedPrefs;

    private Properties props;

    private String version;

    private String apiKey;

    private String xmppHost;

    private String xmppPort;

//    private String callbackActivityPackageName;

//    private String callbackActivityClassName;

    public ServiceManager(Context context){
        this.context = context;
        // 需要将 IntentService 从 ServiceManager 通过 NotificationService 传递到 XmppManager，
        // 然后 NotificationPacketListener 持有 XmppManager 引用进而调用到 IntentService
//        if(context instanceof Activity){
//            LogUtil.i("Callback Activity...");
//            Activity callbackActivity = (Activity)context;
//            callbackActivityPackageName = callbackActivity.getPackageName();
//            callbackActivityClassName = callbackActivity.getClass().getName();
//        }

        //        apiKey = getMetaDataValue("ANDROIDPN_API_KEY");
        //        LogUtil.i("apiKey=" + apiKey);
        //        //        if (apiKey == null) {
        //        //            LogUtil.e("Please set the androidpn api key in the manifest file.");
        //        //            throw new RuntimeException();
        //        //        }

        props = loadProperties();
        apiKey = props.getProperty("apiKey", "1234567890");
        xmppHost = props.getProperty("xmppHost", "192.168.0.103");
        xmppPort = props.getProperty("xmppPort", "5222");
        version = props.getProperty("version", "0.5.0") + BuildConfig.BUILD_TIMESTAMP;
        LogUtil.i("apiKey=" + apiKey);
        LogUtil.i("xmppHost=" + xmppHost);
        LogUtil.i("xmppPort=" + xmppPort);
        LogUtil.i("version=" + version);

        sharedPrefs = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(Constants.API_KEY, apiKey);
        editor.putString(Constants.VERSION, version);
        editor.putString(Constants.XMPP_HOST, xmppHost);
        editor.putInt(Constants.XMPP_PORT, Integer.parseInt(xmppPort));
//        editor.putString(Constants.CALLBACK_ACTIVITY_PACKAGE_NAME,
//                callbackActivityPackageName);
//        editor.putString(Constants.CALLBACK_ACTIVITY_CLASS_NAME,
//                callbackActivityClassName);
        editor.commit();
        // LogUtil.i("sharedPrefs=" + sharedPrefs.toString());
    }

    /**
     * 启动推送服务
     * @param clientDeviceID push sdk　所在应用认为的设备唯一ID
     */
    public void startService(String clientDeviceID) {
        //　如果有传入 clientDeviceID，使用传入的，否则获取上一次设置的
        if (CommonUtil.isNotEmpty(clientDeviceID)) {
            sharedPrefs.edit().putString(Constants.CLIENT_DEVICE_ID, clientDeviceID).commit();
        } else {
            clientDeviceID = sharedPrefs.getString(Constants.CLIENT_DEVICE_ID, "");
        }

        if (CommonUtil.isEmpty(clientDeviceID)) {
            LogUtil.e("传入的 clientDeviceID 为空");
        } else {
            LogUtil.i("传入的 clientDeviceID " + clientDeviceID);
        }
        Intent intent = NotificationService.getIntent(context, NotificationService.ACTION_SET_CLIENT_DEVICE_ID);
        intent.putExtra("clientDeviceID", clientDeviceID);
        context.getApplicationContext().startService(intent);
    }

    public void stopService(){
        Intent intent = NotificationService.getIntent(context, null);
        context.stopService(intent);
    }

    public <T extends HFIntentService> void registerPushIntentService(final Class<T> service){
        String name;
        if (service == null) {
            // 获取已存在的 service
            name = sharedPrefs.getString(Constants.INTENT_SERVICE_NAME, "");
        } else {
            name = service.getName();
            sharedPrefs.edit().putString(Constants.INTENT_SERVICE_NAME, name).commit();
        }
        if (CommonUtil.isEmpty(name)) {
            LogUtil.e("传入的 service 为空");
        } else {
            LogUtil.i("传入的 service " + name);
        }
        Intent intent = NotificationService.getIntent(context, NotificationService.ACTION_SET_UIS);
        intent.putExtra("uis", name);
        context.getApplicationContext().startService(intent);
    }

    //    private String getMetaDataValue(String name, String def) {
    //        String value = getMetaDataValue(name);
    //        return (value == null) ? def : value;
    //    }
    //
    //    private String getMetaDataValue(String name) {
    //        Object value = null;
    //        PackageManager packageManager = context.getPackageManager();
    //        ApplicationInfo applicationInfo;
    //        try {
    //            applicationInfo = packageManager.getApplicationInfo(context
    //                    .getPackageName(), 128);
    //            if (applicationInfo != null && applicationInfo.metaData != null) {
    //                value = applicationInfo.metaData.get(name);
    //            }
    //        } catch (NameNotFoundException e) {
    //            throw new RuntimeException(
    //                    "Could not read the name in the manifest file.", e);
    //        }
    //        if (value == null) {
    //            throw new RuntimeException("The name '" + name
    //                    + "' is not defined in the manifest file's meta data.");
    //        }
    //        return value.toString();
    //    }

    private Properties loadProperties(){
        //        InputStream in = null;
        //        Properties props = null;
        //        try {
        //            in = getClass().getResourceAsStream(
        //                    "/org/androidpn/client/client.properties");
        //            if (in != null) {
        //                props = new Properties();
        //                props.load(in);
        //            } else {
        //                LogUtil.e("Could not find the properties file.");
        //            }
        //        } catch (IOException e) {
        //            LogUtil.e("Could not find the properties file.", e);
        //        } finally {
        //            if (in != null)
        //                try {
        //                    in.close();
        //                } catch (Throwable ignore) {
        //                }
        //        }
        //        return props;

        Properties props = new Properties();
        try{
            int id = context.getResources().getIdentifier("androidpn", "raw", context.getPackageName());
            props.load(context.getResources().openRawResource(id));
        } catch(Exception e){
            LogUtil.e("Could not find the properties file." + e);
            // e.printStackTrace();
        }
        return props;
    }

    public String getVersion(){
        return version;
    }
    //
    //    public String getApiKey() {
    //        return apiKey;
    //    }

//    public void setNotificationIcon(int iconId){
//        Editor editor = sharedPrefs.edit();
//        editor.putInt(Constants.NOTIFICATION_ICON, iconId);
//        editor.commit();
//    }

    //    public void viewNotificationSettings() {
    //        Intent intent = new Intent().setClass(context,
    //                NotificationSettingsActivity.class);
    //        context.startActivity(intent);
    //    }

   /* public static void viewNotificationSettings(Context context){
        Intent intent = new Intent().setClass(context, NotificationSettingsActivity.class);
        context.startActivity(intent);
    }*/

    //设置别名
    public void setAlias(final String alias){
        final String username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        if(/*CommonUtil.isEmpty(alias) || */CommonUtil.isEmpty(username)){
            return;
        }
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Thread.sleep(500);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
                NotificationService service = NotificationService.getNotification();
                if (service == null) {
                    LogUtil.e("service is null, set alias failed");
                    return;
                }
                XmppManager xmppManager = service.getXmppManager();
                if(xmppManager != null){
                    if(!xmppManager.isAuthenticated()){
                        synchronized(xmppManager){
                            try{
                                LogUtil.d("wait for authenticate");
                                xmppManager.wait();
                            } catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    LogUtil.d("authenticated");
                    SetAliasIQ setAliasIQ = new SetAliasIQ();
                    setAliasIQ.setType(IQ.Type.SET);
//                    setAliasIQ.setUsername(username);
                    setAliasIQ.setAlias(alias);
                    LogUtil.d("username " + username + ", alias " + alias);
                    xmppManager.getConnection().sendPacket(setAliasIQ);
                }
            }
        }).start();
    }

    public void setTags(final String[] tags){
        final String username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        if(CommonUtil.isEmpty(tags) || CommonUtil.isEmpty(username)){
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(","); // 标签分割规则 ,标签1,标签2,标签3
                for(String tag : tags){
                    sb.append(tag).append(",");
                }
                NotificationService service = NotificationService.getNotification();
                if (service == null) {
                    LogUtil.e("service is null, set tags failed");
                    return;
                }
                XmppManager xmppManager = service.getXmppManager();
                if (xmppManager != null) {
                    if (!xmppManager.isAuthenticated()) {
                        synchronized (xmppManager) {
                            try {
                                LogUtil.d("wait for authenticate");
                                xmppManager.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LogUtil.d("authenticated");
                    SetTagsIQ iq = new SetTagsIQ();
                    iq.setType(IQ.Type.SET);
                    iq.setTags(sb.toString());
                    LogUtil.d("username " + username + ", tags " + sb.toString());
                    xmppManager.getConnection().sendPacket(iq);
                }
            }
        }).start();
    }

    /**
     * 清空用户本地信息
     */
    public void clearUserInfo() {
        stopService();
        sharedPrefs.edit().clear().commit();
    }
}
