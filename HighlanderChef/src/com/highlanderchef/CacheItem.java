package com.highlanderchef;

public class CacheItem {
	long accessTime; //last time we used it
	int numAccess;   //num times used
	byte[] bytes;

	public CacheItem() {
		accessTime = 0;
		numAccess = 0;
		bytes = new byte[0];
	}
}

