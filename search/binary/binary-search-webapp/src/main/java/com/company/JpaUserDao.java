package com.company;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class JpaUserDao implements UserDao
{
  
  private final String QUERY_ALL = "select u from User u";

  private EntityManager em;

  @PersistenceContext
  public void setEntityManager(EntityManager entityManager) {
      this.em = entityManager;
  }

  public List<User> queryAll()
  {
    return em.createQuery(QUERY_ALL).getResultList();
  }

  public void remove(User user)
  {
    User toDel = findById(user.getId());
    em.remove(toDel);
  }

  public void save(User user)
  {
    em.persist(user);
  }

  public void update(User user)
  {
    em.merge(user);
  }

  public User findById(Long id)
  {
    return em.find(User.class, id);
  }
}