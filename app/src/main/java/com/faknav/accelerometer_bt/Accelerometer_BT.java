package com.faknav.accelerometer_bt;


import android.content.IntentFilter;
import android.graphics.Color;
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
import android.view.Menu;

///////Bluetooth
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ListView;
import android.widget.ArrayAdapter;


public class Accelerometer_BT extends Activity {
    TextView textX, textY, textZ, text;
    SensorManager sensorManager;
    Sensor accelerometer;
    Vibrator v;
    float vibrateThreshold = 5;

    Switch mySwitch;
    ListView myListView;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;

    private static final int REQUEST_ENABLE_BT = 1;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer__bt);

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        text = (TextView) findViewById(R.id.text);

        //set the switch to OFF
        mySwitch.setChecked(false);
        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            text.setText("Status: not supported");
            Toast.makeText(getApplicationContext(),"Tu Dispositivo no tiene soporte para Bluetooth", Toast.LENGTH_LONG).show();
        }

        else {   //attach a listener to check for changes in state
            mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        ON();
                        FIND();
                        text.setText("Activado");
                        text.setTextColor(Color.GREEN);
                    }
                    else{
                        OFF();
                        text.setText("Desactivado");
                        text.setTextColor(Color.RED);
                        BTArrayAdapter.clear();
                    }
                }
            });
        }

        myListView = (ListView)findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        BTArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_fak);
        myListView.setAdapter(BTArrayAdapter);

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

    public void ON(){


        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        Toast.makeText(getApplicationContext(), "Bluetooth Encendido", Toast.LENGTH_LONG).show();
    }

    public void OFF(){
        myBluetoothAdapter.disable();

        Toast.makeText(getApplicationContext(),"Bluetooth Apagado", Toast.LENGTH_LONG).show();
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };


    public void FIND() {

            myBluetoothAdapter.startDiscovery();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
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


