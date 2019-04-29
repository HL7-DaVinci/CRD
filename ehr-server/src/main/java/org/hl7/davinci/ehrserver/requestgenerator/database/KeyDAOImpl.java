package org.hl7.davinci.ehrserver.requestgenerator.database;

import org.hl7.davinci.ehrserver.requestgenerator.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class KeyDAOImpl implements KeyDAO {

  static final Logger logger = LoggerFactory.getLogger(KeyDAOImpl.class);

  @Autowired
  private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;


  @PostConstruct
  public void setJdbcTemplate() throws SQLException {
    jdbcTemplate = new JdbcTemplate(dataSource);
    String dbUrl = "jdbc:derby:directory:target/jpaserver_derby_files;create=true";
    Connection conn = DriverManager.getConnection(dbUrl);
    // create table
    Statement stmt = conn.createStatement();
    try {
      jdbcTemplate.execute("Create table publickeys (id varchar(255) primary key, e varchar(10) NOT NULL, n varchar(400) NOT NULL, kty varchar(10) NOT NULL)");
      logger.info("KeyDAOImpl: Public Key table created in database");

    } catch (Exception e) {
      System.out.println(e);
      logger.info("KeyDAOImpl: Public Key table already exists, closing connection to database");
    }
    conn.close();
  }


  @Override
  public void createKey(Key key) {
    String sql = "insert into publickeys (id, e, n, kty) values (?, ?, ?, ?)";
    jdbcTemplate.update(sql, key.getId(), key.getE(), key.getN(), key.getKty());
    logger.info("Created key id = " + key.getId());

  }
  @Override
  public void create(String id, String e, String n, String kty) {
    String sql = "insert into publickeys (name, email) values (?, ?)";
    jdbcTemplate.update(sql, id, e, n, kty);
    logger.info("Created User id = " + id + " kty = " + kty);
  }

  @Override
  public Key getKey(String id) {
    String sql = "select * from publickeys where id = ?";
    List<Key> user = jdbcTemplate.query(sql, new Object[]{id}, new KeyMapper());
    if(user.size()>0) {
      return user.get(0);
    } else {
      throw new ResourceNotFoundException();
    }

  }

  @Override
  public List<Key> listUsers() {
    String sql = "select * from publickeys";
    List<Key> users = jdbcTemplate.query(sql, new KeyMapper());
    return users;
  }

  @Override
  public void updateKey(String id, String e, String n, String kty) {
    String sql = "update publickeys set e = ?, n = ?, kty = ?, where id = ?";
    jdbcTemplate.update(sql, id, e, n, kty);
    logger.info("Updated Record with ID = " + id);
  }

  @Override
  public void delete(String id) {
    String sql = "delete from publickeys where id = ?";
    jdbcTemplate.update(sql, id);
    logger.info("Deleted Record with ID = " + id);
  }
}
