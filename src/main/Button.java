package main;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PConstants;

public class Button {

	public String id;
	public float x;
	public float y;
	public float w;
	public float h;
	public Boolean active; // whether the button actually exists
	public Boolean disabled; // whether clicking the button works
	public String text;
	public PApplet p;
	public Color color;
	public int textSize; // 0 = tiny (10px), 1 = small (20px), 2 = medium (30px), 3 = large (40px), 4 = giant (50px)
	
	public Button(String id, PApplet p,float x, float y, float w, float h, Boolean active, Boolean disabled, String text, Color color, int textSize) {
		this.id = id;
		this.p = p;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.active = active;
		this.disabled = disabled;
		this.text = text;
		this.color = color;
		this.textSize = textSize;
	}
	public Boolean click(float mX, float mY) {
		if (!active || disabled) {
			return false;
		}
		if (mX > x && mX < x+w && mY > y && mY < y + h) {
			return true;
		}
		return false;
	}
	public void draw() {
		if (active) {
			p.fill(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			if (disabled) {
				p.fill(color.getRed()/2, color.getGreen()/2, color.getBlue()/2, color.getAlpha());
			}
			p.rect(x,y,w,h);
			p.fill(0);
			p.textSize(textSize*10+10);
			p.textAlign(PConstants.CENTER,PConstants.CENTER);
			p.text(text,x+w/2,y+h/2);
		}
		
	}
}
