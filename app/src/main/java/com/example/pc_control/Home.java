package com.example.pc_control;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.SharedPreferences;
import android.widget.EditText;
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
    private Button addNewDevicesButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int themeId = sharedPreferences.getInt("theme", R.style.RedTheme); // По умолчанию - красная тема
        setTheme(themeId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        computerList = findViewById(R.id.computer_list);
        loadComputerList();
        addNewDevicesButton = findViewById(R.id.Add_new_devices);
        addNewDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Начало поиска", Toast.LENGTH_SHORT).show();
                addNewDevicesButton.setEnabled(false);
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
        Button red_theme_button = popupView.findViewById(R.id.red_theme_button);
        red_theme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                setThemeColor(R.color.red);
                Toast.makeText(Home.this, "Красная тема", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        // Set up the buttons in the PopupWindow
        Button green_theme_button = popupView.findViewById(R.id.green_theme_button);
        green_theme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                setThemeColor(R.color.green);
                Toast.makeText(Home.this, "Зеленая тема", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
        // Set up the buttons in the PopupWindow
        Button blue_theme_button = popupView.findViewById(R.id.blue_theme_button);
        blue_theme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                setThemeColor(R.color.blue);
                Toast.makeText(Home.this, "Синяя тема", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        Button delete_ip = popupView.findViewById(R.id.delete_ip);
        delete_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the database
                dbHelper.clearAllComputers();
                // Clear the list in the activity
                computerList.removeAllViews();
                Toast.makeText(Home.this, "Все компьютеры удалены", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        Button information = popupView.findViewById(R.id.information);
        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                Toast.makeText(Home.this, "Кнопка Информация о нас нажата", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
        Button support = popupView.findViewById(R.id.support);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
                Toast.makeText(Home.this, "Кнопка Поддержать нас нажата", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
    }

    private void setThemeColor(int colorResId) {
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (colorResId == R.color.red) {
            setTheme(R.style.RedTheme);
            editor.putInt("theme", R.style.RedTheme);
        } else if (colorResId == R.color.green) {
            setTheme(R.style.GreenTheme);
            editor.putInt("theme", R.style.GreenTheme);
        } else if (colorResId == R.color.blue) {
            setTheme(R.style.BlueTheme);
            editor.putInt("theme", R.style.BlueTheme);
        }

        editor.apply();
        recreate();  // Перезапуск активности для применения изменений темы
    }


    private void  startScanning() {
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
            String deviceName = ipAddress;
            Log.d(TAG, "Adding computer: " + deviceName);
            dbHelper.addComputer(ipAddress, ipAddress, "Last connected time");
            addComputerToLayout(deviceName, ipAddress);
        }
    }

    private void addComputerToLayout(final String deviceName, final String ipAddress) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View computerItemView = inflater.inflate(R.layout.computer_item, computerList, false);

        TextView ipTextView = computerItemView.findViewById(R.id.ip_address);
        ipTextView.setText(deviceName);

        Button gearButton = computerItemView.findViewById(R.id.gear_button);
        gearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceSettingsMenu(v, ipAddress);
            }
        });

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

    private void showDeviceSettingsMenu(View anchorView, final String ipAddress) {
        // Inflate the device_settings_menu layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_menu_device_settings, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // Set up the buttons in the PopupWindow
        Button renameDeviceButton = popupView.findViewById(R.id.rename);
        renameDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDeviceMenu(anchorView, ipAddress, popupWindow);
            }
        });

        Button removeDeviceButton = popupView.findViewById(R.id.remove_device);
        removeDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.removeComputer(ipAddress);
                loadComputerList(); // Refresh the list
                Toast.makeText(Home.this, "Removed device: " + ipAddress, Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });

        // Show the PopupWindow at the center of the screen
        popupWindow.showAtLocation(anchorView.getRootView(), android.view.Gravity.CENTER, 0, 0);
    }

    private void showRenameDeviceMenu(View anchorView, final String ipAddress, final PopupWindow parentPopup) {
        // Inflate the rename_device_menu layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.rename_device_menu, null);

        // Create the PopupWindow
        final PopupWindow renamePopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // Set up the input field and button in the PopupWindow
        EditText renameInput = popupView.findViewById(R.id.rename_input);
        Button confirmRenameButton = popupView.findViewById(R.id.confirm_rename);
        confirmRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = renameInput.getText().toString().trim();
                if (newName.length() <= 20 && !newName.isEmpty()) {
                    dbHelper.updateComputerName(ipAddress, newName);
                    loadComputerList(); // Refresh the list
                    Toast.makeText(Home.this, "Renamed device to: " + newName, Toast.LENGTH_SHORT).show();
                    renamePopupWindow.dismiss();
                    parentPopup.dismiss();
                } else {
                    Toast.makeText(Home.this, "Name must be 1-20 characters long", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show the PopupWindow at the center of the screen
        renamePopupWindow.showAtLocation(anchorView.getRootView(), android.view.Gravity.CENTER, 0, 0);
    }

    private void loadComputerList() {
        computerList.removeAllViews(); // Clear the list before loading
        List<String> computers = dbHelper.getAllComputers();
        for (String computer : computers) {
            String[] parts = computer.split(" - ");
            if (parts.length == 2) {
                addComputerToLayout(parts[0], parts[1]);
            }
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
            addNewDevicesButton.setEnabled(true);
        }
    }
}
