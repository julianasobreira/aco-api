package com.example.rest.exception;

import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MyException extends WebApplicationException {
    public MyException(Integer code, String message) {
        super(Response.status(code)
            .entity(createJSONError(code, message).toString())
            .type(MediaType.APPLICATION_JSON)
            .build());
    }

    private static JSONObject createJSONError(Integer code, String message) {
      try {
        JSONObject error = new JSONObject();
        error.put("code", code);
        error.put("detail", message);
        return error;
      } catch (JSONException e) {
        System.out.println(e.getMessage());
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
}
