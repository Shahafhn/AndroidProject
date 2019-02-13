package com.company.fluffiels.projectgram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ServerConnection {
	private static String currentLink = "";
	
	public static final int SUCCESS = 200;
	public static final int USER_EXISTS = 401;
	public static final int USER_NOT_FOUND = 404;
	public static final int NO_INTERNET = 500;
	
	public static byte[] getImageBytes(String user, String pass,String profileName,int position){
		URL url;
		HttpURLConnection connection = null;
		String action = "action=getpic";
		String credentials = "&user=" + user + "&pass=" + pass;
		String passParam = "&profile=" + profileName + "&position=" + position;
		InputStream inputStream = null;
		ByteArrayOutputStream arrayOutputStream = null;
		try {
			url = new URL(currentLink + action + credentials + passParam);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "image/jpg");
			connection.connect();
			arrayOutputStream = new ByteArrayOutputStream();
			inputStream = connection.getInputStream();
			byte[] bytes = new byte[2048];
			int actuallyRead;
			while ((actuallyRead = inputStream.read(bytes)) != -1) {
				arrayOutputStream.write(bytes, 0, actuallyRead);
			}
			bytes = arrayOutputStream.toByteArray();
			return bytes;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (arrayOutputStream != null) {
				try {
					arrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static int login(boolean isNewUser,String username,String password){
		URL url;
		HttpURLConnection connection = null;
		currentLink = "http://"+MainActivity.currentIP+":8080/project?";
		String action = "action="+(isNewUser ? "register" : "login");
		String userParam = "&user="+username.toLowerCase();
		String passParam = "&pass="+password.toLowerCase();
		try {
			url = new URL(currentLink+action+userParam+passParam);
			connection = (HttpURLConnection)url.openConnection();
			connection.setUseCaches(false);
			connection.setRequestMethod("GET");
			connection.setDoOutput(false);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.connect();
			return connection.getResponseCode();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return NO_INTERNET;
	}
	
	public static UserProfile[] getUsersStrings(String user, String pass, String search){
		List<UserProfile> list = new ArrayList<>();
		String action = "action=search";
		String auth = "&user=" + user.toLowerCase() + "&pass=" + pass.toLowerCase();
		String look = "&look=" + search.toLowerCase();
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(currentLink + action + auth + look);
			connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(false);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			inputStream = connection.getInputStream();
			byte[] bytes;
			int actuallyRead;
			while((actuallyRead = inputStream.read()) != -1){
				if (actuallyRead == 100)
					break;
				int nameLength = actuallyRead;
				bytes = new byte[nameLength];
				if (inputStream.read(bytes) == nameLength){
					String username = new String(bytes);
					int numOfImages = inputStream.read();
					list.add(new UserProfile(username,numOfImages));
				}
			}
			UserProfile[] userProfiles = list.toArray(new UserProfile[0]);
			return  list.toArray(userProfiles);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connection != null)
				connection.disconnect();
		}
		return new UserProfile[0];
	}
	
	public static String getCurrentLink() {
		return currentLink;
	}
}
