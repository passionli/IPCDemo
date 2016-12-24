package com.liguang.ipcdemo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import hugo.weaving.DebugLog;

public class MyService extends Service {
    private static final String TAG = "MyService";

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean();

    private RemoteCallbackList<IOnNewBookArrivedListener> mListeners = new RemoteCallbackList<>();
    private final List<Book> mBookList = new CopyOnWriteArrayList<>();
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private Runnable mNotifier = new Runnable() {
        int i = 0;

        @Override
        public void run() {
            while (!mIsServiceDestroyed.get()) {
                Book book = new Book(i++, "liguang " + i);
                mBookList.add(book);
                onNewBookArrived(book);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void onNewBookArrived(Book book) {
        int n = mListeners.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnNewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            try {
                //调用客户端BinderStub
                listener.onNewBookArrived(book);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mListeners.finishBroadcast();
    }

    /**
     * Binder 服务器端Stub
     */
    private final IBookManager.Stub mBinder = new IBookManager.Stub() {

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //权限检查
            int check = checkCallingOrSelfPermission("com.liguang.ipcdemo.permission.ACCESS_BOOK_SERVICE");
            if (check == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "onBind: PERMISSION_DENIED");
                //非法
                return false;
            }

            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }

            if (!packageName.startsWith("com.liguang1")) {
                return false;
            }

            //再到父类
            return super.onTransact(code, data, reply, flags);
        }

        @DebugLog
        @Override
        public List<Book> getBookList() throws RemoteException {
            SystemClock.sleep(10000);
            return mBookList;
        }

        @DebugLog
        @Override
        public void addBook(Book book) throws RemoteException {
            if (!mBookList.contains(book)) {
                mBookList.add(book);
            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListeners.register(listener);
            Log.d(TAG, "registerListener: " + mListeners.getRegisteredCallbackCount());
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListeners.unregister(listener);
            Log.d(TAG, "unregisterListener: " + mListeners.getRegisteredCallbackCount());
        }
    };

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //检查调用进程的权限
        int check = checkCallingOrSelfPermission("com.liguang.ipcdemo.permission.ACCESS_BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "onBind: PERMISSION_DENIED");
            //非法
            return null;
        }
        mExecutor.execute(mNotifier);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        //UI线程
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }
}
