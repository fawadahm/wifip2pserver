package com.nsl.serverwifip2p;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * Created by fawadahmad on 9/28/16.
 */

public class FileServerAsyncTask extends Service {

    final String className = "FileServerAsyncTask";


    public static InetAddress groupOwnerAddress;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d (className, "onStartCommand");
        doInBackground();
        return super.onStartCommand(intent, flags, startId);


    }



    private void clientProcess ()
    {

            new Thread(new Runnable() {
                @Override
                public void run ()
                {
                    /**
                     Create a client socket and look for the server
                     */
                    Log.d(className, "Creating Client Socket ");

                    int portToConnectTo = MainActivity.portToConnectTo;
                    Socket clientSocket = new Socket();

                    try {
                        clientSocket.connect(new InetSocketAddress( groupOwnerAddress, portToConnectTo ),1000);
                        Log.d(className, "Connected to " + groupOwnerAddress + " on port " + portToConnectTo);
                    } catch (IOException e) {
                        Log.d (className, e.getMessage());
                    }


                    InputStream stream = new ByteArrayInputStream("Project Completed\n".getBytes(StandardCharsets.UTF_8));

                    byte [] byteArray = {1,2,3,4,5,6,7,8,9,10};
                    try {
                        OutputStreamWriter outputStream = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
                        outputStream.write ("Project Complete", 0, "Project Complete".length());
                        Log.d (className, "Message sent");
                    }
                    catch (IOException e)
                    {
                        Log.d (className, e.getMessage());
                    }



                }
            }).start();




    }

    private void serverProcess ()
    {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            int portToConnect = MainActivity.portToConnectTo;
            final ServerSocket serverSocket = new ServerSocket(portToConnect);
            Log.d (className, "Waiting on : " +serverSocket.getInetAddress() + "\n" + "Port : " + serverSocket.getLocalPort());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d (className, "In Run");
                    try {
                        Log.d(className, "Waiting for a connection");
                        Socket client = serverSocket.accept();
                        Log.d(className, "Accepted");
                        InputStream inputStream = client.getInputStream();
                        Log.d (className, "Got input stream");
                        Scanner s = new Scanner (inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        Log.d (className, "ABC" + result);
                    }
                    catch (IOException e)
                    {
                        Log.d (className, e.getMessage());
                    }
                }
            }).start();




        } catch (IOException e) {
            Log.d(className, e.getMessage());
            // return null;
        }

    }

    public void doInBackground() {

        Log.d (className, "In background");
        if (WiFiDirectBroadcastReceiver.isGroupOwner)
            serverProcess ();
        else
            clientProcess ();

    }

    /**
     * Start activity that can handle the JPEG image
     */

   /*
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            statusText.setText("File copied - " + result);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
            context.startActivity(intent);
        }
    }
    */


// convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
