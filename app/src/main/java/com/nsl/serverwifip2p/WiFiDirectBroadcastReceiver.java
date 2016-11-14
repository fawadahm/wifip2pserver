package com.nsl.serverwifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import android.net.wifi.p2p.WifiP2pInfo;


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver{



    /**
     private final boolean debug = false;
     private final boolean isServer = false;

     */




    private final boolean debug = false;
    private int deviceIndex = 0;
    private TextView getDeviceNameTextView;
    private TextView getWifiStateTextView;
    private TextView getNetworkConnectivityTextView;
    private TextView getNetworkStateTextView;
    private TextView getGroupOwnerTextView;
    private TextView getInitialDiscovery;
    public static TextView socketView;
    public TextView getPeersAvailable;


    private boolean isDiscovery = true;
    private boolean isConnected = false;
    private boolean connectingDevicePresent = false;

    private String devNameString = "";
    private String wifiStateString = "";
    private String networkConnectivity = "";
    private String networkStateString = "";
    private String groupOwnerString = "";
    private String socketViewString = "";
    private String initialDiscovery = "";
    private int numberOfPeersAvailable = 0;


    public static boolean isGroupOwner = false;
    public String connectToDeviceOne, connectToDeviceTwo;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private WifiP2pManager.PeerListListener peerListener;
    private List peers;
    private final String className = "Broadcast Receiver";
    private WifiP2pConfig peerToConnectTo = new WifiP2pConfig();
    private Context baseContext;

    private boolean alreadyListening;

    private boolean alreadyConnected;





    //This is the class constructor, the arguments are the WiFi manager, the channel and the activity we want to respond to
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity, Context baseContext) {


        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        peers = new ArrayList();
        this.baseContext = baseContext;
        alreadyListening = false;
        isDiscovery = true;
        alreadyConnected = false;//to make sure we don't connect to the same device again and again
        connectingDevicePresent = false;


        //DEVICES TO CONNECT TO
        connectToDeviceOne = MainActivity.DeviceOne;
        connectToDeviceTwo = MainActivity.DeviceTwo;



        //TEXT VIEWS TO GIVE CURRENT STATE OF APPLICATION

        getDeviceNameTextView = (TextView) activity.findViewById(R.id.HelloWorldTextField);
        getWifiStateTextView =  (TextView) activity.findViewById(R.id.WiFiDevicesField);
        getNetworkConnectivityTextView =  (TextView) activity.findViewById(R.id.NetworkState);
        getNetworkStateTextView =  (TextView) activity.findViewById(R.id.State);
        getGroupOwnerTextView =  (TextView) activity.findViewById(R.id.GroupOwner);
        getInitialDiscovery = (TextView) activity.findViewById(R.id.InitialDiscovery);
        getPeersAvailable = (TextView) activity.findViewById(R.id.PeersAvailable);




    }



    //Peer List Listener for when Peers are Available or When they have changed
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            //Update Peer list
            peers.clear();//clear the list
            peers.addAll(peerList.getDeviceList());//update the list from peerList
            numberOfPeersAvailable = peers.size();//get the number of peers available

            getPeersAvailable.setText("PEERS AVAILABLE = " +numberOfPeersAvailable);//update it in the text view

            if (debug)
                Log.d(className, "Number of Peers = " +peers.size());
            String textViewString = "Device List\n";
            for (int counter = 0; counter < peers.size(); counter++) {
                //To find the peer we want to connect to
                String peerPhrase = "" + peers.get(counter);
                String delimiter = "\n";
                String[] peerInfo = peerPhrase.split(delimiter);

                //if (debug)
                    Log.d(className, "Peer = " + counter + " " + peerInfo[0]);
                textViewString += peerInfo [0] + "\n";

                if (peerInfo[0].equals(connectToDeviceOne) || peerInfo[0].equals(connectToDeviceTwo))//see if they are one of the devices we want to connect to
                {

                    connectingDevicePresent = true;
                    Log.d (className, "Connecting device present");
                    deviceIndex = counter;//store the index of the device we want to connect to
                    Log.d(className, "Breaking out of loop");
                    break;
                }
                else
                    connectingDevicePresent = false;
            }

            if (peers.size() != 0) {//if size is not zero
                //if (debug)


                Log.d (className, "Already connected = " + alreadyConnected + " connecting device present = " + connectingDevicePresent);
                if (alreadyConnected == false && connectingDevicePresent) {//check if we have already connected
                    Log.d(className, "Going to connect to index " + deviceIndex);

                    WifiP2pDevice connectToDevice = (WifiP2pDevice) peers.get(deviceIndex);//get the device we want to connect to
                    WifiP2pConfig deviceConfiguration = new WifiP2pConfig();//new config object to keep regarding the device
                    deviceConfiguration.deviceAddress = connectToDevice.deviceAddress;//store device MAC in config object

                    //if (devNameString != connectToDeviceOne)//we want only one device to do the connecting, because it results in race conditions
                    // Log.d(className, "Connecting to " + deviceConfiguration);
                    mManager.connect(mChannel, deviceConfiguration, connectionActionListener);//try to connect to device and store results in connectionActionListener
                }
                else
                    Log.d(className, "Stopped extra connections");
            }
        }
    };






    private WifiP2pManager.ActionListener connectionActionListener = new WifiP2pManager.ActionListener ()
    {
        @Override
        public void onSuccess ()//Connection Successful
        {
            if (debug)
                Log.d(className, "Initial Connection Successful");
            alreadyConnected = true;

        }

        @Override
        public void onFailure (int reasonCode)//Connection Failed
        {
            if (debug)
                Log.d(className, "Initial Connection Failed " + reasonCode);
            alreadyConnected = false;
        }

    };



    //ACTION LISTENER FOR DISCOVERY PROCESS
    private WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener()
    {
        //Action Listener to DiscoverPeers()
        @Override
        public void onSuccess ()
        {
            if (debug)
                Log.d(className, "Peers discovered");

            //Are there any peers? If yes, then request the list
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);//request peers
            }
        }
        @Override
        public void onFailure (int reasonCode)
        {
            if (debug)
                Log.d("Discover Peers", "Failed because of " + reasonCode);
            isDiscovery = true;
        }
    };


    //Function to start looking for peers
    public void discoverPeers() {
        //Should only be called the first time, keeps looking until a group is formed
        //remains active until stopped (by forming a connection or initiating a P2P group)
        if (debug)
            Log.d(className, "Going to call Discover Peers()");
        mManager.discoverPeers(mChannel, actionListener);//try to discover peers and put results in actionListener
    }




    private WifiP2pManager.ConnectionInfoListener p2pListener = new WifiP2pManager.ConnectionInfoListener ()
    {
        @Override
        public void onConnectionInfoAvailable (final WifiP2pInfo info)
        {
            if (debug)
                Log.d (className, "Change in P2P Connection with Peer");

            if (info.groupFormed)//when the group is formed
            {
                //alreadyConnected = true;
                networkConnectivity = "P2P GROUP CONNECTIVITY = Connected";
                mActivity.internetAddress = info.groupOwnerAddress;//store the IP address of the group owner
                if (info.isGroupOwner) {//if current device is the group owner
                    networkStateString = "P2P Network State = Group Owner";
                    isGroupOwner = true;
                }
                else {//if current device is a client
                    networkStateString = "P2P Network State = Client";
                    isGroupOwner = false;
                }
                groupOwnerString = "GROUP OWNER IP Address : " +info.groupOwnerAddress;
                getGroupOwnerTextView.setText(groupOwnerString);
                getNetworkStateTextView.setText(networkStateString);

                //Send a broadcast to the Main Activity telling it to start sockets
                Intent intentForBroadcast = new Intent ("intentFilterForBroadcast");
                intentForBroadcast.putExtra("isServer", isGroupOwner);
                baseContext.sendBroadcast(intentForBroadcast);
                Log.d(className, "Broadcast sent");
            }
            else
            {
                //alreadyConnected = false;
                networkConnectivity = "P2P GROUP CONNECTIVITY = Not Connected";
                if (debug)
                    Log.d (className, "Group not formed");
                }
            //Group is broke, try discovering peers again
            alreadyConnected = false;
            discoverPeers();
            getNetworkConnectivityTextView.setText(networkConnectivity);
        }
    };

    //onReceiver function for BroadCast Receiver
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();//get the action of the intent

        //SHOWS WHETHER P2P IS SUPPORTED OR NOT
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Get current state of WiFi
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            //Is WiFi P2P supported
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                if (debug)
                    Log.d(className, "Wifi State : " + "Enabled");
                wifiStateString = "WIFI P2P STATE : Enabled";
                getWifiStateTextView.setText(wifiStateString);
                //Wifi is enabled, so start looking for peers
                discoverPeers();
            }

            else {
                if (debug)
                    Log.d(className, "Wifi State : " + "Disabled");
                wifiStateString = "WIFI P2P State : Disabled";
                getWifiStateTextView.setText(wifiStateString);
            }

        }


        //TO DETERMINE WHETHER DEVICE IS DOING P2P DISCOVERY OR NOT
        else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action))
        {
            //Get current state of P2P Discovery
            int p2pDiscoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            //P2P Discovery has started
            if (p2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                if (debug)
                    Log.d(className, "Discovery Started");
                initialDiscovery = "DISCOVERY : In Progress";
                getInitialDiscovery.setText(initialDiscovery);
            }
            //P2P Discovery has stopped
            else if (p2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                if (debug)
                    Log.d(className, "Discovery Stopped");
                initialDiscovery = "DISCOVERY : Stopped";
                getInitialDiscovery.setText(initialDiscovery);
            }
        }

        //TO DETERMINE WHETHER STATES OF CURRENT PEERS HAVE CHANGED OR NOT
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //Initially called when discover peers finds peers
            if (debug)
                Log.d(className, "Peer Action Change");
            //Peers action changed, request peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
        }

        //FOR UPDATES REGARDING CURRENT CONNECTION
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (debug)
                Log.d(className, "P2P Connection : " + "Changed");
            mManager.requestConnectionInfo(mChannel, p2pListener);
        }

        //FOR UPDATES REGARDING CHANGES IN THIS DEVICE
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            if (debug)
                Log.d(className, "P2P Connection : " +  "This Device Changed");
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            devNameString = device.deviceName;
            getDeviceNameTextView.setText("DEV NAME : "  + devNameString);
        }
    }
}
