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
import net.arccotangent.pacchat.net.Server;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {
	
	private static final Logger km_log = new Logger("FILESYSTEM/KEYMANAGER");
	private static final String user_home = System.getProperty("user.home");
	private static final String installationPath = user_home + File.separator + ".pacchat";
	private static final File installationFile = new File(installationPath);
	
	private static final File privkeyFile = new File(installationPath + File.separator + "local.priv");
	private static final File pubkeyFile = new File(installationPath + File.separator + "local.pub");
	
	private static void generateNewKeys() {
		km_log.i("Generating new keys.");
		KeyPair keyPair = RSA.generateRSAKeypair(4096);
		km_log.d("4096 bit RSA key generated.");
		
		assert keyPair != null;
		PrivateKey privkey = keyPair.getPrivate();
		PublicKey pubkey = keyPair.getPublic();
		
		saveKeys(privkey, pubkey);
	}
	
	private static void saveKeys(PrivateKey privkey, PublicKey pubkey) {
		km_log.i("Saving keys to disk.");
		
		km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
		km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
		
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubkey.getEncoded());
		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privkey.getEncoded());
		
		try {
			km_log.d(pubkeyFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubkeyFile);
			pubOut.write(Base64.encodeBase64(pubSpec.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key!");
			e.printStackTrace();
		}
		
		try {
			km_log.d(privkeyFile.createNewFile() ? "Creation of private key file successful." : "Creation of private key file failed!");
			
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
			km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
			km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
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
			km_log.d("Key file = " + pubFile.getAbsolutePath());
			
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
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void saveKeyByIP(String ip_address, PublicKey publicKey) {
		km_log.i("Saving public key for " + ip_address);
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKey.getEncoded());
		File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
		km_log.d("Key file = " + pubFile.getAbsolutePath());
		
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
				km_log.i("Installation creation successful. Generating new keys.");
				generateNewKeys();
			} else {
				km_log.e("Installation creation failed! Could not create directory.");
			}
		}
	}
	
	public static PublicKey downloadKeyFromIP(String ip_address) {
		try {
			km_log.i("Downloading key from IP " + ip_address);
			Socket socketGetkey = new Socket();
			socketGetkey.connect(new InetSocketAddress(InetAddress.getByName(ip_address), Server.PORT), 1000);
			BufferedReader inputGetkey = new BufferedReader(new InputStreamReader(socketGetkey.getInputStream()));
			BufferedWriter outputGetkey = new BufferedWriter(new OutputStreamWriter(socketGetkey.getOutputStream()));
			
			outputGetkey.write("301 getkey");
			outputGetkey.newLine();
			outputGetkey.flush();
			
			String pubkeyB64 = inputGetkey.readLine();
			byte[] pubEncoded = Base64.decodeBase64(pubkeyB64);
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			outputGetkey.close();
			inputGetkey.close();
			
			return keyFactory.generatePublic(pubSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			km_log.e("Error saving recipient's key!");
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean checkInstallation() {
		return installationFile.exists();
	}
	
	private static boolean checkLocalKeys() {
		return (privkeyFile.exists() && pubkeyFile.exists());
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void deleteLocalKeysIfExist() {
		if (pubkeyFile.exists())
			pubkeyFile.delete();
		if (privkeyFile.exists())
			privkeyFile.delete();
	}
	
	public static String fingerprint(PublicKey pubkey) {
		byte[] keyBytes = pubkey.getEncoded();
		try {
			MessageDigest hasher = MessageDigest.getInstance("SHA-256");
			hasher.update(keyBytes);
			return Hex.encodeHexString(hasher.digest());
		} catch (NoSuchAlgorithmException e) {
			km_log.e("Error computing key fingerprint!");
			e.printStackTrace();
		}
		return null;
	}
	
}
