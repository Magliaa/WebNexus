package it.unimib.sd2024;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

import static it.unimib.sd2024.DBHandler.*;


@Path("/orders")
public class ServerAPIOrders {
    /**
     * Implementazione di GET "/orders/{userId}".
     * @param userId id dell'utente.
     * @return lista degli ordini dell'utente, restituisce 200.
     * Se l'id dell'utente è vuoto, restituisce 400.
     * Se l'utente non esiste, restituisce 404.
     * Se c'è un errore interno, restituisce 500.
     * Se l'utente non ha ordini, restituisce 200 con una lista vuota.
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetOrders(@PathParam("userId") String userId) {
        String response = null;
        List<String> answer;
        try {
            if (userId == null || userId.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            var result = jsonb.toJson(userId, String.class);
            System.out.println(result);

            answer = sendRequest(Command.GET_IF, "orders", List.of("orders"), String.valueOf(List.of("userId", result)), dbConn);
            if (answer == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            response = answer.get(1);
            System.out.println(response);

            closeConnection(dbConn);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        return Response.ok(response).build();
    }
}

