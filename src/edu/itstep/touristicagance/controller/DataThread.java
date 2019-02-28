package edu.itstep.touristicagance.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.itstep.tourdata.TourData;
import edu.itstep.userdata.UserData;

public class DataThread implements Runnable
{
	private String id;
	
	private Socket socket;
	private Gson gson;
	private Scanner sc;
	private PrintWriter pw;
	
	private String strGSONfromClient;
	private String[] arrayFromClientGSONwithUserEmailAndPasswordOrNewUserInfo;
	private String strGSONtoClient;
	private UserData userData;
	
	private Connection connection;
	private Statement statement;
	private CallableStatement callableStatement; 
	private ResultSet result;
	private ResultSetMetaData meta;
	private String login;
	private String password;
	
	private ArrayList<String> arrayUserInfo; //��������� ��� �������� ������� UserData
	private ArrayList<String> clientRequest; //��������� ��� �������� ������� TourData
	private ArrayList<String> getCountriesList; //��������� ��� �������� ������ �����
	private ArrayList<String> getDurationsList; 
	private ArrayList<String> boughtTours; //��������� ��� �������� ������ ��������� ������
	
	private String toClient; //��� �������� json ������ �������
	
	private ArrayList<TourData> data;//������ �������� � ����� � �����
	private ArrayList<TourData> dataBourhtTour;//������ �������� � ����� � ��������� �����
	
	private Type typeStringArray; // ��� �������������� JSON � ���������� � ������
	
//	private int counter;
	private int index;// 

	
	private String[] arrayFromClien;//��� �������������� �������� JSON ������

	private boolean isAlive;
	
	public DataThread(Socket socket, Connection connection)
	{
		try
		{
			System.out.println("DataThread");
			this.connection=connection;
			this.socket = socket;
			typeStringArray = new TypeToken<String[]>(){}.getType();
			arrayUserInfo = new ArrayList<>();
			getCountriesList = new ArrayList<>();
			getDurationsList = new ArrayList<>();
			clientRequest = new ArrayList<>();
			boughtTours = new ArrayList<>();
			data = new ArrayList<>();
			dataBourhtTour = new ArrayList<>();
			login ="test2";
			password = "test";
			gson = new Gson();
			sc = new Scanner(socket.getInputStream());
			pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public void run()
	{
		receive();
	}
	private void receive()
	{
		while(sc.hasNext())
		{
			strGSONfromClient = sc.nextLine();	
			System.out.println("receive strGSONfromClient: " + strGSONfromClient);
			arrayFromClien = ((String[])gson.fromJson(strGSONfromClient, typeStringArray));
			System.out.println("reveive arrayFromClient BEFORE remove [0]: ");
			for(int i=0;i<arrayFromClien.length;i++)
			{
				System.out.print(arrayFromClien[i].toString() + " ");
			}
			System.out.println();
			index = Integer.parseInt(arrayFromClien[0].toString());
			arrayFromClien = ArrayUtils.removeElement(arrayFromClien, arrayFromClien[0]);
			System.out.println("reveive arrayFromClient AFTER remove [0]: ");
			System.out.println();
			for(int i=0;i<arrayFromClien.length;i++)
			{
				System.out.print(arrayFromClien[i].toString() + " ");
			}
			
			switch(index)
			{
			case 0:
				checkLoginAndPassword(arrayFromClien);
				break;
			case 1:
				userInfoRequest(arrayFromClien);
				break;
			case 2:
				clientRequest(arrayFromClien);
				break;
			case 3:
				registrationNewUser(arrayFromClien);
				break;
			case 4:
				addBoughtTourToDataBase(arrayFromClien);
				break;
			case 5:
				showAllBoughtTours();
				break;
			case 6:
				updateUserInfo(arrayFromClien);
				break;
			case 7: 
				deleteBougthTour(arrayFromClien);
				break;
			}
		}
	}

	private void checkLoginAndPassword(String[] arrayLoginAndPassword)
	{
		
		try
		{
			callableStatement = connection.prepareCall("{call getUserInfo(?)}");
			callableStatement.setString(1, arrayLoginAndPassword[0]);
			callableStatement.execute();
			
			result = callableStatement.getResultSet();
			meta = result.getMetaData();
			
			while(result.next())
			{
				for (int i = 1; i <= meta.getColumnCount(); i++)
				{
					arrayUserInfo.add(result.getString(i));
					if(arrayUserInfo.size()==meta.getColumnCount() && arrayLoginAndPassword[1].equals(arrayUserInfo.get(2)))
				    {
				    	userData = new UserData(arrayUserInfo.get(0), arrayUserInfo.get(1), arrayUserInfo.get(2), 
								arrayUserInfo.get(3), arrayUserInfo.get(4), arrayUserInfo.get(5));
				    } 
				}
			}
			callableStatement.close();
			//statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		
		if(userData==null)
		{
			System.out.println("No such user or invalid password");
		}
		
		sendUserInfo();
	}

	private void userInfoRequest(String[] arrayLoginAndPassword)
	{
		getCountriesList();
		getDurationsList();
		
		try
		{
			callableStatement = connection.prepareCall("{call getUserInfo(?)}");
			callableStatement.setString(1, arrayLoginAndPassword[0]);
			callableStatement.execute();
			
			result = callableStatement.getResultSet();
			meta = result.getMetaData();
			
			while(result.next())
			{
				for (int i = 1; i <= meta.getColumnCount(); i++)
				{
					 arrayUserInfo.add(result.getString(i));
				    if(arrayUserInfo.size()==meta.getColumnCount())
				    {
				    	userData = new UserData(arrayUserInfo.get(0), arrayUserInfo.get(1), arrayUserInfo.get(2), 
								arrayUserInfo.get(3), arrayUserInfo.get(4), arrayUserInfo.get(5));
				    }
				}
			}
			callableStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		if(userData==null)
		{
			System.out.println("No such user or invalid password");
		}
		else if(!arrayLoginAndPassword[1].equals(userData.getPassword()))
		{
			System.out.println("Invalid password");
		}
		sendUserInfo();
	}
	
	private void clientRequest(String[] request)
	{
		
		try
		{
			System.out.println(clientRequest);
			
			statement = connection.createStatement();
			if(!request[0].equals("0") && request[1].equals("0") && request[2].equals("0"))//������ �� ���� ++++++++++
			{
				System.out.println("1");
				result = statement.executeQuery("Select * from showAllInfoView " +  
						"Where _Date>=" + "'" + request[0].toString() + "'");
			}
			else if(!request[0].equals("0") && request[1].equals("0") && !request[2].equals("0")) // �� ���� � ����������������� ++++++++++++
			{
				System.out.println("2");
				result = statement.executeQuery("Select * from showAllInfoView " +  
						"Where _Date>=" + "'" + request[0].toString() + "'" + " AND " +
						"Duration=" + request[2].toString());
			}
			else if (!request[0].equals("0") && !request[1].equals("0") && request[2].equals("0"))// �� ���� � ������ ++++++++++++
			{
				System.out.println("3");
				result = statement.executeQuery("Select * from showAllInfoView " +  
						"Where _Date>=" + "'" + request[0].toString() + "'" + " AND " +
						"Country= " + "'" + request[1].toString() + "'");
			}
			//___________________________________________________________________________________________________
			else if(request[0].equals("0") && request[1].equals("0") && !request[2].equals("0")) // �� ����������������� +++++++++++++
			{
				System.out.println("4");
				result = statement.executeQuery("Select * from showAllInfoView Where " +  
						"Duration=" + request[2].toString());
			}
			else if(request[0].equals("0") && !request[1].equals("0") && !request[2].equals("0")) //�� ����������������� � ������ ++++++++
			{
				System.out.println("5");
				result = statement.executeQuery("Select * from showAllInfoView Where " +  
						"Duration=" + request[2].toString() + " AND " +
						"Country= " + "'" + request[1].toString() + "'");
			}
			//___________________________________________________________________________________________________
			
			else if(request[0].equals("0") && !request[1].equals("0") && request[2].equals("0"))// �� ������ ++++++++++++
			{
				System.out.println("6");
				result = statement.executeQuery("Select * from showAllInfoView " +  
						"Where Country= " + "'" + request[1].toString() + "'");
			}
			else
			{
				System.out.println("7");
				result = statement.executeQuery("Select * from showAllInfoView " +  
						"Where _Date=" + "'" + request[0].toString() + "'" + " AND " +
						"Duration=" + request[2].toString() + " AND " +
						"Country= " + "'" + request[1].toString() + "'");
			}
			
			meta = result.getMetaData();
			
			while (result.next())
			{
				for (int i = 1; i <= meta.getColumnCount(); i++)
				{
					clientRequest.add(result.getString(i));
				    if(clientRequest.size()==meta.getColumnCount())
				    {
				    	TourData td = new TourData(clientRequest.get(0), clientRequest.get(1), clientRequest.get(2),
				    							   clientRequest.get(3), clientRequest.get(4), clientRequest.get(5));
						data.add(td);
						clientRequest.clear();
				    }
				}
			}
			
			statement.close();
			System.out.println("clientRequest Client data " + data.toString());
			toClient = gson.toJson(data);
			data.clear();
			System.out.println("clientRequest Client JSON " + toClient);
			send(toClient);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void registrationNewUser(String[] arrayNewUserInfo)
	{
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("Select Email From showUserInfoView Where Email= " + "'" 
											+ arrayNewUserInfo[0] + "'");
			if(result.next()==true)
			{
				System.out.println("Email error. Use another Email.");
				statement.close();
			}
			else
			{
				callableStatement = connection.prepareCall("{call createNewUserProcedure(?,?,?,?,?)}");
				callableStatement.setString(1, arrayNewUserInfo[0]);
				callableStatement.setString(2, arrayNewUserInfo[1]);
				callableStatement.setString(3, arrayNewUserInfo[2]);
				callableStatement.setString(4, arrayNewUserInfo[3]);
				callableStatement.setString(5, arrayNewUserInfo[4]);
				callableStatement.execute();
				callableStatement.close();
				
				result = statement.executeQuery("Select id From showUserInfoView Where Email= " + "'" 
												+ arrayNewUserInfo[0] + "'");
				result.next();
				id = result.getString(1);
				
				userData = new UserData(id,arrayNewUserInfo[0],arrayNewUserInfo[1],
										arrayNewUserInfo[2],arrayNewUserInfo[3],
										arrayNewUserInfo[4]);
				
				statement.close();
				System.out.println("registrationNewUser " + userData.getId().toString());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		sendUserInfo();
	}
	
	private void addBoughtTourToDataBase(String[] arrayFromClien)
	{
		try
		{
			System.out.println("addBoughtTourToDataBase: ");
			for(int i=0;i<arrayFromClien.length;i++)
			{
				System.out.print(arrayFromClien[i].toString() + " ");
			}
			
			callableStatement = connection.prepareCall("{call addBoughtTour(?,?,?,?,?,?)}");
			callableStatement.setString(1, arrayFromClien[2]);
			callableStatement.setDouble(2,Double.parseDouble(arrayFromClien[3]));
			callableStatement.setInt(3, Integer.parseInt(arrayFromClien[4]));
			callableStatement.setString(4, arrayFromClien[5]);
			callableStatement.setString(5, arrayFromClien[6]);
			callableStatement.setInt(6, Integer.parseInt(arrayFromClien[0]));
			callableStatement.execute();
			callableStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void showAllBoughtTours()
	{
		try
		{
			System.out.println("userData: " + userData.getId());
			statement = connection.createStatement();
			result = statement.executeQuery("Select * from BoughtTours where idUser = " + userData.getId());
			meta = result.getMetaData();
				
			while(result.next())
			{
				for(int i=1;i<meta.getColumnCount();i++)
				{
					boughtTours.add(result.getString(i));
					System.out.println("result.getString(i): " + result.getString(i));
					if(boughtTours.size()+1==meta.getColumnCount())
					{
						TourData td = new TourData(boughtTours.get(0), boughtTours.get(1), boughtTours.get(2),
												   boughtTours.get(3), boughtTours.get(4), boughtTours.get(5));
						dataBourhtTour.add(td);
						boughtTours.clear();
					}
				}
			}
			statement.close();
			System.out.println("showAllBoughtTours  dataBourhtTour " + dataBourhtTour.toString());
			toClient = gson.toJson(dataBourhtTour);
			if(dataBourhtTour.size()!=0)
			{
				dataBourhtTour.clear();
				System.out.println("showAllBoughtTours  JSON " + toClient);
				send(toClient);
			}
			else
			{
				toClient = "0";
				send(toClient);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateUserInfo(String[] arrayFromClien)
	{
		try
		{
			System.out.println("updateUserInfo");
			for(int i=0;i<arrayFromClien.length;i++)
			{
				System.out.print(arrayFromClien[i].toString() + " ");
			}
			
			callableStatement = connection.prepareCall("{call updateUserInfo(?,?,?,?,?,?)}");
			callableStatement.setInt(1, Integer.parseInt(arrayFromClien[0]));
			callableStatement.setString(2, (arrayFromClien[1]));
			callableStatement.setString(3, (arrayFromClien[2]));
			callableStatement.setString(4, (arrayFromClien[3]));
			callableStatement.setString(5, (arrayFromClien[4]));
			callableStatement.setString(6, (arrayFromClien[5]));
			callableStatement.execute();
			
			callableStatement.close();
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void deleteBougthTour(String[] arrayFromClien)
	{
		try
		{
			
			System.out.println("deleteBougthTour: ");
			for(int i=0;i<arrayFromClien.length;i++)
			{
				System.out.print(arrayFromClien[i].toString() + " ");
			}
			
			callableStatement = connection.prepareCall("{call deleteBoughtTour(?)}");
			callableStatement.setInt(1, Integer.parseInt(arrayFromClien[0]));
			callableStatement.execute();
			callableStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendUserInfo()
	{
		if(userData==null)
		{
			userData = new UserData("0", "0", "0", "0", "0", "0");
		}
		strGSONtoClient = gson.toJson(userData);
		pw.println(strGSONtoClient);
		pw.flush();
		System.out.println("sendUserInfo " + strGSONtoClient);
		arrayUserInfo.clear();
	}
	
	private void getCountriesList()
	{
		
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("Select Countries.Name from countries");
			meta = result.getMetaData();
			getCountriesList.add(" ");
			while (result.next())
			{
				for (int i = 1; i <= meta.getColumnCount(); i++)
				{
					getCountriesList.add(result.getString(i));
				}
			}
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		toClient = gson.toJson(getCountriesList);
		getCountriesList.clear();
		sendCountriesList(toClient);
		System.out.println("getCountriesList : " + toClient);
	}
	
	private void getDurationsList()
	{
		try
		{
			callableStatement = connection.prepareCall("{call getDurationList}");
			callableStatement.execute();
			
			result = callableStatement.getResultSet();
			meta = result.getMetaData();
			
			getDurationsList.add(" ");
			while (result.next())
			{
				for (int i = 1; i <= meta.getColumnCount(); i++)
				{
					getDurationsList.add(result.getString(i));
				}
			}
			callableStatement.close();
			
			toClient = gson.toJson(getDurationsList);
			getDurationsList.clear();
			sendDurationsList(toClient);
			System.out.println("getDurationsList : " + toClient);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendCountriesList(String toClient)
	{
		pw.println(toClient);
		pw.flush();
	}
	
	private void sendDurationsList(String toClient)
	{
		pw.println(toClient);
		pw.flush();
	}

	private void send(String toClient)
	{
		System.out.println("send Client" + toClient);
		pw.println(toClient);
		pw.flush();
	}
}
