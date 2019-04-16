package com.example.testalram;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Log.LOGV) Log.v("AlarmInitReceiver" + action);

        if (context.getContentResolver() == null) {
            Log.e("AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            Alarms.disableExpiredAlarms(context);
        }
        Alarms.setNextAlert(context);
    }
}
