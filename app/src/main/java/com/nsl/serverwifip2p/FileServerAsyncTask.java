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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.Scanner;


/**
 * Created by fawadahmad on 9/28/16.
 */

public class FileServerAsyncTask extends Service {

    final String className = "FileServerAsyncTask";


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



    public void doInBackground() {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */



            Log.d (className, "Creating Server Socket");

            // final ServerSocket serverSocket = new ServerSocket();
            //serverSocket.setReuseAddress(true);
            // serverSocket.bind(new InetSocketAddress(0));

            int portToConnect = 12125;
            final ServerSocket serverSocket = new ServerSocket(portToConnect);


            Log.d (className, "Waiting on : " +serverSocket.getInetAddress() + "\n" + "Port : " + serverSocket.getLocalPort());



            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d (className, "In Run");
                    try {
                        Log.d(className, "waiting for a connection");
                        Socket client = serverSocket.accept();
                        Log.d(className, "Accepted");
                        InputStream inputStream = client.getInputStream();
                        Log.d (className, "Got input stream");
                        Scanner s = new Scanner (inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";


                        //int ch;
                        //StringBuilder sb = new StringBuilder();
                        // while((ch = inputStream.read()) != -1)
                        //   sb.append((char)ch);



                        Log.d (className, "ABC" + result);
                    }
                    catch (IOException e)
                    {
                        Log.d (className, e.getMessage());
                    }
                }
            }).start();



            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */

            Log.d(className, Environment.getExternalStorageDirectory() + "/"
                    + "com.nsl.WiFiP2P" + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".jpg");
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + "com.nsl.WiFiP2P" + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".jpg");

            /**
             *
             *

             File dirs = new File(f.getParent());
             if (!dirs.exists())
             dirs.mkdirs();
             f.createNewFile();
             InputStream inputstream = client.getInputStream();
             //copyFile(inputstream, new FileOutputStream(f));
             serverSocket.close();
             //return f.getAbsolutePath();

             */
        } catch (IOException e) {
            Log.d(className, e.getMessage());
            // return null;
        }
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
