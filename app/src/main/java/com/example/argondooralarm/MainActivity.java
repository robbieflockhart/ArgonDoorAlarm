package com.example.argondooralarm;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ParticleCloudSDK.init(this);
        setContentView(R.layout.activity_main);

        Button armBTN = (Button)findViewById(R.id.armBTN);
        Button disarmBTN = (Button)findViewById(R.id.disarmBTN);
        TextView statusTxt = (TextView)findViewById(R.id.statusTxt);
        boolean alarmStatus = true;

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

        armBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                armBTN.setVisibility(View.INVISIBLE);
                disarmBTN.setVisibility(View.VISIBLE);
                statusTxt.setText("ARMED");
                statusTxt.setTextColor(Color.parseColor("#4CAF50"));

            }
        });
        disarmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                armBTN.setVisibility(View.VISIBLE);
                disarmBTN.setVisibility(View.INVISIBLE);
                statusTxt.setText("DISARMED");
                statusTxt.setTextColor(Color.parseColor("#F44336"));
            }
        });
        /*Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Integer>() {

            public Integer callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn("ido@particle.io", "myl33tp4ssw0rd");
                return 1;
            }

            @Override
            public void onSuccess(Integer value) {
                Toaster.s(MainActivity.this, "Room temp is " + value + " degrees.");
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Log.e("some tag", "Something went wrong making an SDK call: ", e);
                Toaster.l(MainActivity.this, "Uh oh, something went wrong.");
            }
        });*/
    }
}