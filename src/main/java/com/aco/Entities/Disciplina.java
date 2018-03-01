package com.aco;

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
public class Disciplina {
  private String codDisciplina;
  private String nome;
  private String nomeCurso;
  private String ciclo;
  private int cargaHoraria;
  private ArrayList<String> preRequisitos = new ArrayList<String>();
  private ArrayList<String> coRequisitos = new ArrayList<String>();
  private ArrayList<String> proRequisitos = new ArrayList<String>();
  private ArrayList<String> equivalencias = new ArrayList<String>();
  private int periodo;
  private LocalDateTime createdTime;
}