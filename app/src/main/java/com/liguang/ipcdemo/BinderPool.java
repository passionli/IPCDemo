package com.liguang.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

/**
 * Binder连接池。同步式接口，接口可能耗时，上层需要在非UI线程中使用该模块。
 */
public class BinderPool {
    private static final String TAG = "BinderPool";
    public static final int BINDER_SECURITY_CENTER = 0;
    public static final int BINDER_COMPUTE = 1;
    //GuardBy this
    private IBinderPool mBinderPool;
    private Context mContext;

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (BinderPool.this) {
                IBinderPool binderPool = IBinderPool.Stub.asInterface(service);
                try {
                    //向下层注册死亡回调
                    service.linkToDeath(mDeathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //point to that object
                mBinderPool = binderPool;
                //通知工作线程，这里不能用notify
                BinderPool.this.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //do nothing
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied: ");
            //运行在Binder线程池中
            //前一个对象移除死亡回调，help gc
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            synchronized (BinderPool.this) {
                //加锁再写入数据
                mBinderPool = null;
            }
            //重连服务端
            connectBinderPoolService();
        }
    };

    //Holder模式
    private static class BinderPoolHolder {
        //只在运行ClassLoader的线程中运行一次
        private static BinderPool binderPool = new BinderPool();
    }

    //禁止外部调用
    private BinderPool() {
    }

    /**
     * 连接BinderPool服务，需要运行在工作线程
     */
    private synchronized void connectBinderPoolService() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            throw new RuntimeException("connectBinderPoolService should not run in UI thread");
        }

        Intent intent = new Intent(mContext, BackendService.class);
        //因为需要绑定的服务是我们自己的服务，故不需要检查bindService的返回值
        mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        //等待主线程写入数据
        while (mBinderPool == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static BinderPool getInstance() {
        return BinderPoolHolder.binderPool;
    }

    public synchronized void init(Context context) {
        //防ActivityContext泄漏
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    /**
     * query binder by binderCode from binder pool. Guard by this.
     *
     * @param binderCode the unique token of binder
     * @return binder who's token is binderCode
     * null when the binder not found or the service died
     */
    public synchronized IBinder queryBinder(int binderCode) {
        IBinder binder = null;

        try {
            while (mBinderPool == null) {
                Log.d(TAG, "queryBinder: wait for main thread's notifyAll");
                wait();
            }
            binder = mBinderPool.queryBinder(binderCode);
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }

        return binder;
    }
}
