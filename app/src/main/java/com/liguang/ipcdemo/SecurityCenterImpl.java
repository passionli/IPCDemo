package com.liguang.ipcdemo;

import android.os.Parcel;
import android.os.RemoteException;

public class SecurityCenterImpl extends ISecurityCenter.Stub {

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return super.onTransact(code, data, reply, flags);
    }

    @Override
    public int method(int num, String name) throws RemoteException {
        return 0;
    }
}
