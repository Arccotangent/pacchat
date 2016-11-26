/*
This file is part of PacChat.

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PacChat is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PacChat.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.arccotangent.pacchat.filesystem;

import net.arccotangent.pacchat.logging.Logger;

import java.io.*;
import java.util.ArrayList;

public class ContactManager {
	
	private static final Logger cm_log = new Logger("FILESYSTEM/CONTACTS");
	private static final String user_home = System.getProperty("user.home");
	private static final File contactsFile = new File(user_home + File.separator + ".pacchat" + File.separator + "contacts.txt");
	
	private static void createContactFileIfNotExist() {
		cm_log.i("Creating new contact file.");
		cm_log.d("Contact file = " + contactsFile.getAbsolutePath());
		try {
			cm_log.d(contactsFile.createNewFile() ? "Creation of contact file successful." : "Creation of contact file failed!");
		} catch (IOException e) {
			cm_log.e("Error creating new contact file!");
			e.printStackTrace();
		}
	}
	
	public static void addContact(String name, String ip) {
		cm_log.i("Attempting to save contact '" + name + "' and IP address " + ip);
		cm_log.d("Contact file = " + contactsFile.getAbsolutePath());
		try {
			FileWriter contactWriter = new FileWriter(contactsFile, true);
			contactWriter.write(name + ":" + ip + "\n");
			contactWriter.flush();
			contactWriter.close();
		} catch (IOException e) {
			cm_log.i("Error while saving contact!");
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getAllContacts() {
		if (!contactsFile.exists())
			createContactFileIfNotExist();
		cm_log.i("Reading all contacts.");
		cm_log.d("Contact file = " + contactsFile.getAbsolutePath());
		ArrayList<String> contacts = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(contactsFile));
			
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				contacts.add(line);
			}
			
			reader.close();
		} catch (IOException e) {
			cm_log.e("Error while reading contact list!");
			e.printStackTrace();
			return null;
		}
		
		return contacts;
	}
	
	public static void deleteByIndex(int index) {
		cm_log.i("Deleting contact at index " + index);
		cm_log.d("Contact file = " + contactsFile.getAbsolutePath());
		ArrayList<String> contacts = getAllContacts();
		
		assert contacts != null;
		
		try {
			contacts.remove(index);
		} catch (IndexOutOfBoundsException e) {
			cm_log.e("Error removing contact! Index " + index + " does not exist!");
		}
		
		writeContactList(contacts);
	}
	
	private static void writeContactList(ArrayList<String> contacts) {
		cm_log.i("Updating contact list.");
		cm_log.d("Contact file = " + contactsFile.getAbsolutePath());
		try {
			FileWriter contactWriter = new FileWriter(contactsFile, false);
			for (String contact : contacts) {
				contactWriter.write(contact + "\n");
			}
			contactWriter.flush();
			contactWriter.close();
		} catch (IOException e) {
			cm_log.i("Error while saving contact!");
			e.printStackTrace();
		}
	}
	
}
