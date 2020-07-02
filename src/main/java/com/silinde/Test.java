package com.silinde;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Test {
	public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
        	telegramBotsApi.registerBot(new TelegramBot());
		JFrame frame = new JFrame("Hangman Game");
        	JPanel panel = new JPanel();
        	panel.setLayout(new FlowLayout());
        	JLabel label = new JLabel("Game is running. Close to exit.");        	   	
        	panel.add(label);        	
        	frame.add(panel);
        	frame.setSize(300, 100);
        	frame.setLocationRelativeTo(null);
        	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	frame.setVisible(true); 
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
	}

}
