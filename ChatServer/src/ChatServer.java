import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class ChatServer {
	int port = 3454;
	ServerSocket serSocket;
	Socket socket;
	Vector<ServerThread> vec;
	
	public ChatServer(int port)
	{
		this.port = port;
		vec = new Vector<ServerThread>(10,10);
		
		try
		{
			serSocket = new ServerSocket(port);
			System.out.println("Server Connecting ...");
			
			while(true)
			{
				socket = serSocket.accept();
				ServerThread serverThread = new ServerThread(this, socket);
				vec.addElement(serverThread);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				serSocket.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	class ServerThread extends Thread{
		ChatServer server;
		Socket socket;
		BufferedReader bufReader;
		PrintWriter pWriter;
		String proMsg, chatName;
		
		
		public ServerThread(ChatServer server, Socket socket) throws IOException {
			this.server = server;
			this.socket = socket;
			this.bufReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.pWriter = new PrintWriter(socket.getOutputStream(), true);
			System.out.println(socket.getInetAddress() + "Connect!");
		}
		
		public void run()
		{
			try
			{
				StringTokenizer token;
				String msg;
				
				while( (proMsg = bufReader.readLine()) != null )
				{
					token = new StringTokenizer(proMsg, "|");
					int protocol = Integer.parseInt(token.nextToken());
					
					chatName = token.nextToken();
					broadcastUserList();
					broadCast("¡Ú¡Ú" + chatName + "¡Ú¡Ú, Welcome~");
				}
			}
			catch(IOException e)
			{
				System.out.println(e);
				vec.removeElement(this);
			}
			finally
			{
				try
				{
					vec.removeElement(this);
					broadCast("¡Ú¡Ú" + chatName + "¡Ú¡Ú, Good Bye~");
					broadcastUserList();
					
					bufReader.close();
					pWriter.close();
					System.out.println(socket.getInetAddress() + "DisConnect!");
					socket.close();
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
		}
	}
	
	public void broadcastUserList()
	{
		StringBuffer sb = new StringBuffer("100");
		String chatName;
		for( int i = 0; i< vec.size(); i++)
		{
			ServerThread serverThread = vec.elementAt(i);
			chatName = serverThread.chatName;
			sb = sb.append("|" + chatName);
		}
		broadCast(sb.toString());
	}
	
	public void broadCast(String str)
	{
		for (int i = 0; i < vec.size() ; i++)
		{
			ServerThread st1 = vec.elementAt(i);
			st1.pWriter.println(str);
		}
	}
	
	public static void main(String args[])
	{
		ChatServer server = new ChatServer(3454);
	}
}
