package com.example.remote_command_control;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final String ip_address = "192.168.1.21";
    private final int port = 4444;
    private final DataList dataList = new DataList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Runnable getCommandList = new Runnable() {
            @Override
            public void run() {
                getCommandList();
            }
        };

        Thread getCommandListThread = new Thread(getCommandList);
        getCommandListThread.start();

        /*List<String> commandList = getCommandList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commandList);
        ListView listView = findViewById(R.id.commandList);
        listView.setAdapter(adapter);
         */
    }

    protected void getCommandList() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            // Listen for incoming data.
            Runnable getTask = () -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(dataList.android_port);
                    System.out.println("Listening for client from " + serverSocket.getInetAddress().getHostAddress());
                    while (dataList.commandList.isEmpty()) {
                        Socket connected = serverSocket.accept();
                        System.out.println("Device found " + connected.getLocalAddress().getHostAddress());

                        DataInputStream dis = new DataInputStream(new BufferedInputStream(connected.getInputStream()));
                        JSONObject jsonObject = new JSONObject(dis.readUTF());
                        System.out.println(jsonObject);
                    }
                } catch (IOException | JSONException e) {
                    System.out.println(e.getMessage());
                }
            };

            // New Thread
            System.out.println("Submitting task...");
            executorService.submit(getTask);

            System.out.println("Sending data...");
            InetAddress computer = InetAddress.getByName("192.168.1.21");
            Socket connection = new Socket(computer, 4444);
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            // Send command to the client.
            dos.write("get command list".getBytes(StandardCharsets.UTF_8));
            dos.flush();
            System.out.println("Done");
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }
}