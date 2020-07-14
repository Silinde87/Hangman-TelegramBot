package com.silinde;

import java.io.File;
import java.util.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class TelegramBot extends TelegramLongPollingBot{
	
	HangmanGame hg;	
	//This map keeps all info that game needs to run while the player is playing
	static Map<Integer, HangmanGame> userList = new HashMap<Integer, HangmanGame>();	

	@Override
	public void onUpdateReceived(Update update) {		
		Long chatID = update.getMessage().getChatId();
		Integer userID = update.getMessage().getFrom().getId();
		StringBuilder mess = new StringBuilder("");
		int chInPlay = 0;
		
		//Bot is used from Telegram group condition
		if (update.getMessage().isGroupMessage())
			chInPlay = 1;
		
		//Detecting commands
		switch (update.getMessage().getText().toLowerCase()) {			
			case "/help":
				sendM("¡Hola " + update.getMessage().getFrom().getFirstName() + "!" + 
						"\n/start - Iniciar un nuevo juego" +
						"\n/exit - Borrar partida actual",chatID);
				break;
				
			case "/exit":
				if(userList.containsKey(userID)) {
					userList.remove(userID);
					sendM("Partida borrada con éxito.\nEscribe /start para iniciar una nueva.",chatID);
				}else {
					sendM(update.getMessage().getFrom().getFirstName() + ", no has iniciado ninguna partida.\n"
							+ "Escribe /start para iniciar una nueva.",chatID);					
				}
				break;

			//HangMangame Case
			case "/start":
				//User exists on Map userList, show warning message. Still in play..
				if(userList.containsKey(userID)) {					
					mess.append(update.getMessage().getFrom().getFirstName() + ", ¡No dejes colgado al cactus!");					
					attLeft(userID, mess, chatID);					
					sendM("\n\nEscribe una letra...",chatID);
					break;
				}else {
					//User doesn't exists on Map userList. Game begins...
					hg = new HangmanGame(userID,chatID);				
					//Storing user id
					userList.put(userID, hg);				
					sendM("| Bienvenido al juego del ahorcado |\n"
							+ "¿Serás capaz de descubrir la palabra?\n" + printWord(userID) + 
							"\n\n" + update.getMessage().getFrom().getFirstName() + ", escribe una letra...",chatID);					
					break;	
				}
				
			default:
				//Default case is used for unknown commands or letters when the game is started.
				//Game is started
				if (userList.get(userID).hangmanIsStarted) {
					//Game is alive condition.
					if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)>0 &&
							userList.get(userID).rightChars != userList.get(userID).totalChars) {
						userList.get(userID).charInPlay = update.getMessage().getText().trim().toUpperCase();
						
						
						//Check valid input options
						if((userList.get(userID).charInPlay.isEmpty() || userList.get(userID).charInPlay.matches(hg.REGEX)) ||
								(chInPlay==0 && userList.get(userID).charInPlay.length()>1) ||
								(chInPlay==1 && userList.get(userID).charInPlay.length()>2)) {
							sendM(update.getMessage().getFrom().getFirstName() + 
									", debes escribir una sola letra. Prueba de nuevo...",chatID);
							return;
						}						
					
						//Checking result of selected-by-user valid input character.
						switch (matchingChar(userID, userList.get(userID).charInPlay.charAt(chInPlay))) {
							case 1:
								//Case repeated letter
								mess.append("¡FALLO!\nLa letra " + userList.get(userID).charInPlay.charAt(chInPlay) + " está repetida");								
								if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0)						
									break;	
								attLeft(userID, mess, chatID);		
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));
								break;
							case 2:
								//case right letter
								if((userList.get(userID).totalChars - userList.get(userID).rightChars == 1)) {
									userList.get(userID).rightChars++;
									break;
								}else {
									mess.append("¡ACIERTO!\nLa letra " + userList.get(userID).charInPlay.charAt(chInPlay) + 
											" ha sido encontrada.\n");
									break;
								}

							case 3:
								//case wrong letter
								mess.append("¡FALLO!\nLa letra " + userList.get(userID).charInPlay.charAt(chInPlay) + 
										" no ha sido encontrada.");		
								if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0)						
									break;	
								attLeft(userID, mess, chatID);								
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));
								break;						
							}
						if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts) == 0 |
								(userList.get(userID).totalChars - userList.get(userID).rightChars) == 0) {							
							//Check final score
							if ((userList.get(userID).totalChars - userList.get(userID).rightChars) == 0) {
								mess.append("¡HAS GANADO!\nLa palabra es: " + printWord(userID) + 
										"\n\nEscribe /start para jugar de nuevo.");
								sendPh(userID, chatID,22);
								sendM(mess.toString(),chatID);
								userList.get(userID).hangmanIsStarted = false;
								userList.remove(userID);
							}else if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0){
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));
								for (char c : userList.get(userID).word.toCharArray())
									userList.get(userID).letters.replace(c, true);												
								sendM("¡HAS SIDO DERROTADO!\nLa palabra era: " + printWord(userID) + 
										"\n\nEscribe /start para jugar de nuevo.",chatID);							
								userList.get(userID).hangmanIsStarted = false;
								userList.remove(userID);								
							}
							break;
						}
						mess.append(printWord(userID) + "\n\n" + update.getMessage().getFrom().getFirstName() + 
								", escribe una letra...");
						sendM(mess.toString(),chatID);
						mess.setLength(0);						
					}
				//Game is NOT started
				}else
					sendM(update.getMessage().getFrom().getFirstName() + ", has elegido una opción no existente",chatID);						
		}
	}
	
	
	//This method shows the attempts left
	public void attLeft(int uID, StringBuilder ms, long cID) {		
		if(((hg.MAX_ATTEMPTS) - userList.get(uID).attempts) > 1 ) {
			ms.append("\nTe quedan " + (hg.MAX_ATTEMPTS - userList.get(uID).attempts) + " intentos.");
			sendM(ms.toString(),cID);
			ms.setLength(0);
		}else {
			ms.append("\nTe queda " + (hg.MAX_ATTEMPTS - userList.get(uID).attempts) + " intento.");
			sendM(ms.toString(),cID);
			ms.setLength(0);
		}
	}

	
	//This method prints the word with blank spaces while the game works.
	public String printWord(int uID) {
		userList.get(uID).printedWord = "";		
		userList.get(uID).rightChars = 0;
		for(int i = 0; i<userList.get(uID).word.length(); i++) {
			if(userList.get(uID).letters.get(userList.get(uID).word.charAt(i))) {
				userList.get(uID).printedWord += userList.get(uID).word.charAt(i) + " ";
				userList.get(uID).rightChars++;
			}else
				userList.get(uID).printedWord += "__ ";
		}		
		return userList.get(uID).printedWord;
	}	
	
	//This method finds the char at word and changes the value at HashMap
	public int matchingChar(int uID, Character c) {
		if(userList.get(uID).letters.get(c)) {
			userList.get(uID).attempts++;			
			return 1;
			//Case repeating letter
		}else {
			if(userList.get(uID).word.contains(c.toString())) {
				userList.get(uID).letters.replace(c, true);				
				return 2;
				//case right letter
			}else {
				userList.get(uID).attempts++;
				//case wrong letter
				return 3;
			}
		}
	}
	
	//This method sends hang man photo
	public void sendPh(int uID, Long l, int filename) {
		//Thanks @Sweeetbrush-Graphic Designer for this cute Cactus
		SendSticker st = new SendSticker();
		st.setChatId(l);
		st.setSticker(new File("src/main/resources/" + filename + ".webp"));
		try {
			execute(st);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	//This method sends message using String parameter as a return.
	public void sendM(String a, Long l) {
		SendMessage smh = new SendMessage();
		smh.setChatId(l);
		smh.setText(a);
		try {
			execute(smh);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}		
	}
	
	
	@Override
	public String getBotUsername() {
		return "SilindeBot";
	}

	@Override
	public String getBotToken() {
		return "1168114454:AAHZFy3i0Gxh8gswzwAi7p84okZWd3s3NqY";
	}

}
