package it.unimib.sd2024;

import it.unimib.sd2024.objs.Domain;
import it.unimib.sd2024.objs.Order;
import it.unimib.sd2024.objs.RegisterPayload;
import it.unimib.sd2024.objs.RenewPayload;
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
import static it.unimib.sd2024.DBHandler.closeConnection;

@Path("/domains")
public class ServerAPIDomains {
    /**
     * Implementazione di GET "/domains/{domainName}".
     * @param domainName Nome del dominio.
     * @return 200 con i dettagli del dominio e dell'utente se il dominio è valido.
     * Se c'è un problema con i parametri, ritorna 400.
     * Se il dominio è scaduto, non ritorna i dettagli dell'utente.
     * Se il dominio non esiste, ritorna 404.
     * Se c'è un errore interno, ritorna 500.
     */
    @GET
    @Path("/{domainName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("domainName") String domainName) {
        String response;
        List<String> answer;
        try {
            if (domainName == null || domainName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Domain name is required").build();
            }

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

            LocalDate dateToCheck = LocalDate.parse(domain.expirationDate);
            LocalDate currentDate = LocalDate.now();
            boolean isExpired = dateToCheck.isBefore(currentDate);

            // Fetch user details if ownershipUserId exists
            if (domain.ownershipUserId != null && !isExpired) {
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
     * @param userId ID dell'utente
     * @return 200 con un array di domini se l'utente ha domini.
     * Se l'ID dell'utente non è valido, ritorna 400.
     * Se l'utente non esiste, ritorna 404.
     * Se c'è un errore interno, ritorna 500.
     * Se l'utente non ha domini, ritorna 200 con un array vuoto.
     */
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetUsersDomains(@PathParam("userId") String userId) {
        String response = null;
        List<String> answer;
        try {
            if (userId == null || userId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
            }

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
     * Implementazione di POST "/domains/register".
     * @param payload Payload per registrare un dominio.
     *                uid: ID dell'utente.
     *                domainName: Nome del dominio.
     *                registerTime: Durata della registrazione in anni.
     *                cvv: CVV della carta di credito.
     *                cardNumber: Numero della carta di credito.
     *                cardOwnerName: Nome del proprietario della carta di credito.
     *                cardOwnerSurname: Cognome del proprietario della carta di credito.
     *                cardExpireDate: Data di scadenza della carta di credito.
     * @return 200 se il dominio è stato registrato correttamente.
     * 400 se i dati non sono validi.
     * 404 se l'utente non esiste.
     * 409 se il dominio è già registrato.
     * 500 se c'è un errore interno.
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response RegisterDomain(RegisterPayload payload) {
        List<String> answer;
        try {
            if (payload.registerTime == null || payload.registerTime.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Register time is required").build();
            }
            if (Integer.parseInt(payload.registerTime) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Register time must be greater than 0").build();
            }
            if (payload.domainName == null || payload.domainName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Domain name is required").build();
            }
            if (payload.uid == null || payload.uid.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
            }
            if (payload.cardOwnerName == null || payload.cardOwnerName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Card owner name is required").build();
            }
            if (payload.cardOwnerSurname == null || payload.cardOwnerSurname.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Card owner surname is required").build();
            }
            if (!Common.isCardNumberValid(payload.cardNumber)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid card number").build();
            }
            if (!Common.isCvvValid(payload.cvv)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid card expire date").build();
            }

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
            String oldDomainData = null;
            if (domainExists) {
                domain = jsonb.fromJson(answer.get(1), Domain.class);
                LocalDate dateToCheck = LocalDate.parse(domain.expirationDate);
                LocalDate currentDate = LocalDate.now();
                boolean isExpired = dateToCheck.isBefore(currentDate);
                if (domain.ownershipUserId != null && !isExpired) {
                    return Response.status(Response.Status.CONFLICT).entity("Domain already registered").build();
                }
                oldDomainData = domain.expirationDate;
            } else {
                domain = new Domain();
                domain.price = 100 * Integer.parseInt(payload.registerTime); // Set default price
            }

            domain.registerDate = LocalDate.now().toString();
            domain.ownershipUserId = payload.uid;
            domain.expirationDate = LocalDate.now().plusYears(Integer.parseInt(payload.registerTime)).toString();

            var domainData = jsonb.toJson(domain, Domain.class);

            if (domainExists && oldDomainData != null) {
                answer = sendRequest(Command.SET_IF, "domains", List.of("domains", payload.domainName), String.valueOf(List.of(domainData, "expirationDate", oldDomainData)), dbConn);
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
     * Implementazione di PUT "/domains/renew".
     * @param payload Payload per rinnovare un dominio.
     *                userId: ID dell'utente.
     *                domainName: Nome del dominio.
     *                renewTime: Durata del rinnovo in anni.
     *                cvv: CVV della carta di credito.
     *                cardNumber: Numero della carta di credito.
     *                cardOwnerName: Nome del proprietario della carta di credito.
     *                cardOwnerSurname: Cognome del proprietario della carta di credito.
     *                cardExpireDate: Data di scadenza della carta di credito.
     * @return 200 se il dominio è stato rinnovato correttamente.
     * 400 se i dati non sono validi.
     * 404 se il dominio non esiste.
     * 403 se l'utente non possiede il dominio o è scaduto e non può essere rinnovato.
     * 400 se il tempo totale di registrazione supera i 10 anni.
     * 500 se c'è un errore interno.
     */
    @PUT
    @Path("/renew")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renewDomain(RenewPayload payload) {
        List<String> answer;
        try {
            if (payload.renewTime == null || payload.renewTime.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Renew time is required").build();
            }
            if (Integer.parseInt(payload.renewTime) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Renew time must be greater than 0").build();
            }
            if (payload.domainName == null || payload.domainName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Domain name is required").build();
            }
            if (payload.userId == null || payload.userId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
            }
            if (payload.cardOwnerName == null || payload.cardOwnerName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Card owner name is required").build();
            }
            if (payload.cardOwnerSurname == null || payload.cardOwnerSurname.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Card owner surname is required").build();
            }
            if (!Common.isCardNumberValid(payload.cardNumber)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid card number").build();
            }
            if (!Common.isCvvValid(payload.cvv)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid card expire date").build();
            }

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

            if (currentExpirationDate.isBefore(LocalDate.now())) {
                return Response.status(Response.Status.FORBIDDEN).entity("Domain is expired").build();
            }

            // Check if the total registration time exceeds 10 years
            if (totalYears > 10) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Total registration time cannot exceed 10 years").build();
            }

            // Update the expiration date in the domain
            String oldExpirationDate = domain.expirationDate;
            domain.expirationDate = newExpirationDate.toString();
            var domainData = jsonb.toJson(domain, Domain.class);
            answer = sendRequest(Command.SET_IF, "domains", List.of("domains", payload.domainName), String.valueOf(List.of(domainData, "expirationDate", oldExpirationDate)), dbConn);

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
