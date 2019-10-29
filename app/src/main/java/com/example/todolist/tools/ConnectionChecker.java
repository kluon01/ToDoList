package com.example.todolist.tools;

import android.app.Application;
import android.util.Log;

public class ConnectionChecker extends Application {

    private boolean connected;

    public boolean getConnected() {
        return connected;
    }

    public void setConnected(boolean isConnected) {
            this.connected = isConnected;
        Log.i("connectionchecker",String.valueOf(this.connected));
    }
}
