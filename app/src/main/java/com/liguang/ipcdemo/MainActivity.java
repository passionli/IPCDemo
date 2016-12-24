package com.liguang.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final Object mLock = new Object();
    private IBookManager mBookManager;
    private boolean mIsBind;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @DebugLog
        @Override
        public void binderDied() {
            //运行在Binder线程池
            if (mBookManager == null)
                return;
            boolean isBinderAlive = mBookManager.asBinder().isBinderAlive();
            try {
                //测试Binder死亡后，客户端调用会发生什么情况
                mBookManager.getBookList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBookManager = null;

            //re bind
            boolean bound = bindService(new Intent(MainActivity.this, MyService.class), mConn, Context.BIND_AUTO_CREATE);
            if (!bound) {
                Log.d(TAG, "binderDied: bindService false");
            }
        }
    };

    private IOnNewBookArrivedListener.Stub mBinder = new IOnNewBookArrivedListener.Stub() {

        @DebugLog
        @Override
        public void onNewBookArrived(final Book newBook) throws RemoteException {
            //运行在客户端Binder线程池中
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //切换到主线程回调
                    onNewBookArrivedMainThread(newBook);
                }
            });
        }
    };

    @DebugLog
    private void onNewBookArrivedMainThread(Book newBook) {
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @DebugLog
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mIsBind = true;
            synchronized (mLock) {
                mBookManager = IBookManager.Stub.asInterface(binder);
                //通知底层工作线程数据已经写入，可以读取了
                mLock.notify();
            }
            boolean isBinderAlive = binder.isBinderAlive();
            try {
                //register callback
                binder.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //register listener
            try {
                mBookManager.registerListener((IOnNewBookArrivedListener) mBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //
            try {
                mBookManager.getBookList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

        @DebugLog
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBind = false;
            synchronized (mLock) {
                mBookManager = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean bound = bindService(new Intent(MainActivity.this, MyService.class), mConn, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.d(TAG, "onCreate: bindService false");
        }


        UserManager.sId = 1;
        Log.d(TAG, "onCreate: " + UserManager.sId);
//        try {
//            writeObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        startActivity(new Intent(this, Main2Activity.class));


    }

    @Override
    protected void onDestroy() {
        //判读对面的Binder是否活着
        if (mBookManager != null && mBookManager.asBinder().isBinderAlive()) {
            try {
                mBookManager.unregisterListener((IOnNewBookArrivedListener) mBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mIsBind)
            unbindService(mConn);
        super.onDestroy();
    }

    private void writeObject() throws IOException {
        User user = new User(0, "liguang", true);
        Log.d(TAG, "writeObject: " + user);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                Environment.getExternalStorageDirectory() + "/liguang/cache.txt"));
        out.writeObject(user);
        out.close();
    }

    private class Worker extends AsyncTask<Void, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(Void... params) {
            if (isCancelled())
                return null;

            synchronized (mLock) {
                //子线程等待主线程连接服务后才能执行后续RPC调用
                while (mBookManager == null || !mBookManager.asBinder().isBinderAlive()) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    //RPC调用，需要在子线程中调用
                    List<Book> books = mBookManager.getBookList();
                    return books;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
