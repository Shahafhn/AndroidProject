package com.company.fluffiels.projectgram;

public class UserProfile {
	private String username;
	private int numOfImages;
	
	public UserProfile(String username, int numOfImages) {
		this.username = username;
		this.numOfImages = numOfImages;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getNumOfImages() {
		return numOfImages;
	}
}
