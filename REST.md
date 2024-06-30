# Progetto Sistemi Distribuiti 2023-2024 - API REST

## `/domains/{domainName}`

### GET

**Descrizione**: Restituisce il dettaglio del dominio cercato.

**Parametri**: `domainName` (string): Nome del dominio.

**Header**: -.

**Body richiesta**: -.

**Risposta**:  {
    "domain": {
      "domainName": "example.com",
      "expirationDate": "2024-12-31",
      "ownershipUserId": "123",
      "price": 100
    },
    "user": {
      "email": "jd@gmail.com",
      "surname": "Doe",
      "name": "John "
    }
  }
  domain: Dettagli del dominio.
  user: Dettagli dell'utente se il dominio è valido e non scaduto.

**Codici di stato restituiti**: 
    `200` OK
    `404` Not Found -> Se il dominio cercato non esiste.


## `domains/user/{userId}`

### GET

**Descrizione**: Restituisce i domini posseduti dall'utente specificato.

**Parametri**: 
- `userId` (string): ID dell'utente.

**Header**: -.

**Body richiesta**: -.

**Risposta**: La risposta è un dizionario di domini in formato JSON, posseduti dall'utente specificato.

**Esempio di risposta**:
```json
{
    "ffeafae": {
        "expirationDate": "2029-06-30",
        "ownershipUserId": "4",
        "price": 500,
        "registerDate": "2024-06-30"
    }
}
```

**Codici di stato restituiti**: 
    `200` OK -> Se ci sono dati viene tornato il dizionario altrimenti oggetto vuoto


## `/domains/register`

### POST

**Descrizione**: Registra un dominio con i dettagli forniti nel payload.

**Parametri**: -

**Header**: -.

**Body richiesta**: 
```json
  {
    "uid": "string",
    "domainName": "string",
    "registerTime": "integer",
    "cvv": "string",
    "cardNumber": "string",
    "cardOwnerName": "string",
    "cardOwnerSurname": "string",
    "cardExpireDate": "string"
  }
```
**Risposta**: {}

**Codice di stato restituiti**
`200 OK`: Il dominio è stato registrato correttamente. La risposta contiene un oggetto JSON vuoto.
`404 Not Found`: L'utente non esiste.
`409 Conflict`: Il dominio è già registrato.


## `/domains/renew`

### PUT

**Descrizione**: Rinnova un dominio con i dettagli forniti nel payload.

**Parametri**: -

**Header**: -.

**Body richiesta**:
```json
  {
    "userId": "string",
    "domainName": "string",
    "renewTime": "string"
  }
```

**Risposta**: {}

**Codice di stato Restituiti**:
`200 OK`: Il dominio è stato rinnovato correttamente. La risposta contiene un oggetto JSON vuoto.
`404 Not Found`: Il dominio non esiste.
`403 Forbidden`: L'utente non possiede il dominio o è scaduto e non può essere rinnovato.
`400 Bad Request`: Il tempo totale di registrazione supera i 10 anni.


## `/orders/{userId}`

### GET

**Descrizione**: Restituisce la lista degli ordini dell'utente specificato.

**Parametri**: 
- `userId` (string): ID dell'utente.

**Header**: -.

**Body richiesta**: -.

**Risposta**: 
La risposta è un dizionario di ordini dell'utente [key = userId] in formato JSON. Se l'utente non ha ordini, viene restituita un oggetto vuoto.

**Esempio di risposta**:
```json
{
    "4": {
        "cardCvv": "feafae",
        "cardExpireDate": "2029-06-30",
        "cardId": "feafeafae",
        "cardName": "feafaefae",
        "cardSurname": "faefaeefa",
        "domain": "ffeafae",
        "price": 500,
        "type": "register",
        "userId": "4"
    }
}
```

**Codici di stato restituiti**: 
- `200 OK`: La richiesta è stata eseguita con successo e la lista degli ordini è restituita nel corpo della risposta.
- `404 Not Found`: L'utente non esiste.
- `500 Internal Server Error`: Si è verificato un errore interno del server.


## `/user/signup`

### POST

**Descrizione**: Registra un nuovo utente con i dati forniti nel payload.

**Parametri**: - 

**Header**: -.

**Body richiesta**:
```json
  {
    "email": "string",
    "name": "string",
    "surname": "string"
  }
```

**Risposta**: Oggetto JSON contenente l'id univoco assegnato all'utente.

**Codice di stato restituiti**:
`200 OK`: L'utente è stato registrato con successo. La risposta contiene l'ID dell'utente registrato in formato JSON.
`400 Bad Request`: I dati forniti non sono validi.
`409 Conflict`: L'email fornita è già registrata.
