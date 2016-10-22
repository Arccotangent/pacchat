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

import net.arccotangent.pacchat.crypto.RSA;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {
	
	private static Logger km_log = new Logger("FILESYSTEM/KEYMANAGER");
	private static final String user_home = System.getProperty("user.home");
	private static final String installationPath = user_home + File.separator + ".pacchat";
	private static final File installationFile = new File(installationPath);
	
	private static final File privkeyFile = new File(installationPath + File.separator + "local.priv");
	private static final File pubkeyFile = new File(installationPath + File.separator + "local.pub");
	
	private static void generateNewKeys() {
		km_log.i("Generating new keys.");
		KeyPair keyPair = RSA.generateRSAKeypair(4096);
		
		assert keyPair != null;
		PrivateKey privkey = keyPair.getPrivate();
		PublicKey pubkey = keyPair.getPublic();
		
		saveKeys(privkey, pubkey);
	}
	
	private static void saveKeys(PrivateKey privkey, PublicKey pubkey) {
		km_log.i("Saving keys to disk.");
		
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubkey.getEncoded());
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privkey.getEncoded());
		
		try {
			km_log.i(pubkeyFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubkeyFile);
			pubOut.write(Base64.encodeBase64(pubSpec.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key!");
			e.printStackTrace();
		}
		
		try {
			km_log.i(privkeyFile.createNewFile() ? "Creation of private key file successful." : "Creation of private key file failed!");
			
			FileOutputStream privOut = new FileOutputStream(privkeyFile);
			privOut.write(Base64.encodeBase64(privSpec.getEncoded()));
			privOut.flush();
			privOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving private key!");
			e.printStackTrace();
		}
		
		km_log.i("Finished saving keys to disk. Operation appears successful.");
	}
	
	public static KeyPair loadRSAKeys() {
		try {
			km_log.i("Loading RSA key pair from disk.");
			byte[] privEncoded = Files.readAllBytes(privkeyFile.toPath());
			byte[] pubEncoded = Files.readAllBytes(pubkeyFile.toPath());
			
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decodeBase64(pubEncoded));
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privEncoded));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			PublicKey pubkey = keyFactory.generatePublic(pubSpec);
			PrivateKey privkey = keyFactory.generatePrivate(privSpec);
			
			return new KeyPair(pubkey, privkey);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			km_log.e("Error while loading keypair!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static PublicKey loadKeyByIP(String ip_address) {
		km_log.i("Loading public key for " + ip_address);
		try {
			File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
			
			byte[] pubEncoded = Files.readAllBytes(pubFile.toPath());
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decodeBase64(pubEncoded));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(pubSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			km_log.e("Error while loading public key for " + ip_address + "!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean checkIfIPKeyExists(String ip_address) {
		File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
		return pubFile.exists();
	}
	
	public static PublicKey saveKeyByIP(String ip_address, PublicKey publicKey) {
		km_log.i("Saving public key for " + ip_address);
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKey.getEncoded());
		File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
		
		km_log.i("Deleting old key if it exists.");
		if (pubFile.exists())
			pubFile.delete();
		
		try {
			km_log.i(pubFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubFile);
			pubOut.write(Base64.encodeBase64(pubSpec.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key for " + ip_address + "!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void createInstallationIfNotExist() {
		km_log.i("Checking if installation exists...");
		if (checkInstallation()) {
			km_log.i("Installation exists. Checking for keys...");
			
			if (checkLocalKeys()) {
				km_log.i("Keys exist. No further action required.");
			} else {
				km_log.w("One or both keys do not exist. Generating new keys.");
				deleteLocalKeysIfExist();
				generateNewKeys();
			}
		} else {
			km_log.w("Installation does not exist. Creating new installation now.");
			boolean mkdirSuccessful = installationFile.mkdir();
			if (mkdirSuccessful) {
				km_log.i("Installation creation successful.");
				generateNewKeys();
			} else {
				km_log.e("Installation creation failed! Could not create directory.");
			}
		}
	}
	
	private static boolean checkInstallation() {
		return installationFile.exists();
	}
	
	private static boolean checkLocalKeys() {
		return (privkeyFile.exists() && pubkeyFile.exists());
	}
	
	private static void deleteLocalKeysIfExist() {
		if (pubkeyFile.exists())
			pubkeyFile.delete();
		if (privkeyFile.exists())
			privkeyFile.delete();
	}
	
}
