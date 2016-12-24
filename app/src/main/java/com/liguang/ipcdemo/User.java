package com.liguang.ipcdemo;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/21.
 */

public class User implements Serializable {
    public long serialVersionUID = 34513451354L;
//    public long serialVersionUID = 1L;
    public int userId;
    public boolean isMale;
    public String userName;
    public String userName1;

    public User(int userId, String userName, boolean isMale) {
        this.userId = userId;
        this.userName = userName;
        this.isMale = isMale;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", isMale=" + isMale +
                '}';
    }
}
