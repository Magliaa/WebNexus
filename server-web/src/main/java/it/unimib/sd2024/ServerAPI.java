package it.unimib.sd2024;

import it.unimib.sd2024.objs.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static it.unimib.sd2024.DBHandler.*;

/**
 * Rappresenta la risorsa "example" in "http://localhost:8080/example".
 */

/**
 GET: /domains/
 GET: /user-detail/{uid}/
 GET: /orders/
 POST: /buy-domain/ ->
 Body: {
 user: {
 firstName,
 lastName,
 email
 },
 creditCard: {
 cardNumber,
 expirationDate,
 cvv,
 firstName,
 lastName
 }
 }
 POST: /user/ ->
 Body: {
 firstName,
 lastName,
 email
 }
 PUT: /renew-domain/ ->
 Body: {
 id,
 renew_period
 }
 */

@Path("")
public class ServerAPI {

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains() {
        String response = null;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            answer = sendRequest(DBHandler.Command.GET, "domains", List.of("domains"), null, dbConn);
            if (answer == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Path("/domains/{domainName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("domainName") String domainName) {
        String response = null;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            answer = sendRequest(DBHandler.Command.GET, "domains", List.of("domains", domainName), null, dbConn);
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

    /**
     * Implementazione di GET "/domains/user/{userId}"
     */
    @GET
    @Path("domains/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetUserDomains(@PathParam("userId") String userId) {
        String response = null;
        List<String> answer;

        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            var result = jsonb.toJson(userId, String.class);

            answer = sendRequest(Command.GET_IF, "domains", List.of("domains"), String.valueOf(List.of("userId", result)), dbConn);

            if (answer == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            response = answer.get(1);
            System.out.println("test" + response);

            closeConnection(dbConn);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        return Response.ok(response).build();
    }

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Path("orders/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetOrders(@PathParam("userId") String userId) {
        String response = null;
        List<String> answer;
        try {
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

    /**
     * Implementazione di GET "/domains".
     */
    @POST
    @Path("user/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterDomain(RegisterPayload payload) {
        String response = null;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            answer = sendRequest(Command.GET, "users", List.of("users", payload.uid), null, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            var config = new JsonbConfig().withNullValues(false);
            Jsonb jsonb = JsonbBuilder.create(config);

            var domain = new Domain();
            domain.registerDate = LocalDate.now().toString();
            domain.ownershipUserId = payload.uid;
            domain.expirationDate = LocalDate.now().plusYears(Integer.parseInt(payload.registerTime)).toString();

            var domainData = jsonb.toJson(domain, Domain.class);

            answer = sendRequest(Command.SET_IF_NOT_EXISTS, "domains", List.of("domains", payload.domainName), domainData, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            var orderId = ordersId.getAndIncrement();

            var order = new Order();
            order.userId = payload.uid;
            order.cardCvv = payload.cvv;
            order.domain = payload.domainName;
            order.cardExpireDate = payload.expDate;
            order.cardId = payload.cardNumber;
            order.price = "100";
            order.cardName = payload.cardOwnerName;
            order.cardSurname = payload.cardOwnerSurname;

            order.type = "register";

            var orderData = jsonb.toJson(order, Order.class);

            answer = sendRequest(Command.SET, "orders", List.of("orders", ""+orderId), orderData, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            response = answer.get(1);
            System.out.println(response);

            closeConnection(dbConn);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(response).build();
    }

    /**
     * Implementazione di GET "/domains".
     */
    @POST
    @Path("user/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterUser(UserRegisterPayload payload) {
        String response = null;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            var result = jsonb.toJson(payload.email, String.class);
            System.out.println(result);

            answer = sendRequest(Command.GET_IF, "users", List.of("users"), String.valueOf(List.of("email", result)), dbConn);

            if (answer.getFirst().equals("false"))
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

            if (!answer.get(1).equals("{}"))
                return Response.status(Response.Status.CONFLICT).build();

            var userId = id.getAndAdd(1);
            var answer2 = sendRequest(Command.SET_IF_NOT_EXISTS, "users", List.of("users", ""+userId), jsonb.toJson(payload), dbConn);

            if (answer2.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            response = answer2.get(1);
            System.out.println(response);

            UserSignupResponse signupResponse = new UserSignupResponse();
            signupResponse.uid = String.valueOf(userId);

            response = jsonb.toJson(signupResponse);

            closeConnection(dbConn);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(response).build();
    }

    /**
     * Implementazione di GET "/domains".
     */
    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetUsersDomains(@PathParam("userId") String userId) {
        String response = null;
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            var result = jsonb.toJson(userId, String.class);
            System.out.println(result);

            answer = sendRequest(Command.GET_IF, "domains", List.of("domains"), String.valueOf(List.of("ownershipUserId", result)), dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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