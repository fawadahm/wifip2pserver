package com.nsl.serverwifip2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 *
 * Server Side Code
 */

public class MainActivity extends AppCompatActivity {


    private final String appName = "WiFiP2P";
    public static final int portToConnectTo = 1230;
    public static final String DeviceOne = "Device: NSL-android-1";
    public static final String DeviceTwo = "Device: NSL-android-2";
    public static InetAddress internetAddress;

    private TextView SocketStateView, DataRateView, DataTransferView;
    private String setSocketState, setDataRate, setDataTransfer;


    private String clientTAG = "CLIENT";
    private String serverTAG = "SERVER";

    private boolean firstTimeSend;


    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;




    NsdManager.RegistrationListener mRegistrationListener;
    IntentFilter mIntentFilter;
    public static Context appContext;


    int mLocalPort;

    private void setView (final TextView textView, final String message)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(message);
            }
        });

    }


    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getLocalClassName(), "onReceive in Main Activity");

            Bundle bundle = intent.getExtras();
            boolean isServer = bundle.getBoolean("isServer");

            //Depending on the value of isServer, start a new thread
            if (isServer)
                new Thread(new ServerThread()).start();//Server thread
            else
                new Thread(new ClientThread()).start();//Client thread


            Log.d(getLocalClassName(), "got a message saying " + isServer);
        }
    };
    /**

    WiFi P2P Method

    initialize()	Registers the application with the Wi-Fi framework. This must be called before calling any other Wi-Fi P2P method.
    connect()	Starts a peer-to-peer connection with a device with the specified configuration.
    cancelConnect()	Cancels any ongoing peer-to-peer group negotiation.
    requestConnectInfo()	Requests a device's connection information.
    createGroup()	Creates a peer-to-peer group with the current device as the group owner.
    removeGroup()	Removes the current peer-to-peer group.
    requestGroupInfo()	Requests peer-to-peer group information.
    discoverPeers()	Initiates peer discovery
    requestPeers()	Requests the current list of discovered peers.


     */



    /**

    WiFi P2P Listeners

    WifiP2pManager.ActionListener	connect(), cancelConnect(), createGroup(), removeGroup(), and discoverPeers()
    WifiP2pManager.ChannelListener	initialize()
    WifiP2pManager.ConnectionInfoListener	requestConnectInfo()
    WifiP2pManager.GroupInfoListener	requestGroupInfo()
    WifiP2pManager.PeerListListener	requestPeers()

     */



    public Context returnContext ()
    {
        return getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(appName, "onCreate ()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(br, new IntentFilter("intentFilterForBroadcast"));

        firstTimeSend = true;


        //WiFi P2P Registration
        Log.d(appName, "Registering with WiFi");
        //get an instance of WiFiP2P Manager
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        //Initialize it, now we can listen to it in its onReceive function
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,this, getApplicationContext());
        appContext = getApplicationContext();





        //To use WIFI P2P listen for broadcast intents
        //Listen only for WiFi broadcasts by attaching them to the instantiated intent


        //Intent Filters
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);//change in WiFi state
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);//change in WiFi peers list
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);//change in WiFi P2P connectivity
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);//change in configuration of this device
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);//change in the discovery process


        SocketStateView = (TextView) this.findViewById(R.id.SocketState);
        DataRateView = (TextView) this.findViewById(R.id.DataRate);
        DataTransferView = (TextView) this.findViewById(R.id.DataSentOrReceived);



    }



    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        Log.d(appName, "On Resume Called");
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        registerReceiver(br, new IntentFilter("intentFilterForBroadcast"));
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        Log.d(appName, "On Pause Called");
        unregisterReceiver(mReceiver);
        unregisterReceiver(br);
        super.onPause();

    }


    class ClientThread implements Runnable
    {

        @Override
        public void run ()
        {
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(internetAddress, portToConnectTo), 0);
                Log.d (clientTAG, "Connected to Server on " +internetAddress +" at port = " +portToConnectTo );
                setView(SocketStateView, "SOCKET STATE: Connected as Client");

                //open the input file
                FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/pointcloudfiles/thirtythree.txt");
                //Specify File Size
                int dataToSend = fis.available()/(1000 * 1000);
                Log.d(clientTAG, "File size = " + fis.available()/(1000 * 1000.0f) + " MB");
                //Get the output stream
                OutputStream outputStream = clientSocket.getOutputStream();

                int streamSize = 1024 * 1024;
                byte [] byteArray = new byte [streamSize];
                int flag = 0;

                long startTime = 0;
                int counter = 0;
                int myCounter = 0;
                int deltaDataSent = 0;
                long currentTime = 0;
                float elapsedTime = 0;
                while ( (flag = fis.read(byteArray, counter * byteArray.length, byteArray.length)) != -1)
                {
                    if (firstTimeSend)
                    {
                        Log.d(clientTAG, "Transmission Start Time = " + System.currentTimeMillis() +" ms");
                        startTime = System.currentTimeMillis();//Note the Start Time
                        firstTimeSend = false;
                    }
                    outputStream.write(byteArray);//write data to the output port



                    deltaDataSent = ((myCounter+1)*byteArray.length)/(1000);
                    currentTime = System.currentTimeMillis();
                    elapsedTime = (currentTime - startTime)/(1000.0f*1000.0f);

                    //TextViews
                    setView(DataTransferView, "DATA SENT = " + deltaDataSent + " kb");
                    setView(DataRateView, "DATA RATE = " +
                            (deltaDataSent*8.0f)/(elapsedTime*1000*1000) + "" + " MBps");
                    myCounter++;
                }
                Log.d(clientTAG, "End Time = " +System.currentTimeMillis() + " ms");

                Log.d (clientTAG, "Data Sent");
                long endTime = System.currentTimeMillis();//Note the End Time
                elapsedTime = (endTime - startTime)/1000.0f;
                float dataSent = dataToSend;

                if (elapsedTime != 0)
                    Log.d(clientTAG, "Speed =  " + (dataSent * 8.0f)/(elapsedTime) + " Mbps");
                Log.d (clientTAG, "Data Sent = " + (dataSent * 8.0f)/(1000*1000.0f) + " MB");
                Log.d (clientTAG, "Time = " + elapsedTime + " seconds");



                outputStream.flush();
                outputStream.close();
                fis.close();

                clientSocket.close();
            }
            catch (IOException e)
            {
                Log.d(clientTAG, e.getMessage());
                setView(SocketStateView, "SOCKET STATE: Reset App");
            }
        }

    }



    class ServerThread implements  Runnable
    {

        @Override
        public void run ()
        {
            try {

                Log.d(serverTAG, "Started server thread");
                //Create a new server socket and listen for connections
                ServerSocket serverSocket = new ServerSocket(portToConnectTo);
                //Get the server Inet Address
                //internetAddress = serverSocket.getInetAddress();

                String timeStamp = new SimpleDateFormat("dd_HHmmss").format(Calendar.getInstance().getTime());
                //Log.d(getLocalClassName(), Environment.getExternalStorageDirectory() + "/nslTests/" + timeStamp);

                //Wait and accept connections on the server socket
                setView(SocketStateView, "SOCKET STATE: Waiting for Connections");
                Socket commSocket = serverSocket.accept();
                setView(SocketStateView, "SOCKET STATE: Accepted Connection");
                //File Output Stream
                Log.d(serverTAG, "Accepted connection");


                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Recv" + timeStamp + ".yml");

                //Input Stream
                InputStream is = commSocket.getInputStream();

                int bufferSize = 1024 * 1024;

                byte [] byteBuffer = new byte [bufferSize];


                //Variables for Average Measurements
                long startTime = System.currentTimeMillis();
                long endTime = 0;
                float elapsedTime = 0.0f;
                int counter = 0;
                int bytesRead = 0;
                int totalBytes = 0;


                //Variables for Instantaneous Measurements
                long instStartTime = 0;
                long instEndTime = 0;
                float instElapsedTime = 0;
                float instDataRead = 0;

                instStartTime = System.currentTimeMillis();//Get the current time in milliseconds

                while ((bytesRead = is.read(byteBuffer)) != -1)
                {
                    if (firstTimeSend)
                    {
                        Log.d(serverTAG, "Server Data Received Start Time = " + startTime + " ms");
                        firstTimeSend = false;
                    }


                    //AVERAGE MEASUREMENTS
                    totalBytes += bytesRead;
                    endTime = System.currentTimeMillis();
                    elapsedTime = ((endTime - startTime)/1000.0f);


                    /**
                    //INSTANTANEOUS MEASUREMENTS
                    instEndTime = System.currentTimeMillis();
                    instElapsedTime = ((instEndTime - instStartTime)/1000.0f);
                    instDataRead += bytesRead;



                    //INSTANTANEOUS MEASUREMENTS
                    //Let's do them every 250 milliseconds
                    if ((instElapsedTime*1000) >= 500 && instElapsedTime != 0)//record data every 10 milliseconds
                    {
                       // Log.d(serverTAG, "Instantaneous Data Received = " + (instDataRead)/(1000*1000.0f) + " MB");
                       // Log.d(serverTAG, "Inst Elapsed Time = " + instElapsedTime + " seconds");
                        Log.d(serverTAG, "Throughput = " + + System.currentTimeMillis() + " : " + (instDataRead*8)/(1000*1000*instElapsedTime));

                        //Reset all counters

                        instDataRead = 0;//counter reset
                        instStartTime = System.currentTimeMillis();//get a new start time
                    }
                    else;//keep going

                     */

                    //AVERAGE MEASUREMENTS
                    setView(DataTransferView, "DATA RECEIVED = " + (totalBytes)/(1000*1000.0f) + " MB");
                    setView(DataRateView, "DATA RATE = " + (totalBytes*8)/(1000*1000*elapsedTime) + " MBps");



                }

                //Log.d(serverTAG, "Server Throughput = " + + System.currentTimeMillis() + " : " + (totalBytes*8)/(1000*1000*elapsedTime));
                Log.d(serverTAG, "Transmission End Time = " + System.currentTimeMillis() + " ms");


                float dataReceived = ((totalBytes)/(1000*1000.0f));
                Log.d(serverTAG, "Received " + dataReceived + " MB");

                endTime = System.currentTimeMillis();
                elapsedTime = ((endTime - startTime)/1000.0f);
                setView(DataTransferView, "DATA RECEIVED = " + (totalBytes)/(1000*1000) + " MB");
                setView(DataRateView, "DATA RATE = " + (totalBytes*8)/(1000*1000*elapsedTime) + " MBps");
                Log.d (serverTAG, "Elapsed Time = " + elapsedTime + " seconds");
                Log.d(serverTAG, "Throughput = " + (dataReceived*8)/elapsedTime + " Mbps");
                is.close();
                fos.close();

                //fis.write("Hello World".getBytes());
                Log.d(serverTAG, "Done");

                //File createFile = new File (getApplicationContext().)



            }
            catch (IOException e)
            {
                Log.d (serverTAG, e.getMessage());
            }
        }
    }
}



