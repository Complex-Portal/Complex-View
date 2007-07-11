package com.company;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Address
{
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;
  private String street;
  private String zip;
  private String city;
  private String state;

  public String getCity()
  {
    return city;
  }
  
  public void setCity(String city)
  {
    this.city = city;
  }

  public String getState()
  {
    return state;
  }
  
  public void setState(String state)
  {
    this.state = state;
  }
  
  public String getStreet()
  {
    return street;
  }
  
  public void setStreet(String street)
  {
    this.street = street;
  }
  
  public String getZip()
  {
    return zip;
  }
  
  public void setZip(String zip)
  {
    this.zip = zip;
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