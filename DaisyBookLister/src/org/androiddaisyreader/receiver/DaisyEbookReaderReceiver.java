package org.androiddaisyreader.receiver;

import org.androiddaisyreader.service.DaisyEbookReaderService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DaisyEbookReaderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			Intent serviceIntent = new Intent(context, DaisyEbookReaderService.class);
			context.startService(serviceIntent);
		}
	}
}
