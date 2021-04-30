/**
 * MainActivity for Argon Door Alarm.
 * @author Robbie Flockhart
 */
package com.example.argondooralarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity {

    boolean alarmStatus = false;
    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        ParticleCloudSDK.init(this);

        particleLogin();
        setAlarmStatus();
        particleSubscribe();
        armClick();
    }
    //sets alarm status and UI on app start-up depending on the status of the Particle Argon Door Alarm
    public void setAlarmStatus(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    ParticleDevice myDevice = ParticleCloudSDK.getCloud().getDevice("e00fce684e8083453f49b051");
                    int armed = myDevice.getIntVariable("armedInt"); //gets real-time variable from Particle Argon
                    if(armed == 0){
                        //main thread
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                alarmStatus = false;
                                uIRefresh(false);
                            }
                        });
                    }
                    else if(armed == 1){
                        //main thread
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                alarmStatus = true;
                                uIRefresh(true);
                            }
                        });
                    }

                    int notify = myDevice.getIntVariable("notify"); //gets real-time variable from Particle Argon

                     if(notify > 0){
                         //main thread
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                alarmStatus = true;
                                TextView statusTxt = (TextView)findViewById(R.id.statusTxt);
                                statusTxt.setText("ALARM TRIPPED");
                                statusTxt.setTextColor(Color.parseColor("#F44336"));;
                            }
                        });
                    }
                    return "Alarm Status Updated";
                }
                catch(ParticleCloudException | IOException | ParticleDevice.VariableDoesNotExistException e) {
                    Log.e(TAG, "Error Receiving Status: " + e.toString());
                    return "Error Receiving Status";
                }
            }

            protected void onPostExecute(String msg) {
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }
    //changes the UI depending on the status of the alarm
    public void uIRefresh(boolean alarmStatus){
        Button armBTN = (Button)findViewById(R.id.armBTN);
        TextView statusTxt = (TextView)findViewById(R.id.statusTxt);

        statusTxt.setVisibility(View.VISIBLE);

        if (alarmStatus == false)
        {
            armBTN.setVisibility(View.VISIBLE);
            statusTxt.setText("DISARMED");
            statusTxt.setTextColor(Color.parseColor("#FFFF9800"));
        }
        else if (alarmStatus == true)
        {
            armBTN.setVisibility(View.INVISIBLE);
            statusTxt.setText("ARMED");
            statusTxt.setTextColor(Color.parseColor("#4CAF50"));
        }
    }
    //logs into the Particle Cloud
    public void particleLogin(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    // Log in to Particle Cloud using username and password
                    ParticleCloudSDK.getCloud().logIn("robb20@hotmail.co.uk", "sykcuk-megfy0-hErqys");
                    return "Logged in!";
                }
                catch(ParticleCloudException e) {
                    Log.e(TAG, "Error logging in: " + e.toString());
                    return "Error logging in!";
                }
            }

            protected void onPostExecute(String msg) {
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }
    //on click listener for the 'Arm' button - publishes event to Particle Cloud, changes alarm status and updates the UI
    public void armClick(){
        Button armBTN = (Button)findViewById(R.id.armBTN);
        armBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        try {
                            ParticleCloudSDK.getCloud().publishEvent("Arm_Alarm", "true", ParticleEventVisibility.PRIVATE, 60);
                            return "Door Alarm Armed";
                        }
                        catch(ParticleCloudException e) {
                            Log.e(TAG, "Error Arming Alarm: " + e.toString());
                            return "Error Arming Alarm!";
                        }
                    }

                    protected void onPostExecute(String msg) {
                        Toaster.s(MainActivity.this, msg);
                    }
                }.execute();

                alarmStatus = true;
                uIRefresh(true);

            }
        });
    }
    //subscribes to any events that are published to the Particle Cloud from the device with ID 'e00fce684e8083453f49b051'
    public void particleSubscribe(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    // Subscribe to an event
                    long subscriptionId = ParticleCloudSDK.getCloud().subscribeToDeviceEvents(
                            null, "e00fce684e8083453f49b051",
                            new ParticleEventHandler() {
                                // Trigger this function when the event is received
                                public void onEvent(String eventName, ParticleEvent event) {
                                    Toaster.s(MainActivity.this,
                                            "Example event happened!");
                                    Log.i("some tag", "Received event with payload: " + event.dataPayload);
                                    //if event published with name 'Disarm_Alarm' change alarm status and update UI
                                    if (eventName.equals("Disarm_Alarm")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
                                        //main thread
                                        runOnUiThread(new Runnable(){
                                            @Override
                                            public void run(){
                                                alarmStatus = false;
                                                uIRefresh(false);
                                            }
                                        });
                                    }
                                    //if event published with name 'Alarm_Tripped' update UI and call the first notification
                                    if (eventName.equals("Alarm_Tripped")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
                                        //main thread
                                        runOnUiThread(new Runnable(){
                                            @Override
                                            public void run(){
                                                TextView statusTxt = (TextView)findViewById(R.id.statusTxt);
                                                statusTxt.setText("ALARM TRIPPED");
                                                statusTxt.setTextColor(Color.parseColor("#F44336"));
                                                notification1();
                                            }
                                        });
                                    }
                                    //if event published with name 'No_Code' call the second notification
                                    if (eventName.equals("No_Code")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
                                        //main thread
                                        runOnUiThread(new Runnable(){
                                            @Override
                                            public void run(){
                                                notification2();
                                            }
                                        });
                                    }
                                }

                                public void onEventError(Exception e) {
                                    Log.e(TAG, "Event error: ", e);
                                }
                            });
                    return "Subscribed!";
                }
                catch(IOException e) {
                    Log.e(TAG, e.toString());
                    return "Error subscribing!";
                }
            }

            protected void onPostExecute(String msg) {
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }
    //creates notification for when alarm is tripped
    public void notification1(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Argon Door Alarm")
                .setContentText("Alarm has been tripped!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(100, builder.build());

    }
    //create notification for when no passcode is entered
    public void notification2(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Argon Door Alarm")
                .setContentText("No code entered!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(101, builder.build());

    }
    //creates a notification channel with ID: 'Channel1'
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel1";
            String description = "Particle Cloud subscribed events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel1", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}