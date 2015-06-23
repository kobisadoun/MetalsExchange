package com.kobi.metalsexchange.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MetalsExchangeSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MetalsExchangeSyncAdapter sMetalsExchangeSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("MetalsExchangeSyncSvc", "onCreate - MetalsExchangeSyncService");
        synchronized (sSyncAdapterLock) {
            if (sMetalsExchangeSyncAdapter == null) {
                sMetalsExchangeSyncAdapter = new MetalsExchangeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMetalsExchangeSyncAdapter.getSyncAdapterBinder();
    }
}