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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

import java.util.*;
import java.net.URI;
import com.aco.entities.*;
import com.google.gson.*;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.xml.bind.DatatypeConverter;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Claims;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/api/v1.0")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {
  String FRASE_SEGREDO = "FRASE_SEGREDO";
  @POST
  @Path("/login")
  public Response login(String credentialJson) {
    try {
      Gson gson = new Gson();
      Credential credencial = gson.fromJson(credentialJson, Credential.class);
      validarCrendenciais(credencial);

      String token = gerarToken(credencial.getLogin(),1);
      return Response.ok(token).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.UNAUTHORIZED).build();
    }  
  }

  private void validarCrendenciais(Credential crendencial) throws Exception {
    try {
      if(!crendencial.getLogin().equals("teste") || !crendencial.getSenha().equals("123"))
        throw new Exception("Crendencias não válidas!");
    } catch (Exception e) {
      throw e;
    } 
  }

  private String gerarToken(String login,Integer expiraEmDias ) {
    SignatureAlgorithm algoritimoAssinatura = SignatureAlgorithm.HS512;
    Date agora = new Date();
    Calendar expira = Calendar.getInstance();
    expira.add(Calendar.DAY_OF_MONTH, expiraEmDias);

    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(FRASE_SEGREDO);
    SecretKeySpec key = new SecretKeySpec(apiKeySecretBytes, algoritimoAssinatura.getJcaName());
    JwtBuilder construtor = Jwts.builder()
      .setIssuedAt(agora)
      .setIssuer(login)
      .signWith(algoritimoAssinatura, key)
      .setExpiration(expira.getTime());
    return construtor.compact();//Constrói o token retornando ele como uma String
  }

  public Claims validaToken(String token) {
    try{
      Claims claims = Jwts.parser()
        .setSigningKey(DatatypeConverter.parseBase64Binary(FRASE_SEGREDO))
        .parseClaimsJws(token).getBody();
        System.out.println(claims.getIssuer());
        return claims;
      } catch(Exception ex) {
        throw ex;
      }
    }
}
