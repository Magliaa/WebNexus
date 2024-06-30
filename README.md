# Progetto Sistemi Distribuiti 2023-2024

Progetto: WebNexus
Descrizione: Il database utilizza il pattern strategy per permettere di inserire nuove operazioni facilmente.
Il client è sviluppato simulando una Single Page Application, la prima fase di iterazione consiste nella registrazione di un utente, nel momento in cui la registrazione va a buon fine viene assegnato un id univoco all'utente (che viene memorizzato nei localStorage del dispositivo e rimossi solo nel caso di logout in modo che rimanga effettuato l'accesso), successivamente alla registrazione si può ricercare un dominio e nel caso sia libero registrarlo, visualizzare la lista dei domini registrati e rinnovarli e visualizzare la lista di tutti gli ordini effettuati.
Il client e il server comunicano tramite chiamate REST.

## Componenti del gruppo

* Theofilia Jessica (894476) <t.jessica@campus.unimib.it>
* Andrea Magliani (894395) <a.magliani@campus.unimib.it>

## Compilazione ed esecuzione

Sia il server Web sia il database sono applicazioni Java gestire con Maven. All'interno delle rispettive cartelle si può trovare il file `pom.xml` in cui è presenta la configurazione di Maven per il progetto. Si presuppone l'utilizzo della macchina virtuale di laboratorio, per cui nel `pom.xml` è specificato l'uso di Java 21.

Il server Web e il database sono dei progetti Java che utilizano Maven per gestire le dipendenze, la compilazione e l'esecuzione.

### Client Web

Per avviare il client Web è necessario utilizzare l'estensione "Live Preview" su Visual Studio Code, come mostrato durante il laboratorio. Tale estensione espone un server locale con i file contenuti nella cartella `client-web`.

**Attenzione**: è necessario configurare CORS in Google Chrome come mostrato nel laboratorio.

### Server Web

Il server Web utilizza Jetty e Jersey. Si può avviare eseguendo `mvn jetty:run` all'interno della cartella `server-web`. Espone le API REST all'indirizzo `localhost` alla porta `8080`.

### Database

Il database è una semplice applicazione Java. Si possono utilizzare i seguenti comandi Maven:

* `mvn clean`: per ripulire la cartella dai file temporanei,
* `mvn compile`: per compilare l'applicazione,
* `mvn exec:java`: per avviare l'applicazione (presuppone che la classe principale sia `Main.java`). Si pone in ascolto all'indirizzo `localhost` alla porta `3030`.
