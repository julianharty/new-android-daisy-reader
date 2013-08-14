package org.openUDID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;


/*
 * You have to add this in your manifest
 
<service android:name="org.OpenUDID.OpenUDID_service">
	<intent-filter>
		<action android:name="org.OpenUDID.GETUDID" />
	</intent-filter>
</service>

*/


public class OpenUDIDService extends Service{
	@Override
	public IBinder onBind(Intent arg0) {
		return new  Binder() {
			@Override
			public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) {
				final SharedPreferences preferences = getSharedPreferences(OpenUDIDManager.PREFS_NAME, Context.MODE_PRIVATE);
		
				reply.writeInt(data.readInt()); //Return to the sender the input random number
				reply.writeString(preferences.getString(OpenUDIDManager.PREF_KEY, null));
				return true;
			}
		};
	}
}
