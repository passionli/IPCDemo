// IBookManager.aidl
package com.liguang.ipcdemo;

// Declare any non-default types here with import statements
import com.liguang.ipcdemo.Book;
import com.liguang.ipcdemo.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);

    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
