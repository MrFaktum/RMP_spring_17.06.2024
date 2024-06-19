package com.example.pc_control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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

        Button openPopupButton = findViewById(R.id.button_keyboard);
        openPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
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
        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.keyboard, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // Show the PopupWindow
        popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0);

        // Set up the buttons in the PopupWindow
        // Кнопка Q
        Button q = popupView.findViewById(R.id.q);
        q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("q");
            }
        });
        // Кнопка W
        Button w = popupView.findViewById(R.id.w);
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("w");
            }
        });
        // Кнопка E
        Button e = popupView.findViewById(R.id.e);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("e");
            }
        });
        // Кнопка R
        Button r = popupView.findViewById(R.id.r);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("r");
            }
        });
        // Кнопка T
        Button t = popupView.findViewById(R.id.t);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("t");
            }
        });
        // Кнопка Y
        Button y = popupView.findViewById(R.id.y);
        y.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("y");
            }
        });
        // Кнопка U
        Button u = popupView.findViewById(R.id.u);
        u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("u");
            }
        });
        // Кнопка I
        Button i = popupView.findViewById(R.id.i);
        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("i");
            }
        });
        // Кнопка O
        Button o = popupView.findViewById(R.id.o);
        o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("o");
            }
        });
        // Кнопка P
        Button p = popupView.findViewById(R.id.p);
        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("p");
            }
        });
        // Кнопка A
        Button a = popupView.findViewById(R.id.a);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("a");
            }
        });
        // Кнопка S
        Button s = popupView.findViewById(R.id.s);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("s");
            }
        });
        // Кнопка D
        Button d = popupView.findViewById(R.id.d);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("d");
            }
        });
        // Кнопка F
        Button f = popupView.findViewById(R.id.f);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("f");
            }
        });
        // Кнопка G
        Button g = popupView.findViewById(R.id.g);
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("g");
            }
        });
        // Кнопка H
        Button h = popupView.findViewById(R.id.h);
        h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("h");
            }
        });
        // Кнопка J
        Button j = popupView.findViewById(R.id.j);
        j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("j");
            }
        });
        // Кнопка K
        Button k = popupView.findViewById(R.id.k);
        k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("k");
            }
        });
        // Кнопка L
        Button l = popupView.findViewById(R.id.l);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("l");
            }
        });
        // Кнопка Z
        Button z = popupView.findViewById(R.id.z);
        z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("z");
            }
        });
        // Кнопка X
        Button x = popupView.findViewById(R.id.x);
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("x");
            }
        });
        // Кнопка C
        Button c = popupView.findViewById(R.id.c);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("c");
            }
        });
        // Кнопка V
        Button v = popupView.findViewById(R.id.v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("v");
            }
        });
        // Кнопка B
        Button b = popupView.findViewById(R.id.b);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("b");
            }
        });
        // Кнопка N
        Button n = popupView.findViewById(R.id.n);
        n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("n");
            }
        });
        // Кнопка M
        Button m = popupView.findViewById(R.id.m);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("m");
            }
        });

        // Кнопка 1
        Button button1 = popupView.findViewById(R.id.n1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("1");
            }
        });
        // Кнопка 2
        Button button2 = popupView.findViewById(R.id.n2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("2");
            }
        });
        // Кнопка 3
        Button button3 = popupView.findViewById(R.id.n3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("3");
            }
        });
        // Кнопка 4
        Button button4 = popupView.findViewById(R.id.n4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("4");
            }
        });
        // Кнопка 5
        Button button5 = popupView.findViewById(R.id.n5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("5");
            }
        });
        // Кнопка 6
        Button button6 = popupView.findViewById(R.id.n6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("6");
            }
        });
        // Кнопка 7
        Button button7 = popupView.findViewById(R.id.n7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("7");
            }
        });
        // Кнопка 8
        Button button8 = popupView.findViewById(R.id.n8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("8");
            }
        });
        // Кнопка 9
        Button button9 = popupView.findViewById(R.id.n9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("9");
            }
        });
        // Кнопка 0
        Button button0 = popupView.findViewById(R.id.n0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("0");
            }
        });

        // Кнопка Enter
        Button buttonEnter = popupView.findViewById(R.id.buttonEnter);
        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("\n");
            }
        });
        // Кнопка Backspace
        Button buttonBackspace = popupView.findViewById(R.id.buttonBackspace);
        buttonBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("\b");
            }
        });
        // Кнопка Space
        Button buttonSpace = popupView.findViewById(R.id.buttonSpace);
        buttonSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand(" ");
            }
        });
    }
}
