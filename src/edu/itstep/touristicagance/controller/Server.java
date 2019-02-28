package edu.itstep.touristicagance.controller;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;

import edu.itstep.touristicagance.view.StartFrame;


public class Server
{

	public static void main(String[] args)
	{
		try
		{
			DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
			StartFrame frame = new StartFrame();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
