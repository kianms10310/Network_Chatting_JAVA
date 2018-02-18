import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;


public class ChatClient extends Frame implements ActionListener{	
	TextArea ta;
	Choice choice;												//선택
	TextField tf, tfHost, tfChatName;
	Button btnConn, btnDisconn, btnOK, btnExit, btnSend;		//Connect, disConnect, OK, Exit, Send 버튼
	List userList;		//유저 List 
	
	Socket socket;
	PrintWriter pWriter;
	
	Dialog conDialog;
	
	String host = "127.0.0.1", chatName="localhost";
	String selFunction = "ALL";
	String selUser = null;

	int port = 3454;
	
	public ChatClient() 										//생성자
	{
		super("Clinet");
		setFrame();
		setSize(500, 400);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				if(socket != null)
					disconnect();
				System.exit(0);
			}
			
		});
	}
	
	public void setFrame()
	{
		Panel panNorth = new Panel();
		Panel panSouth = new Panel();
		Panel panEast = new Panel(new BorderLayout());
		
		tf = new TextField("", 40);
		ta = new TextArea("", 10, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
		
		choice = new Choice();
		
		btnConn = new Button("Connect");
		btnDisconn = new Button("DisConnect");
		btnSend = new Button("Send");
				
		panNorth.add(btnConn);
		panNorth.add(btnDisconn);
		add(panNorth, BorderLayout.NORTH);
		enButton(true);
		
		
		ta.setEditable(false);
		add(ta, BorderLayout.CENTER);
				
		choice.add("ALL");
		panSouth.add(choice);
		panSouth.add(tf);
		panSouth.add(btnSend);
		add(panSouth, BorderLayout.SOUTH);
		
		panEast.add(new Label("User List", Label.CENTER), BorderLayout.NORTH);
		userList = new List(10, false);
		panEast.add(userList, BorderLayout.CENTER);
		add(panEast, BorderLayout.EAST);
		
		setDialog();
		
		btnConn.addActionListener(this);
		btnDisconn.addActionListener(this);
		btnSend.addActionListener(this);
		
		tf.addActionListener(this);
	}
	
	public void enButton(boolean flag)
	{
		btnConn.setEnabled(flag);
		btnDisconn.setEnabled(!flag);
	}

	public void setDialog()
	{
		Panel panAll = new Panel();
		Panel panLabel = new Panel(new GridLayout(2, 1));
		Panel panTextField = new Panel(new GridLayout(2, 1));
		Panel panBtn = new Panel();
		
		btnOK = new Button("OK");
		btnExit = new Button("Exit");
		conDialog = new Dialog(this, "Connect Setting", true);
		conDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				conDialog.hide();
			}
		});
		conDialog.add(panAll, BorderLayout.CENTER);
		panLabel.add(new Label("Host", Label.LEFT));
		panLabel.add(new Label("NickName", Label.LEFT));
		
		tfHost = new TextField(20);
		tfChatName = new TextField(20);
		panTextField.add(tfHost);
		panTextField.add(tfChatName);
		
		panAll.add(panLabel);
		panAll.add(panTextField);
		
		conDialog.add(panBtn, BorderLayout.SOUTH);
		
		panBtn.add(btnOK);
		panBtn.add(btnExit);
		conDialog.pack();
		
		btnOK.addActionListener(this);
		btnExit.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e){
		Object obj = e.getSource();
		
		if(obj == btnConn)
		{
			Point point = this.getLocation();
			tfHost.setText(host);
			tfHost.setEditable(false);
			tfChatName.setText(chatName);
			conDialog.setLocation(point.x + 150, point.y + 150);
			conDialog.show();
			
		}
		/*
		else if(obj == btnExit)
		{
			conDialog.hide();
		}
		*/
		else if(obj == btnOK)
		{
			host = "127.0.0.1";
			chatName = tfChatName.getText().trim();
			
			if(host.equals("") || chatName.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Please, Enter Host Address, NickName");
				return;
			}
			conDialog.hide();
			connect();
		}
		else if(obj == btnDisconn)
		{
			disconnect();
			enButton(true);
			ta.append("DisConnect");
		}
		else if(obj == tf || obj == btnSend)
		{
			String msg = tf.getText();
			
			if(msg.equals("/clear"))
				ta.setText("");
			else
			{
				String protocol = "200|" + msg;
				pWriter.println(protocol);
			}
			tf.setText("");
		}
	}
	
	public void connect()
	{
		try
		{
			socket = new Socket(host, port);
			pWriter = new PrintWriter(socket.getOutputStream(), true);
			enButton(false);
			choice.select("ALL");
			pWriter.println("100|" + chatName);
			tf.requestFocus();
			ClientThread cThread = new ClientThread();
			cThread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			enButton(true);
			System.out.println(e);
			JOptionPane.showMessageDialog(this, "Cannot Connect Host");
		}
	}
	
	class ClientThread extends Thread{
		BufferedReader brSocket;
		
		public ClientThread() throws IOException{
			brSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		
		public void run()
		{
			try
			{
				String receMsg;
				StringTokenizer token;
				
				while((receMsg = brSocket.readLine()) != null)
				{
					token = new StringTokenizer(receMsg, "|");
					String protocol = token.nextToken();
					if(protocol.equals("100"))
					{
						userList.removeAll();
						while(token.hasMoreElements()){
							String usr = token.nextToken();
							userList.add(usr);
						}
					}
					else
						ta.append(receMsg + "\n");
				}
			}
			catch(IOException e)
			{
				System.out.println(e);
			}finally
			{
				try
				{
					brSocket.close();
				}
				catch(IOException e)
				{
					System.out.println(e);
				}
			}
		}
	}
	
	public void disconnect()
	{
		try
		{
			socket.close();
			socket = null;
			userList.removeAll();
			choice.select("ALL");
			System.out.println("Disconnect Server");
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
	
	public static void main(String args[])
	{
		new ChatClient();
	}

}
