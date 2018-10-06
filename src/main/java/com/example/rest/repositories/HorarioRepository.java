package com.example.rest.repositories;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.TimeZone ;
import java.util.ArrayList;
import com.example.rest.entities.Horario;
import com.example.rest.entities.Disciplina;
import com.example.rest.exception.MyException;

public class HorarioRepository {
  Connection con = null;

  public HorarioRepository() {
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
    HashMap<String, Horario> ofertasMap = new HashMap<String, Horario>();
    HashMap<String, Disciplina> disciplinasMap = new HashMap<String, Disciplina>();
    String query = "SELECT disciplinas.ds_nome, disciplinas.id_curso, disciplinas.nr_carga_horaria, " + 
                   "disciplinas.nr_periodo, disciplinas.id_disciplina, disciplinas.ds_ciclo, " + 
                   "dependencias.ds_tipo, dependencias.id_disciplina_dependencia, " +
                   "ofertas.cod_oferta, ofertas.id_disciplina, ofertas.ds_dia, " +
                   "ofertas.nr_horario_inicial, ofertas.nr_duracao_horas, ofertas.ds_oferta_semestre, ofertas.created_at " +
                   "from disciplinas " +
                   "INNER JOIN ofertas ON ofertas.id_disciplina=disciplinas.id_disciplina " +
                   "LEFT JOIN dependencias ON dependencias.id_disciplina=disciplinas.id_disciplina ";
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
        // confere se registro existe
        String codDisciplina = rs.getString(5);
        String codOferta = rs.getString(9);
        String dia = rs.getString(11);
        String hashCodDisciplina = codDisciplina + codOferta + dia;
        Disciplina value = disciplinasMap.get(hashCodDisciplina);
        if (value == null) {
          Disciplina disciplina = new Disciplina();
          disciplina.setNome(rs.getString(1));
          disciplina.setCodCurso(rs.getInt(2));
          disciplina.setCargaHoraria(rs.getInt(3));
          disciplina.setPeriodo(rs.getInt(4));
          disciplina.setCodDisciplina(codDisciplina);
          disciplina.setCiclo(rs.getString(6));
          disciplinasMap.put(hashCodDisciplina, disciplina);
        }
        String tipoDpendencia = rs.getString(7);
        String codDisciplinaDependencia = rs.getString(8);
        if (Objects.equals(tipoDpendencia, "eq")) {
          disciplinasMap.get(hashCodDisciplina).getEquivalencias().add(codDisciplinaDependencia);
          disciplinasMap.put(hashCodDisciplina, disciplinasMap.get(hashCodDisciplina));
        } else if (Objects.equals(tipoDpendencia, "co")) {
          disciplinasMap.get(hashCodDisciplina).getCoRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(hashCodDisciplina, disciplinasMap.get(hashCodDisciplina));
        } else if (Objects.equals(tipoDpendencia, "pre")) {
          disciplinasMap.get(hashCodDisciplina).getPreRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(hashCodDisciplina, disciplinasMap.get(hashCodDisciplina));
        } else if (Objects.equals(tipoDpendencia, "pro")) {
          disciplinasMap.get(hashCodDisciplina).getProRequisitos().add(codDisciplinaDependencia);
          disciplinasMap.put(hashCodDisciplina, disciplinasMap.get(hashCodDisciplina));
        }   

        Horario oferta = new Horario();
        oferta.setCodOferta(codOferta);
        oferta.setCodDisciplina(rs.getString(10));
        oferta.setDia(dia);
        oferta.setHorarioInicial(rs.getInt(12));
        oferta.setDuracaoHoras(rs.getInt(13));
        oferta.setSemestre(rs.getString(14));
        oferta.setCreatedTime(rs.getTimestamp(15).toLocalDateTime());
        oferta.setDisciplinaOfertada(disciplinasMap.get(hashCodDisciplina));
        ofertasMap.put(codOferta + dia, oferta);
      }
    } catch (Exception e) {
      System.out.println(e);
      throw new java.lang.Error(e);
    }

    ArrayList<Horario> ofertas = new ArrayList<Horario>(ofertasMap.values());
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