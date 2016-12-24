// IOnNewBookArrivedListener.aidl
package com.liguang.ipcdemo;

// Declare any non-default types here with import statements
import com.liguang.ipcdemo.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
