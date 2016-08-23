package com.example.daniel.socketprinter;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Daniel on 22/08/2016.
 */
public class SocketService extends Service {
    private String TAG = SocketService.class.getName();
    private ServerSocket serverSocket;

    private final IBinder mBinder = new SocketBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ServiceAsync serviceAsync = new ServiceAsync();
            serviceAsync.execute();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            try {
                serverSocket.close();
            } catch (IOException io) {
                Log.e(TAG, io.getMessage(), io);
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            serverSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "onUnbind: " + e.getMessage(), e);
        }
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Service task
     */
    class ServiceAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... sockets) {

            try {
                while (true) {
                    serverSocket = new ServerSocket(9100);
                    Socket socket = serverSocket.accept();
                    ClientAsync client = new ClientAsync();
                    //TODO: uncheck this warning
                    client.execute(socket);
                }
            } catch (IOException e) {
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Error: " + e.toString());
                }
                Log.e(TAG, "Error: " + e.toString());
            }


            return null;
        }
    }

    class ClientAsync extends AsyncTask<Socket, String, Void> {

        private Socket socketClient;
        private BufferedReader reader;

        @Override
        protected Void doInBackground(Socket... sockets) {
            this.socketClient = sockets[0];
            try {
                //Get Input stream
                reader = new BufferedReader(new InputStreamReader(this.socketClient.getInputStream()));
            } catch (IOException e) {
                Log.e("socket", "Error: " + e.toString());
            }

            while (true) {
                try {
                    String msg = reader.readLine();
                    //Se manda al Log la linea recibida por el cliente
                    publishProgress(msg);
                    Log.d(TAG, "Client: " + msg);
                } catch (IOException e) {
                    Log.e(TAG, "Error: " + e.toString());
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //TODO: HERE MUST SEND DATA TO ACTIVITY
            Log.d(TAG, "onProgressUpdate: " + values[0]);
            super.onProgressUpdate(values);
        }
    }

    /**
     * BINDER
     */
    public class SocketBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }
}
