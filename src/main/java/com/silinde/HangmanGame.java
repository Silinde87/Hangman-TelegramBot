package com.silinde;

import java.io.*;
import java.util.*;

public class HangmanGame {
	
	public int totalChars = 0, rightChars = 0, attempts = 0, userID;
	public final int MAX_ATTEMPTS = 6;
	public final String REGEX = "[^A-Za-zÑñ]+";
	public String word = "", printedWord = "", charInPlay = "";	
	public final Map<Character,Boolean> letters = new HashMap<Character,Boolean>();
	public long chatID;
	public boolean hangmanIsStarted = false;
	
	//Constructor initializing all the game needs.
	public HangmanGame(int userID, long chatID){
		this.userID = userID;
		this.chatID = chatID;
		this.hangmanIsStarted = true;
		try {
			choosingWord();
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalChars = word.length();
		
		for (Character c : "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ".toCharArray()) {
			letters.put(c, false);
		}		
	}	
		
	//Choosing a random word from a list
	public void choosingWord() throws IOException{
		int numWords = -1;
		String line = "";
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader fr = new BufferedReader(new FileReader("src/main/resources/diccionarioES.txt"));
		while((line = fr.readLine())!=null) {			
			numWords++;
			list.add(line);
		}
		fr.close();	
		int i = (int)Math.ceil(Math.random()*numWords);
		word = list.get(i)
				.replace('á', 'a').replace('à', 'a')
				.replace('é', 'e').replace('è', 'e')
				.replace('í', 'i').replace('ì', 'i')
				.replace('ó', 'o').replace('ò', 'o')
				.replace('ú', 'u').replace('ù', 'u')
				.toUpperCase();		
	}	

}
