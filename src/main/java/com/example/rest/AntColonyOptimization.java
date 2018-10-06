package com.example.rest;

import java.util.ArrayList;
import java.util.Random;
import org.jfree.data.category.DefaultCategoryDataset;
import com.example.rest.entities.Disciplina;
import com.example.rest.entities.Horario;
import com.example.rest.entities.CompSolucao;

public class AntColonyOptimization {
    private ArrayList < Horario > horariosPossiveis = new ArrayList < Horario > ();
    private ArrayList < Disciplina > todasDisciplinas = new ArrayList < Disciplina > ();
    private ArrayList < CompSolucao > grafo = new ArrayList < CompSolucao > ();
    private DefaultCategoryDataset dados = new DefaultCategoryDataset();
    private DefaultCategoryDataset dados2 = new DefaultCategoryDataset();
    private int alfa;
    private int beta;
    private int delta;
    private int quantidadeMaximaIteracoes;
    private int quantidadeFormigas;
    private int feromonioInicial;
    private int evaporacao;
    private int ganho;
    private int gama;
    private int cargaHorariaOptativaCursada;
    public AntColonyOptimization(ArrayList < Horario > horariosPossiveis, ArrayList < Disciplina > todasDisciplinas, int feromonioInicial, int alfa, int beta, int quantidadeMaximaIteracoes, int quantidadeFormigas, int evaporacao, int ganho, int gama, int cargaHorariaOptativaCursada, int delta) {
        this.horariosPossiveis = horariosPossiveis;
        this.todasDisciplinas = todasDisciplinas;
        this.feromonioInicial = feromonioInicial;
        this.grafo = gerarComponentes(getCods());
        this.alfa = alfa;
        this.beta = beta;
        this.quantidadeMaximaIteracoes = quantidadeMaximaIteracoes;
        this.quantidadeFormigas = quantidadeFormigas;
        this.evaporacao = evaporacao;
        this.ganho = ganho;
        this.gama = gama;
        this.cargaHorariaOptativaCursada = cargaHorariaOptativaCursada;
    }
    public ArrayList < CompSolucao > melhorGrade() {
        ArrayList < CompSolucao > melhorGrade = new ArrayList < CompSolucao > ();
        //GRAFICO 2
        ArrayList < Integer > maximos = new ArrayList < Integer > ();
        ArrayList < Integer > total = new ArrayList < Integer > ();
        //
        //mostrarGrafo();
        boolean melhorou;
        int cont = 0;
        do {
            int media = 0;
            int max = 0;
            int posicaoInicial;
            cont++;
            Random geradorAleatorio = new Random(System.currentTimeMillis());
            posicaoInicial = geradorAleatorio.nextInt(grafo.size()); //Gerando aleatoriamente a posicao inicial das formigas
            melhorou = false;
            for (int i = 0; i < quantidadeFormigas; i++) {
                ArrayList < CompSolucao > solucaoEncontrada = new ArrayList < CompSolucao > ();
                solucaoEncontrada = obterSolucao(posicaoInicial);
                if (funcaoDesempenho(solucaoEncontrada) > funcaoDesempenho(melhorGrade)) {
                    melhorGrade = solucaoEncontrada;
                    melhorou = true;
                }
                atualizarFeromonio(solucaoEncontrada, funcaoDesempenho(solucaoEncontrada));
                //GRAFICO 1
                if (i == 0) max = funcaoDesempenho(solucaoEncontrada);
                else if (funcaoDesempenho(solucaoEncontrada) > max) max = funcaoDesempenho(solucaoEncontrada);
                media += funcaoDesempenho(solucaoEncontrada);
                //GRAFICO 2
                if (maximos.size() < quantidadeFormigas) {
                    maximos.add(funcaoDesempenho(solucaoEncontrada));
                    total.add(funcaoDesempenho(solucaoEncontrada));
                } else {
                    total.set(i, total.get(i) + funcaoDesempenho(solucaoEncontrada));
                    if (funcaoDesempenho(solucaoEncontrada) > maximos.get(i)) {
                        maximos.set(i, funcaoDesempenho(solucaoEncontrada));
                    }
                }
                //
            }
            quantidadeMaximaIteracoes--;
            //GRAFICO 1
            dados.addValue(max, "maximo", "" + cont);
            dados.addValue(media / quantidadeFormigas, "medio", "" + cont);
            //GRAFICO 2
            for (int i = 0; i < quantidadeFormigas; i++) {
                dados2.addValue(maximos.get(i), "maximo", "" + (i + 1));
                int med = total.get(i) / (quantidadeFormigas);
                dados2.addValue(med, "medio", "" + (i + 1));
            }
        } while ( /*melhorou && */ (quantidadeMaximaIteracoes > 0));
        int cht = 0;
        for (int i = 0; i < melhorGrade.size(); i++) cht += melhorGrade.get(i).getDisciplina().getCargaHoraria();
        System.out.println("funcaoDesempenho: " + cht + " " + funcaoDesempenho(melhorGrade));
        return melhorGrade;
    }
    private ArrayList < CompSolucao > obterSolucao(int posicaoInicial) {
        int posicaoNoGrafo = posicaoInicial;
        int cargaHorariaAtingida = 0;
        ArrayList < CompSolucao > solucao = new ArrayList < CompSolucao > ();
        iniciaGrafo();
        do {
            verficarCHOptativa(solucao);
            //mostraFactiveis();
            //Verificando CH total da solucao
            if ((cargaHorariaAtingida + grafo.get(posicaoNoGrafo).getDisciplina().getCargaHoraria()) > 495) break;
            else cargaHorariaAtingida += grafo.get(posicaoNoGrafo).getDisciplina().getCargaHoraria();
            solucao.add(grafo.get(posicaoNoGrafo)); // Adicionando novo componente a solucao
            removeInfactiveis(grafo.get(posicaoNoGrafo)); //Removendo choques e componentes repetidos para manter a factibilidade da solucao
            //Procurando novo componente para adicionar a solucao atraves dos feromonio (funcao probabilistica)
            ArrayList < Double > probabilidade = new ArrayList < Double > ();
            double totalFeromonio = 0;
            for (int i = 0; i < grafo.size(); i++)
                if (grafo.get(i).isFactivel())
                    totalFeromonio += grafo.get(i).getFeromonio();
            for (int i = 0; i < grafo.size(); i++)
                if (grafo.get(i).isFactivel())
                    probabilidade.add(grafo.get(i).getFeromonio() / totalFeromonio);

            Random geradorAleatorio = new Random(System.currentTimeMillis());
            double numero = geradorAleatorio.nextDouble();
            int posicao = probabilidade.size() - 1;
            for (int i = 0; i < probabilidade.size(); i++) {
                numero -= probabilidade.get(i);
                if (numero < 0) {
                    posicao = i;
                    break;
                }
            }
            posicaoNoGrafo = -1;
            do {
                posicaoNoGrafo++;
                if (grafo.get(posicaoNoGrafo).isFactivel())
                    posicao--;
            } while (posicao >= 0);
        } while (existemFactiveis());
        //System.out.println("Saiu do loop");
        return solucao;
    }

    private void verficarCHOptativa(ArrayList < CompSolucao > solucao) {
        int cho = cargaHorariaOptativaCursada;
        for (int i = 0; i < solucao.size(); i++) {
            if (solucao.get(i).getDisciplina().getPeriodo() == 0) {
                cho += solucao.get(i).getDisciplina().getCargaHoraria();
            }
        }
        for (int i = 0; i < grafo.size(); i++) {
            if (grafo.get(i).getDisciplina().getPeriodo() == 0) {
                if ((grafo.get(i).getDisciplina().getCargaHoraria() + cho) > 240) {
                    grafo.get(i).setFactivel(false);
                }
            }
        }
    }
    private void iniciaGrafo() {
        for (int i = 0; i < grafo.size(); i++)
            grafo.get(i).setFactivel(true);
    }
    private void mostraFactiveis() {
        int cont = 0;
        for (int i = 0; i < grafo.size(); i++)
            if (grafo.get(i).isFactivel())
                cont++;
        System.out.println(cont);

    }
    private boolean existemFactiveis() {
        for (int i = 0; i < grafo.size(); i++) {
            if (grafo.get(i).isFactivel()) {
                //System.out.println(grafo.get(i).getCodOferta());
                return true;
            }
        }

        return false;
    }
    private void mostrarGrafo() {
        for (int i = 0; i < grafo.size(); i++) {
            System.out.println("POSSIBILIDADE " + (i + 1));
            System.out.println(grafo.get(i).getCodOferta() + " " + grafo.get(i).getDisciplina().getNome() + " " + grafo.get(i).getFeromonio());
        }
    }

    private void atualizarFeromonio(ArrayList < CompSolucao > solucaoEncontrada, int funcaoDesempenho) {
        int acrescimo = (funcaoDesempenho - 1000) / 100;
        //Atualizando pela qualidade da solu��o
        for (int i = 0; i < solucaoEncontrada.size(); i++)
            for (int j = 0; j < grafo.size(); j++)
                if (solucaoEncontrada.get(i).getCodOferta().equals(grafo.get(j).getCodOferta()))
                    grafo.get(j).setFeromonio((int)(grafo.get(j).getFeromonio() * funcaoDesempenho + acrescimo * ganho));
        //Evapora��o
        for (int i = 0; i < grafo.size(); i++) grafo.get(i).setFeromonio(grafo.get(i).getFeromonio() - evaporacao);
    }
    public int funcaoDesempenho(ArrayList < CompSolucao > solucaoEncontrada) {
        int desempenho = 0;
        int cargaHorariaPosRequisitos = 0;
        int cargaHorariaAtingida = 0;
        int optativas = 0;
        for (int i = 0; i < solucaoEncontrada.size(); i++) cargaHorariaAtingida += solucaoEncontrada.get(i).getDisciplina().getCargaHoraria();

        for (int i = 0; i < solucaoEncontrada.size(); i++) {
            // System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! ----- " + solucaoEncontrada.size() + " ---- " + solucaoEncontrada.get(i).getDisciplina().getCodDisciplina() + " --- " + solucaoEncontrada.get(i).getDisciplina().getProRequisitos().size());
            for (int j = 0; j < solucaoEncontrada.get(i).getDisciplina().getProRequisitos().size(); j++)
                for (int k = 0; k < todasDisciplinas.size(); k++)
                    if (todasDisciplinas.get(k).getCodDisciplina().equals(solucaoEncontrada.get(i).getDisciplina().getProRequisitos().get(j)))
                        cargaHorariaPosRequisitos += todasDisciplinas.get(k).getCargaHoraria();
        }
        int p[] = new int[10];
        for (int i = 0; i < 10; i++)
            p[i] = 0;
        for (int i = 0; i < solucaoEncontrada.size(); i++)
            if (solucaoEncontrada.get(i).getDisciplina().getPeriodo() != 0)
                p[solucaoEncontrada.get(i).getDisciplina().getPeriodo() - 1]++;
        for (int i = 0; i < solucaoEncontrada.size(); i++)
            if (solucaoEncontrada.get(i).getDisciplina().getPeriodo() == 0)
                optativas++;
        int mesmoPeriodoMax = 0;
        for (int i = 0; i < 10; i++)
            if (p[i] > mesmoPeriodoMax)
                mesmoPeriodoMax = p[i];
        int penalidade = solucaoEncontrada.size() - mesmoPeriodoMax;
        boolean temCoReq = true;
        for (int i = 0; i < solucaoEncontrada.size(); i++) {
            temCoReq = true;
            for (int j = 0; j < solucaoEncontrada.get(i).getDisciplina().getCoRequisitos().size(); j++) {
                temCoReq = false;
                for (int k = 0; k < solucaoEncontrada.size(); k++) {
                    if (solucaoEncontrada.get(i).getDisciplina().getCoRequisitos().get(j).equals(solucaoEncontrada.get(k).getDisciplina().getCodDisciplina())) {
                        temCoReq = true;
                    }
                }
            }
            if (!temCoReq) break;
        }
        int penCoReq = 0;
        if (!temCoReq) penCoReq = 1500;
        desempenho = cargaHorariaAtingida * alfa + cargaHorariaPosRequisitos * beta - penalidade * delta - optativas * gama - penCoReq;
        return desempenho;
    }
    private void removeInfactiveis(CompSolucao solucao) {
        Disciplina d = solucao.getDisciplina();
        for (int i = 0; i < grafo.size(); i++)
            if (grafo.get(i).getDisciplina().getCodDisciplina().equals(d.getCodDisciplina())) {
                //System.out.println("Deletou Disciplina Repetida");
                grafo.get(i).setFactivel(false);
            }

        String oferta = solucao.getCodOferta();
        for (int i = 0; i < horariosPossiveis.size(); i++) {
            if (horariosPossiveis.get(i).getCodOferta().equals(oferta)) {
                for (int j = 0; j < grafo.size(); j++) {
                    String codOferta = grafo.get(j).getCodOferta();
                    for (int k = 0; k < horariosPossiveis.size(); k++) {
                        if (horariosPossiveis.get(k).getCodOferta().equals(codOferta)) {
                            if (horariosPossiveis.get(k).getDia().equals(horariosPossiveis.get(i).getDia())) {
                                if (horariosPossiveis.get(k).getHorarioInicial() > horariosPossiveis.get(i).getHorarioInicial()) {
                                    if ((horariosPossiveis.get(i).getHorarioInicial() + horariosPossiveis.get(i).getDuracaoHoras()) > horariosPossiveis.get(k).getHorarioInicial()) {
                                        //System.out.println("Deletou: " +grafo.get(j).getCodOferta() + "por causa de " + solucao.getCodOferta());
                                        grafo.get(j).setFactivel(false);
                                    }
                                } else {
                                    if (horariosPossiveis.get(k).getHorarioInicial() < horariosPossiveis.get(i).getHorarioInicial()) {
                                        if ((horariosPossiveis.get(k).getHorarioInicial() + horariosPossiveis.get(k).getDuracaoHoras()) > horariosPossiveis.get(i).getHorarioInicial()) {
                                            //System.out.println("Deletou: " +grafo.get(j).getCodOferta() + "por causa de " + solucao.getCodOferta());
                                            grafo.get(j).setFactivel(false);
                                        }
                                    } else {
                                        //System.out.println("Deletou: " +grafo.get(j).getCodOferta() + "por causa de " + solucao.getCodOferta());
                                        grafo.get(j).setFactivel(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    private ArrayList < CompSolucao > gerarComponentes(ArrayList < String > codOfertasPossiveis) {
        ArrayList < CompSolucao > componentes = new ArrayList < CompSolucao > ();
        for (int i = 0; i < codOfertasPossiveis.size(); i++) {
            Disciplina d = null;
            for (int j = 0; j < horariosPossiveis.size(); j++) {
                if (codOfertasPossiveis.get(i).equals(horariosPossiveis.get(j).getCodOferta())) {
                    d = horariosPossiveis.get(j).getDisciplinaOfertada();
                    break;
                }
            }
            CompSolucao componente = new CompSolucao(codOfertasPossiveis.get(i), d, feromonioInicial);
            componentes.add(componente);
        }
        return componentes;
    }
    private ArrayList < String > getCods() {
        ArrayList < String > cods = new ArrayList < String > ();
        for (int i = 0; i < horariosPossiveis.size(); i++) {
            String codigo = horariosPossiveis.get(i).getCodOferta();
            boolean temNoArray = false;
            for (int j = 0; j < cods.size(); j++) {
                if (codigo.equals(cods.get(j))) {
                    temNoArray = true;
                    break;
                }
            }
            if (!temNoArray) cods.add(codigo);
        }
        return cods;
    }
    public DefaultCategoryDataset getDados() {
        return dados;
    }
    public DefaultCategoryDataset getDados2() {
        return dados2;
    }
    public int testarGrade(ArrayList < Disciplina > gradeGerada) {
        int desempenho = 0;
        int cargaHorariaPosRequisitos = 0;
        int cargaHorariaAtingida = 0;
        int optativas = 0;
        for (int i = 0; i < gradeGerada.size(); i++)
            cargaHorariaAtingida += gradeGerada.get(i).getCargaHoraria();
        for (int i = 0; i < gradeGerada.size(); i++)
            for (int j = 0; j < gradeGerada.get(i).getProRequisitos().size(); j++)
                for (int k = 0; k < todasDisciplinas.size(); k++)
                    if (todasDisciplinas.get(k).getCodDisciplina().equals(gradeGerada.get(i).getProRequisitos().get(j)))
                        cargaHorariaPosRequisitos += todasDisciplinas.get(k).getCargaHoraria();
        int p[] = new int[10];
        for (int i = 0; i < 10; i++)
            p[i] = 0;
        for (int i = 0; i < gradeGerada.size(); i++)
            if (gradeGerada.get(i).getPeriodo() != 0)
                p[gradeGerada.get(i).getPeriodo() - 1]++;
        for (int i = 0; i < gradeGerada.size(); i++)
            if (gradeGerada.get(i).getPeriodo() == 0)
                optativas++;
        int mesmoPeriodoMax = 0;
        for (int i = 0; i < 10; i++)
            if (p[i] > mesmoPeriodoMax)
                mesmoPeriodoMax = p[i];
        int penalidade = gradeGerada.size() - mesmoPeriodoMax;
        desempenho = cargaHorariaAtingida * alfa + cargaHorariaPosRequisitos * beta - penalidade * delta - optativas * gama;
        //System.out.println(cargaHorariaAtingida + " " + desempenho);
        return desempenho;
    }
}