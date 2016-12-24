package com.liguang.ipcdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Main3Activity extends AppCompatActivity {
    private static final String TAG = "Main3Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Log.d(TAG, "onCreate: " + UserManager.sId);
        try {
            User user = readObject();
            Log.d(TAG, "onCreate: " + user);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private User readObject() throws IOException, ClassNotFoundException {
        User user;
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(
                    Environment.getExternalStorageDirectory() + "/liguang/cache.txt"));
            user = (User) in.readObject();
        } finally {
            if (in != null)
                in.close();
        }
        return user;
    }
}
