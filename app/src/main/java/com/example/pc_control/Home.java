package com.example.pc_control;
// Home.java
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.widget.PopupWindow;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";
    private DatabaseHelper dbHelper;
    private LinearLayout computerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        computerList = findViewById(R.id.computer_list);
        loadComputerList();
        Button addNewDevicesButton = findViewById(R.id.Add_new_devices);
        addNewDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Начало поиска", Toast.LENGTH_SHORT).show();
                startScanning();
            }
        });

        Button openPopupButton = findViewById(R.id.open_popup);
        openPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
    }

    private void showPopupWindow(View view) {
        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.home_menu, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // Show the PopupWindow
        popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0);

        // Set up the buttons in the PopupWindow
        Button button1 = popupView.findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                Toast.makeText(Home.this, "Кнопка 1 нажата", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        Button button2 = popupView.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                Toast.makeText(Home.this, "Кнопка 2 нажата", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        Button button3 = popupView.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                Toast.makeText(Home.this, "Кнопка 3 нажата", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
    }
    private void startScanning() {
        computerList.removeAllViews();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ipString = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        new NetworkScanner(ipString).execute();
    }

    private void updateAvailablePcsList(List<String> result) {
        for (String ipAddress : result) {
            Log.d(TAG, "Adding computer: " + ipAddress);
            dbHelper.addComputer(ipAddress, "Last connected time");
            addComputerToLayout(ipAddress);
        }
    }

    private void addComputerToLayout(final String ipAddress) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View computerItemView = inflater.inflate(R.layout.computer_item, computerList, false);

        TextView ipTextView = computerItemView.findViewById(R.id.ip_address);
        ipTextView.setText(ipAddress);

        computerItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Connect_page.class);
                intent.putExtra("IP_ADDRESS", ipAddress);
                startActivity(intent);
            }
        });

        computerList.addView(computerItemView);
    }

    private void loadComputerList() {
        List<String> computers = dbHelper.getAllComputers();
        for (String ip: computers) {
            addComputerToLayout(ip);
        }
    }

    public class NetworkScanner extends AsyncTask<Void, String, List<String>> {

        private static final int TIMEOUT = 500;
        private String localIpAddress;

        public NetworkScanner(String localIpAddress) {
            this.localIpAddress = localIpAddress;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> availableIps = new ArrayList<>();
            String subnet = getSubnetAddress(localIpAddress);
            ExecutorService executor = Executors.newFixedThreadPool(10);

            for (int i = 1; i < 255; i++) {
                final int j = i;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        String host = subnet + "." + j;
                        try {
                            InetAddress inetAddress = InetAddress.getByName(host);
                            if (inetAddress.isReachable(TIMEOUT)) {
                                synchronized (availableIps) {
                                    availableIps.add(host);
                                    publishProgress(host);
                                }
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error checking IP: " + host, e);
                        }
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            return availableIps;
        }

        private String getSubnetAddress(String ipAddress) {
            String[] parts = ipAddress.split("\\.");
            return parts[0] + "." + parts[1] + "." + parts[2];
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, "IP Found: " + values[0]);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            Toast.makeText(Home.this, "Поиск завершен", Toast.LENGTH_SHORT).show();
            updateAvailablePcsList(result);
        }
    }
}
