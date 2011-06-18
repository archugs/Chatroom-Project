import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import java.io.*;
import java.net.*;


public class ChatClient {

	private JFrame frame;
	private JTextArea output;
	private JTextField input;
	private JButton sendButton;
	private JButton quitButton;
	private JLabel heading;
	private JComboBox usernames;
	private JDialog aboutDialog;
	
	private Socket connection = null;
	private BufferedReader serverIn = null;
	private PrintStream serverOut = null;
	
	
	public ChatClient()
	{
		output = new JTextArea(10,50);
		input = new JTextField(50);
		sendButton = new JButton("Send");
		quitButton = new JButton("Quit");
		heading = new JLabel("Chat room");
		String[] names = {"Jim Brown", "Jane Simms", "Mary George"};
		usernames = new JComboBox(names);
		
	}
	
	public void launchFrame()
	{
		frame = new JFrame("Chat Room");
		frame.setLayout(new BorderLayout());
		frame.add(heading, BorderLayout.NORTH);
		frame.add(output, BorderLayout.WEST);
		frame.add(input, BorderLayout.SOUTH);
		
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		file.add(quitMenuItem);
		mb.add(file);
		frame.setJMenuBar(mb);
		JMenu help= new JMenu("Help");
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutHandler());
		help.add(aboutMenuItem);
		mb.add(help);
		
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3,1));
		p1.add(sendButton);
		p1.add(quitButton);
		p1.add(usernames);
		
		frame.add(p1,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		sendButton.addActionListener(new SendHandler());
		input.addActionListener(new SendHandler());
		frame.addWindowListener(new CloseHandler());
		quitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		doConnect();
	}
	
	public void doConnect()
	{
		String serverIP = System.getProperty("serverIP", "127.0.0.1");
		String serverPort = System.getProperty("serverPort", "2000");
		try
		{
			connection = new Socket(serverIP, Integer.parseInt(serverPort));
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			serverIn = new BufferedReader(isr);
			serverOut = new PrintStream(connection.getOutputStream());
			Thread t = new Thread(new RemoteReader());
			t.start();
		}
		
		catch(Exception e)
		{
			System.err.println("Unable to connect to server");
			e.printStackTrace();
		}
	}
	
	private class RemoteReader implements Runnable
	{
		public void run()
		{
			try
			{
				while(true)
				{
					String nextLine = serverIn.readLine();
					output.append(nextLine + "\n");
				}
			}
				
				catch(Exception e)
				{
					System.err.println("Error while reading from server");
					e.printStackTrace();
				}
			}
		}
	
	private class SendHandler implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String text = input.getText();
			text = usernames.getSelectedItem() + ": " + text + "\n";
			serverOut.print(text);
			input.setText("");
			
		}
		
	}
	
	private class CloseHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			System.exit(0);
		}
	}
	
	private class AboutHandler implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(aboutDialog == null)
			{
				aboutDialog = new AboutDialog(frame, "About", true);
			}
			aboutDialog.setVisible(true);
			}
			
		}
		
	private class AboutDialog extends JDialog implements ActionListener
	{
		public AboutDialog(JFrame parent, String title, boolean modal)
		{
			super(parent, title, modal);
			add(new JLabel("The ChatClient is a neat tool that allows you to talk to other ChatClients via a ChatServer"),BorderLayout.NORTH);
			JButton b = new JButton("OK");
			add(b, BorderLayout.SOUTH);
			b.addActionListener(this);
			pack();
		}
		
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}
	
	public static void main(String[] args)
	{
		ChatClient c= new ChatClient();
		c.launchFrame();
	}
}
