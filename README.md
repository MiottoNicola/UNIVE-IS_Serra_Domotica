###### Ingegneria del Software 2023/2024 - Università Ca' Foscari Venezia

# Informazioni gruppo
**Nome:** CaligGroup

**Membri:**      
- Michele Bertoldo - 890571@stud.unive.it   [capo-gruppo]
- Bordignon Giacomo - 897270@stud.unive.it
- Miotto Nicola - 895216@stud.unive.it
- Titotto Paolo - 895227@stud.unive.it

# Informazioni progetto
**Nome APP:** Your Greenhouses

**Versione:** 1.1

**Github:** https://github.com/MiottoNicola/UNIVE-IS_Serra_Domotica

### Descrizione:
Un'applicazione Android che consente agli utenti di monitorare e gestire serre domotiche in tempo reale, integrando dati meteo e informazioni raccolte dai sensori.

### Obiettivo:
L'obiettivo principale è sviluppare un'app che permetta agli utenti di:
  - Visualizzare i dati raccolti dalle proprie serre (temperatura, umidità dell'aria, umidità del terreno, luminosità);
  - Agire sulla serra da remoto (luce ed irrigazione);
  - Integrare informazioni meteo in tempo reale basate sulla posizione GPS dell'utente.

## Stato di Avanzamento
### Funzionalità Completate:
1. **Autenticazione Utente** (v.1.0):
   - Implementata tramite Firebase Authentication (login, registrazione e ripristino password).
2. **Gestione Dati Firebase** (v.1.0):
   - Struttura del database progettata per salvare utenti, dispositivi e dati sensoriali.
   - Associazione utente-dispositivo completata.
3. **Dashboard Principale** (v.1.0):
   - Visualizzazione dei dati meteo in tempo reale (integrazione API OpenWeather).
   - Monitoraggio dei dati raccolti della serra.
   - Azionare attuatori della serra.
4. **Gestione degli errori** (v.1.0):
   - Popup che segnalano eventuali errori all'utente.
5. **Applicazione multilingua** (v.1.1):
    - Traduzione applicazione lingue differenti: 
      * inglese [default]
      * arabo
      * tedesco
      * francese
      * italiano
      * portoghese
      * spagnolo
      * giapponese
      * cinese semplificato
      * cinese tradizionale
6. **widget avanzati** (v.1.1):
      - Implementato widget Android per una rapida visualizzazione degli ultimi dati della serra (aggiornamento ogni 30 min).

## Problemi Conosciuti
Elencho di alcuni problemi noti agli sviluppatori che sono stati risolti nella nuova versione ma che comunque potrebbero ripresentarsi in futuro:
  - **Layout dell'applicazione**: Alcune parti dell'app potrebbero non adattarsi perfettamente su alcuni dispositivi.
  - **Layout del Widget**: Il widget potrebb non adattarsi perfettamente su alcuni dispositivi. 
  - **Blocco per Assenza di Connessione**: Se l'app si blocca in assenza di rete, si consiglia di riavviarla. Stiamo lavorando per rendere l'app più resiliente in condizioni di rete instabile.

## Struttura del Progetto
- **`/app/src/main/java`**:
  - Codice sorgente suddiviso in package (es. `forecast`, `utils`, `widget`).
- **`/res/layout`**:
  - File XML per il design delle activity, delle finestre di dialogo.
- **`/res/values`**:
  - File di configurazione (colori, stringhe, stili, ecc.).
- **`/res/drawable`**:
    - Fie vettori utilizzati come immagini.
- **`res/mipmap`**:
    - File icone dell'app.
- **`README.md`**:
  - Questo file, che illustra lo stato di avanzamento.

### Librerie/API implementate:
- [Firebase](https://firebase.google.com/) per l'autenticazione e il database.
- [Picasso](https://square.github.io/picasso/) per il caricamento delle immagini.
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) per la gestione dei grafici avanzata.
- [OpenWeather](https://openweathermap.org/) per i dati meteo.

### Dati richiesti per monitorare l'utente
* **Posizone:** [possibilità di scelta]
  * `android.permission.ACCESS_FINE_LOCATION` -> esatta [consigliata]
  * `android.permission.ACCESS_COARSE_LOCATION` -> approssimata
* **Internet:**
  * `android.permission.INTERNET`
* **Stato connessione:**
  * `android.permission.ACCESS_NETWORK_STATE`

### Dati collegati all'utente
L'applicazione richiede l'iscrizione e l'accesso per sbloccare tutte le funzionalità. 
* **Iidentificativi:**
  * ID utente
* **informazioni di contatto:**
  * Email
  * Nome

**Note:** tali dati vengono salvati all'interno della piattafora Firebase in maniera cifrata e non accessibili dall'esterno. La piattaforma mette a disposizione lo strumento "Firebase Authentication" per la gestione automatica delle informazioni personali.

# Note finali
- L'applicazione assume che l'utente abbia già una propria serra installata, configurata e compatibile con la nostra app.
- L'applicazione, essendo sempre collegata ad internet, riochiede una onnessione stabile.