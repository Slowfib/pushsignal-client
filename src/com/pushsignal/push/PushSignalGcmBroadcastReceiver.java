package com.pushsignal.push;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class PushSignalGcmBroadcastReceiver extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(final Context context) {
		return PushClientGcmImpl.class.getName();
	}
}
