package com.practice.phuc.server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyApp";
    private static final int SocketServerPORT = 8080;
    private static final String SERVICE_TYPE = "_http._tcp.";

    private String SERVICE_NAME = "ProTube";
    private String serverIp = "10.0.2.15";

    private Handler handler = new Handler();
    private ServerSocket serverSocket;
    private NsdManager mNsdManager;

    private NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(TAG, "onRegistrationFailed: " + serviceInfo.getServiceName());
            Log.d(TAG, "onRegistrationFailed: error code: " + errorCode);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(TAG, "onUnregistrationFailed: " + serviceInfo.getServiceName());
            Log.d(TAG, "onUnregistrationFailed: error code: " + errorCode);
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            String serviceName = serviceInfo.getServiceName();
            SERVICE_NAME = serviceName;

            Log.d(TAG, "Registered name : " + serviceName);

            Thread fst = new Thread(new ServerThread());
            fst.start();
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG, "onServiceUnregistered : " + serviceInfo.getServiceName());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        serverIp = getLocalIpAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNsdManager != null) {
            registerService(SocketServerPORT);
        }
    }

    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (serverIp != null) {
                    serverSocket = new ServerSocket(SocketServerPORT);
                    
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = serverSocket.accept();
                        Log.d(TAG, "run: Connected.");

                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String line;

                            while ((line = in.readLine()) != null) {
                                Log.d(TAG, line);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // DO WHATEVER YOU WANT TO THE FRONT END
                                        // THIS IS WHERE YOU CAN BE CREATIVE
                                    }
                                });
                            }
                            break;
                        } catch (Exception e) {
                            Log.d(TAG, "run: Connection was interrupted.");
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "run: Couldn't detect internet connection.");
                }
            } catch (Exception e) {
                Log.d(TAG, "run: error.");
                e.printStackTrace();
            }
        }
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
}
