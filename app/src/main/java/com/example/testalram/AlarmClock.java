package com.example.testalram;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.Calendar;


public class AlarmClock extends Activity implements OnItemClickListener {

    static String ranklist[] = {"", "", "", "", "", "", "", "", "", ""};
    static String weathertotdata[] = {"1", "2", "3", "4", "5"};
    private Object tc, tmax, tmin, skyname, skycode;

    final static String PREFERENCES = "AlarmClock";
    final static String PREF_CLOCK_FACE = "face";
    final static String PREF_SHOW_CLOCK = "show_clock";

    LocationManager locationManager;
    static double latitude = 0.0;
    static double longitude = 0.0;


    final static int MAX_ALARM_COUNT = 12;


    final static boolean DEBUG = false;

    private SharedPreferences mPrefs;
    private LayoutInflater mFactory;
    private ViewGroup mClockLayout;
    private View mClock = null;
    private ListView mAlarmsList;
    private Cursor mCursor;

    private String mAm, mPm;


    private int mFace = -1;


    final static int[] CLOCKS = {
            R.layout.digital_clock
    };

    private class AlarmTimeAdapter extends CursorAdapter {
        @SuppressWarnings("deprecation")
        public AlarmTimeAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.alarm_time, parent, false);

            ((TextView) ret.findViewById(R.id.am)).setText(mAm);
            ((TextView) ret.findViewById(R.id.pm)).setText(mPm);


            DigitalClock digitalClock = (DigitalClock) ret.findViewById(R.id.digitalClock);
            digitalClock.setLive(false);
            if (Log.LOGV) Log.v("newView " + cursor.getPosition());
            return ret;
        }

        public void bindView(View view, Context context, Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);

            CheckBox onButton = (CheckBox) view.findViewById(R.id.alarmButton);
            onButton.setChecked(alarm.enabled);
            onButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    Alarms.enableAlarm(AlarmClock.this, alarm.id,
                            isChecked);
                    if (isChecked) {
                        SetAlarm.popAlarmSetToast(AlarmClock.this,
                                alarm.hour, alarm.minutes, alarm.daysOfWeek);
                    }
                }
            });

            DigitalClock digitalClock =
                    (DigitalClock) view.findViewById(R.id.digitalClock);

            // set the alarm text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minutes);
            digitalClock.updateTime(c);

            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView =
                    (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr =
                    alarm.daysOfWeek.toString(AlarmClock.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView =
                    (TextView) digitalClock.findViewById(R.id.label);
            if (alarm.label != null && alarm.label.length() != 0) {
                labelView.setText(alarm.label);
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }
        }
    }




    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        switch (item.getItemId()) {
            case R.id.delete_alarm:
                // Confirm that the alarm will be deleted.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_alarm))
                        .setMessage(getString(R.string.delete_alarm_confirm))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d,
                                                        int w) {
                                        Alarms.deleteAlarm(AlarmClock.this, id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.enable_alarm:
                final Cursor c = (Cursor) mAlarmsList.getAdapter()
                        .getItem(info.position);
                final Alarm alarm = new Alarm(c);
                Alarms.enableAlarm(this, alarm.id, !alarm.enabled);
                if (!alarm.enabled) {
                    SetAlarm.popAlarmSetToast(this, alarm.hour, alarm.minutes,
                            alarm.daysOfWeek);
                }
                return true;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);


        //GPS가 켜져있는지 체크
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
            finish();
        }

        //마시멜로 이상이면 권한 요청하기
        if (Build.VERSION.SDK_INT >= 23) {
            //권한이 없는 경우
            if (ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AlarmClock.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            //권한이 있는 경우
            else {
                requestMyLocation();
            }
        }
        //마시멜로 아래
        else {
            requestMyLocation();
        }

        String[] ampm = new DateFormatSymbols().getAmPmStrings();
        mAm = ampm[0];
        mPm = ampm[1];
        mFactory = LayoutInflater.from(this);
        mPrefs = getSharedPreferences(PREFERENCES, 0);
        mCursor = Alarms.getAlarmsCursor(getContentResolver());
        updateLayout();
        setClockVisibility(mPrefs.getBoolean(PREF_SHOW_CLOCK, true));
        JsoupAsyncTask JsoupAsyncTask = new JsoupAsyncTask();
        JsoupAsyncTask.execute();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //ACCESS_COARSE_LOCATION 권한
        if (requestCode == 1) {
            //권한받음
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestMyLocation();
            }
            //권한못받음
            else {

                finish();
            }
        }
    }

    //나의 위치 요청
    public void requestMyLocation() {
        if (ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
    }

    locationListener locationListener = new locationListener();


    //위치정보 구하기 리스너
    public class locationListener implements LocationListener {
        public locationListener() {
        }

        Location location;

        public void onLocationChanged(Location location) {
            if (ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(AlarmClock.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //나의 위치를 한번만 가져오기 위해
            locationManager.removeUpdates(locationListener);
            this.location = location;
            //위도 경도
            latitude = location.getLatitude();   //위도
            longitude = location.getLongitude(); //경도

        }

        public Double getLat() {

            return latitude;

        }

        public Double getLong() {

            return longitude;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {


        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
    public class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
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

            textView1.setText(ranklist[0]);
            textView2.setText(ranklist[1]);
            textView3.setText(ranklist[2]);
            textView4.setText(ranklist[3]);
            textView5.setText(ranklist[4]);
            textView6.setText(ranklist[5]);
            textView7.setText(ranklist[6]);
            textView8.setText(ranklist[7]);
            textView9.setText(ranklist[8]);
            textView10.setText(ranklist[9]);

            TextView tc = (TextView) findViewById(R.id.tc);
            TextView tmin = (TextView) findViewById(R.id.tmin);
            TextView tmax = (TextView) findViewById(R.id.tmax);
            TextView skyname = (TextView) findViewById(R.id.skyname);

            tc.setText(weathertotdata[0]);
            tmax.setText(weathertotdata[1]);
            tmin.setText(weathertotdata[2]);
            skyname.setText(weathertotdata[3]);
            ImageView skycode = (ImageView) findViewById(R.id.skycode);

            if (weathertotdata[4].equals("SKY_O01")) {
                skycode.setImageResource(R.mipmap.sky_o01);
            } else if (weathertotdata[4].equals("SKY_O02")) {
                skycode.setImageResource(R.mipmap.sky_o02);
            } else if (weathertotdata[4].equals("SKY_O03")) {
                skycode.setImageResource(R.mipmap.sky_o03);
            } else if (weathertotdata[4].equals("SKY_O04")) {
                skycode.setImageResource(R.mipmap.sky_o04);
            } else if (weathertotdata[4].equals("SKY_O05")) {
                skycode.setImageResource(R.mipmap.sky_o05);
            } else if (weathertotdata[4].equals("SKY_O06")) {
                skycode.setImageResource(R.mipmap.sky_o06);
            } else if (weathertotdata[4].equals("SKY_O07")) {
                skycode.setImageResource(R.mipmap.sky_o07);
            } else if (weathertotdata[4].equals("SKY_O08")) {
                skycode.setImageResource(R.mipmap.sky_o08);
            } else if (weathertotdata[4].equals("SKY_O09")) {
                skycode.setImageResource(R.mipmap.sky_o09);
            } else if (weathertotdata[4].equals("SKY_O10")) {
                skycode.setImageResource(R.mipmap.sky_o10);
            } else if (weathertotdata[4].equals("SKY_O11")) {
                skycode.setImageResource(R.mipmap.sky_o11);
            } else if (weathertotdata[4].equals("SKY_O2")) {
                skycode.setImageResource(R.mipmap.sky_o12);
            } else if (weathertotdata[4].equals("SKY_O13")) {
                skycode.setImageResource(R.mipmap.sky_o13);
            } else if (weathertotdata[4].equals("SKY_O14")) {
                skycode.setImageResource(R.mipmap.sky_o14);
            } else {
                skycode.setImageResource(R.mipmap.sky_o00);
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream in = URLManager.getURLInputStream("http://www.daum.net");
                Document doc = Jsoup.parse(in, URLManager.ENCODING_UTF8, "");
                int cnt = 1;
                Elements root = doc.select("div[class=hot_issue issue_mini #searchrank #issue]");
                Elements rankList = root.select("ol[class=list_hotissue]");

                for (int j = 0; j < 10; j++) {

                    ranklist[j] = rankList.select("li[class]").get(j).select("span[class = txt_issue]").select("a").attr("title");
                    cnt++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Object lat = "37.6";
            Object lon = "127";

            URL weatherurl = null;
            try {
                weatherurl = new URL("http://apis.skplanetx.com/weather/current/hourly?" +
                        "version=1" +
                        "&lat=" + lat +
                        "&lon=" + lon +
                        "&appKey=bb51aee1-9ee7-3460-97d6-1fb1d2254ae0");


                InputStreamReader wisr = new InputStreamReader(weatherurl.openConnection().getInputStream(), "UTF-8");

                // 날씨부분
                JSONObject weatherobject = (JSONObject) JSONValue.parse(wisr);
                JSONObject weatherhead = (JSONObject) weatherobject.get("weather");
                String weatherdata = weatherhead.toString();

                JSONParser wjsonParser = new JSONParser();
                JSONObject wjsonObj = (JSONObject) wjsonParser.parse(weatherdata);
                JSONArray wmemberArray = (JSONArray) wjsonObj.get("hourly");

                for (int i = 0; i < wmemberArray.size(); i++) {
                    JSONObject tempObj = (JSONObject) wmemberArray.get(i);

                    JSONObject griddata = (JSONObject) tempObj.get("sky");
                    skyname = griddata.get("name");
                    skycode = griddata.get("code");

                    JSONObject tempdata = (JSONObject) tempObj.get("temperature");
                    tc = tempdata.get("tc");
                    tmax = tempdata.get("tmax");
                    tmin = tempdata.get("tmin");

                    weathertotdata[0] = tc.toString().substring(0, 2);
                    weathertotdata[1] = tmax.toString().substring(0, 2);
                    weathertotdata[2] = tmin.toString().substring(0, 2);
                    weathertotdata[3] = skyname.toString();
                    weathertotdata[4] = skycode.toString();
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout();
        inflateClock();
    }

    private void updateLayout() {
        setContentView(R.layout.alarm_clock);
        mAlarmsList = (ListView) findViewById(R.id.alarms_list);
        mAlarmsList.setAdapter(new AlarmTimeAdapter(this, mCursor));
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnItemClickListener(this);
        mAlarmsList.setOnCreateContextMenuListener(this);

        mClockLayout = (ViewGroup) findViewById(R.id.clock_layout);
        mClockLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        int face = mPrefs.getInt(PREF_CLOCK_FACE, 0);
        if (mFace != face) {
            if (face < 0 || face >= AlarmClock.CLOCKS.length) {
                mFace = 0;
            } else {
                mFace = face;
            }
            inflateClock();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
        mCursor.deactivate();
    }

    protected void inflateClock() {
        if (mClock != null) {
            mClockLayout.removeView(mClock);
        }

        LayoutInflater.from(this).inflate(CLOCKS[mFace], mClockLayout);
        mClock = findViewById(R.id.clock);

        TextView am = (TextView) findViewById(R.id.am);
        TextView pm = (TextView) findViewById(R.id.pm);

        if (am != null) {
            am.setText(mAm);
        }
        if (pm != null) {
            pm.setText(mPm);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenuInfo menuInfo) {

        getMenuInflater().inflate(R.menu.context_menu, menu);


        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final Cursor c =
                (Cursor) mAlarmsList.getAdapter().getItem((int) info.position);
        final Alarm alarm = new Alarm(c);


        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
        cal.set(Calendar.MINUTE, alarm.minutes);
        final String time = Alarms.formatTime(this, cal);


        final View v = mFactory.inflate(R.layout.context_menu_header, null);
        TextView textView = (TextView) v.findViewById(R.id.header_time);
        textView.setText(time);
        textView = (TextView) v.findViewById(R.id.header_label);
        textView.setText(alarm.label);

        menu.setHeaderView(v);

        if (alarm.enabled) {
            menu.findItem(R.id.enable_alarm).setTitle(R.string.disable_alarm);
        }
    }


    public void onItemClick(AdapterView parent, View v, int pos, long id) {
        Intent intent = new Intent(this, SetAlarm.class);
        intent.putExtra(Alarms.ALARM_ID, (int) id);
        startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_add_alarm).setVisible(
                mAlarmsList.getAdapter().getCount() < MAX_ALARM_COUNT);
        menu.findItem(R.id.menu_toggle_clock).setTitle(
                getClockVisibility() ? R.string.hide_clock
                        : R.string.show_clock);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_alarm:
                Uri uri = Alarms.addAlarm(getContentResolver());
                // FIXME: scroll to new item?
                String segment = uri.getPathSegments().get(1);
                int newId = Integer.parseInt(segment);
                if (Log.LOGV) {
                    Log.v("In AlarmClock, new alarm id = " + newId);
                }
                Intent intent = new Intent(this, SetAlarm.class);
                intent.putExtra(Alarms.ALARM_ID, newId);
                startActivity(intent);
                return true;

            case R.id.menu_toggle_clock:
                setClockVisibility(!getClockVisibility());
                saveClockVisibility();
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean getClockVisibility() {
        return mClockLayout.getVisibility() == View.VISIBLE;
    }

    private void setClockVisibility(boolean visible) {
        mClockLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void saveClockVisibility() {
        mPrefs.edit().putBoolean(PREF_SHOW_CLOCK, getClockVisibility()).commit();
    }
}

