package org.openUDID;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

public final class OpenUDIDManager implements ServiceConnection {
    public static final String PREF_KEY = "openudid";
    public static final String PREFS_NAME = "openudid_prefs";
    public static final String TAG = "OpenUDID";
    // Display or not debug message
    private static final boolean LOG = true;
    // Application context
    private final Context mContext;
    // List of available OpenUDID Intents
    private List<ResolveInfo> mMatchingIntents;
    // Map of OpenUDIDs found so far
    private Map<String, Integer> mReceivedOpenUDIDs;
    // Preferences to store the OpenUDID
    private final SharedPreferences mPreferences;
    private final Random mRandom;

    private static final int NUMBITS = 64;
    private static final int RADIX = 16;
    private static final int MINLENGTHUDID = 15;

    private OpenUDIDManager(Context context) {
        mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mContext = context;
        mRandom = new Random();
        mReceivedOpenUDIDs = new HashMap<String, Integer>();
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        // Get the OpenUDID from the remote service
        try {
            // Send a random number to the service
            android.os.Parcel data = android.os.Parcel.obtain();
            data.writeInt(mRandom.nextInt());
            android.os.Parcel reply = android.os.Parcel.obtain();
            service.transact(1, android.os.Parcel.obtain(), reply, 0);
            if (data.readInt() == reply.readInt())
            // Check if the service
            // returns us this number
            {
                final String openUDIDValue = reply.readString();
                // if valid OpenUDID, save it
                if (openUDIDValue != null) {
                    if (LOG)
                        Log.d(TAG, "Received " + openUDIDValue);

                    if (mReceivedOpenUDIDs.containsKey(openUDIDValue))
                        mReceivedOpenUDIDs.put(openUDIDValue,
                                mReceivedOpenUDIDs.get(openUDIDValue) + 1);
                    else
                        mReceivedOpenUDIDs.put(openUDIDValue, 1);

                }
            }
        } catch (RemoteException e) {
            if (LOG)
                Log.e(TAG, "RemoteException: " + e.getMessage());
        }
        mContext.unbindService(this);
        // Try the next one
        startService();
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
    }

    private void storeOpenUDID() {
        final Editor e = mPreferences.edit();
        e.putString(PREF_KEY, openUDID);
        e.commit();
    }

    /*
     * Generate a new OpenUDID
     */
    private void generateOpenUDID() {
        if (LOG)
            Log.d(TAG, "Generating openUDID");
        // Try to get the ANDROID_ID
        openUDID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
        if (openUDID == null || openUDID.equals("9774d56d682e549c")
                || openUDID.length() < MINLENGTHUDID) {
            // if ANDROID_ID is null, or it's equals to the GalaxyTab generic
            // ANDROID_ID or bad, generates a new one
            final SecureRandom random = new SecureRandom();
            openUDID = new BigInteger(NUMBITS, random).toString(RADIX);
        }
    }

    /*
     * Start the oldest service
     */
    private void startService() {
        // There are some Intents untested
        if (mMatchingIntents.size() > 0) {
            if (LOG)
                Log.d(TAG,
                        "Trying service "
                                + mMatchingIntents.get(0).loadLabel(mContext.getPackageManager()));

            final ServiceInfo servInfo = mMatchingIntents.get(0).serviceInfo;
            final Intent i = new Intent();
            i.setComponent(new ComponentName(servInfo.applicationInfo.packageName, servInfo.name));
            mContext.bindService(i, this, Context.BIND_AUTO_CREATE);
            mMatchingIntents.remove(0);
        } else {
            // No more service to test
            getMostFrequentOpenUDID();
            // Choose the most frequent
            // No OpenUDID was chosen, generate one
            if (openUDID == null)
                generateOpenUDID();
            if (LOG)
                Log.d(TAG, "OpenUDID: " + openUDID);
            // Store it locally
            storeOpenUDID();
            mInitialized = true;
        }
    }

    private void getMostFrequentOpenUDID() {
        if (mReceivedOpenUDIDs.isEmpty() == false) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final TreeMap<String, Integer> sorted_OpenUDIDS = new TreeMap(new ValueComparator());
            sorted_OpenUDIDS.putAll(mReceivedOpenUDIDs);

            openUDID = sorted_OpenUDIDS.firstKey();
        }
    }

    private static String openUDID = null;
    private static boolean mInitialized = false;

    /**
     * The Method to call to get OpenUDID
     * 
     * @return the OpenUDID
     */
    public static String getOpenUDID() {
        if (!mInitialized)
            Log.e("OpenUDID", "Initialisation isn't done");
        return openUDID;
    }

    /**
     * The Method to call to get OpenUDID
     * 
     * @return the OpenUDID
     */
    public static boolean isInitialized() {
        return mInitialized;
    }

    /**
     * The Method the call at the init of your app
     * 
     * @param context you current context
     */
    public static void sync(Context context) {
        // Initialise the Manager
        OpenUDIDManager manager = new OpenUDIDManager(context);

        // Try to get the openudid from local preferences
        openUDID = manager.mPreferences.getString(PREF_KEY, null);
        // Not found
        if (openUDID == null) {
            // Get the list of all OpenUDID services available (including
            // itself)
            manager.mMatchingIntents = context.getPackageManager().queryIntentServices(
                    new Intent("org.OpenUDID.GETUDID"), 0);
            if (LOG && manager.mMatchingIntents != null) {
                Log.d(TAG, manager.mMatchingIntents.size() + " services matches OpenUDID");
            }

            if (manager.mMatchingIntents != null)
                // Start services one by one
                manager.startService();

        } else {
            // Got it, you can now call getOpenUDID()
            if (LOG)
                Log.d(TAG, "OpenUDID: " + openUDID);
            mInitialized = true;
        }
    }

    /*
     * Used to sort the OpenUDIDs collected by occurrence
     */
    @SuppressWarnings("rawtypes")
    private class ValueComparator implements Comparator {
        public int compare(Object a, Object b) {

            if (mReceivedOpenUDIDs.get(a) < mReceivedOpenUDIDs.get(b)) {
                return 1;
            } else if (mReceivedOpenUDIDs.get(a).equals(mReceivedOpenUDIDs.get(b))) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
