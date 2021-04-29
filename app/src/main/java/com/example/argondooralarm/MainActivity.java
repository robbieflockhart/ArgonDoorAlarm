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

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
//import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Async;
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

        //Button armBTN = (Button)findViewById(R.id.armBTN);
        //TextView statusTxt = (TextView)findViewById(R.id.statusTxt);
        //uIRefresh(alarmStatus);

        particleLogin();
        setAlarmStatus();
        particleSubscribe();
        armClick();
    }

    public void setAlarmStatus(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    ParticleDevice myDevice = ParticleCloudSDK.getCloud().getDevice("e00fce684e8083453f49b051");
                    int armed = myDevice.getIntVariable("armedInt");
                    if(armed == 0){
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                alarmStatus = false;
                                uIRefresh(false);
                            }
                        });
                    }
                    else if(armed == 1){
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                alarmStatus = true;
                                uIRefresh(true);
                            }
                        });
                    }

                    int notify = myDevice.getIntVariable("notify");

                     if(notify > 0){
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
                // Show Toast containing message from doInBackground
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }
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

    public void particleLogin(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                //  LOG IN TO PARTICLE
                try {
                    // Log in to Particle Cloud using username and password
                    ParticleCloudSDK.getCloud().logIn("robb20@hotmail.co.uk", "sykcuk-megfy0-hErqys");
                    ParticleDevice myDevice = ParticleCloudSDK.getCloud().getDevice("e00fce684e8083453f49b051");
                    return "Logged in!";
                }
                catch(ParticleCloudException e) {
                    Log.e(TAG, "Error logging in: " + e.toString());
                    return "Error logging in!";
                }
            }

            protected void onPostExecute(String msg) {
                // Show Toast containing message from doInBackground
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }

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
                        // Show Toast containing message from doInBackground
                        Toaster.s(MainActivity.this, msg);
                    }
                }.execute();

                alarmStatus = true;
                uIRefresh(true);

            }
        });
    }

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

                                    if (eventName.equals("Disarm_Alarm")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
                                        runOnUiThread(new Runnable(){
                                            @Override
                                            public void run(){
                                                alarmStatus = false;
                                                uIRefresh(false);
                                            }
                                        });
                                    }
                                    if (eventName.equals("Alarm_Tripped")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
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
                                    if (eventName.equals("No_Code")){
                                        System.out.println("-------------EVENT RECEIVED-------------");
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
                    // We end up here if there was an error subscribing
                    Log.e(TAG, e.toString());
                    return "Error subscribing!";
                }
            }

            // This code is run after the doInBackground code finishes
            protected void onPostExecute(String msg) {
                Toaster.s(MainActivity.this, msg);
            }
        }.execute();
    }

    public void notification1(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Argon Door Alarm")
                .setContentText("Alarm has been tripped!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(100, builder.build());

    }

    public void notification2(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_baseline_announcement_24)
                .setContentTitle("Argon Door Alarm")
                .setContentText("No code entered!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(101, builder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel1";
            String description = "Particle Cloud subscribed events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}