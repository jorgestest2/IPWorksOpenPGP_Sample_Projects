/*
 * IPWorks OpenPGP 2022 Java Edition- Demo Application
 *
 * Copyright (c) 2023 /n software inc. - All rights reserved. - www.nsoftware.com
 *
 */

import java.io.*;

import ipworksopenpgp.*;

public class pfilemailer extends ConsoleDemo {
	Pfilemailer mailer = new Pfilemailer();
	Keymgr keymgr = new Keymgr();
	
	boolean encrypt = false;
	boolean sign = false;
	
	public pfilemailer() {
		String keyringDir = "";

		try {
			System.out.println("**************************************************************");
			System.out.println("IPWorks OpenPGP Java Edition - PFilemailer Demo Application.");
			System.out.println("This demo shows how to use the PFilemailer component to send  ");
			System.out.println("encrypted and signed emails.                                  ");
			System.out.println("**************************************************************\n");

			keymgr.addKeymgrEventListener(new DefaultKeymgrEventListener() {
				public void keyList(KeymgrKeyListEvent e) {
					System.out.println(String.format("%-80s [%-8s] %-12s", e.userId, e.keyId, (e.hasSecretKey ? "private" : "")));
				}
			});	      

			while(true) {	    	
				int command = Character.getNumericValue(ask("Please enter a command", ":", "(0: quit, 1: configure pgp settings, 2: send new email)"));
				if (command < 0 || command > 2) {
					System.out.println("Invalid Command.");
					continue;
				}
				if (command == 0) {
					break;
				} else if(command == 1) { //Configure PGP settings.
					while(true) {
						System.out.println("Current PGP Settings...");
						System.out.println("\tKeyring:                " + 
											(keymgr.getKeyring().length() > 0 ? keymgr.getKeyring() : "Not set"));
						System.out.println("\tPrivate Key:            " + 
											(mailer.getKeys().size() > 0 ? mailer.getKeys().get(0).getUserId() : "None selected"));
						System.out.println("\tPrivate Key Passphrase: " + (mailer.getKeys().size() > 0 ? 
											(mailer.getKeys().get(0).getPassphrase().length() > 0 ? "Set" : "Not set") : "Not set"));
						System.out.println("\tRecipient Key:          " + 
											(mailer.getRecipientKeys().size() > 0 ? mailer.getRecipientKeys().get(0).getUserId() : "Non selected"));
						System.out.println();
						int option = Character.getNumericValue(ask("Please select an operation", ":", "(0: back, 1: change keyring, " + 
											"2: list keys, 3: change private key, 4: change recipient key)"));
						
						if(option == 0) { 			//Go back to previous menu.
							break;
						} else if(option == 1) { 	//Specify keyring
							boolean keyringLoaded = false;
							while(!keyringLoaded) {
								try {
									String entry = prompt("Keyring Directory (Enter \":q\" to exit)");
									if(entry.equalsIgnoreCase(":q"))
										break;
									keymgr.loadKeyring(entry);
									keyringDir = entry;
									keyringLoaded = true;
								} catch(Exception ex) {
									displayError(ex);      
								}
							}

							System.out.println("\nListing keys...\n");
							System.out.println(String.format("%-80s %-10s %-12s", "User ID", "Key ID", "Private Key"));
							keymgr.listKeys();
							System.out.println();
						} else if(option == 2) { 	//List keys
							System.out.println("\nListing keys...\n");
							System.out.println(String.format("%-80s %-10s %-12s", "User ID", "Key ID", "Private Key"));
							keymgr.listKeys();
							System.out.println();
						} else if(option == 3) { 	//Specify private key
							boolean keyLoaded = false;
							while(!keyLoaded) {
								try {
									String entry = prompt("UserId (Enter \":q\" to exit, \":c\" to clear entry)");
									if(entry.equalsIgnoreCase(":q")) {
										break;
									} else if(entry.equalsIgnoreCase(":c")) {
										mailer.getKeys().clear();
									} else {
										mailer.getKeys().clear();
										mailer.getKeys().add(new Key(keyringDir, entry));
										mailer.getKeys().get(0).setPassphrase(prompt("Passphrase"));
									}
									keyLoaded = true;
								} catch(Exception ex) {
									displayError(ex);
								}
							}							
						} else if(option == 4) {	//Specify recipient key
							boolean keyLoaded = false;
							while(!keyLoaded) {
								try {
									String entry = prompt("Recipient UserId (Enter \":q\" to exit, \":c\" to clear entry)");
									if(entry.equalsIgnoreCase(":q")) {
										break;
									} else if(entry.equalsIgnoreCase(":c")) {
										mailer.getRecipientKeys().clear();
									} else {
										mailer.getRecipientKeys().clear();
										mailer.getRecipientKeys().add(new Key(keyringDir, entry));
									}
									keyLoaded = true;
								} catch(Exception ex) {
									displayError(ex);
								}
							}
						} else {					//Invalid option specified
							System.out.println("Invalid command.");
						}
					}
				} else if(command == 2) {	//Send an email
					mailer.setMailServer(prompt("Enter mail server"));
					mailer.setFrom(prompt("From"));
					mailer.setSendTo(prompt("To"));
					mailer.setSubject(prompt("Subject"));
					
					//Use these properties for client authentication.
					//mailer.setUser(prompt("User"));
					//mailer.setPassword(prompt("Password"));
					
					mailer.config("ProcessAttachments=true");
					
					System.out.println("Please enter the message. When finished enter \":q\" on a line by itself:");
		            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
		            String temp = "";
		            while (!(temp = bf.readLine()).equalsIgnoreCase(":q")) {
		              temp += "\n";
		              mailer.setMessageText(temp);
		            }
		            
		            mailer.getAttachments().clear();
		            System.out.println("Please enter the full path to attachments that should be sent, one per line." + 
		            					"When finished, enter \":q\" on a line by itself:");
		            while(!(temp = bf.readLine()).equalsIgnoreCase(":q")) {
		            	mailer.getAttachments().add(new FileAttachment(temp));
		            }
		            
		            if(mailer.getKeys().size() > 0 && mailer.getRecipientKeys().size() > 0) {
		            	mailer.signAndEncrypt();
		            } else if(mailer.getKeys().size() > 0) {
		            	mailer.sign();
		            } else if(mailer.getRecipientKeys().size() > 0) {
		            	mailer.encrypt();
		            }
		            
		            mailer.send();
		            System.out.println("Message sent.");
				}
			}			
		} catch(Exception e) {
			displayError(e);
		}
	}

	public static void main(String[] args) {
		new pfilemailer();
	}

}


class ConsoleDemo {
  private static BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

  static String input() {
    try {
      return bf.readLine();
    } catch (IOException ioe) {
      return "";
    }
  }
  static char read() {
    return input().charAt(0);
  }

  static String prompt(String label) {
    return prompt(label, ":");
  }
  static String prompt(String label, String punctuation) {
    System.out.print(label + punctuation + " ");
    return input();
  }

  static String prompt(String label, String punctuation, String defaultVal)
  {
	System.out.print(label + " [" + defaultVal + "] " + punctuation + " ");
	String response = input();
	if(response.equals(""))
		return defaultVal;
	else
		return response;
  }

  static char ask(String label) {
    return ask(label, "?");
  }
  static char ask(String label, String punctuation) {
    return ask(label, punctuation, "(y/n)");
  }
  static char ask(String label, String punctuation, String answers) {
    System.out.print(label + punctuation + " " + answers + " ");
    return Character.toLowerCase(read());
  }

  static void displayError(Exception e) {
    System.out.print("Error");
    if (e instanceof IPWorksOpenPGPException) {
      System.out.print(" (" + ((IPWorksOpenPGPException) e).getCode() + ")");
    }
    System.out.println(": " + e.getMessage());
    e.printStackTrace();
  }
}




