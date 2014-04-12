package org.biu.ufo.model;

public class Feedback {
	String comment = "";
	int starsCount = 0;
	
	public void setComment(CharSequence text) {
		this.comment = text.toString();
	}

	public void setRating(int numStars) {
		this.starsCount = numStars;
	}

}
