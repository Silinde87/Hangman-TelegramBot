package com.silinde;

import java.awt.FlowLayout;
import javax.swing.*;


import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Test {
	static JFrame frame;
	static JPanel panel;
	static JLabel labStatus;
	static JLabel labTimesPlayed;
	static JLabel labCurrentPlayers;
	static JTextField text;
	
	
	public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramBot());
            mainFrame();
    	
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
	}
	
	public static void mainFrame() {		
		frame = new JFrame("Hangman Game");
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		labStatus = new JLabel("Game is running. Close to exit.");
		panel.add(labStatus);
		frame.add(panel);
		frame.setSize(300,100);
		frame.setLocationRelativeTo(null);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);	
	}
	
	
}






