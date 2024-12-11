//package com.ab.services.platform_via_flutter;
//
//import io.flutter.embedding.android.FlutterActivity;
//
//public class MainActivity extends FlutterActivity {
//}




package com.ab.services.platform_via_flutter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    private BroadcastReceiver timerReceiver;
    private static final String CHANNEL = "com.ab.services/native";
    private static final String TIMER_UPDATED_ACTION = "com.ab.services.TIMER_UPDATED";
    MethodChannel methodChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Register the BroadcastReceiver for timer updates
        timerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int count = intent.getIntExtra("count", 0);
                String res = intent.getStringExtra("response");

                sendCountToFlutter(count); // Send count to Flutter using MethodChannel
                sendApiResponseToFlutter(res);

            }
        };
        registerReceiver(timerReceiver, new IntentFilter(TIMER_UPDATED_ACTION));

        // Start the foreground service
//        Intent serviceIntent = new Intent(this, ForeGroundService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent);
//        } else {
//            startService(serviceIntent);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        if (timerReceiver != null) {
            unregisterReceiver(timerReceiver);
        }
    }



        @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
            methodChannel   = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);


            methodChannel.setMethodCallHandler((call, result) -> {
                    if (call.method.equals("startService")) {
                        startNativeService();
                        result.success("Service started");
                    } else {
                        result.notImplemented();
                    }
                });
    }

    // Method to send updated count to Flutter
    private void sendCountToFlutter(int count) {
        methodChannel.invokeMethod("updateCount", count);
    }

    void sendApiResponseToFlutter(String resp){
        methodChannel.invokeMethod("updateResponse",resp);
    }


    // Method to start the foreground service from Flutter
    private void startNativeService() {
        Intent serviceIntent = new Intent(this, ForeGroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
