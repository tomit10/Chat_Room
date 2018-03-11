import java.net.*;
import java.io.*;
import java.util.*;

/*
 * Il client può essere eseguito tramite CLI oppure tramite GUI
 */
public class Client  {

 

    // Per I/O

    private ObjectInputStream sInput;       // per leggere dal socket
    private ObjectOutputStream sOutput;     // per scrivere sul socket
    private Socket socket;
 
    // opzionale : non viene considerato il modalità CLI
    private ClientGUI cg;
  
    // il server, la porta e il nome utente
    private String server, username;
    private int port;
 
    /*

     *  Il costruttore viene chiamato tramite CLI
     *  server: l'indirizzo del server 
     *  port: numero di porta
     *  username: nome utente     ATTENZIONE!! Il nome utente di default è Anonymous
     */
    Client(String server, int port, String username) 
    {
        
        this(server, port, username, null);
    }

 

    /*
     * Il costruttore è utilizzato quando utilizziamo la GUI
     * in modalità CLI il parametro ClienGUI è null
     */

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

     

    /*
     * Inizia la comunicazione
     */
    
    public boolean start() {

        // cerca di connettersi al server
        try {
            socket = new Socket(server, port);
        	}
        
        // errore di connessione al server

        catch(Exception ec) {
        	
            display("Errore di connessione al server:" + ec);

            return false;

        }

         

        String msg = "Connessione accettata " + socket.getInetAddress() + ":" + socket.getPort();

        display(msg);
        
        /* Creazione Data Stream */

        try

        {

            sInput  = new ObjectInputStream(socket.getInputStream());

            sOutput = new ObjectOutputStream(socket.getOutputStream());

        }

        catch (IOException eIO) {

            display("Eccezione durante la creazione di un nuovo Input/output Streams: " + eIO);

            return false;

        }
        
        class ListenFromServer extends Thread 
        {

     

            public void run() {

                while(true) {

                    try {

                        String msg = (String) sInput.readObject();

                        // se si utilizza la console mode stampa il messaggio e torna al programma

                        if(cg == null) {

                            System.out.println(msg);

                            System.out.print("> ");

                        }

                        else {
                            cg.append(msg);

                        }

                    }

                    catch(IOException e) {

                        display("Il Server ha chiuso la connessione: " + e);

                        if(cg != null)

                            cg.connectionFailed();

                        break;
                    }

                    // Eccezione String object

                    catch(ClassNotFoundException e2) {

                    }
                }
            }

    	}

 

        // crea il Thread per ascoltare dal server

        new ListenFromServer().start();

        // Invia il nostro username al server questo è l'unico messaggio inviato come una stringa

        // Tutti gli altri messaggi saranno ChatMessage objects

        try

        {

            sOutput.writeObject(username);

        }

        catch (IOException eIO) {

            display("Eccezione,fai il login : " + eIO);
            disconnect();
            return false;

        }

        // funziona, possiamo avvisare il client

        return true;
    }
    /*
     * Per GUI e CLI
     */
    private void display(String msg) 
    {
        if(cg == null)
            System.out.println(msg);      // println in CLI
        else
            cg.append(msg + "\n");      // in append alla classe ClientGUI 
    }

     
    /*
     * Per inviare un messaggio al server
     */

    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        	}
        catch(IOException e) 
        {
            display("Eccezione durante la scrittura al server: " + e);

        }

    }

    /*
     * Quando ci sono dei problemi 
     * Chiudi le Input/Output streams e disconnettiti
     */
    
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} 
        try {
            if(sOutput != null) sOutput.close();
        	}
        
        catch(Exception e) {} 
        try{
            if(socket != null) socket.close();
            }

        catch(Exception e) {} 

         

        // informare la GUI

        if(cg != null)

            cg.connectionFailed();

             

    }

    /*
     * Per iniziare nella modalità CLI utilizza i seguenti comandi
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
 
     * La porta di default è 1500
     * Se l'indirizzo IP del server viene utilizzato quello di default (localhost)
     * L'username di default è Anonymous
     * > java Client
     * equivale a 
     * > java Client Anonymous 1500 localhost
     *
     * In modalità console, se dovesse presentarsi un errore il programma si interrompe
     */
    public static void main(String[] args) {
        // valori di default
    	
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

       
        switch(args.length) {

            // > javac Client username portNumber serverAddr
            case 3:

                serverAddress = args[2];

            // > javac Client username portNumber

            case 2:

                try {
                    portNumber = Integer.parseInt(args[1]);
                	}
                
                catch(Exception e) 
                	{
                    System.out.println("Numero di porta non valido.");

                    System.out.println("Istruzione: > java Client [username] [portNumber] [serverAddress]");

                    return;
                    }

            // > javac Client username

            case 1:

                userName = args[0];

            // > java Client

            case 0:

                break;

            // invalid number of arguments

            default:

                System.out.println("Istruzione: > java Client [username] [portNumber] {serverAddress]");

            return;

        }

        // crea il Client object

        Client c = new Client(serverAddress, portNumber, userName);

        // controlla se possimo collegarci al server
        // se dovesse fallire non possiamo inviare i messaggi

        if(!c.start())

            return;

         

        // attende un maessaggio dall'utente

        Scanner scan = new Scanner(System.in);

        // loop aspettando un messaggio dall'utente

        while(true) {

            System.out.print("> ");

            // legge il messaggio dall'utente

            String msg = scan.nextLine();

            // LOGOUT

            if(msg.equalsIgnoreCase("LOGOUT")) {

                c.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));

                // interrompe per disconnettersi

                break;

            }

            // messaggio WhoIsIn

            else if(msg.equalsIgnoreCase("WHOISIN")) {

                c.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));              

            }

            else {              // messaggio di default

                c.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));

            }

        }

        //si disconnette

        	c.disconnect();   

    }


}


