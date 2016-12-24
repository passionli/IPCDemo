package com.liguang.ipcdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Main4Activity extends AppCompatActivity {
    private static final String TAG = "Main4Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        for (int i = 0; i < 100; i++)
            new MyTask().execute();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        ISecurityCenter mSecurityCenter;
        ICompute mCompute;

        @Override
        protected Void doInBackground(Void... params) {
            IBinder binder;
            while ((binder = BinderPool.getInstance().queryBinder(BinderPool.BINDER_SECURITY_CENTER)) == null)
                ;
            mSecurityCenter = SecurityCenterImpl.Stub.asInterface(binder);

            while ((binder = BinderPool.getInstance().queryBinder(BinderPool.BINDER_COMPUTE)) == null)
                ;
            mCompute = ComputeImpl.Stub.asInterface(binder);

            try {
                mSecurityCenter.method(1, "data");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                mCompute.method("data", 2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "onPostExecute: " + System.identityHashCode(this));
        }
    }
}
