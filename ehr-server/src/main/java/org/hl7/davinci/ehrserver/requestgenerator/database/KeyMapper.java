package org.hl7.davinci.ehrserver.requestgenerator.database;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KeyMapper implements RowMapper<Key> {

  @Override
  public Key mapRow(ResultSet rs, int rowNum) throws SQLException {
    Key key = new Key();
    key.setId(rs.getString("id"));
    key.setE(rs.getString("e"));
    key.setKty(rs.getString("kty"));
    key.setN(rs.getString("n"));
    return key;
  }
}
