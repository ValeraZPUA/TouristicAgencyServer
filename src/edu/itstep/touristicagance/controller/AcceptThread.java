package edu.itstep.touristicagance.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.itstep.touristicagance.view.StartFrame;

public class AcceptThread implements Runnable
{
	private int port;
	private ServerSocket ss;
	private volatile boolean isAlive;
	private Socket clientSocket;
	private DataThread dataThread;
	private Connection connection;
	private String login, password;
	
	public AcceptThread(int port, String login, String password)
	{
		this.port = port;
		this.login = login;
		this.password=password;
		
		isAlive = true;
		
		try
		{
			ss = new ServerSocket(port);
			connection = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1;instance=SQLEXPRESS;" +
					 "databaseName=TouristicAgency;user="+login+";password="+password+";");
			
			/*connection = DriverManager.getConnection("jdbc:sqlserver://10.3.11.100;" 
					+ "databaseName=TouristicAgency;user="+login+";password="+password+";"); */
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Invalid password or login!");
			stop();
		}
	}
	
	public void run()
	{
		
		do
		{
			System.out.println("Server started");
			try
			{
				if(isAlive)
				{
					System.out.println("Waiting for user");
					clientSocket = ss.accept();	
					System.out.println("User connected");
					dataThread = new DataThread(clientSocket, connection);
					Thread thread = new Thread(dataThread);
					thread.start();
				}
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}while(isAlive);
	}

	

	public void stop()
	{
		isAlive = false;
		StartFrame.setEditable();
	}
}
