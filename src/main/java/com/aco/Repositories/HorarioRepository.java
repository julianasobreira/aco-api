package com.aco.repositories;

import java.sql.*;
import java.time.*;
import java.util.TimeZone ;
import java.util.ArrayList;
import com.aco.entities.Horario;
import com.aco.entities.Disciplina;
import com.aco.exception.MyException;

public class HorarioRepository {
  Connection con = null;

  public HorarioRepository() {
    String url = "jdbc:" + System.getenv("DB_URL");
    String username = System.getenv("DB_USER");
    String password = System.getenv("DB_PWD");
    try {
      Class.forName("com.mysql.jdbc.Driver");
      con = DriverManager.getConnection(url, username, password);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public ArrayList<String> findSemestersByCourse(Integer codCurso) {
    String sql = "SELECT ofertas.ds_oferta_semestre as ds_oferta_semestre FROM ofertas " +
                 "LEFT JOIN disciplinas ON ofertas.id_disciplina=disciplinas.id_disciplina " + 
                 "WHERE disciplinas.id_curso='" + codCurso +
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

  public ArrayList<Horario> findAll(Integer codCurso, String semestre) {
    ArrayList<Horario> ofertas = new ArrayList<Horario>();
    String query = "SELECT disciplinas.ds_nome, disciplinas.id_curso, disciplinas.nr_carga_horaria, " + 
                   "disciplinas.nr_periodo, disciplinas.id_disciplina, disciplinas.ds_ciclo, " + 
                   "ofertas.cod_oferta, ofertas.id_disciplina, ofertas.ds_dia, " +
                   "ofertas.nr_horario_inicial, ofertas.nr_duracao_horas, ofertas.ds_oferta_semestre, ofertas.created_at " +
                   "from disciplinas INNER JOIN ofertas ON " + 
                   "ofertas.id_disciplina=disciplinas.id_disciplina ";
    ArrayList<String> conditions = new ArrayList<String>();

    if (codCurso != null) {
      conditions.add("disciplinas.id_curso='" + codCurso + "'");
    }

    if (semestre != null) {
      conditions.add("ofertas.ds_oferta_semestre='" + semestre + "'");
    }

    if (codCurso != null || semestre != null) {
      query = query + "WHERE " + String.join(" AND ", conditions);
    }

    try { 
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next()) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome(rs.getString(1));
        disciplina.setCodCurso(rs.getInt(2));
        disciplina.setCargaHoraria(rs.getInt(3));
        disciplina.setPeriodo(rs.getInt(4));
        disciplina.setCodDisciplina(rs.getString(5));
        disciplina.setCiclo(rs.getString(6));

        Horario oferta = new Horario();
        oferta.setCodOferta(rs.getString(7));
        oferta.setCodDisciplina(rs.getString(8));
        oferta.setDia(rs.getString(9));
        oferta.setHorarioInicial(rs.getInt(10));
        oferta.setDuracaoHoras(rs.getInt(11));
        oferta.setSemestre(rs.getString(12));
        oferta.setCreatedTime(rs.getTimestamp(13).toLocalDateTime());
        oferta.setDisciplinaOfertada(disciplina);
        ofertas.add(oferta);
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }

    return ofertas;
  }

  public void create(ArrayList<Horario> ofertas, String semestre) {
    String query = "INSERT INTO ofertas (cod_oferta, id_disciplina, ds_dia, ds_oferta_semestre, nr_horario_inicial, nr_duracao_horas) " + 
    "VALUES(?, ?, ?, ?, ?, ?)";

    try {
      PreparedStatement st = con.prepareStatement(query);

      for(Horario oferta : ofertas) {
        st.setString(1, oferta.getCodOferta());
        st.setString(2, oferta.getCodDisciplina());
        st.setString(3, oferta.getDia());
        st.setString(4, semestre);
        st.setInt(5, oferta.getHorarioInicial());
        st.setInt(6, oferta.getDuracaoHoras());
        st.executeUpdate();
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }
  }

  public void delete(Integer codCurso, String semestre) {
    String query = "DELETE ofertas FROM ofertas LEFT JOIN disciplinas ON ofertas.id_disciplina=disciplinas.id_disciplina " + 
                   "WHERE disciplinas.id_curso=? AND ofertas.ds_oferta_semestre=?";

    try {
      PreparedStatement st = con.prepareStatement(query);
      st.setInt(1, codCurso);
      st.setString(2, semestre);
      st.executeUpdate();
      System.out.println("==============================");
      System.out.println(st);
      System.out.println(query);
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }
  }
}