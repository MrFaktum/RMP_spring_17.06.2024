package com.example.pc_control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
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

    private AudioManager audioManager;
    private TextView touchPad;
    private String selectedPcIp;
    private int serverPort = 12345;
    private static final String TAG = "Connect_page";
    private DatabaseHelper databaseHelper;
    private EditText ipAddressEditText;
    private ListView computersListView;
    private ArrayAdapter<String> adapter;
    private List<String> computersList;
    private Button control_settings;
    private int sensitivity = 3;
    private float lastX = 0;
    private float lastY = 0;
    private boolean isLongPress = false;
    private Handler longPressHandler = new Handler();
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            sendCommand("RIGHT_CLICK");
            isLongPress = true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_page);

        Button disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Connect_page.this, Home.class);
                startActivity(intent);
            }
        });


        Button pauseButton = findViewById(R.id.pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("PLAY_PAUSE");
            }
        });

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("PREVIOUS");
            }
        });

        Button forwardButton = findViewById(R.id.button_forward);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("NEXT");
            }
        });

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        selectedPcIp = getIntent().getStringExtra("IP_ADDRESS");
        Toast.makeText(Connect_page.this, selectedPcIp, Toast.LENGTH_SHORT).show();
        touchPad = findViewById(R.id.touch_pad);

        SeekBar seekBarSound = findViewById(R.id.seekBar_Sound);
        seekBarSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    String command = "VOLUME:" + (progress / 100.0f);
                    sendCommand(command);
                } catch (Exception e) {
                    Log.e("MainActivity", "Ошибка отправки данных: " + e.getMessage());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        touchPad.setOnTouchListener(new View.OnTouchListener() {
            private float lastX = 0;
            private float lastY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = (event.getX() - lastX) * sensitivity;
                        float deltaY = (event.getY() - lastY) * sensitivity;
                        lastX = event.getX();
                        lastY = event.getY();
                        sendCommand("MOVE:" + deltaX + ":" + deltaY);
                        break;
                }
                return true;
            }
        });

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Connect_page.this, "Левая кнопка нажата", Toast.LENGTH_SHORT).show();
                sendCommand("LEFT_CLICK");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Connect_page.this, "Правая кнопка нажата", Toast.LENGTH_SHORT).show();
                sendCommand("RIGHT_CLICK");
            }
        });

        Button openPopupButton = findViewById(R.id.button_keyboard);
        openPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        control_settings = findViewById(R.id.button_gear);
        control_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showControlSettingsPopup(v);
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

    private void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.keyboard, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Setup keyboard buttons
        setupKeyboardButtons(popupView);
    }

    private void setupKeyboardButtons(View popupView) {
        int[] buttonIds = {
                R.id.n1, R.id.n2, R.id.n3, R.id.n4, R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9, R.id.n0,
                R.id.q, R.id.w, R.id.e, R.id.r, R.id.t, R.id.y, R.id.u, R.id.i, R.id.o, R.id.p,
                R.id.a, R.id.s, R.id.d, R.id.f, R.id.g, R.id.h, R.id.j, R.id.k, R.id.l,
                R.id.z, R.id.x, R.id.c, R.id.v, R.id.b, R.id.n, R.id.m, R.id.buttonEnter, R.id.buttonBackspace, R.id.buttonSpace
        };

        for (int id : buttonIds) {
            Button button = popupView.findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendCommand("KEY:" + button.getText().toString());
                }
            });
        }
    }

    private void showControlSettingsPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_menu_mouse_sensitivity_slider, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        SeekBar sensitivitySeekBar = popupView.findViewById(R.id.sensitivitySeekBar);
        sensitivitySeekBar.setProgress(sensitivity - 1); // Set initial progress based on current sensitivity
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivity = progress + 1; // Update sensitivity (1 to 10)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
