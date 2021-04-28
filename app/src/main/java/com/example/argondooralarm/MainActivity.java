package com.example.argondooralarm;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
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

    boolean alarmStatus = true;
    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParticleCloudSDK.init(this);

        Button armBTN = (Button)findViewById(R.id.armBTN);
        Button disarmBTN = (Button)findViewById(R.id.disarmBTN);
        TextView statusTxt = (TextView)findViewById(R.id.statusTxt);

        alarmStatus(alarmStatus);

        particleLogin();


        /*new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    // Subscribe to an event
                    long subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents(
                            null,  // Subscribe to events containing "example"
                            new ParticleEventHandler() {
                                // Trigger this function when the event is received
                                public void onEvent(String eventName, ParticleEvent event) {
                                    Toaster.s(MainActivity.this,
                                            "Example event happened!");
                                    /*if (eventName.equals("Arm_Alarm")){
                                        alarmStatus = true;
                                        alarmStatus(true);
                                    }
                                    //if (eventName.equals("Disarm_Alarm")){
                                        alarmStatus = false;
                                        alarmStatus(false);
                                        System.out.println("Event xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                                   // }
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
        }.execute();*/

        armClick();
        disarmClick();
    }

    public void alarmStatus(boolean alarmStatus){
        Button armBTN = (Button)findViewById(R.id.armBTN);
        Button disarmBTN = (Button)findViewById(R.id.disarmBTN);
        TextView statusTxt = (TextView)findViewById(R.id.statusTxt);

        if (alarmStatus == false)
        {
            armBTN.setVisibility(View.VISIBLE);
            disarmBTN.setVisibility(View.INVISIBLE);
            statusTxt.setText("DISARMED");
            statusTxt.setTextColor(Color.parseColor("#F44336"));
        }
        else if (alarmStatus == true)
        {
            armBTN.setVisibility(View.INVISIBLE);
            disarmBTN.setVisibility(View.VISIBLE);
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
                alarmStatus(true);

            }
        });
    }

    public void disarmClick(){
        Button disarmBTN = (Button)findViewById(R.id.disarmBTN);
        disarmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmStatus = false;
                alarmStatus(false);
            }
        });
    }

}