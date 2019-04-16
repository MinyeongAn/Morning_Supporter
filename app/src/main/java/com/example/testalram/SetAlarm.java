package com.example.testalram;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SetAlarm extends PreferenceActivity
        implements TimePickerDialog.OnTimeSetListener {

    private EditTextPreference mLabel;
    private Preference mTimePref;
    private AlarmPreference mAlarmPref;
    private CheckBoxPreference mVibratePref;
    private RepeatPreference mRepeatPref;
    private MenuItem mDeleteAlarmItem;
    private MenuItem mTestAlarmItem;

    private int     mId;
    private boolean mEnabled;
    private int     mHour;
    private int     mMinutes;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.alarm_prefs);

        mLabel = (EditTextPreference) findPreference("label");
        mLabel.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference p,
                            Object newValue) {
                        p.setSummary((String) newValue);
                        return true;
                    }
                });
        mTimePref = findPreference("time");
        mAlarmPref = (AlarmPreference) findPreference("alarm");
        mVibratePref = (CheckBoxPreference) findPreference("vibrate");
        mRepeatPref = (RepeatPreference) findPreference("setRepeat");

        Intent i = getIntent();
        mId = i.getIntExtra(Alarms.ALARM_ID, -1);
        if (Log.LOGV) {
            Log.v("In SetAlarm, alarm id = " + mId);
        }

        Alarm alarm = Alarms.getAlarm(getContentResolver(), mId);
        mEnabled = alarm.enabled;
        mLabel.setText(alarm.label);
        mLabel.setSummary(alarm.label);
        mHour = alarm.hour;
        mMinutes = alarm.minutes;
        mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
        mVibratePref.setChecked(alarm.vibrate);
        mAlarmPref.setAlert(alarm.alert);
        updateTime();

        getListView().setItemsCanFocus(true);

        FrameLayout content = (FrameLayout) getWindow().getDecorView()
                .findViewById(android.R.id.content);

        ListView lv = getListView();
        ((ViewGroup)lv.getParent()).removeView(lv);
        content.removeView(lv);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        ll.addView(lv, lp);

        View v = LayoutInflater.from(this).inflate(
                R.layout.save_cancel_alarm, ll);

        Button b = (Button) v.findViewById(R.id.alarm_save);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    saveAlarm();
                    finish();
                }
        });
        b = (Button) v.findViewById(R.id.alarm_cancel);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
        });

        setContentView(ll);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mTimePref) {
            new TimePickerDialog(this, this, mHour, mMinutes,
                    DateFormat.is24HourFormat(this)).show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onBackPressed() {
        saveAlarm();
        finish();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinutes = minute;
        updateTime();
        mEnabled = true;
    }

    private void updateTime() {
        if (Log.LOGV) {
            Log.v("updateTime " + mId);
        }
        mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinutes,
                mRepeatPref.getDaysOfWeek()));
    }

    private void saveAlarm() {
        final String alert = mAlarmPref.getAlertString();
        Alarms.setAlarm(this, mId, mEnabled, mHour, mMinutes,
                mRepeatPref.getDaysOfWeek(), mVibratePref.isChecked(),
                mLabel.getText(), alert);

        if (mEnabled) {
            popAlarmSetToast(this, mHour, mMinutes,
                    mRepeatPref.getDaysOfWeek());
        }
    }

    private static void saveAlarm(
            Context context, int id, boolean enabled, int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek, boolean vibrate, String label,
            String alert, boolean popToast) {
        if (Log.LOGV) Log.v("** saveAlarm " + id + " " + label + " " + enabled
                + " " + hour + " " + minute + " vibe " + vibrate);

        Alarms.setAlarm(context, id, enabled, hour, minute, daysOfWeek, vibrate,
                label, alert);

        if (enabled && popToast) {
            popAlarmSetToast(context, hour, minute, daysOfWeek);
        }
    }

    static void popAlarmSetToast(Context context, int hour, int minute,
                                 Alarm.DaysOfWeek daysOfWeek) {

        String toastText = formatToast(context, hour, minute, daysOfWeek);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    static String formatToast(Context context, int hour, int minute,
                              Alarm.DaysOfWeek daysOfWeek) {
        long alarm = Alarms.calculateAlarm(hour, minute,
                                           daysOfWeek).getTimeInMillis();
        long delta = alarm - System.currentTimeMillis();;
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                    (dispHour ? 2 : 0) |
                    (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mDeleteAlarmItem = menu.add(0, 0, 0, R.string.delete_alarm);
        mDeleteAlarmItem.setIcon(android.R.drawable.ic_menu_delete);

        if (AlarmClock.DEBUG) {
            mTestAlarmItem = menu.add(0, 0, 0, "test alarm");
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mDeleteAlarmItem) {
            Alarms.deleteAlarm(this, mId);
            finish();
            return true;
        }
        if (AlarmClock.DEBUG) {
            if (item == mTestAlarmItem) {
                setTestAlarm();
                return true;
            }
        }

        return false;
    }
    void setTestAlarm() {

        // start with now
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(java.util.Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(java.util.Calendar.MINUTE);

        int minutes = (nowMinute + 1) % 60;
        int hour = nowHour + (nowMinute == 0 ? 1 : 0);

        saveAlarm(this, mId, true, hour, minutes, mRepeatPref.getDaysOfWeek(),
                true, mLabel.getText(), mAlarmPref.getAlertString(), true);
    }

}

