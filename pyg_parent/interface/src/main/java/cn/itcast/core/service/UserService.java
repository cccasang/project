package cn.itcast.core.service;


import cn.itcast.core.pojo.user.User;

public interface UserService {

    public void sendCode(String phone);

    public void add(User user);

    public boolean checkSmsCode(String phone,String smsCode);
}
