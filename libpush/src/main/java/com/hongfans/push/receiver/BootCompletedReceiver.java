package com.hongfans.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.hongfans.push.Constants;
import com.hongfans.push.ServiceManager;
import org.jivesoftware.smack.util.LogUtil;

/**
 * TODO
 * Created by MEI on 2017/11/17.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    TelephonyManager  telephonyManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("收到开机广播");
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (pref.getBoolean(Constants.SETTINGS_AUTO_START, true)) {
            ServiceManager serviceManager = new ServiceManager(context);
            telephonyManager= (TelephonyManager)context.getSystemService( Context.TELEPHONY_SERVICE );
            serviceManager.startService(telephonyManager.getDeviceId());
            serviceManager.registerPushIntentService(null);
        }
    }
}
