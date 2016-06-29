package org.corapp.cor;

import android.app.Activity;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public abstract class SocketFunctionality {

    protected Socket mSocket;

    void connect() {
        try {
            // Create socket
            mSocket = IO.socket(Constants.IP_ADDRESS);
            // Connect socket to server
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.d("Socket cannot connect.", e.getMessage());
        }

    }

    void sendMessage(String msg, Object data) {
        mSocket.emit(msg, data);
    }
}
