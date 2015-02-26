package com.highlanderchef;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Direction implements Serializable, Parcelable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7310913146457605035L;

	String text;

	ArrayList<Bitmap> images;

	public Direction(String text) {
		this.text = text;
	}

	public Direction(String text, Bitmap bmp) {
		this.text = text;
		this.images = new ArrayList<Bitmap>();
		images.add(bmp);
	}

	public Direction(String text, ArrayList<Bitmap> bmps) {
		this.text = text;
		this.images = (ArrayList<Bitmap>)bmps.clone();
	}

	public Direction(Parcel in) {
		this.text = in.readString();
		images = new ArrayList<>();
		Object[] bmps = in.readArray(Bitmap.class.getClassLoader());
		for (int i = 0; i < bmps.length; i++) {
			images.add((Bitmap) bmps[i]);
		}
	}

	public String getDirectionText()
	{
		return this.text;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text);
		Bitmap[] b = new Bitmap[images.size()];
		for (int i = 0; i < images.size(); i++) {
			b[i] = images.get(i);
		}
		dest.writeParcelableArray(b, flags);
	}

	public static final Parcelable.Creator<Direction> CREATOR
	= new Parcelable.Creator<Direction>() {
		@Override
		public Direction createFromParcel(Parcel in) {
			return new Direction(in);
		}

		@Override
		public Direction[] newArray(int size) {
			return new Direction[size];
		}
	};

}
