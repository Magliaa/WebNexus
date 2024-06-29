package it.unimib.sd2024;

import it.unimib.sd2024.objs.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.unimib.sd2024.DBHandler.*;


@Path("")
public class ServerAPI {
    /**
     * Implementazione di GET "/domains/{domainName}".
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

            // Fetch domain details
            answer = sendRequest(DBHandler.Command.GET, "domains", List.of("domains", domainName), null, dbConn);
            if (answer == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Jsonb jsonb = JsonbBuilder.create();
            Domain domain = jsonb.fromJson(answer.get(1), Domain.class);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("domain", domain);

            // Fetch user details if ownershipUserId exists
            if (domain.ownershipUserId != null) {
                List<String> userAnswer = sendRequest(DBHandler.Command.GET, "users", List.of("users", domain.ownershipUserId), null, dbConn);
                if (userAnswer != null && !userAnswer.getFirst().equals("false")) {
                    Object userData = jsonb.fromJson(userAnswer.get(1), Object.class);
                    responseData.put("user", userData);
                }
            }

            response = jsonb.toJson(responseData);
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
     * Implementazione di GET "/domains/user/{userId}
     */
    @GET
    @Path("domains/user/{userId}")
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
     * Implementazione di GET "/orders/{userId}".
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


    // The RegisterDomain method
    @POST
    @Path("domains/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterDomain(RegisterPayload payload) {
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            // Check if user exists
            answer = sendRequest(Command.GET, "users", List.of("users", payload.uid), null, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            var config = new JsonbConfig().withNullValues(false);
            Jsonb jsonb = JsonbBuilder.create(config);

            // Check if domain exists
            answer = sendRequest(Command.GET, "domains", List.of("domains", payload.domainName), null, dbConn);

            boolean domainExists = !answer.getFirst().equals("false");
            Domain domain;
            if (domainExists) {
                domain = jsonb.fromJson(answer.get(1), Domain.class);
                if (domain.ownershipUserId != null) {
                    return Response.status(Response.Status.CONFLICT).entity("Domain already registered").build();
                }
            } else {
                domain = new Domain();
                domain.price = 100 * Integer.parseInt(payload.registerTime); // Set default price
            }

            domain.registerDate = LocalDate.now().toString();
            domain.ownershipUserId = payload.uid;
            domain.expirationDate = LocalDate.now().plusYears(Integer.parseInt(payload.registerTime)).toString();

            var domainData = jsonb.toJson(domain, Domain.class);

            if (domainExists) {
                answer = sendRequest(Command.SET, "domains", List.of("domains", payload.domainName), domainData, dbConn);
            } else {
                answer = sendRequest(Command.SET_IF_NOT_EXISTS, "domains", List.of("domains", payload.domainName), domainData, dbConn);
            }

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            var orderId = ordersId.getAndIncrement();

            var order = new Order();
            order.userId = payload.uid;
            order.cardCvv = payload.cvv;
            order.domain = payload.domainName;
            order.cardExpireDate = LocalDate.now().plusYears(Integer.parseInt(payload.registerTime)).toString();
            order.cardId = payload.cardNumber;
            order.price = domain.price;
            order.cardName = payload.cardOwnerName;
            order.cardSurname = payload.cardOwnerSurname;
            order.type = "register";

            var orderData = jsonb.toJson(order, Order.class);

            answer = sendRequest(Command.SET, "orders", List.of("orders", "" + orderId), orderData, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            closeConnection(dbConn);

            // Return 200 status with an empty JSON object
            return Response.ok("{}").build();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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


    @PUT
    @Path("domains/renew")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renewDomain(RenewPayload payload) {
        List<String> answer;
        try {
            var dbConn = connectToDatabase();

            if (dbConn == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            Jsonb jsonb = JsonbBuilder.create();

            // Check if domain exists
            answer = sendRequest(Command.GET, "domains", List.of("domains", payload.domainName), null, dbConn);

            System.out.println(answer);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Domain not found").build();
            }

            Domain domain = jsonb.fromJson(answer.get(1), Domain.class);

            // Check if the domain is owned by the user
            if (!domain.ownershipUserId.equals(payload.userId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("User does not own this domain").build();
            }

            // Calculate the total registration time
            LocalDate registerDate = LocalDate.parse(domain.registerDate);
            LocalDate currentExpirationDate = LocalDate.parse(domain.expirationDate);
            LocalDate newExpirationDate = currentExpirationDate.plusYears(Integer.parseInt(payload.renewTime));
            long totalYears = ChronoUnit.YEARS.between(registerDate, newExpirationDate);

            System.out.println("anni totali" + totalYears);

            // Check if the total registration time exceeds 10 years
            if (totalYears > 10) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Total registration time cannot exceed 10 years").build();
            }

            // Update the expiration date in the domain
            domain.expirationDate = newExpirationDate.toString();
            var domainData = jsonb.toJson(domain, Domain.class);
            answer = sendRequest(Command.SET, "domains", List.of("domains", payload.domainName), domainData, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            // Create a new order
            var orderId = ordersId.getAndIncrement();

            var order = new Order();
            order.userId = payload.userId;
            order.domain = payload.domainName;
            order.type = "renew";
            order.price = domain.price; // Assuming the price remains the same
            order.cardExpireDate = newExpirationDate.toString(); // Assuming the card expiration date is updated

            var orderData = jsonb.toJson(order, Order.class);
            answer = sendRequest(Command.SET, "orders", List.of("orders", "" + orderId), orderData, dbConn);

            if (answer.getFirst().equals("false")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            closeConnection(dbConn);

            // Return 200 status with an empty JSON object
            return Response.ok("{}").build();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

