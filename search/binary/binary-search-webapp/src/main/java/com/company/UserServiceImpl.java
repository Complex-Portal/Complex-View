package com.company;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class UserServiceImpl implements UserService
{

  private UserDao userDao;
  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public List<User> queryAllUsers()
  {
    return userDao.queryAll();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void removeUser(User user)
  {
    userDao.remove(user);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void saveUser(User user)
  {
    userDao.save(user);
  }
  
  @Transactional(propagation = Propagation.REQUIRED)
  public void updateUser(User user)
  {
    userDao.update(user);
  }
}