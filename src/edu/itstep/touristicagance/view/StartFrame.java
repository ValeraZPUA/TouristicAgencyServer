package edu.itstep.touristicagance.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import edu.itstep.touristicagance.controller.AcceptThread;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.SwingConstants;
import javax.swing.JTextField;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPasswordField;

public class StartFrame extends JFrame
{

	private JPanel contentPane;
	private static JTextField tfOnLine;
	private static JTextField tfLogin;
	private static JPasswordField pfPassword;
	private JButton btnStart;
	private JButton btnStop;
	
	private AcceptThread at;
	private String login, password;
	
	public StartFrame()
	{
		setResizable(false);
		setTitle("Server");
		setBounds(100, 100, 213, 186);
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		btnStart = new JButton("Start");
		btnStart.setBounds(10, 123, 187, 23);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				login = tfLogin.getText();
				password = new String(pfPassword.getPassword());
				if(!login.equals("") && !password.equals(""))
				{
					tfOnLine.setBackground(Color.red);
					tfOnLine.setForeground(Color.black);
					tfOnLine.setText("OnLine");
					tfLogin.setEditable(false);
					pfPassword.setEditable(false);
					
					connect(login, password);	
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Enter Login and Password!");
				}
				
				
			}
		});
		contentPane.setLayout(null);
		contentPane.add(btnStart);
		
		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if(at!=null)
				{
					at.stop();
					System.out.println("Server stopped");
				}
			}
		});
		btnStop.setBounds(108, 123, 89, 23);
		contentPane.add(btnStop);
		
		tfOnLine = new JTextField();
		tfOnLine.setForeground(Color.LIGHT_GRAY);
		tfOnLine.setHorizontalAlignment(SwingConstants.CENTER);
		tfOnLine.setEditable(false);
		tfOnLine.setText("OffLine");
		tfOnLine.setBounds(54, 11, 86, 20);
		contentPane.add(tfOnLine);
		tfOnLine.setColumns(10);
		
		JLabel lblLogin = new JLabel("Login: ");
		lblLogin.setBounds(10, 67, 74, 14);
		contentPane.add(lblLogin);
		
		JLabel lblPassword = new JLabel("Password: ");
		lblPassword.setBounds(10, 92, 74, 14);
		contentPane.add(lblPassword);
		
		JLabel lblDataBase = new JLabel("Data Base:");
		lblDataBase.setHorizontalAlignment(SwingConstants.CENTER);
		lblDataBase.setHorizontalTextPosition(SwingConstants.CENTER);
		lblDataBase.setBounds(54, 42, 86, 14);
		contentPane.add(lblDataBase);
		
		tfLogin = new JTextField();
		tfLogin.setText("");
		tfLogin.setBounds(108, 64, 89, 20);
		contentPane.add(tfLogin);
		tfLogin.setColumns(10);
		
		pfPassword = new JPasswordField();
		pfPassword.setBounds(108, 89, 89, 20);
		contentPane.add(pfPassword);
	}

	protected void connect(String login, String password)
	{
		at = new AcceptThread(5296, login, password);
		Thread thread = new Thread(at);
		thread.start();
	}
	public static void setEditable()
	{
		tfLogin.setEditable(true);
		pfPassword.setEditable(true);
		tfOnLine.setForeground(Color.LIGHT_GRAY);
		tfOnLine.setBackground(new Color(238,238,238));
		tfOnLine.setEditable(false);
		tfOnLine.setText("OffLine");
	}
	
}
