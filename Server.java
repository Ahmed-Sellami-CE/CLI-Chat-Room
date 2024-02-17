import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server implements Runnable{
	ServerSocket server;
	Socket socket;
	LinkedList<CustomClient> clients;
	Thread startThread;
	CustomClient temp;

	public Server(ServerSocket server){
		this.server=server;
		this.clients=new LinkedList<>();
		this.startThread=new Thread(this);
	}

	public static void main(String[] args){
		try{
			Server serv=new Server(new ServerSocket(8080));
			serv.startThread.start();
		}catch(Exception e){
			System.out.println(e);
		}
	}

	@Override
	public void run(){
		int id=0;
		while(this.clients.size()<=100){
			try{
				this.socket=this.server.accept();
				this.temp=new CustomClient(this.socket,this.socket.getInetAddress().getHostAddress(),this.socket.getPort(),new DataInputStream(this.socket.getInputStream()),id,new MessageThread(this));
				String result="";
				if(this.clients.size()==0){
					result="#Server : No Users are currently available !";
				}else{
					result+="#Server : Available Users = ";
					for(CustomClient cc:this.clients){
						cc.dos.writeUTF("#Server : "+temp.name+" joined the chat!");
						cc.dos.flush();
						result+=cc.name+";";
					}
				}
				temp.dos.writeUTF(result);
				temp.dos.flush();
				this.clients.add(temp);
				id++;
			}catch(Exception e){}
		}
		try{
			this.startThread.join();
		}catch(Exception e){}
	}
}

class CustomClient{
	int id;
	Socket socket;
	String ipv4Address;
	int port;
	String name;
	DataInputStream dis;
	DataOutputStream dos;
	MessageThread mt;
	Thread thread;

	public CustomClient(Socket socket,String ipv4Address,int port,DataInputStream dis,int id,MessageThread mt){
		this.socket=socket;
		this.ipv4Address=ipv4Address;
		this.port=port;
		this.dis=dis;
		this.id=id;
		this.mt=mt;
		this.mt.client=this;
		try{
			this.dos=new DataOutputStream(this.socket.getOutputStream());
			this.name=(String)dis.readUTF();
		}catch(Exception e){}
		this.thread=new Thread(this.mt);
		this.mt.thread=this.thread;
		this.thread.start();
	}
}

class MessageThread implements Runnable{
	Server parent;
	CustomClient client;
	Thread thread;
	public MessageThread(Server parent){
		this.parent=parent;
	}

	@Override
	public void run(){
				while(!this.client.socket.isClosed()){
							try{
								String message=this.client.dis.readUTF();
								if(message!=null){
									if(message.length()>0){
										System.out.println(this.client.name+" : "+message);
										for(CustomClient cc:this.parent.clients){
											if(cc!=this.client){
												try{
													cc.dos.writeUTF("> "+this.client.name+" : "+message);
													cc.dos.flush();													
												}catch(Exception exc){}
											}
										}
										if(message.equals("quit")||message.equals("bye")){
											try{
												this.parent.clients.remove(this.client);
												for(CustomClient cc:this.parent.clients){
													cc.dos.writeUTF("Server : "+this.client.name+" left the chat!");
													cc.dos.flush();
												}
												this.thread.join();
												this.client.socket.close();
											}catch(Exception e){}
										}

									}
								}else{
									continue;
								}

							}catch(Exception e){}
						}
	}
}
