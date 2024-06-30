# Progetto Sistemi Distribuiti 2023-2024 - TCP

## Definizione di elementi comuni a tutti i comandi
### Collezione
Una collezione è un insieme di documenti.
Ogni documento è un insieme di coppie chiave-valore. Le chiavi sono stringhe, i valori possono essere stringhe, numeri o booleani o altri documenti.

#### Comando
La collezione deve essere una stringa formata da solo numeri, lettere (Minuscole/Maiuscolo) e spazi.
##### Esempio
```
collection1
```

### Chiavi
Le chiavi sono stringhe formate da solo numeri, lettere (Minuscole/Maiuscolo), spazi e ..

#### Comando
Le chiavi deveno essere una stringa formata da solo numeri, lettere (Minuscole/Maiuscolo), spazi e . e ogni chiave è divisa dalla ",".

##### Esempio
```
key1, key2, ke y3
```

### Documento
Un documento è un insieme di coppie chiave-valore o un elemento con un certo significato. Le chiavi sono stringhe, i valori possono essere stringhe, numeri o booleani o altri documenti, nel nostro caso per documento intendiamo un elemento JSON.

##### Esempio
```
Es 1
{
    "key1": "value1",
    "key2": "value2",
    "key3": "value3"
}
Es 2
"value1"
Es 3
1000
Es 4
true
```

## **Comandi del database**

__SET - Comando per aggiungere un documento a una collezione.__
```
set collection ; keys ; document
```
```
Example:
- Set collection1 ; key1 , key2, key3 ; {"keyD1": "value1", "keyD2": "value2"}
- Set collection1 ; key4 ; "value1"
- Set collection1 ; key5 ; 1000

Risposta:
- ["true"] se il documento è stato aggiunto correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato aggiunto correttamente
```

__SET_IF - Comando per aggiungere un documento a una collezione se all'interno del documento nel database esiste una coppia key : value uguale a key_check : value_check.__
```
setif collection ; keys ; [document, key_check, value_check]
```
```
Example:
- Setif collection1 ; key1 , key2, key3 ; [{"keyD1": "value1", "keyD2": "newvalue2"}, "keyD1", "value1"]

Risposta:
- ["true"] se il documento è stato aggiunto correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato aggiunto correttamente
```

__SET_IF_NOT_EXIST - Comando per aggiungere un documento a una collezione se non esiste un documento associato alle keys.__
```
setifnotexist collection ; keys ; document
```
```
Example:
- Setifnotexist collection1 ; key1 , key2, key3 ; {"keyD1": "value1", "keyD2": "value2"}
- Setifnotexist collection1 ; key4 ; "value1"
- Setifnotexist collection1 ; key5 ; 1000

Risposta:
- ["true"] se il documento è stato aggiunto correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato aggiunto correttamente
```

__GET - Comando per ottenere un documento da una collezione.__
```
get collection ; keys ;
```
```
Example:
- Get collection1 ; key1 , key2, key3 ;
- Get collection1 ; key4 ;
- Get collection1 ; key5 ;

Risposta:
- ["true", document] se il documento è stato ottenuto correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato ottenuto correttamente
```

__GET_IF - Comando per ottenere un documento da una collezione se all'interno del documento nel database esiste una coppia key : value uguale a key_check : value_check.__
```
get collection ; keys ; [key_check, value_check]
```
```
Example:
- GetIf collection1 ; key1 , key2, key3 ; ["keyD1", "value1"]

Risposta:
- ["true", document] se il documento è stato ottenuto correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato ottenuto correttamente
```

__Remove - Comando per rimuovere un documento da una collezione.__
```
get collection ; keys ;
```
```
Example:
- Remove collection1 ; key1 , key2, key3 ;

Risposta:
- ["true"] se il documento è stato rimosso correttamente
- ["false", messaggio d'errore (Stringa)] se il documento non è stato rimosso correttamente
```
