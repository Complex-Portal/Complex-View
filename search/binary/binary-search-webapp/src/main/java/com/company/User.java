package com.company;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class User
{
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;
  private String firstname;
  private String secondname;
  @Temporal(value = TemporalType.DATE) private Date birthday;
  
  @OneToOne(cascade=CascadeType.ALL) private Address address;
  
  public User()
  {
    this.address = new Address();
  }
  
  public Address getAddress()
  {
    return address;
  }
  public void setAddress(Address address)
  {
    this.address = address;
  }
  public Date getBirthday()
  {
    return birthday;
  }
  public void setBirthday(Date birthday)
  {
    this.birthday = birthday;
  }
  public String getFirstname()
  {
    return firstname;
  }
  public void setFirstname(String firstname)
  {
    this.firstname = firstname;
  }
  public String getSecondname()
  {
    return secondname;
  }
  public void setSecondname(String secondname)
  {
    this.secondname = secondname;
  }

  public Long getId()
  {
    return id;
  }
  
  public void setId(Long id)
  {
    this.id = id;
  }
}