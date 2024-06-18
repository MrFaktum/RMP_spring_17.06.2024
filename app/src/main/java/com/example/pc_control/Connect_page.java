package com.example.pc_control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Connect_page extends AppCompatActivity {

    private TextView touchPad;
    private String selectedPcIp;
    private int serverPort = 12345;
    private static final String TAG = "Connect_page";
    private DatabaseHelper databaseHelper;
    private EditText ipAddressEditText;
    private ListView computersListView;
    private ArrayAdapter<String> adapter;
    private List<String> computersList;

    private float lastX = 0;
    private float lastY = 0;
    private boolean isLongPress = false;
    private Handler longPressHandler = new Handler();
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            // Отправляем команду на правый клик, так как удержание было достаточно долгим
            sendCommand("RIGHT_CLICK");
            isLongPress = true; // Помечаем, что удержание было долгим
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_page);

        selectedPcIp = getIntent().getStringExtra("IP_ADDRESS");
        Toast.makeText(Connect_page.this, selectedPcIp, Toast.LENGTH_SHORT).show();
        touchPad = findViewById(R.id.touch_pad);

        touchPad.setOnTouchListener(new View.OnTouchListener() {
            private boolean isRightClicking = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getEventTime() - event.getDownTime() > 1500) {
                            // Long press detected, simulate right click
                            isRightClicking = true;
                            sendCommand("RIGHT_CLICK");
                        } else if ((event.getEventTime() - event.getDownTime() < 1500) || (event.getEventTime() - event.getDownTime() > 500)) {
                            // Short press detected, simulate left click
                            sendCommand("LEFT_CLICK");
                        }
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - lastX;
                        float deltaY = event.getY() - lastY;
                        lastX = event.getX();
                        lastY = event.getY();
                        sendCommand("MOVE:" + deltaX + ":" + deltaY);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isRightClicking) {
                            isRightClicking = false;
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void sendCommand(final String command) {
        if (selectedPcIp != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket(selectedPcIp, serverPort);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(command.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        socket.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending command: " + e.getMessage(), e);
                    }
                }
            }).start();
        } else {
            Log.e(TAG, "No PC selected");
        }
    }
}
