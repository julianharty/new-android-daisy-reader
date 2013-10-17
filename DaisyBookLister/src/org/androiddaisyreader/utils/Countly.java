package org.androiddaisyreader.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openUDID.OpenUDIDManager;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public final class Countly {
    private static Countly sharedInstance;
    private ConnectionQueue queue;
    private EventQueue eventQueue;
    private boolean isVisible;
    private double unsentSessionLength;
    private double lastTime;
    private int activityCount;

    private static final long DELAY = 60 * 1000;
    private static final long PERIOD = 60 * 1000;
    public static final double THOUSAND = 1000.0;
    public static final int TEN = 10;

    public static Countly sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new Countly();
        }

        return sharedInstance;
    }

    private Countly() {
        queue = new ConnectionQueue();
        eventQueue = new EventQueue();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onTimer();
            }
        }, DELAY, PERIOD);

        isVisible = false;
        unsentSessionLength = 0;
        activityCount = 0;
    }

    public void init(Context context, String serverURL, String appKey) {
        OpenUDIDManager.sync(context);
        queue.setContext(context);
        queue.setServerURL(serverURL);
        queue.setAppKey(appKey);
    }

    public void onStart() {
        activityCount++;
        if (activityCount == 1) {
            onStartHelper();
        }
    }

    public void onStop() {
        activityCount--;
        if (activityCount == 0) {
            onStopHelper();
        }
    }

    public void onStartHelper() {
        lastTime = System.currentTimeMillis() / THOUSAND;
        queue.beginSession();
        isVisible = true;
    }

    public void onStopHelper() {
        if (eventQueue.size() > 0) {
            queue.recordEvents(eventQueue.events());
        }

        double currTime = System.currentTimeMillis() / THOUSAND;
        unsentSessionLength += currTime - lastTime;

        int duration = (int) unsentSessionLength;
        queue.endSession(duration);
        unsentSessionLength -= duration;

        isVisible = false;
    }

    public void recordEvent(String key, int count) {
        eventQueue.recordEvent(key, count);

        if (eventQueue.size() >= TEN) {
            queue.recordEvents(eventQueue.events());
        }
    }

    public void recordEvent(String key, int count, double sum) {
        eventQueue.recordEvent(key, count, sum);

        if (eventQueue.size() >= TEN) {
            queue.recordEvents(eventQueue.events());
        }

    }

    public void recordEvent(String key, Map<String, String> segmentation, int count) {
        eventQueue.recordEvent(key, segmentation, count);

        if (eventQueue.size() >= TEN) {
            queue.recordEvents(eventQueue.events());
        }
    }

    public void recordEvent(String key, Map<String, String> segmentation, int count, double sum) {
        eventQueue.recordEvent(key, segmentation, count, sum);

        if (eventQueue.size() >= TEN) {
            queue.recordEvents(eventQueue.events());
        }
    }

    private void onTimer() {
        if (!isVisible) {
            return;
        }

        double currTime = System.currentTimeMillis() / THOUSAND;
        unsentSessionLength += currTime - lastTime;
        lastTime = currTime;

        int duration = (int) unsentSessionLength;
        queue.updateSession(duration);
        unsentSessionLength -= duration;

        if (eventQueue.size() > 0) {
            queue.recordEvents(eventQueue.events());
        }
    }
}

class ConnectionQueue {
    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    private Thread thread = null;
    private String appKey;
    private Context context;
    private String serverURL;

    public void setAppKey(String appKeyValue) {
        appKey = appKeyValue;
    }

    public void setContext(Context contextValue) {
        context = contextValue;
    }

    public void setServerURL(String serverURLValue) {
        serverURL = serverURLValue;
    }

    private static final String APP_KEY = "app_key=";
    private static final String DEVICE_ID = "device_id=";
    private static final String TIMESTAMP = "timestamp=";

    public void beginSession() {
        String data;
        data = APP_KEY + appKey;
        data += "&" + DEVICE_ID + DeviceInfo.getUDID();
        data += "&" + TIMESTAMP + (long) (System.currentTimeMillis() / Countly.THOUSAND);
        data += "&" + "sdk_version=" + "1.0";
        data += "&" + "begin_session=" + "1";
        data += "&" + "metrics=" + DeviceInfo.getMetrics(context);

        queue.offer(data);

        tick();
    }

    public void updateSession(int duration) {
        String data;
        data = APP_KEY + appKey;
        data += "&" + DEVICE_ID + DeviceInfo.getUDID();
        data += "&" + TIMESTAMP + (long) (System.currentTimeMillis() / Countly.THOUSAND);
        data += "&" + "session_duration=" + duration;

        queue.offer(data);

        tick();
    }

    public void endSession(int duration) {
        String data;
        data = APP_KEY + appKey;
        data += "&" + DEVICE_ID + DeviceInfo.getUDID();
        data += "&" + TIMESTAMP + (long) (System.currentTimeMillis() / Countly.THOUSAND);
        data += "&" + "end_session=" + "1";
        data += "&" + "session_duration=" + duration;

        queue.offer(data);

        tick();
    }

    public void recordEvents(String events) {
        String data;
        data = APP_KEY + appKey;
        data += "&" + DEVICE_ID + DeviceInfo.getUDID();
        data += "&" + TIMESTAMP + (long) (System.currentTimeMillis() / Countly.THOUSAND);
        data += "&" + "events=" + events;

        queue.offer(data);

        tick();
    }

    private void tick() {
        if (thread != null && thread.isAlive() || queue.isEmpty()) {
            return;
        }

        thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    String data = queue.peek();

                    if (data == null) {
                        break;
                    }
                    int index = data.indexOf("REPLACE_UDID");
                    if (index != -1) {
                        if (!OpenUDIDManager.isInitialized()) {
                            break;
                        }
                        data = data.replaceFirst("REPLACE_UDID", OpenUDIDManager.getOpenUDID());
                    }
                    try {
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpGet method = new HttpGet(new URI(serverURL + "/i?" + data));
                        HttpResponse response = httpClient.execute(method);
                        InputStream input = response.getEntity().getContent();
                        while (input.read() != -1) {
                            httpClient.getConnectionManager().shutdown();
                        }
                        Log.d("Countly", "ok ->" + data);
                        queue.poll();
                    } catch (Exception e) {
                        Log.d("Countly", e.toString());
                        Log.d("Countly", "error ->" + data);
                        break;
                    }
                }
            }
        };

        thread.start();
    }
}

class DeviceInfo {
    public static String getUDID() {
        return OpenUDIDManager.isInitialized() == false ? "REPLACE_UDID" : OpenUDIDManager
                .getOpenUDID();
    }

    public static String getOS() {
        return "Android";
    }

    public static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static String getDevice() {
        return android.os.Build.MODEL;
    }

    public static String getResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics.widthPixels + "x" + metrics.heightPixels;
    }

    public static String getCarrier(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    public static String getLocale() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    public static String appVersion(Context context) {
        String result = "1.0";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }

        return result;
    }

    public static String getMetrics(Context context) {
        String result = "{";

        result += "\"" + "_device" + "\"" + ":" + "\"" + getDevice() + "\"";

        result += "," + "\"" + "_os" + "\"" + ":" + "\"" + getOS() + "\"";

        result += "," + "\"" + "_os_version" + "\"" + ":" + "\"" + getOSVersion() + "\"";

        result += "," + "\"" + "_carrier" + "\"" + ":" + "\"" + getCarrier(context) + "\"";

        result += "," + "\"" + "_resolution" + "\"" + ":" + "\"" + getResolution(context) + "\"";

        result += "," + "\"" + "_locale" + "\"" + ":" + "\"" + getLocale() + "\"";

        result += "," + "\"" + "_app_version" + "\"" + ":" + "\"" + appVersion(context) + "\"";

        result += "}";

        try {
            result = java.net.URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("UnsupportedEncodingException", e.getMessage());
        }

        return result;
    }
}

class Event {
    public String key = null;
    public Map<String, String> segmentation = null;
    public int count = 0;
    public double sum = 0;
    public double timestamp = 0;
}

class EventQueue {
    private ArrayList<Event> events;

    public EventQueue() {
        events = new ArrayList<Event>();
    }

    public int size() {
        synchronized (this) {
            return events.size();
        }
    }

    public String events() {
        String result = "[";

        synchronized (this) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);

                result += "{";

                result += "\"" + "key" + "\"" + ":" + "\"" + event.key + "\"";

                if (event.segmentation != null) {
                    String segmentation = "{";

                    String keys[] = event.segmentation.keySet().toArray(new String[0]);

                    for (int j = 0; j < keys.length; ++j) {
                        String key = keys[j];
                        String value = event.segmentation.get(key);
                        segmentation += "\"" + key + "\"" + ":" + "\"" + value + "\"";
                        if (j + 1 < keys.length) {
                            segmentation += ",";
                        }
                    }
                    segmentation += "}";
                    result += "," + "\"" + "segmentation" + "\"" + ":" + segmentation;
                }
                result += "," + "\"" + "count" + "\"" + ":" + event.count;
                if (event.sum > 0) {
                    result += "," + "\"" + "sum" + "\"" + ":" + event.sum;
                }
                result += "," + "\"" + "timestamp" + "\"" + ":" + (long) event.timestamp;
                result += "}";
                if (i + 1 < events.size()) {
                    result += ",";
                }
            }

            events.clear();
        }

        result += "]";

        try {
            result = java.net.URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("UnsupportedEncodingException", e.getMessage());
        }

        return result;
    }

    public void recordEvent(String key, int count) {
        synchronized (this) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);

                if (event.key.equals(key)) {
                    event.count += count;
                    event.timestamp = (event.timestamp + (System.currentTimeMillis() / Countly.THOUSAND)) / 2;
                    return;
                }
            }

            Event event = new Event();
            event.key = key;
            event.count = count;
            event.timestamp = System.currentTimeMillis() / Countly.THOUSAND;
            events.add(event);
        }
    }

    public void recordEvent(String key, int count, double sum) {
        synchronized (this) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);

                if (event.key.equals(key)) {
                    event.count += count;
                    event.sum += sum;
                    event.timestamp = (event.timestamp + (System.currentTimeMillis() / Countly.THOUSAND)) / 2;
                    return;
                }
            }

            Event event = new Event();
            event.key = key;
            event.count = count;
            event.sum = sum;
            event.timestamp = System.currentTimeMillis() / Countly.THOUSAND;
            events.add(event);
        }
    }

    public void recordEvent(String key, Map<String, String> segmentation, int count) {
        synchronized (this) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);

                if (event.key.equals(key) && event.segmentation != null
                        && event.segmentation.equals(segmentation)) {
                    event.count += count;
                    event.timestamp = (event.timestamp + (System.currentTimeMillis() / Countly.THOUSAND)) / 2;
                    return;
                }
            }

            Event event = new Event();
            event.key = key;
            event.segmentation = segmentation;
            event.count = count;
            event.timestamp = System.currentTimeMillis() / Countly.THOUSAND;
            events.add(event);
        }
    }

    public void recordEvent(String key, Map<String, String> segmentation, int count, double sum) {
        synchronized (this) {
            for (int i = 0; i < events.size(); ++i) {
                Event event = events.get(i);

                if (event.key.equals(key) && event.segmentation != null
                        && event.segmentation.equals(segmentation)) {
                    event.count += count;
                    event.sum += sum;
                    event.timestamp = (event.timestamp + (System.currentTimeMillis() / Countly.THOUSAND)) / 2;
                    return;
                }
            }

            Event event = new Event();
            event.key = key;
            event.segmentation = segmentation;
            event.count = count;
            event.sum = sum;
            event.timestamp = System.currentTimeMillis() / Countly.THOUSAND;
            events.add(event);
        }
    }
}
