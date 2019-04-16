package com.example.testalram;

import android.os.Bundle;

/**
 * Full screen alarm alert: pops visible indicator and plays alarm tone. This
 * activity displays the alert in full screen in order to be secure. The
 * background is the current wallpaper.
 */
public class AlarmAlertFullScreen extends AlarmAlert {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }
    
    @Override
    public void onBackPressed() {

        return;
    }
}
