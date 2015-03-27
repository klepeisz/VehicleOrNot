package com.pannonegyetem.projekt.vehicleornot;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private static final int POLL_INTERVAL = 300; //constant

    private boolean mRunning = false; //running state, currently set to false

    private int mTreshold; //config state

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    private TextView mStatusView,noise;

    private DetectdB mSensor;

    private CameraActivity mCameraView;

    ProgressBar bar;

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            start();
        }
    };

    private void updateDisplay(String status, double signalEMA){
        mStatusView.setText(status);
        bar.setProgress((int)signalEMA);
        Log.d("SOUND", String.valueOf(signalEMA));
        noise.setText(signalEMA+" dB");
    }

    private void callForHelp(double signalEMA){
        Toast.makeText(getApplicationContext(),"Starting Camera, and detecting motion!", Toast.LENGTH_LONG).show();
        Log.d("SOUND", String.valueOf(signalEMA));
        noise.setText(signalEMA+" dB");
        stop();
        Intent cameraintent = new Intent(this,CameraActivity.class);
        startActivity(cameraintent);
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = mSensor.getAmplitude();
            updateDisplay("Monitoring dB...", amp);
            if ((amp > mTreshold)){
                callForHelp(amp);
            }
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusView = (TextView) findViewById(R.id.status);
        noise = (TextView) findViewById(R.id.noise);
        bar = (ProgressBar) findViewById(R.id.progressBar1);
        mSensor = new DetectdB();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"Noise Alert!");
    }

    private void initializeApplicationConstant(){
        mTreshold = 8;
    }

    @Override
    public void onResume(){
        super.onResume();
        initializeApplicationConstant();
        if (!mRunning){
            mRunning = true;
            start();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        stop();
    }

    private void start(){
        mSensor.start();
        if(!mWakeLock.isHeld()){
            mWakeLock.acquire();
        }
        mHandler.postDelayed(mPollTask,POLL_INTERVAL);
    }

    private void stop(){
        Log.i("Noise","Stop noise monitoring");
        if(mWakeLock.isHeld()){
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        bar.setProgress(0);
        updateDisplay("stopped",0.0);
        mRunning = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
