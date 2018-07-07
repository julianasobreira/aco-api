package com.aco.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
@ToString(callSuper=true, includeFieldNames=true)
public class Horario {
  private String idOferta;
  private String codOferta;
  private Disciplina disciplinaOfertada;
  private String codDisciplina;
  private String dia;
  private String semestre;
  private int horarioInicial;
  private int duracaoHoras;
  private LocalDateTime createdTime;
}