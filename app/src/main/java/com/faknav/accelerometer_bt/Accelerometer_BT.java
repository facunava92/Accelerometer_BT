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
import android.view.View;
import android.view.View.OnClickListener;
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


public class Accelerometer_BT extends Activity {
    TextView textX, textY, textZ, text;
    SensorManager sensorManager;
    Sensor accelerometer;
    Vibrator v;
    float vibrateThreshold = 7;

    Switch mySwitch;
    ListView myListView;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;
    ImageButton imageButton;


    private static final int REQUEST_ENABLE_BT = 1;
    static final int BT_CONN_REQUEST = 1;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer__bt);

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        text = (TextView) findViewById(R.id.text);

        //configuro el switch OFF
        mySwitch.setChecked(false);
        //inicializo el bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            text.setText("BT NO SOPORTADO");
            Toast.makeText(getApplicationContext(), "Tu Dispositivo no tiene soporte para Bluetooth", Toast.LENGTH_LONG).show();
        } else {

            //interrupcion a cambio de estado
            mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ON();
                        text.setText("Activado");
                        text.setTextColor(Color.GREEN);
                    } else {
                        OFF();
                        text.setText("Desactivado");
                        text.setTextColor(Color.RED);
                        BTArrayAdapter.clear();
                    }
                }
            });

            imageButton = (ImageButton) findViewById(R.id.imageButton1);
            imageButton.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    FIND(v);
                }
            });

            // arreglo q contiene a BTDevices
            BTArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_fak);
            myListView = (ListView) findViewById(R.id.listView1);
            myListView.setOnItemClickListener(mDeviceClickListener);
            myListView.setAdapter(BTArrayAdapter);
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // es verdadero si tengo acelerometro

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accelListener, accelerometer, 1000000);
        } else {
            // no tengo acelerometro
        }

        //incializa la vibracion
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);
    }

    public void ON() {

        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        Toast.makeText(getApplicationContext(), "Bluetooth Encendido", Toast.LENGTH_LONG).show();
    }

    public void OFF() {
        myBluetoothAdapter.disable();

        Toast.makeText(getApplicationContext(), "Bluetooth Apagado", Toast.LENGTH_LONG).show();
    }


    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Cuando discovery descubre un dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Obtiene BluetoothDevice del Intento
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Adhiere el nombre y MAC del BT encontrado
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void FIND(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // Si se vuelve a apretar el bonton se reinicia la busqueda
            myBluetoothAdapter.cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    //onResume() rregistro el acelerometro
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelListener, accelerometer, 1000000);


    }

    //onPause() desregistro el acelerometro al estar en pausa, disminuye consumos
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelListener);
    }

    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText(String.format("%.3f", x));
            textY.setText(String.format("%.3f", y));
            textZ.setText(String.format("%.3f", z));

            if (Math.abs(y) > vibrateThreshold) {
                v.vibrate(1000);
            }
        }
    };

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Get the device Name
            String aux = ((TextView) v).getText().toString();
            String name =  aux.substring(0,aux.length() - 17) ;

            text.setText("Conectando a " + name + address);
            text.setTextColor(Color.YELLOW);


            //Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent(Accelerometer_BT.this, BT_Conn.class);
            i.putExtra("Address", address);
            i.putExtra("Name", name);
            startActivityForResult(i,BT_CONN_REQUEST);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK)
        {
            text.setText("Activado");
            text.setTextColor(Color.GREEN);
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

        }
    }
}


