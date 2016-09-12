package com.faknav.accelerometer_bt;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;

/////Switch
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

//ImageButton

import android.widget.ImageButton;

///////Bluetooth
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ListView;
import android.widget.ArrayAdapter;


//Coneccion BT
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

//OTRO


import java.io.IOException;

import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class BT_Conn extends Activity {
    TextView textX, textY, textZ, text;
    SensorManager sensorManager;
    Sensor accelerometer;
    Vibrator v;
    float vibrateThreshold = 7;

    float value;

    Switch mySwitch;

    //Memeber Fields
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String newAddress = null;
    public String newName = null;


    private static final int REQUEST_ENABLE_BT = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer__bt);

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        text = (TextView) findViewById(R.id.text);

        //configuro el switch ON
        mySwitch.setChecked(true);
        //INICIALIZO BT
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            text.setText("BT NO SOPORTADO");
            Toast.makeText(getApplicationContext(), "Tu Dispositivo no tiene soporte para Bluetooth", Toast.LENGTH_LONG).show();
        } else {

            //interrupcion a cambio de estado
            mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            });
        }


        /////////////ACELEROMETRO///////////////
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // es verdadero si tengo acelerometro

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accelListener, accelerometer, 10000000);
        } else {
            // no tengo acelerometro
        }

        //incializa la vibracion
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);
    }


    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc

        Bundle extras= getIntent().getExtras();
        if(extras !=null) {
            newAddress = extras.getString("Address");
            newName = extras.getString("Name");
        }

        // Set up a pointer to the remote device using its address.
        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        //Attempt to create a bluetooth socket for comms
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try
        {
            btSocket.connect();
            text.setText("Conectado a " + newName);
            text.setTextColor(Color.GREEN);

        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can talk to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }
        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure

    }

    //onPause() desregistro el acelerometro al estar en pausa, disminuye consumos
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelListener);

        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted

        //Close BT socket to device
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            String s = String.format("%.3f", y);

            sendData('*' + s );

            textX.setText(String.format("%.3f", x));
            textY.setText(String.format("%.3f", y));
            textZ.setText(String.format("%.3f", z));

            if (Math.abs(y) > vibrateThreshold) {
                v.vibrate(1000);
            }
        }
    };

}