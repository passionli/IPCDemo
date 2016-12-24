package com.liguang.ipcdemo;

import android.os.RemoteException;

public class ComputeImpl extends ICompute.Stub{
    @Override
    public int method(String name, int num) throws RemoteException {
        return 0;
    }
}
