package com.univasf;

import java.sql.*;

import java.util.*;

public class DisciplinaRepository {
  Connection con = null;

  public DisciplinaRepository() {
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

  public ArrayList<Disciplina> findAll(String curso) {
    HashMap<String, Disciplina> disciplinasMap = new HashMap<String, Disciplina>();
    String sql = "SELECT disciplinas.ds_nome, disciplinas.ds_nome_curso, disciplinas.nr_carga_horaria, " + 
                 "disciplinas.nr_periodo, disciplinas.id_disciplina, " + 
                 "dependencias.ds_tipo, dependencias.id_disciplina_dependencia " +
                 "from disciplinas LEFT JOIN dependencias ON dependencias.id_disciplina=disciplinas.id_disciplina " + 
                 "WHERE disciplinas.ds_nome_curso='" + curso + "'";
    try { 
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        // confere se registro existe
        String codDisciplina = rs.getString(5);
        Disciplina value = disciplinasMap.get(codDisciplina);
        if (value == null) {
          Disciplina disciplina = new Disciplina();
          disciplina.setNome(rs.getString(1));
          disciplina.setNomeCurso(rs.getString(2));
          disciplina.setCargaHoraria(rs.getInt(3));
          disciplina.setPeriodo(rs.getInt(4));
          disciplina.setCodDisciplina(codDisciplina);
          disciplinasMap.put(codDisciplina, disciplina);
        }
        String tipoDpendencia = rs.getString(6);
        String codDisciplinaDependencia = rs.getString(7);
        if (Objects.equals(tipoDpendencia, "eq")) {
          disciplinasMap.get(codDisciplina).getEquivalencias().add(codDisciplinaDependencia);
          disciplinasMap.put(codDisciplina, disciplinasMap.get(codDisciplina));
        } else if (Objects.equals(tipoDpendencia, "co")) {
          disciplinasMap.get(codDisciplina).getCoRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(codDisciplina, disciplinasMap.get(codDisciplina));
        } else if (Objects.equals(tipoDpendencia, "pre")) {
          disciplinasMap.get(codDisciplina).getPreRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(codDisciplina, disciplinasMap.get(codDisciplina));
        } else if (Objects.equals(tipoDpendencia, "eq")) {
          disciplinasMap.get(codDisciplina).getProRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(codDisciplina, disciplinasMap.get(codDisciplina));
        }        
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    ArrayList<Disciplina> disciplinas = new ArrayList<Disciplina>(disciplinasMap.values());
    return disciplinas;
  }

  public void create(ArrayList<Disciplina> disciplinas, String curso) {
    String sql = "insert into disciplinas (ds_nome, ds_nome_curso, nr_carga_horaria, nr_periodo, id_disciplina) " +
                 "values(?, ?, ?, ?, ?)";
    String sqlDepencia = "insert into dependencias (ds_tipo, id_disciplina, id_disciplina_dependencia) " +
                 "values(?, ?, ?)";
    try { 
      PreparedStatement st = con.prepareStatement(sql);
      PreparedStatement std = con.prepareStatement(sqlDepencia);

      // inserir disciplinas
      for (Disciplina disciplina : disciplinas) {
        st.setString(1, disciplina.getNome());
        st.setString(2, curso);
        st.setInt(3, disciplina.getCargaHoraria());
        st.setInt(4, disciplina.getPeriodo());
        st.setString(5, disciplina.getCodDisciplina());
        st.executeUpdate();
      }

      // inserir dependencias
      for (Disciplina disciplina : disciplinas) {
        // inserir preRequisitos
        for (String preRequisito : disciplina.getPreRequisitos()) {
          std.setString(1, "pre");
          std.setString(2, disciplina.getCodDisciplina());
          std.setString(3, preRequisito);
          std.executeUpdate();
        }

        // inserir coRequisitos
        for (String coRequisito : disciplina.getCoRequisitos()) {
          std.setString(1, "co");
          std.setString(2, disciplina.getCodDisciplina());
          std.setString(3, coRequisito);
          std.executeUpdate();
        }

        // inserir proRequisito
        for (String proRequisito : disciplina.getProRequisitos()) {
          std.setString(1, "pro");
          std.setString(2, disciplina.getCodDisciplina());
          std.setString(3, proRequisito);
          std.executeUpdate();
        }

        // inserir equivalencias
        for (String equivalencia : disciplina.getEquivalencias()) {
          std.setString(1, "eq");
          std.setString(2, disciplina.getCodDisciplina());
          std.setString(3, equivalencia);
          std.executeUpdate();
        }
      }
    } catch (Exception e) {
        System.out.println(e);
        throw new java.lang.Error(e);
    }
  }
}