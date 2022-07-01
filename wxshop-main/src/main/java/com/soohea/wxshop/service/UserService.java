package com.soohea.wxshop.service;

import com.soohea.wxshop.dao.UserDao;
import com.soohea.wxshop.generate.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNoExist(String tel) {
        User user = new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        try {
            userDao.insertUser(user);
        } catch (DuplicateKeyException e) {
            return userDao.getUserByTel(tel);
        }
        return user;
    }

    /**
     * 根据电话返回用户，若用户不存在，返回null
     *
     * @param tel
     * @return 返回用户
     */
    public User getUserByTel(String tel) {
        return userDao.getUserByTel(tel);
    }
}
