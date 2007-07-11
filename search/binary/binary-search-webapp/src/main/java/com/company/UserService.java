package com.company;

import java.util.List;

public interface UserService
{

  void saveUser(User user);
  void removeUser(User user);
  void updateUser(User user);
  List<User> queryAllUsers();
}