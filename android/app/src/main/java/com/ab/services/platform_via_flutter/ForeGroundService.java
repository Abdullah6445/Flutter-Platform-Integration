package com.ab.services.platform_via_flutter;//package com.ab.services.platform_via_flutter;
//
//public class ForeGroundService {
//}





import static java.lang.System.err;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.ab.services.platform_via_flutter.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ForeGroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private int count = 0;
    String response;
    private Timer timer;
    boolean serverIsOn = false;


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Create notification channel for Android O and above
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getNotification()); // Start service as foreground
        startTimer(); // Start timer
        return START_STICKY; // Ensures service is restarted if killed by the system
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer(); // Stop timer when service is destroyed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't bind to this service
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // JSONPlaceholder API URL
                    String apiUrl = "https://jsonplaceholder.typicode.com/users";

                    System.out.println("<<<===== Api Hit Successfully =====>>>");

                    // Hit the API and get the response
                    response = hitApi(apiUrl);
                    System.out.println("API Response: " + response);

                    // Call your custom method to update notifications
//                    updateNotification();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 1000, 10000); // Schedule to run every second
    }

    // Function to hit the API and return the response as a string

    private String hitApi(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        // Check response code
        int status = connection.getResponseCode();

        if (status != 200) {
            System.out.println("<<<==========>>>");
            System.out.println("<<<===== server ruk gya =====>>>");
            System.out.println("<<<==========>>>");

            serverIsOn = false; // Update server status
            updateNotification(); // Update the notification
            System.out.println("<<<===== Notification Updated =====>>>");

            // Stop further processing and throw the exception
            throw new RuntimeException("Failed : HTTP error code : " + status);
        }

        // If the status is 200, continue with the response
        serverIsOn = true; // Update server status

        System.out.println("<<<==========>>>");
        System.out.println("<<<===== server nhi ruka =====>>>");
        System.out.println("<<<==========>>>");
        updateNotification();
        System.out.println("<<<===== else k bad b chal rha =====>>>");

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel(); // Cancel the timer task
            timer = null;   // Nullify the timer
        }
    }

    private void updateNotification() {

        int counttt = count++;

        System.out.println("<<<==========>>>");
        System.out.println("<<<===== update notification chal rha =====>>>");
        System.out.println("<<<==========>>>");

        // Create notification with updated count
        Notification notification = new NotificationCompat.Builder(this, "SERVICE_CHANNEL")
                .setContentTitle(serverIsOn ? "server is on" : "server is off")
                .setContentText("Timer: " + counttt + "\n" + response) // Increment count with each update
                .setSmallIcon(R.drawable.launch_background)
                .build();

        // Update notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);

        // Broadcast the updated count to MainActivity
        Intent intent = new Intent("com.ab.services.TIMER_UPDATED");
        intent.putExtra("count", counttt);
        intent.putExtra("response" ,response );


        sendBroadcast(intent);
    }

    private Notification getNotification() {
        // Initial notification when service starts
        return new NotificationCompat.Builder(this, "SERVICE_CHANNEL")
                .setContentTitle("Timer Service Running")
                .setContentText("Timer is running in the background.")
                .setSmallIcon(R.drawable.launch_background)
                .build();
    }

    private void createNotificationChannel() {
        // Create notification channel for devices running Android O or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "SERVICE_CHANNEL",
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
