import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

 

/*
 * Il server con  GUI
 */

public class ServerGUI extends JFrame implements ActionListener, WindowListener {

     

    private static final long serialVersionUID = 1L;

    // bottoni start e stop

    private JButton stopStart;

    // JTextArea per la chat room

    private JTextArea chat, event;

    // numero di porta

    private JTextField tPortNumber;

    // istanza di creazione del server

    private Server server;

     

     

    /* costruttore del server che riceve la porta
     * per ascoltare le richieste dei client
     */

    ServerGUI(int port) {

        super("Chat Server");

        server = null;

        // numero di porta nel NorthPanel

        JPanel north = new JPanel();
        north.add(new JLabel("Numero di Porta: "));
        tPortNumber = new JTextField("  " + port);
        north.add(tPortNumber);

        // fermiamo o avviamo il server,inizia con il comando "Start"

        stopStart = new JButton("Start");
        stopStart.addActionListener(this);
        north.add(stopStart);
        add(north, BorderLayout.NORTH);

         

        // l'evento e la chat room

        JPanel center = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80,80);
        chat.setEditable(false);
        appendRoom("Chat room.\n");
        center.add(new JScrollPane(chat));
        event = new JTextArea(80,80);
        event.setEditable(false);
        appendEvent("Events log.\n");
        center.add(new JScrollPane(event));
        add(center);

        // deve essere avvisato quando l'utente utilizza il bottone di chiusura sul frame

        addWindowListener(this);
        setSize(400, 600);
        setVisible(true);
    }      


    // messaggio in append alle due JTextArea
    // position at the end

    void appendRoom(String str) 
	{
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }

    void appendEvent(String str) {

        event.append(str);

        event.setCaretPosition(chat.getText().length() - 1);

         
    }

    // inizia o si interrompe quando si utilizzano i bottoni
    public void actionPerformed(ActionEvent e) {

        // se parte dobbiamo fermarci

        if(server != null) 
        {
            server.stop();
            server = null;
            tPortNumber.setEditable(true);
            stopStart.setText("Start");
            return;

        }

        // Avvia il Server

        int port;

        try 
		{
            port = Integer.parseInt(tPortNumber.getText().trim());
        }

        catch(Exception er) 
		{

            appendEvent("Numero di porta non valido!");
            return;

        }

        // crea un nuovo Server

        server = new Server(port, this);

        // e lo avvia come un thread

        new ServerRunning().start();
        stopStart.setText("Stop");
        tPortNumber.setEditable(false);
    }

     

    // inizializzazione porta del Server

    public static void main(String[] arg) {

        // Porta di default 1500

        new ServerGUI(1500);

    }

    /*
     * Se l'utente utilizza la X per chiudere la connessione
     * Bisogna chiudere la connessione con il server per liberare la porta
     */

    public void windowClosing(WindowEvent e) {

        // se il Server esiste

        if(server != null) {
        	
            try 
			{
                server.stop();          // chiede al frame di chiudere la connessione
            }

            catch(Exception eClose) 
			{

            }
            server = null;

        }

        // disporre il frame

        dispose();
        System.exit(0);
    }

    //Metodi WindowListener

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

 

    /*
     * Un thread per avviare il Server
     */

    class ServerRunning extends Thread {

        public void run() 
		{

            server.start();         // dovrebbe eseguire il programma fin quando non fallisce

            // il server fallisce

            stopStart.setText("Start");
            tPortNumber.setEditable(true);
            appendEvent("Server crashed\n");
            server = null;
        }

    }
}
