package dao.impl;

import annotation.Repository;
import dao.UserDao;

@Repository("userDaoImpl")
public class UserDaoImpl implements UserDao {

    @Override
    public void insert() {
        System.out.println("execute UserDaoImpl.insert()....");
    }
}
