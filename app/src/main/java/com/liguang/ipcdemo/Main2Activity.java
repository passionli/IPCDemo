package com.liguang.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import hugo.weaving.DebugLog;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";
    private IBinderPool mBinderPool;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @DebugLog
        @Override
        public void binderDied() {
            //运行在Binder线程池
            if (mBinderPool == null)
                return;
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBinderPool = null;

            //re bind
            boolean bound = bindService(new Intent(Main2Activity.this, BackendService.class), mConn, Context.BIND_AUTO_CREATE);
            if (!bound) {
                Log.d(TAG, "binderDied: bindService false");
            }
        }
    };

    private ServiceConnection mConn = new ServiceConnection() {
        @DebugLog
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinderPool = IBinderPool.Stub.asInterface(binder);
            try {
                //register callback
                binder.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                IBinder computeBinder = mBinderPool.queryBinder(BackendService.BINDER_COMPUTE);
                ICompute iCompute = ICompute.Stub.asInterface(computeBinder);
                int output = iCompute.method("data", 1);

                IBinder securityBinder = mBinderPool.queryBinder(BackendService.BINDER_SECURITY_CENTER);
                ISecurityCenter iSecurityCenter = ISecurityCenter.Stub.asInterface(securityBinder);
                int result = iSecurityCenter.method(2, "data");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @DebugLog
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinderPool = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        boolean bound = bindService(new Intent(Main2Activity.this, BackendService.class), mConn, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.d(TAG, "onCreate: bindService false");
        }
//        Log.d(TAG, "onCreate: " + UserManager.sId);
//        UserManager.sId = 2;
//        startActivity(new Intent(this, Main3Activity.class));
    }
}
