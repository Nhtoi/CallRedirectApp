package com.example.callredirectapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) { // Call answered
            Toast.makeText(context, "Call Received", Toast.LENGTH_LONG).show();
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            Intent divertIntent = new Intent(context, CallDivertActivity.class);
            divertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(divertIntent);
        }
    }
}

