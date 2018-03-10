package com.aco;

import java.sql.*;

import java.util.ArrayList;

public class HorarioRepository {
  Connection con = null;

  public HorarioRepository() {
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

  public ArrayList<String> findSemestersByCourse(String curso) {
    String sql = "SELECT ds_oferta_semestre FROM ofertas WHERE ds_nome_curso='" + curso +
                "' GROUP BY ds_oferta_semestre";
    try {
      ArrayList<String> semestres = new ArrayList<String>();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        semestres.add(rs.getString(1));
      }
      return semestres;
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }
  }
  public ArrayList<Horario> findAll(String curso, String semestre) {
    ArrayList<Horario> ofertas = new ArrayList<Horario>();
    String sql = "SELECT disciplinas.ds_nome, disciplinas.ds_nome_curso, disciplinas.nr_carga_horaria, " + 
                 "disciplinas.nr_periodo, disciplinas.id_disciplina, disciplinas.ds_ciclo, " + 
                 "ofertas.cod_oferta, ofertas.ds_nome_curso, ofertas.id_disciplina, ofertas.ds_dia, " +
                 "ofertas.nr_horario_inicial, ofertas.nr_duracao_horas " +
                 "from disciplinas INNER JOIN ofertas ON " + 
                 "ofertas.id_disciplina=disciplinas.id_disciplina " + 
                 "WHERE ofertas.ds_nome_curso='" + curso + "' AND ofertas.ds_oferta_semestre='" + semestre + "'";
    try { 
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome(rs.getString(1));
        disciplina.setNomeCurso(rs.getString(2));
        disciplina.setCargaHoraria(rs.getInt(3));
        disciplina.setPeriodo(rs.getInt(4));
        disciplina.setCodDisciplina(rs.getString(5));
        disciplina.setCiclo(rs.getString(6));

        Horario oferta = new Horario();
        oferta.setCodOferta(rs.getString(7));
        oferta.setNomeCurso(rs.getString(8));
        oferta.setCodDisciplina(rs.getString(9));
        oferta.setDia(rs.getString(10));
        oferta.setHorarioInicial(rs.getInt(11));
        oferta.setDuracaoHoras(rs.getInt(12));
        oferta.setDisciplinaOfertada(disciplina);
        ofertas.add(oferta);
      }
    } catch (Exception e) {
      System.out.println(e);
    }

    return ofertas;
  }

  public void create(ArrayList<Horario> ofertas, String curso, String semestre) {
    String sql = "insert into ofertas (cod_oferta, id_disciplina, ds_dia, ds_oferta_semestre, nr_horario_inicial, nr_duracao_horas, ds_nome_curso) " + 
    "values(?, ?, ?, ?, ?, ?, ?)";

    try {
      PreparedStatement st = con.prepareStatement(sql);

      for(Horario oferta : ofertas) {
        st.setString(1, oferta.getCodOferta());
        st.setString(2, oferta.getCodDisciplina());
        st.setString(3, oferta.getDia());
        st.setString(4, semestre);
        st.setInt(5, oferta.getHorarioInicial());
        st.setInt(6, oferta.getDuracaoHoras());
        st.setString(7, curso);
        st.executeUpdate();
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }
  }
}