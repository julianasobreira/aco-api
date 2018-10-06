package com.example.rest.repositories;

import java.sql.*;
import java.util.ArrayList;
import com.example.rest.exception.MyException;
import com.example.rest.entities.Curso;

public class CursoRepository {
  Connection con = null;

  public CursoRepository() {
    // String database = System.getenv("DB_DATABASE");
    // String url = "jdbc:mysql://" + database + ":3306/aco?autoReconnect=true";
    String url = "jdbc:mysql://database:3306/aco?autoReconnect=true";
    String username = System.getenv("DB_USER");
    String password = System.getenv("DB_PWD");
    try {
      Class.forName("com.mysql.jdbc.Driver");
      con = DriverManager.getConnection(url, username, password);
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
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

  public ArrayList<Curso> findAll() {
    String sql = "SELECT cursos.id_curso, cursos.ds_nome FROM cursos";
    ArrayList<Curso> cursos = new ArrayList<Curso>();
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        Curso curso = new Curso();
        curso.setId(rs.getInt(1));
        curso.setNome(rs.getString(2));
        cursos.add(curso);
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }

    return cursos;
  }
}