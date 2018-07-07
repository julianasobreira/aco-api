package com.aco.repositories;

import java.sql.*;
import com.aco.exception.MyException;
import com.aco.entities.Usuario;

public class UsuarioRepository {
  Connection con = null;

  public UsuarioRepository() {
    String url = "jdbc:" + System.getenv("DB_URL");
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

  public Usuario find(String email) {
    String sql = "SELECT usuarios.ds_nome, usuarios.ds_email, usuarios.ds_senha, usuarios.id_curso" +
                 " FROM usuarios WHERE ds_email=" + email;
    Usuario usuarios = new Usuario();
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        usuarios.setNome(rs.getString(1));
        usuarios.setEmail(rs.getString(2));
        usuarios.setSenha(rs.getString(3));
        usuarios.setCodCurso(rs.getInt(4));
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }

    return usuarios;
  }
}