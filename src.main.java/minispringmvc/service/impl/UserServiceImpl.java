package service.impl;

import annotation.Qualifier;
import annotation.Service;
import dao.UserDao;
import service.UserService;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Qualifier("userDaoImpl")
    private UserDao userDao;
    @Override
    public void insert() {
        System.out.println("UserServiceImpl.insert() start...");
        userDao.insert();
        System.out.println("UserServiceImpl.insert() end.");
    }
}
