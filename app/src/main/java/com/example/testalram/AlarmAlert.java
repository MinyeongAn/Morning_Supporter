package com.example.testalram;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import java.util.Calendar;

public class AlarmAlert extends Activity {

    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";

    private Alarm mAlarm;
    private int mVolumeBehavior;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            if (mAlarm.id == alarm.id) {
                dismiss(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                        DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        updateLayout();

        registerReceiver(mReceiver, new IntentFilter(Alarms.ALARM_KILLED));
    }

    private void setTitle() {
        String label = mAlarm.getLabelOrDefault(this);
        TextView title = (TextView) findViewById(R.id.alertTitle);
        TextView textView1 = (TextView) findViewById(R.id.serchrankdata1);
        TextView textView2 = (TextView) findViewById(R.id.serchrankdata2);
        TextView textView3 = (TextView) findViewById(R.id.serchrankdata3);
        TextView textView4 = (TextView) findViewById(R.id.serchrankdata4);
        TextView textView5 = (TextView) findViewById(R.id.serchrankdata5);
        TextView textView6 = (TextView) findViewById(R.id.serchrankdata6);
        TextView textView7 = (TextView) findViewById(R.id.serchrankdata7);
        TextView textView8 = (TextView) findViewById(R.id.serchrankdata8);
        TextView textView9 = (TextView) findViewById(R.id.serchrankdata9);
        TextView textView10 = (TextView) findViewById(R.id.serchrankdata10);

        textView1.setText(AlarmClock.ranklist[0]);
        textView2.setText(AlarmClock.ranklist[1]);
        textView3.setText(AlarmClock.ranklist[2]);
        textView4.setText(AlarmClock.ranklist[3]);
        textView5.setText(AlarmClock.ranklist[4]);
        textView6.setText(AlarmClock.ranklist[5]);
        textView7.setText(AlarmClock.ranklist[6]);
        textView8.setText(AlarmClock.ranklist[7]);
        textView9.setText(AlarmClock.ranklist[8]);
        textView10.setText(AlarmClock.ranklist[9]);

        TextView tc = (TextView) findViewById(R.id.tc);
        TextView tmin = (TextView) findViewById(R.id.tmin);
        TextView tmax = (TextView) findViewById(R.id.tmax);
        TextView skyname = (TextView) findViewById(R.id.skyname);

        tc.setText(AlarmClock.weathertotdata[0]);
        tmax.setText(AlarmClock.weathertotdata[1]);
        tmin.setText(AlarmClock.weathertotdata[2]);
        skyname.setText(AlarmClock.weathertotdata[3]);
        ImageView skycode = (ImageView) findViewById(R.id.skycode);

        if(AlarmClock.weathertotdata[4].equals("SKY_O01")){
            skycode.setImageResource(R.mipmap.sky_o01);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O02")){
            skycode.setImageResource(R.mipmap.sky_o02);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O03")){
            skycode.setImageResource(R.mipmap.sky_o03);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O04")){
            skycode.setImageResource(R.mipmap.sky_o04);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O05")){
            skycode.setImageResource(R.mipmap.sky_o05);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O06")){
            skycode.setImageResource(R.mipmap.sky_o06);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O07")){
            skycode.setImageResource(R.mipmap.sky_o07);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O08")){
            skycode.setImageResource(R.mipmap.sky_o08);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O09")){
            skycode.setImageResource(R.mipmap.sky_o09);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O10")){
            skycode.setImageResource(R.mipmap.sky_o10);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O11")){
            skycode.setImageResource(R.mipmap.sky_o11);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O12")){
            skycode.setImageResource(R.mipmap.sky_o12);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O13")){
            skycode.setImageResource(R.mipmap.sky_o13);
        }else if(AlarmClock.weathertotdata[4].equals("SKY_O14")){
            skycode.setImageResource(R.mipmap.sky_o14);
        }else{
            skycode.setImageResource(R.mipmap.sky_o00);
        }
        title.setText(label);
    }

    protected View inflateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.alarm_alert, null);
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflateView(inflater));

        SharedPreferences settings =
                getSharedPreferences(AlarmClock.PREFERENCES, 0);
        int face = settings.getInt(AlarmClock.PREF_CLOCK_FACE, 0);
        if (face < 0 || face >= AlarmClock.CLOCKS.length) {
            face = 0;
        }
        ViewGroup clockView = (ViewGroup) findViewById(R.id.clockView);
        inflater.inflate(AlarmClock.CLOCKS[face], clockView);
        View clockLayout = findViewById(R.id.clock);
        if (clockLayout instanceof DigitalClock) {
            ((DigitalClock) clockLayout).setAnimate();
        }

        Button snooze = (Button) findViewById(R.id.snooze);
        snooze.requestFocus();
        snooze.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                snooze();
            }
        });

        findViewById(R.id.dismiss).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                    }
                });
        setTitle();
    }
    


    private void snooze() {
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlert.this, mAlarm.id, snoozeTime);

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_ID, mAlarm.id);
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text,
                    Alarms.formatTime(this, c)), broadcast);
        n.deleteIntent = broadcast;
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        Log.v(displayTime);

        Toast.makeText(AlarmAlert.this, displayTime, Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void dismiss(boolean killed) {
        if (!killed) {
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
            stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Log.LOGV) Log.v("AlarmAlert.OnNewIntent()");

        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        setTitle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.LOGV) Log.v("AlarmAlert.onDestroy()");
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
}
