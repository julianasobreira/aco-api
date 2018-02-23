package com.example;

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
public class Dependencia {
  private int id;
  private String tipo;
  private String codDisciplina;
  private String codDisciplinaDependencia;
  private LocalDateTime createdTime;
}