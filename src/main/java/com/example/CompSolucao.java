package com.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
@ToString(callSuper=true, includeFieldNames=true)
public class CompSolucao {
  private String codOferta;
  private Disciplina disciplina;
  private int feromonio;
  private boolean factivel;

  public CompSolucao(String codOferta, Disciplina disciplina, int feromonio) {
    this.codOferta = codOferta;
    this.disciplina = disciplina;
    this.feromonio = feromonio;
  }
}