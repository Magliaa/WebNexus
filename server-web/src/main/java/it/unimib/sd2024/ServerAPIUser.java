package it.unimib.sd2024;

import it.unimib.sd2024.objs.UserRegisterPayload;
import it.unimib.sd2024.objs.UserSignupResponse;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

import static it.unimib.sd2024.DBHandler.*;

@Path("/user")
public class ServerAPIUser {
    /**
     * Implementazione di POST "/user/signup".
     * @param payload dati dell'utente da registrare.
     *                email: email dell'utente.
     *                name: nome dell'utente.
     *                surname: cognome dell'utente.
     * @return id dell'utente registrato, restituisce 200.
     * Se l'email è già registrata, restituisce 409.
     * Se c'è un errore interno, restituisce 500.
     * Se i dati non sono validi, restituisce 400.
     */
    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterUser(UserRegisterPayload payload) {
        String response;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            var result = jsonb.toJson(payload.email, String.class);
            System.out.println(result);

            synchronized (DBHandler.class) {
                answer = sendRequest(DBHandler.Command.GET_IF, "users", List.of("users"), String.valueOf(List.of("email", result)), dbConn);

                if (answer.getFirst().equals("false"))
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

                if (!answer.get(1).equals("{}"))
                    return Response.status(Response.Status.CONFLICT).build();

                var userId = DBHandler.userId.getAndAdd(1);
                var answer2 = sendRequest(DBHandler.Command.SET, "users", List.of("users", "" + userId), jsonb.toJson(payload), dbConn);

                if (answer2.getFirst().equals("false")) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }

                UserSignupResponse signupResponse = new UserSignupResponse();
                signupResponse.uid = String.valueOf(userId);

                response = jsonb.toJson(signupResponse);
            }

            closeConnection(dbConn);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(response).build();
    }
}
