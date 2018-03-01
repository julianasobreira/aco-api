package com.aco;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

import java.util.*;
import java.net.URI;

import com.aco.Repositories.*;
import com.aco.Entities.*;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/api/v1.0")
@Produces(MediaType.APPLICATION_JSON)
public class MyResource {
    // Repositórios
    private static List<Disciplina> resources = new ArrayList<Disciplina>();
    private CursoRepository cursoRepo = new CursoRepository();
    private DisciplinaRepository disciplinaRepo = new DisciplinaRepository();
    private HorarioRepository horarioRepo = new HorarioRepository();

    // ACO
    private int feromonioInicial = 10000;
    private int alfa = 2;
    private int beta = 1;
    private int quantidadeMaximaIteracoes = 200;
    private int quantidadeFormigas = 10;
    private int evaporacao = 1;
    private int ganho = 1;
    private int gama = 50;
    private static int cargaHorariaCursada;
    private static int cargaHorariaTotal = 4290;
    private static ArrayList <Disciplina> disciplinasCursadas = new ArrayList<Disciplina>();
    private static ArrayList <Disciplina> todasDisciplinas = new ArrayList<Disciplina>();
    private static ArrayList <Horario> ofertaDisciplinas = new ArrayList<Horario>();
    private static ArrayList <CompSolucao> solucao = new ArrayList<CompSolucao>();

    @POST
    @Path("/curso")
    public Response createCurso(@QueryParam("curso") String curso) {
        try {
            cursoRepo.create(curso);
            return Response
               .status(200)
               .build();
       } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
       }
        
    }

    @POST
    @Path("/solucao")
    public ArrayList<Horario> getSolucao(@QueryParam("curso") String curso, @QueryParam("semestre") String semestre, ArrayList<Disciplina> cursadas) {   
        try {
            int cho = 0;
            disciplinasCursadas = cursadas;
            todasDisciplinas = disciplinaRepo.findAll(curso);
            ofertaDisciplinas = horarioRepo.findAll(curso, semestre);
            ArrayList<Disciplina> disciplinasPossiveis = obterDisciplinasPossiveis();
            ArrayList<Horario> horariosPossiveis = obterHorariosPossiveis(disciplinasPossiveis);
            for(int i=0;i<disciplinasCursadas.size();i++)
                if(disciplinasCursadas.get(i).getPeriodo() == 0) 
                    cho+=disciplinasCursadas.get(i).getCargaHoraria();
            AntColonyOptimization ACO = new AntColonyOptimization(
                horariosPossiveis,
                todasDisciplinas,
                feromonioInicial, 
                alfa, 
                beta, 
                quantidadeMaximaIteracoes, 
                quantidadeFormigas, 
                evaporacao, 
                ganho, 
                gama, 
                cho
            );
            solucao = ACO.melhorGrade();
            ArrayList<Horario> gradePersonalizada = new ArrayList<Horario>();
            for(CompSolucao solucaoItem : solucao) {
                for(Horario oferta : ofertaDisciplinas) {
                    if (solucaoItem.getDisciplina().getCodDisciplina().equals(oferta.getCodDisciplina())) {
                        gradePersonalizada.add(oferta);
                    }
                }
            }
            return gradePersonalizada;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private static ArrayList < Horario > obterHorariosPossiveis(ArrayList < Disciplina > disciplinasPossiveis) {
        ArrayList < Horario > horariosPossiveis = new ArrayList < Horario > ();
        for (int i = 0; i < ofertaDisciplinas.size(); i++) {
            for (int j = 0; j < disciplinasPossiveis.size(); j++) {
                if (ofertaDisciplinas.get(i).getDisciplinaOfertada().getCodDisciplina().equals(disciplinasPossiveis.get(j).getCodDisciplina())) {
                    Horario h = ofertaDisciplinas.get(i);
                    horariosPossiveis.add(h);
                    break;
                }
            }
        }
        return horariosPossiveis;
    }

    private static boolean cursada(Disciplina x) {
        for(int i=0;i<disciplinasCursadas.size();i++) {
            if (x.getCodDisciplina().equals(disciplinasCursadas.get(i).getCodDisciplina())) return true;
        }
        return false;
    }

    private static int getCargaHorariaCursada() {
        int sum = 0;
        for(int i=0;i<disciplinasCursadas.size();i++) {
            sum += disciplinasCursadas.get(i).getCargaHoraria();
        }
        return sum;
    }

    private static ArrayList < Disciplina > obterDisciplinasPossiveis() {
        ArrayList < Disciplina > disciplinasPossiveis = new ArrayList < Disciplina > ();
        //pre requisitos
        for (int i = 0; i < todasDisciplinas.size(); i++) {
            boolean discPossivel = true;
            for (int j = 0; j < todasDisciplinas.get(i).getPreRequisitos().size(); j++) {
                boolean temPreReq = false;
                for (int k = 0; k < disciplinasCursadas.size(); k++) {
                    if (todasDisciplinas.get(i).getPreRequisitos().get(j).equals(disciplinasCursadas.get(k).getCodDisciplina())) {
                        temPreReq = true;
                        break;
                    }
                }
                if (!temPreReq) {
                    discPossivel = false;
                    break;
                }
            }
            if (discPossivel && !cursada(todasDisciplinas.get(i))) disciplinasPossiveis.add(todasDisciplinas.get(i));
        }
        //CO REQUISITOS
        int size;
        do {
            size = disciplinasPossiveis.size();
            for (int i = 0; i < disciplinasPossiveis.size(); i++) {
                ArrayList < String > coReq = disciplinasPossiveis.get(i).getCoRequisitos();
                boolean temCoReq = true;
                for (int j = 0; j < coReq.size(); j++) {
                    boolean encontrou = false;
                    for (int k = 0; k < disciplinasPossiveis.size(); k++) {
                        if (coReq.get(j).equals(disciplinasPossiveis.get(k).getCodDisciplina())) {
                            encontrou = true;
                            break;
                        }
                    }
                    for (int k = 0; k < disciplinasCursadas.size(); k++) {
                        if (coReq.get(j).equals(disciplinasCursadas.get(k).getCodDisciplina())) {
                            encontrou = true;
                            break;
                        }
                    }
                    if (!encontrou) temCoReq = false;
                }
                if (!temCoReq) disciplinasPossiveis.remove(disciplinasPossiveis.get(i));
            }
        } while (size != disciplinasPossiveis.size());
        if (cargaHorariaCursada < (cargaHorariaTotal * 0.75)) {
            for (int i = 0; i < disciplinasPossiveis.size(); i++) {
                if (disciplinasPossiveis.get(i).getCodDisciplina().equals("CCMP0078")) {
                    disciplinasPossiveis.remove(disciplinasPossiveis.get(i));
                }
                if (disciplinasPossiveis.get(i).getCodDisciplina().equals("CCMP0080")) {
                    disciplinasPossiveis.remove(disciplinasPossiveis.get(i));
                }
            }
        }
        return disciplinasPossiveis;
    }

    @GET
    @Path("/grade")
    public ArrayList<Disciplina> getGrade(@QueryParam("curso") String curso) {
        try {
            return disciplinaRepo.findAll(curso);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/grade")
    public Response createGrade(@QueryParam("curso") String curso, ArrayList<Disciplina> disciplinas) {
        try {
            disciplinaRepo.create(disciplinas, curso);
            return Response
               .status(200)
               .build();
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        
    }

    @GET
    @Path("/oferta")
    public ArrayList<Horario> getOferta(@QueryParam("curso") String curso, @QueryParam("semestre") String semestre) {
        try {
            return horarioRepo.findAll(curso, semestre);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @POST
    @Path("/oferta")
    public Response createOferta(@Context UriInfo uriInfo, ArrayList<Horario> ofertas) {
        String curso = uriInfo.getQueryParameters().getFirst("curso");
        String semestre = uriInfo.getQueryParameters().getFirst("semestre");
        try {
            horarioRepo.create(ofertas, curso, semestre);
            return Response
               .status(200)
               .build();
       } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
       }
        
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
}
