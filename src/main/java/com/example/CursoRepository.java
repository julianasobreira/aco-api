package com.univasf;

import java.sql.*;

public class CursoRepository {
  Connection con = null;

  public CursoRepository() {
    String url = "jdbc:" + System.getenv("CLEARDB_DATABASE_URL");
    String username = System.getenv("DB_USERNAME");
    String password = System.getenv("DB_PWD");
    try {
      Class.forName("com.mysql.jdbc.Driver");
       con = DriverManager.getConnection(url, username, password);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public void create(String nome) {
    String sql = "insert into cursos (ds_nome) values(?)";
    try {
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, nome);
      st.executeUpdate();
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }
  }
}