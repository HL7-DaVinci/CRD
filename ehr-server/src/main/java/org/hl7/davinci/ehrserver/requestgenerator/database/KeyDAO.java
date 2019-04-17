package org.hl7.davinci.ehrserver.requestgenerator.database;

import javax.sql.DataSource;
import java.util.List;

public interface KeyDAO {

  public void createKey(Key key);


  // create a new user record in the users table
  public void create(String id, String e, String n, String kty);

  // get a user with the passed id
  public Key getKey(String id);

  // get all the users from the users table
  public List<Key> listUsers();

  // update a user's email given given the user's id
  public void updateKey(String id, String e, String n, String kty);

  // delete a user record from the users table given the user's id
  public void delete(String id);
}
