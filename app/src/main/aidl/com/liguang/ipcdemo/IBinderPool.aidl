// IBinderPool.aidl
package com.liguang.ipcdemo;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}
