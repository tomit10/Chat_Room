import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * Il server può essere eseguito tramite CLI oppure tramite GUI
 */

public class Server {

    // ID unico per ogni connessione

    private static int uniqueId;

    // ArrayList per la lista dei Client collegati

    private ArrayList<ClientThread> al;

   

    private ServerGUI sg;

    // per l'orario

    private SimpleDateFormat sdf;

    // il numero di porta per la connessione

    private int port;

    // la variabile boolean per lo stato del server

    private boolean keepGoing;


    /*
     *  Costruttore del server 
     */

    public Server(int port) {

        this(port, null);

    }

     

    public Server(int port, ServerGUI sg) {


        this.sg = sg;

        // la porta 

        this.port = port;

        // visualizzazione data

        sdf = new SimpleDateFormat("HH:mm:ss");

		// ArrayList per i client connessi

        al = new ArrayList<ClientThread>();

    }


    public void start() {

        keepGoing = true;

        /* crea il server e attente la richiesta dal client */

        try

        {

            // istanza server
            ServerSocket serverSocket = new ServerSocket(port);

 

            // loop infinito per attendere la connessione 

            while(keepGoing)

            {
                

                display("Il Server attende la connessione del Client sulla porta: " + port + ".");    

                Socket socket = serverSocket.accept();      // accetta la connessione

                // se chiedo di interrompere 

                if(!keepGoing)

                    break;

                ClientThread t = new ClientThread(socket);  // creo il Thread
                al.add(t);                                  // lo salvo nell'ArrayList
                t.start();
            }

            // ho chiesto di interrompere 

            try {

                serverSocket.close();

                for(int i = 0; i < al.size(); ++i) {

                    ClientThread tc = al.get(i);

                    try {

                    tc.sInput.close();
                    tc.sOutput.close();
                    tc.socket.close();

                    }

                    catch(IOException ioE) 
                    {

                        
                    }
                }

            }

            catch(Exception e) 
			{
                display("Eccezione del server e del client: " + e);
            }
        }

        // something went bad

        catch (IOException e) 
		{
            String msg = sdf.format(new Date()) + " Eccezione riguardo il ServerSocket: " + e + "\n";
            display(msg);
        }
    }      

    /*
     * Per la GUI per interrompere il server
     */

    protected void stop() {

        keepGoing = false;

  

        try 
			{
            	new Socket("localhost", port);
        	}

        catch(Exception e) 
		{
            
        }
    }

    /*
     * Visualizza la data 
     */

    private void display(String msg) 
	{

        String time = sdf.format(new Date()) + " " + msg;

        if(sg == null)
        	
            System.out.println(time);

        else
            sg.appendEvent(time + "\n");
    }

    /*
     *  messaggio di broadcast a tutti i Client
     */

    private synchronized void broadcast(String message) 
	{

       

        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";

        // visualizza il messaggio in modalità GUI e CLI

        if(sg == null)
            System.out.print(messageLf);
        else
            sg.appendRoom(messageLf);    
         

        // se vogliamo rimuovere un Client 
        // perchè si è disconnesso

        for(int i = al.size(); --i >= 0;) 
        {

            ClientThread ct = al.get(i);

            // try to write to the Client if it fails remove it from the list

	            if(!ct.writeMsg(messageLf)) 
	            {
	                al.remove(i);
	                display("Disconnessione del Client " + ct.username + " dalla lista.");
	            }
        }
    }

 

    // messaggio di LOGOUT 

    synchronized void remove(int id) {

        

        for(int i = 0; i < al.size(); ++i) {

            ClientThread ct = al.get(i);

            

            if(ct.id == id) 
            {
                al.remove(i);
                return;
            }
        }
    }



    public static void main(String[] args) {
	
        // porta di default 1500

        int portNumber = 1500;
        switch(args.length) 
        {

            case 1:

                try {
                    portNumber = Integer.parseInt(args[0]);
                	}

                catch(Exception e) 
                {
                    System.out.println("Porta di comunicazione non valida.");
                    System.out.println("Istruzione: > java Server [portNumber]");
                    return;
                }

            case 0:

                break;

            default:

                System.out.println("Istruzione: > java Server [portNumber]");
                return;
                
        }

        // Creazione server

        Server server = new Server(portNumber);
        server.start();

    }

 
   

    class ClientThread extends Thread {

        

        Socket socket;

        ObjectInputStream sInput;

        ObjectOutputStream sOutput;

        // id unico fino alla disconnessione

        int id;

        //  Username del Client

        String username;

        //	tipo di messaggio che riceverò

        ChatMessage cm;

        // 	la data della connessione

        String date;

 

        // Costruttore

        ClientThread(Socket socket) {

            //id

            id = ++uniqueId;

            this.socket = socket;

            /* Creazione del Data Stream */

            System.out.println("Il Thread sta cercando di creare un Object Input/Output Stream");

            try

            {

                // creazione dell'output

                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());

                // legge l'username

                username = (String) sInput.readObject();
                display(username + " si è appena connesso.");
                
            }

            catch (IOException e) 
			{

                display("Eccezione durante la creazione di un Input/output Stream: " + e);
                return;

            }

       

            catch (ClassNotFoundException e) {

            }

            date = new Date().toString() + "\n";

        }

 

        // verrà eseguito all'infinito

        public void run() {

            // fino al LOGOUT

            boolean keepGoing = true;

            while(keepGoing) 
            {
            	

                try {

                    cm = (ChatMessage) sInput.readObject();

                }

                catch (IOException e) {

                    display(username + " Eccezione durante la lettura delle Streams: " + e);

                    break;             

                }

                catch(ClassNotFoundException e2) {
                    break;

                }

                // la porta utilizzata dalla ChatMessage

                String message = cm.getMessage();

 

                // Switch sul tipo di messaggio utilizzato

	                switch(cm.getType()) 
	                {
	
	 
	
	                case ChatMessage.MESSAGE:
	
	                    broadcast(username + ": " + message);
	                    break;
	
	                case ChatMessage.LOGOUT:
	                    display(username + " disconnessione con messaggio di LOGOUT.");
	                    keepGoing = false;
	                    break;
	
	                case ChatMessage.WHOISIN:
	                	
	                    writeMsg("Lista degli utenti connessi il " + sdf.format(new Date()) + "\n");
	
	                    // tutti gli utenti connessi
	
	                    for(int i = 0; i < al.size(); ++i) 
	                    {
	                        ClientThread ct = al.get(i);
	                        writeMsg((i+1) + ") " + ct.username + " dal " + ct.date);
	                    }
	
	                    break;
	                }

            }

            // rimozione degli utenti dalla lista 

            remove(id);
            close();

        }

         

        // cerca di chiudere tutto 

        private void close() {

            // cerca di chiudere la connessione

            try 
			{
                if(sOutput != null) sOutput.close();
            }

            catch(Exception e) {}

            try {

                if(sInput != null) sInput.close();

            }

            catch(Exception e) {};

            try {

                if(socket != null) socket.close();

            }

            catch (Exception e) {}

        }

 

        /*
         * Scrive una stringa al Client
         */

        private boolean writeMsg(String msg) {

            // se il client è connesso
			if(!socket.isConnected()) 
			{
                close();
                return false;
            }

            

            try {
                sOutput.writeObject(msg);
            	}


            catch(IOException e) 
			{
                display("Errore durante l'invio del messaggio a " + username);
                display(e.toString());
            }

            return true;
        }

    }
}

