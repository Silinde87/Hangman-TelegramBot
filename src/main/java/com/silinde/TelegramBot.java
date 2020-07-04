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
				//User exists on Map userList
				if(userList.containsKey(userID)) {
					sendM("¡No dejes colgado al cactus!",chatID);
					if(((hg.MAX_ATTEMPTS) - userList.get(userID).attempts) > 1 )
						sendM("Te quedan " + ((hg.MAX_ATTEMPTS) - 
								userList.get(userID).attempts) + " intentos.",chatID);	
					else 						
						sendM("Te queda " + ((hg.MAX_ATTEMPTS) - 
								userList.get(userID).attempts) + " intento.",chatID);				
					sendM(printWord(userID),chatID);
					sendM("Escribe una letra...",chatID);
					break;
				}else {
					hg = new HangmanGame(userID,chatID);				
					//Storing user id
					userList.put(userID, hg);				
					sendM("| Bienvenido al clásico juego del ahorcado |",chatID);
					sendM("¿Serás capaz de descubrir la siguiente palabra?\n" + printWord(userID),chatID);
					sendM("Escribe una letra...",chatID);	
					break;	
				}
				
			default:
				if (userList.get(userID).hangmanIsStarted) {
					//Game is alive condition.
					if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)>0 &&
							userList.get(userID).rightChars != userList.get(userID).totalChars) {
						userList.get(userID).charInPlay = update.getMessage().getText().trim().toUpperCase();
						
						//Check valid input options
						if(userList.get(userID).charInPlay.length()>1 || 
								userList.get(userID).charInPlay.isEmpty() ||
								userList.get(userID).charInPlay.matches(hg.REGEX)) {	
							sendM("Debes escribir una sola letra. Prueba de nuevo...",chatID);
							return;									
						}
						//Checking result of selected-by-user valid input character.
						switch (matchingChar(userID, userList.get(userID).charInPlay.charAt(0))) {
							case 1:
								//Case repeating letter
								sendM("¡FALLO! La letra " + userList.get(userID).charInPlay.charAt(0) + " está repetida",chatID);
								if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)>1)
									sendM("Te quedan " + (hg.MAX_ATTEMPTS - userList.get(userID).attempts) + " intentos.",chatID);	
								else {
									if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0)						
										break;							
									sendM("Te queda " + (hg.MAX_ATTEMPTS - userList.get(userID).attempts) + " intento.",chatID);
								}
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));						
								break;
							case 2:
								//case right letter
								sendM("¡ACIERTO! La letra " + userList.get(userID).charInPlay.charAt(0) + " ha sido encontrada.",chatID);
								if((userList.get(userID).totalChars - userList.get(userID).rightChars == 1))
									userList.get(userID).rightChars++;
								break;
							case 3:
								//case wrong letter
								sendM("¡FALLO! La letra " + userList.get(userID).charInPlay.charAt(0) + " no ha sido encontrada.",chatID);
								if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)>1)
									sendM("Te quedan " + (hg.MAX_ATTEMPTS - userList.get(userID).attempts) + " intentos.",chatID);	
								else {
									if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0)
										break;
									sendM("Te queda " + (hg.MAX_ATTEMPTS - userList.get(userID).attempts) + " intento.",chatID);
								}
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));
								break;						
							}
						if ((hg.MAX_ATTEMPTS - userList.get(userID).attempts) == 0 |
								(userList.get(userID).totalChars - userList.get(userID).rightChars) == 0) {							
							//Check final score
							if ((userList.get(userID).totalChars - userList.get(userID).rightChars) == 0) {
								sendM("¡HAS GANADO! La palabra es: \n" + printWord(userID),chatID);
								sendPh(userID, chatID,22);
								userList.get(userID).hangmanIsStarted = false;
								userList.remove(userID);
								sendM("Escribe /start para jugar de nuevo.",chatID);
							}else if((hg.MAX_ATTEMPTS - userList.get(userID).attempts)==0){
								sendPh(userID, chatID,(hg.MAX_ATTEMPTS - userList.get(userID).attempts));
								for (char c : userList.get(userID).word.toCharArray())
									userList.get(userID).letters.replace(c, true);												
								sendM("¡HAS SIDO DERROTADO! La palabra era: \n" + printWord(userID),chatID);							
								userList.get(userID).hangmanIsStarted = false;
								userList.remove(userID);
								sendM("Escribe /start para jugar de nuevo.",chatID);
							}
							break;
						}
						sendM(printWord(userID),chatID);
						sendM("Escribe una letra...",chatID);
					}
				}else
					sendM(update.getMessage().getFrom().getFirstName() + ", has elegido una opción no existente",chatID);						
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
