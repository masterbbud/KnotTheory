package main;

import java.awt.Color;

public class Segment {

	public float x;
	public float y;
	public Color color = new Color(255,255,255,255);
	public Boolean end = false;
	
	public Segment(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
