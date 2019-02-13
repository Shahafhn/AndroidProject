package com.company.fluffiels.projectgram;

public class ProfileImages {
	private byte[] bytes;
	
	public ProfileImages(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public ProfileImages(){
		this(null);
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
