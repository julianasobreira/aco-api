package com.example.rest.entities;

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
public class Curso {
  private String nome;
  private int id;
  private ArrayList<Disciplina> disciplinas = new ArrayList<Disciplina>();;
  private ArrayList<String> semestres = new ArrayList<String>(); 
  private LocalDateTime createdTime;
}