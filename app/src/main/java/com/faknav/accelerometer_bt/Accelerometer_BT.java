package com.faknav.accelerometer_bt;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.widget.TextView;


/////Switch
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

///////Bluetooth
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;
import android.content.Intent;


public class Accelerometer_BT extends Activity {
    TextView textX, textY, textZ, text;
    SensorManager sensorManager;
    Sensor accelerometer;
    Vibrator v;
    float vibrateThreshold = 5;

    Switch mySwitch;

     BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer__bt);

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        text = (TextView) findViewById(R.id.text);

        //set the switch to ON
        mySwitch.setChecked(false);
        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            text.setText("Status: not supported");
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        }

        else {   //attach a listener to check for changes in state
            mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        text.setText("Estado: Activado");
                        if (!myBluetoothAdapter.isEnabled()) {
                            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
                        } else
                        {
                            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_LONG).show();
                        }


                    }

                    else{
                        myBluetoothAdapter.disable();
                        text.setText("Estado: Desactivado");

                        Toast.makeText(getApplicationContext(),"Bluetooth turned off", Toast.LENGTH_LONG).show();

                    }

                }
            });
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accelListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);
    }


    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelListener);
    }

    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText(String.format ("%.3f", x));
            textY.setText(String.format ("%.3f", y));
            textZ.setText(String.format ("%.3f", z));

            if ( Math.abs(y) > vibrateThreshold) {
                v.vibrate(100);}


        }

    };
}