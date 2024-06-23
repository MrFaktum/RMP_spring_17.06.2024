package com.example.pc_control;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.DataInputStream;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Translatijn_page extends AppCompatActivity {

    private String selectedPcIp;
    private int serverPort = 12345;

    private Socket socket;
    private OutputStream outputStream;
    private ImageView imageView;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_translatijn_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.stream_view);
        selectedPcIp = getIntent().getStringExtra("IP_ADDRESS");
        Toast.makeText(Translatijn_page.this, selectedPcIp, Toast.LENGTH_SHORT).show();

        new ConnectTask().execute();

    }

    private class ConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(selectedPcIp, 12345);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                DataInputStream dis = new DataInputStream(inputStream);
                outputStream.write("SCREEN".getBytes());

                // Получаем кодек AVC
                MediaCodec codec = MediaCodec.createDecoderByType("video/avc");
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", 1280, 720);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, 30); // Частота кадров
                codec.configure(format, null, null, 0);
                codec.start();

                // Цикл получения и декодирования кадров
                while (true) {
                    int length = dis.readInt();
                    byte[] data = new byte[length];
                    dis.readFully(data);

                    // Декодируем кадр
                    ByteBuffer[] inputBuffers = codec.getInputBuffers();
                    ByteBuffer[] outputBuffers = codec.getOutputBuffers();
                    int inputBufferIndex = codec.dequeueInputBuffer(10000);
                    if (inputBufferIndex >= 0) {
                        inputBuffers[inputBufferIndex].clear();
                        inputBuffers[inputBufferIndex].put(data);
                        codec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                        // Здесь ты можешь отобразить декодированный кадр
                        // Используй `outputBuffer` для получения данных кадра
                        // Например:
                        Bitmap bitmap = BitmapFactory.decodeByteArray(outputBuffer.array(), 0, outputBuffer.capacity());
                        imageView.setImageBitmap(bitmap);

                        codec.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //  Этот метод вызывается после завершения  doInBackground.
            //  Здесь ты можешь выполнить действия, которые нужно сделать после получения всех данных.
        }
    }
}