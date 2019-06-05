package org.hl7.davinci.ehrserver.authproxy;

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

public class PayloadDAOImpl implements PayloadDAO {

  static final Logger logger = LoggerFactory.getLogger(PayloadDAOImpl.class);

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
      jdbcTemplate.execute("Create table appcontext (launchId varchar(255) primary key, launchUrl varchar(212) NOT NULL, patientId varchar(128) NOT NULL, template varchar(128), request varchar(8192), launchCode varchar(512), redirectUri varchar(512), filepath varchar(128))");

      logger.info("PayloadDAOImpl: AppContext table created in database");

    } catch (Exception e) {
      System.out.println(e);
      logger.info("PayloadDAOImpl: AppContext table already exists, closing connection to database");
    }
    conn.close();
  }


  @Override
  public void createPayload(Payload payload) {
    String sql = "insert into appcontext (launchId, launchUrl, patientId, template, request, filepath) values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, payload.getLaunchId(), payload.getLaunchUrl(),payload.getPatientId(), payload.getTemplate(), payload.getRequest(), payload.getFilepath());
    logger.info("Created payload: " + payload.toString());
  }

  @Override
  public Payload getPayload(String launchId) {
    String sql = "select * from appcontext where launchId = ?";
    List<Payload> payloads = jdbcTemplate.query(sql, new Object[]{launchId}, new PayloadMapper());
    if(payloads.size()>0) {
      return payloads.get(0);
    } else {
      throw new ResourceNotFoundException();
    }
  }

  @Override
  public void updateCode(String launchId, String launchCode) {
    String sql = "update appcontext set launchCode = ? where launchId = ?";
    jdbcTemplate.update(sql, launchCode, launchId);
    logger.info("Updated Record with ID = " + launchId);
  }

  @Override
  public Payload findContextByCode(String launchCode) {
    String sql = "select * from appcontext where launchCode = ?";
    List<Payload> payloads = jdbcTemplate.query(sql, new Object[]{launchCode}, new PayloadMapper());
    if(payloads.size()>0) {
      return payloads.get(0);
    } else {
      throw new ResourceNotFoundException();
    }

  }

  @Override
  public void updateRedirect(String launchId, String redirectUri) {
    String sql = "update appcontext set redirectUri = ? where launchId = ?";
    jdbcTemplate.update(sql, redirectUri, launchId);
    logger.info("Updated Record with ID = " + launchId);
  }
}

