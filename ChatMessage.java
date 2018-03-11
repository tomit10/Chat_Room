import java.io.*;
/*
 * Questa classe definisce i diversi tipi di messaggi che verranno scambiati tra i
 * Clients e il Server.
 */

	public class ChatMessage implements Serializable 
	{
	
	    protected static final long serialVersionUID = 1112122200L;
	
	    // I diversi tipi di messaggi inviati dal Client
	    // WHOISIN per ricevere la lista degli utenti connessi
	    // MESSAGE per un messaggio 
	    // LOGOUT per disconnetersi dal server
	
	    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
	    private int type;
	    private String message;
	     
	
	    // costruttore
	
	    ChatMessage(int type, String message) 
		{
	        this.type = type;
	        this.message = message;
	    }
	    // acquisizione
	
	    int getType() 
		{
	        return type;
	    }
	
	    String getMessage() 
		{
	        return message;
	    }
	
	}
