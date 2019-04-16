package com.example.testalram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLManager {
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_EUCKR = "euc-kr";
    public static final String USER_AGENT_PC = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    public static InputStream getURLInputStream(String url) {
        URLConnection conn = getURLConnection(url, null, null);

        if (conn != null) {
            try {
                return conn.getInputStream();
            } catch (IOException e) {
            }
        }

        return null;
    }

    private static URLConnection getURLConnection(String url, String userAgent, String referer) {
        URLConnection conn = null;

        try {
            URL _url = new URL(url);
            conn = _url.openConnection();
            if (userAgent != null)
                conn.addRequestProperty("User-Agent", userAgent);
            if (referer != null)
                conn.addRequestProperty("Referer", referer);
        } catch (Exception e) {
        }

        return conn;
    }
}
