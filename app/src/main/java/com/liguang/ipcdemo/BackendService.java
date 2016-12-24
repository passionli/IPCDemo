package com.liguang.ipcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class BackendService extends Service {
    private static final String TAG = "BackendService";
    public static final int BINDER_SECURITY_CENTER = 0;
    public static final int BINDER_COMPUTE = 1;

    private IBinderPool.Stub mBinder = new IBinderPool.Stub() {

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //TODO permission
            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder result = null;
            switch (binderCode) {
                case BINDER_SECURITY_CENTER:
                    result = new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    result = new ComputeImpl();
                    break;
            }
            return result;
        }
    };

    public BackendService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO permission
        return mBinder;
    }
}
