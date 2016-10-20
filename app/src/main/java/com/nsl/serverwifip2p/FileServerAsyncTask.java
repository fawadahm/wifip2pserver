package com.nsl.serverwifip2p;



import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


/**
 * Created by fawadahmad on 9/28/16.
 */

public class FileServerAsyncTask extends Service {

    final String className = "FileServerAsyncTask";

    public static Activity mainActivity = null;
    private final boolean mainActivityRef = false;

    private final int SOCKET_STATE = 0;
    private final int DATA_STATE = 1;

    public static InetAddress groupOwnerAddress;

    public TextView socketState;
    public String socketStateString;

    public TextView dataState;
    public String dataStateString;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(className, "onStartCommand");

        doInBackground();
        return super.onStartCommand(intent, flags, startId);


    }


    public static void customStart(Activity sentActivity) {
        mainActivity = sentActivity;


    }

    private void clientProcess() {

       /**

        if (mainActivity != null) {
            socketState = (TextView) mainActivity.findViewById(R.id.ReceiveOrSend);
            dataState = (TextView) mainActivity.findViewById(R.id.DataState);
        } else {
            Log.d(className, "Error here");
        }
        */


        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 Create a client socket and look for the server
                 */
                Log.d(className, "Creating Client Socket ");

                int portToConnectTo = MainActivity.portToConnectTo;
                Socket clientSocket = new Socket();
                Log.d(className, "Client Created");

                boolean notConnected = true;
                boolean showErrorMessage = true;
                while (notConnected) {
                    try {
                        clientSocket.connect(new InetSocketAddress(groupOwnerAddress, portToConnectTo), 5000);
                        notConnected = false;
                        Log.d(className, "Connected to " + groupOwnerAddress + " on port " + portToConnectTo);
                    } catch (IOException e) {
                        if (showErrorMessage)
                        {
                            Log.d (className, "Connect to Socket = " + e.getMessage());
                            showErrorMessage = false;
                        }
                        notConnected = true;

                    }
                }


                try {


                    OutputStream outputStream = clientSocket.getOutputStream();
                    Log.d(className, "Sending Hello Socket");
                    outputStream.write("Hello Socket".getBytes());
                    outputStream.flush();
                    Log.d(className, "Sent");
                    //byte [] byteBuffer = new byte [100024];
                    //outputStream.write(byteBuffer, 0 , 100024);

                    long startTime = System.currentTimeMillis();
                    for (int counter = 0; counter < 1000000; counter++)
                        outputStream.write(5);
                    outputStream.flush();
                    long endTime = System.currentTimeMillis();
                    Log.d(className, "Time = " + ((endTime - startTime)/1000) + " seconds");


                    outputStream.close();
                    Log.d(className, "Closed");





                    /**

                    //Fetch File to Send
                    FileInputStream fin = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/fullPCFrame50.yml"));
                    Log.d(className, "Fetched file from disk");

                    byte [] mybyteArray = new byte [(int) fin.getChannel().size()];

                    Log.d(className, "Created myByteArray of size " + fin.getChannel().size()/1024);

                    BufferedInputStream bis = new BufferedInputStream( fin );
                    Log.d(className, "BIS");
                    bis.read(mybyteArray, 0, mybyteArray.length);
                    Log.d(className, "BIS to byte");
                    OutputStream os = clientSocket.getOutputStream();

                    final double byteToKB = 0.001;
                    final int reductionFactor = 10;
                    final int rounds = 1000;


                    //Not going to send the whole data, only a portion of it
                    int dataToSend = mybyteArray.length/(reductionFactor);

                    double dataSize = (int) dataToSend * byteToKB;

                    Log.d(className, "Going to send  " + dataSize + " kb of data to Output Stream in " +rounds +" iterations");


                    //Time variables
                    int timeElapsed = 1;
                    float startTime = System.currentTimeMillis();

                    Log.d(className, "Start Time = " + startTime);




                    for (int counter = 0; counter < rounds; counter++) {

                        os.write(mybyteArray, (dataToSend/rounds) * counter , dataToSend/rounds);

                        int dataSent = (dataToSend/rounds) * counter;

                        //time now that data has been sent

                        //Log.d (className, "Elapsed Time = " + (System.currentTimeMillis() - startTime) + " ms");

                        /**

                        if (System.currentTimeMillis() - startTime != 0) {

                            float dataRate = dataSent / ((System.currentTimeMillis() - startTime) * 1000 );

                            Log.d(className, "Data Rate = " + dataRate+ " MBps");
                        }

                        Log.d(className, "Sent round number " + (counter + 1) + "/" + rounds +" : " + (dataToSend/rounds)*byteToKB + " kb from " + (dataToSend/rounds) * counter + " to " + (dataToSend/rounds) * (counter+1));

                         */


                   // }

                    //Log.d(className, "Flushing : " + dataSize + " kb");
                   // os.flush();

                   // Log.d(className, "Data Sent worth : " + dataSize + " kb in " + (System.currentTimeMillis() - startTime) +" ms");
                   // clientSocket.close();
                   // Log.d(className, "Socket Closed");

                   // */







                }
                catch (IOException e)
                {
                    Log.d(className, e.getMessage());
                }







                /**


                InputStream stream = new ByteArrayInputStream("Project Completed\n".getBytes(StandardCharsets.UTF_8));

                byte[] byteArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                try {
                    OutputStreamWriter outputStream = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
                    outputStream.write("Project Complete", 0, "Project Complete".length());

                    //dataStateString = "DATA STATE : Sent";
                    //if (mainActivityRef)
                    //dataState.setText(dataStateString);
                   //ß setView(DATA_STATE, "Data Sent");
                    Log.d(className, "Message sent");
                } catch (IOException e) {

                    // dataStateString = "DATA STATE : Failed to Send";
                    // if (mainActivityRef)
                    // dataState.setText(dataStateString);
                   // setView(DATA_STATE, "Failed to Send Data");
                    Log.d(className, e.getMessage());
                }


                */

            }
        }).start();


    }



    private void serverProcess() {
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            try {
                Log.d(className, "Waiting for Data ");
                int portToConnect = MainActivity.portToConnectTo;
                final ServerSocket serverSocket = new ServerSocket(portToConnect);
                Log.d(className, "Waiting on : " + serverSocket.getInetAddress() + "\n" + "Port : " + serverSocket.getLocalPort());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(className, "In Run");
                        try {
                            Log.d(className, "Waiting for a connection");
                            Socket client = serverSocket.accept();

                            Log.d(className, "Rec Buffer Size = " + client.getReceiveBufferSize() / 1024);



                            byte [] myByteArray = new byte[1000024];
                            InputStream is = client.getInputStream();
                            Log.d(className, "is has " + is.available());
                            Log.d(className, "Will only read = " + 1024/1024 + " kb");


                            SimpleDateFormat sdf = new SimpleDateFormat("dd_HHmmss");
                            String currentDateAndTime = sdf.format(new Date());


                            boolean EOF = false;
                            int tempValue = 0;
                            int bytesCollected = 0;

                            while (!EOF)
                            {

                               // Log.d (className, "Available = " +is.available());
                                tempValue = is.read(myByteArray, 0, myByteArray.length);
                                if (tempValue == -1)
                                    EOF = true;
                                else
                                    bytesCollected += tempValue;

                                Log.d(className, "Received " + tempValue + " bytes of data");
                            }

                            //bos.write(myByteArray, 0, bytesRead);
                            Log.d(className, "Data Collected = " + bytesCollected/1024 + "kb");




                            /**






                            byte [] myByteArray2 = new byte[10000024];
                            InputStream is2 = client.getInputStream();


                            Log.d(className, "is has " + is.available());
                            Log.d(className, "Will only read = " + 10000024/1024 + " kb");


                            SimpleDateFormat sdf2 = new SimpleDateFormat("dd_HHmmss");
                            String currentDateAndTime2 = sdf2.format(new Date());


                            FileOutputStream fos2 = new FileOutputStream(Environment.getExternalStorageDirectory() + "/xyz/" + currentDateAndTime +  "ReceivedFullPCFrame50.yml");
                            BufferedOutputStream bos2 = new BufferedOutputStream(fos);


                            int bytesRead2 = is.read(myByteArray2, 0, myByteArray2.length);
                            bos.write(myByteArray2, 0, bytesRead2);
                            Log.d(className, "Data Size = " + bytesRead2/1024 + "kb");











                            **/







                            //bos.close();
                            client.close();



                            // dataStateString = "DATA STATE : Still waiting";
                            // if (mainActivityRef)
                            //dataState.setText(dataStateString);

                            //setView(DATA_STATE, "Still waiting for data");



















                            /**final File sentData = new File(Environment.getExternalStorageDirectory() + "/"
                             + getApplicationContext().getPackageName() + "/serverwifip2p" + System.currentTimeMillis()
                             + ".yml");



                             File dirs = new File (sentData.getParent());

                             if (!dirs.exists())
                             dirs.mkdirs();

                             sentData.createNewFile();

                             Log.d (className, "Created a new file");

                             InputStream inputStream = client.getInputStream();
                             Log.d (className, "Got YML file");


                             copyInputStreamToFile (inputStream, sentData);

                             Log.d (className, "Copied to File");

                             serverSocket.close();
                             Log.d (className, "Socket Closed");

                             */

                        } catch (IOException e) {
                            Log.d(className, e.getMessage());
                        }
                    }
                }).start();


            }
            catch (IOException e)
            {
                Log.d(className, e.getMessage());
            }

    }




    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doInBackground() {

        Log.d(className, "In background");


        //socketState = (TextView) mainActivity.findViewById(R.id.ReceiverOrSender);
        //dataState = (TextView) mainActivity.findViewById(R.id.DataState);

        if (WiFiDirectBroadcastReceiver.isGroupOwner) {
            Log.d (className, "Executing Server Process");
            serverProcess();

        }
        else {
            Log.d(className, "Executing Client Process");
            clientProcess();

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

    public void setView (int view, String message)
    {

        /*
        final String dumbMessage = message;
        switch (view)
        {
            case 0:


                if (mainActivity != null)
                mainActivity.runOnUiThread (new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        socketState.setText ("SOCKET STATE : " +dumbMessage);


                    }
                });
                break;


            case 1:

                if (mainActivity != null)
                mainActivity.runOnUiThread (new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        dataState.setText ("DATA STATE : " +dumbMessage);


                    }
                });
                break;

            default:
                break;
        }


*/
        return;
    }





}
