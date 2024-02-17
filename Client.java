import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
	Socket socket;
	Thread thread;
	DataInputStream dis;
	DataOutputStream dos;
	public static void main(String[] args){
		int q=0;
		try{
			Scanner scanner=new Scanner(System.in);
			System.out.print("Enter Username : ");
			String name=scanner.nextLine();
			Client client=new Client();
			client.socket=new Socket("127.0.0.1",8080);
			client.dis=new DataInputStream(client.socket.getInputStream());
			client.dos=new DataOutputStream(client.socket.getOutputStream());

			client.thread=new Thread(client);
			client.thread.start();
			client.dos.writeUTF(name);
			client.dos.flush();
			while(q==0){
				System.out.print("> You : ");
				String string=scanner.nextLine();
				client.dos.writeUTF(string);
				client.dos.flush();
				if(string.equals("quit")||string.equals("bye"))
					q=1;
			}
			client.dos.close();
			scanner.close();
			client.socket.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	@Override
	public void run(){
		try{
			while(this.socket.isConnected()){
				String message=(String)this.dis.readUTF();
				if(message!=null && message.length()>0){
					System.out.println("\n"+message);
					System.out.print("> You : ");
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
