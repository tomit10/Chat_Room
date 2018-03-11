import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * L'interfaccia grafica del Client
 */

public class ClientGUI extends JFrame implements ActionListener 
{
 

    private static final long serialVersionUID = 1L;

   

    private JLabel label;

   

    private JTextField tf;

    

    private JTextField tfServer, tfPort;

   

    private JButton login, logout, whoIsIn;

  

    private JTextArea ta;

   

    private boolean connected;

    // elemento della classe Client

    private Client client;

    // numeri di default della porta

    private int defaultPort;
    private String defaultHost;

 

    // Costruttore

    ClientGUI(String host, int port) {

 

        super("Chat Client");

        defaultPort = port;
        defaultHost = host;     

        // NorthPanel:

        JPanel northPanel = new JPanel(new GridLayout(3,1));

        // Il nome server e porta utilizzata

        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));

        

        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        serverAndPort.add(new JLabel("Indirizzo Server:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Numero di porta:  "));
		serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));

   

        northPanel.add(serverAndPort);

 



        label = new JLabel("Inserisci il tuo username ", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("Anonymous");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

 
        ta = new JTextArea("Benvenuto nella chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

 


        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);       
        whoIsIn = new JButton("Toc Toc");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);      

 
        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();

    }

 

    

    void append(String str) {

        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);

    }

    

    void connectionFailed() 
	{
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Inserisci il tuo username");
        tf.setText("Anonymous");

        

        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);

        

        tfServer.setEditable(false);
        tfPort.setEditable(false);

      

        tf.removeActionListener(this);
        connected = false;

    }

         
    

    public void actionPerformed(ActionEvent e) {

        Object o = e.getSource();

       

        if(o == logout) 
        {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }

        

        if(o == whoIsIn) 
        {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));              
            return;
        }

     

        if(connected) {

          

            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));            
            tf.setText("");
            return;

        }


        if(o == login) {

   

            String username = tf.getText().trim();

            
            if(username.length() == 0)

                return;

       
            String server = tfServer.getText().trim();

            if(server.length() == 0)

                return;



            String portNumber = tfPort.getText().trim();

            if(portNumber.length() == 0)

                return;

            int port = 0;

            try 
			{
                port = Integer.parseInt(portNumber);
			}

            catch(Exception en) 
			{

                return;   

            }

     
            client = new Client(server, port, username, this);

           

            if(!client.start())

                return;

            tf.setText("");
            label.setText("Inserisci il tuo messaggio");
            connected = true;

             

      

            login.setEnabled(false);

  

            logout.setEnabled(true);
            whoIsIn.setEnabled(true);

           

            tfServer.setEditable(false);
            tfPort.setEditable(false);

            

            tf.addActionListener(this);
        }
    }

 

    

    public static void main(String[] args) 
    {
       new ClientGUI("localhost", 1500);
    }
    
}
