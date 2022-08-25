package main;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class Launcher extends PApplet {

	
	//REAL display width = displayWidth - 97
	//REAL display height = displayHeight - 65
	public ArrayList<ArrayList<Segment>> knots = new ArrayList<ArrayList<Segment>>();
	public ArrayList<ArrayList<Integer>> knotData = new ArrayList<ArrayList<Integer>>();
		// 0: direction of the knot (0 = forward, 1 = backward)
	
	private float finishDistance = 25;
	private float highlightDistance = 15;
	private Boolean clearUnderCrossings = true;
	private Boolean alternateWhite = false;
	private float gapDistance = 15; // minimum distance of the gap that undercrossings will be drawn with
	// for some reason if you bring three different strands to the same starting point (inside the gap distance circle only) the whole thing disappears
	//cant have a link bc once you connect the second link the two automatically connect eachother
	// need to make it so equalizing the knots doesn't fill up gaps (going to an end)
	//still have problems sometimes with way too many undercrossings being hidden (especially w multiple knots)
	//e no longer works i guess
	//make e stop condensing at the ends instead
	//tricoloring time!
	//make arrows based on arclength not number of segments
	//setting to only place arrows a bit after and before crossings
	//merging links if you draw from one to another
	//get rid of currentKnot anywhere possible
	//should only be able to autocomplete to the same knot
	//crossings are fucked up during cuts and replacements
	//arrows same color as knots
	//weird problem with evenly spaced arrows, for really low spacings they aren't even on the knot
	//make smart arrows go after the beautify crossings distance
	//cases for vertical/horizontal lines
	//crossing beautification has a lot of crashes
	
	
	//RECENT
	
	//started on beautify crossings, currently busted
	
	//dowker/alternating is now working
		//have to check back from the last crossing to the first crossing
	//still get double crossings problem. see screenshot.
		//i believe the problem is that two segments on one strand both intersect another strand, because they share an endpoint and the endpoint is on the line.
		// solution: if the endpoint is on the line, move it over a tiny bit and then recheck crossings
	//fixed missed crossings (mostly)
	//just got another problem with a random crossing due to very slow drawing, could be fixed by stopping drawing if you're getting too slow?
	//switch over "calculatecrossings" to "refreshcrossings" where possible (keeps parity of crossings when resetting them DONT KNOW IF THIS WORKS? WHEN REVERSING THE STRING)
	//make the crossings that don't change based on their general location (circle)?
	
	//MENU
	// Equal Distance Smoothing
	// Tricolor / Link / Monochrome Coloring Mode
	// None / Constant / Evenly Spaced / Smart / Animated Arrow Settings
	// Change Arrow Spacing
	// Save Knot
	// Paste Knot (from saved)
	// Beautify Crossings
		// make segments at the appropriate points away from a crossing with the set gapDistance
	// Select Knot (or part of knot)
		// Scale Knot (or part)
		// Rotate Knot (or part)
		// Move Knot (or part)
		// Determine Tangle Number / Conway Notation
		// Label Crossings
			// Dowker Notation
		// Flip Arrows (only whole knot)
	// Knot Generation
		// Conway Notation
		// Dowker Notation
		// Then Paste
	
	// Info About the Knot	
			// maybe just have a button to get all this data?
		// Crossing Number
		// Number of Links
		// Linking Number (if applicable)
		// Conway Notation (may have to click to get)
		// Dowker Notation
		// Tricolorability
		// Stick Number???? (this will be extremely difficult)
		//
	
	private Boolean newEnd;
	public ArrayList<ArrayList<Integer>> crossings = new ArrayList<ArrayList<Integer>>(); // 0: knot1 1: segment1 2: knot2 3: segment2 4: (1 if segment2 is over, 2 if segment1 is over) 5: dowker1 6: dowker2 7: parity
	public ArrayList<Integer> highlightedCrossings = new ArrayList<Integer>();
	public int currentKnot = -1;
	public ArrayList<Integer> firstCut; // knot number, segment number
	public ArrayList<Integer> endOfStrand; // knot number, segment number, taking from early (0) or late (1)
	public int arrowSpacing = 1;
	public float arrowLength = 10;
	public int arrowMode = 0;
	public int selectedKnot = -1;
	public Boolean drawing = false;
	private Boolean orientation = false;
	private Boolean showParity = false;
	
	public ArrayList<Button> buttons = new ArrayList<Button>();
	public ArrayList<DisplayField> fields = new ArrayList<DisplayField>();
	
	public static void main(String[] args) {
		System.out.println("-------------\nKnot Creator\n-------------\n");
		PApplet.main("main.Launcher");
	}

	public void settings() {
		size(1440, 800,P2D);
	}

	public void setup() {
		//knots.add(new ArrayList<Segment>());
		ellipseMode(CENTER);
		initButtons();
		
	}
	public void initButtons() {
		buttons.add(new Button("eqs",this,displayWidth-347,150,200,40,true,false,"Equalize Segments",Color.red,1));
		buttons.add(new Button("sarrows",this,displayWidth-347,200,200,40,true,false,"Switch Directionality",Color.red,1));
		buttons.add(new Button("amode",this,displayWidth-347,250,200,40,true,false,"Arrows: None",Color.red,1));
		
		buttons.add(new Button("aleft",this,displayWidth-347,300,40,40,true,false,"<",Color.red,1));
		fields.add(new DisplayField("aspacing",this,displayWidth-297,300,100,40,true,"1",Color.red,1));
		buttons.add(new Button("aright",this,displayWidth-187,300,40,40,true,false,">",Color.red,1));
		
		buttons.add(new Button("beautify",this,displayWidth-347,350,200,40,true,false,"Beautify Crossings",Color.red,1));
		buttons.add(new Button("clearCrossings",this,displayWidth-347,400,200,40,true,false,"Clear Crossings",Color.red,1));
		buttons.add(new Button("altWhite",this,displayWidth-347,450,200,40,true,false,"Alternating Segments",Color.red,1));
		buttons.add(new Button("findDowker",this,displayWidth-347,500,200,40,true,false,"Dowker Labeling",Color.red,1)); //reversible?
		buttons.add(new Button("knotData",this,displayWidth-347,550,200,40,true,false,"Knot Data",Color.red,1));
		buttons.add(new Button("changeStart",this,displayWidth-347,600,200,40,true,false,"Set Knot Start",Color.red,1));
		buttons.add(new Button("orientation",this,displayWidth-347,650,200,40,true,false,"Show Orientation",Color.red,1));
		buttons.add(new Button("delete",this,displayWidth-347,700,200,40,true,false,"Delete Knots",Color.red,1));
		buttons.add(new Button("getParity",this,displayWidth-347,750,95,40,true,false,"Get Parity",Color.red,1));
		buttons.add(new Button("showParity",this,displayWidth-242,750,95,40,true,false,"Show Parity",Color.red,1));
	}
	public void draw() {
		background(15,15,100);
		
		strokeWeight(2);
		
		ArrayList<ArrayList<Integer>> crossingData = getCrossingData();
		ArrayList<Integer> underCrossings = crossingData.get(0); // fix for multiple knots
		ArrayList<Integer> underCrossingKnots = crossingData.get(1);
		
		drawSegments(underCrossings, underCrossingKnots);
		
		drawMenu();
		
		if (orientation) {
			for (ArrayList<Segment> s : knots) {
				fill(255);
				ellipse(s.get(0).x, s.get(0).y, 5, 5);
			}
		}
		
	}
	public void drawSegments(ArrayList<Integer> underCrossings, ArrayList<Integer> underCrossingKnots) {
		Boolean drawWhite = false;
		for (int p = 0; p < knots.size(); p++) {
			ArrayList<Segment> segments = knots.get(p);
			if (segments.size() > 0) {
				float prevX = segments.get(0).x;
				float prevY = segments.get(0).y;
				
				
				for (int i = 1; i < segments.size(); i++) {
					Segment s = segments.get(i);
					
					if (!s.end) {
						Boolean found = false;
						for (int o = 0; o < underCrossings.size(); o++) {
							if (underCrossings.get(o) == i && underCrossingKnots.get(o) == p) {
								found = true;
								ArrayList<Integer> cr = crossingWithSegment(p,i);
								PVector v1 = new PVector(knots.get(cr.get(0)).get(cr.get(1)).x-knots.get(cr.get(0)).get(cr.get(1)-1).x,knots.get(cr.get(0)).get(cr.get(1)).y-knots.get(cr.get(0)).get(cr.get(1)-1).y);
								PVector v2 = new PVector(knots.get(cr.get(2)).get(cr.get(3)).x-knots.get(cr.get(2)).get(cr.get(3)-1).x,knots.get(cr.get(2)).get(cr.get(3)).y-knots.get(cr.get(2)).get(cr.get(3)-1).y);
								v1 = v1.normalize();
								v2 = v2.normalize();
								PVector v3 = v1.add(v2);
								v3 = v3.normalize();
								
								float textDistance = highlightDistance;
								fill(255);
								PVector cross = crossingPoint(crossingWithSegment(p,i));
								if (cr.get(5) != -1) {
									text(cr.get(5),cross.x + textDistance*v3.x, cross.y + textDistance*v3.y);
									text(cr.get(6),cross.x - textDistance*v3.x, cross.y - textDistance*v3.y);
								}
								v3 = v3.rotate((float) (Math.PI/2));
								if (cr.get(7) != 0 && showParity) {
									if (cr.get(7) == -1) {
										text("-",cross.x + textDistance*v3.x, cross.y + textDistance*v3.y);
									}
									else {
										text("+",cross.x + textDistance*v3.x, cross.y + textDistance*v3.y);
									}
								}
								
							}
						}
						
						if (!found || !clearUnderCrossings) {
							
							stroke(s.color.getRed(), s.color.getGreen(), s.color.getBlue(),s.color.getAlpha());
							if (drawWhite) {
								stroke(255);
							}
							if (alternateWhite) {
								drawWhite = ! drawWhite;
							}
							line(prevX,prevY,s.x,s.y);
							
						}
						else {
							//stroke(255*i/segments.size(), s.color.getGreen(), s.color.getBlue(),s.color.getAlpha());
							stroke(0,0);
							line(prevX,prevY,s.x,s.y);
							
						}
					}
					prevX = s.x;
					prevY = s.y;
					
				}
				drawAutoCompleteCircles(segments);
				drawCrossingCircles(segments);
				drawStartCircles(segments);
				if (drawing == false || p != knots.size()-1) {
					if (knotData.get(p).get(0) == 0) {
						drawArrows(segments, p, true);
					}
					else {
						drawArrows(segments, p, false);
					}
				}
				
				
			}
		}
	}
	public ArrayList<ArrayList<Integer>> getCrossingData() {
		ArrayList<Integer> underCrossings = new ArrayList<Integer>(); // fix for multiple knots
		ArrayList<Integer> underCrossingKnots = new ArrayList<Integer>();
		for (ArrayList<Integer> l : crossings) {
			//PVector cross = crossingPoint(l);
			
			
			underCrossings.add(l.get(l.get(4)*2-1));
			underCrossingKnots.add(l.get(l.get(4)*2-2));
			
			
			
			
			//int newi = l.get(l.get(4)*2-1)+1;
			//ArrayList<Segment> segments1 = knots.get(l.get(l.get(4)*2-2));
			//FUCK MY LFE we ifx this later
			/*while (newi < segments1.size()-1) { //ykw im not fixing this right now but we do it later
				//while (newi < segments.size()) {
				underCrossings.add(newi);
				underCrossingKnots.add(l.get(l.get(4)*2-2)); //this is wrong.
				if (Math.sqrt(Math.pow(segments1.get(newi).x-cross.x,2)+Math.pow(segments1.get(newi).y-cross.y,2)) > gapDistance) {
					break;
				}
				
				newi++;
			}
			newi = l.get(l.get(4)*2-1)-1;
			while (newi > 0) {
				underCrossings.add(newi);
				underCrossingKnots.add(l.get(l.get(4)*2-2));
				if (Math.sqrt(Math.pow(segments1.get(newi).x-cross.x,2)+Math.pow(segments1.get(newi).y-cross.y,2)) > gapDistance) {
					break;
				}
				newi--;
			}
			*/
			
		}
		ArrayList<ArrayList<Integer>> crossingData = new ArrayList<ArrayList<Integer>>();
		crossingData.add(underCrossings);
		crossingData.add(underCrossingKnots);
		return crossingData;
	}
	public void drawMenu() {
		fill(200);
		noStroke();
		rect(displayWidth-397,0, 400, displayHeight-65);
		fill(0);
		textAlign(CENTER, BASELINE);
		textSize(40);
		text("MENU", displayWidth-247,50);
		
		
		for (Button b : buttons) {
			b.draw();
		}
		for (DisplayField d : fields) {
			d.draw();
		}
		
	}
	public void drawStartCircles(ArrayList<Segment> segments) {
		if (endOfStrand != null) {
			return;
		}
		for (int i = 1; i < segments.size()-1; i++) {
			Segment s = segments.get(i);
			if (s.end || segments.get(i+1).end) {
				if (Math.sqrt(Math.pow((mouseX-s.x),2)+Math.pow((mouseY-s.y),2)) < finishDistance) {
					fill(200,100);
					stroke(0);
					ellipse(s.x, s.y, finishDistance*2, finishDistance*2);
					return;
				}
			}
		}
	}
	public void drawAutoCompleteCircles(ArrayList<Segment> segments) {
		if (!mousePressed) {
			return;
		}
		for (int a = 0; a < segments.size()-1; a++) {
			Segment i = segments.get(a);
			if (endOfStrand != null) {
				if (endOfStrand.get(2) == 0) {
					if ((a == endOfStrand.get(1)) && Math.sqrt(Math.pow((mouseX-i.x),2)+Math.pow((mouseY-i.y),2)) < finishDistance) {
						fill(200,100);
						stroke(0);
						ellipse(i.x, i.y, finishDistance*2, finishDistance*2);
						return;
					}
				}
				else {
					if (a == endOfStrand.get(1)-1 && Math.sqrt(Math.pow((mouseX-i.x),2)+Math.pow((mouseY-i.y),2)) < finishDistance) {
						fill(200,100);
						stroke(0);
						ellipse(i.x, i.y, finishDistance*2, finishDistance*2);
						return;
					}
				}
			}
			else if ((i.end || segments.get(a+1).end) && Math.sqrt(Math.pow((mouseX-i.x),2)+Math.pow((mouseY-i.y),2)) < finishDistance) {
				for (Segment s : segments) {
					if (Math.sqrt(Math.pow((s.x-i.x),2)+Math.pow((s.y-i.y),2)) > finishDistance) {
						fill(200,100);
						stroke(0);
						ellipse(i.x, i.y, finishDistance*2, finishDistance*2);
						return;
					}
				}
				
			}
		}
		
	}
	public void drawCrossingCircles(ArrayList<Segment> segments) {
		int z = 0;
		for (ArrayList<Integer> i : crossings) {
			// y = mx -mx1 +y1
			
			//System.out.println(m1 +" "+m2+" "+b1+" "+b2+" "+crossx+" "+crossy);
			PVector cross = crossingPoint(i);
			float crossx = cross.x;
			float crossy = cross.y;
			if (Math.sqrt(Math.pow((mouseX-crossx),2)+Math.pow((mouseY-crossy),2)) < highlightDistance) {
				fill(200,100);
				stroke(0);
				ellipse(crossx, crossy, highlightDistance*2, highlightDistance*2);
				if (!highlightedCrossings.contains(z)) {
					highlightedCrossings.add(z);
				}
			}
			else {
				highlightedCrossings.remove((Integer)z);
			}
			z++;
		}
	}
	public void drawArrows(ArrayList<Segment> segments, int knotNum, Boolean forward) {
		if (arrowMode == 0) {
			return;
		}
		if (arrowMode == 1) {
			for (int i = 1; i < segments.size(); i += arrowSpacing) {
				Segment a = segments.get(i);
				Segment b = segments.get(i-1);
				float angle = (float)Math.atan((a.y-b.y)/(a.x-b.x));
				
				if (a.x < b.x) {
					angle += Math.PI;
				}
				if (!forward) {
					angle += Math.PI;
				}
				float xmid = a.x + (b.x-a.x)/2;
				float ymid = a.y + (b.y-a.y)/2;
				stroke(255);
				//arrowLength = 30*i/segments.size();
				line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle+Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle+Math.PI*3/4)));
				line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle-Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle-Math.PI*3/4)));
			}
		}
		else if (arrowMode == 2) {
			ArrayList<Float> lengthList = new ArrayList<Float>();
			for (int i = 1; i < segments.size(); i++) {
				lengthList.add(segmentLength(segments,i));
			}
			float atLeast = 0;
			for (int i = 1; i < segments.size(); i += 1) {
				if (sumTo(lengthList,i) > atLeast) {
					Segment a = segments.get(i);
					Segment b = segments.get(i-1);
					float angle = (float)Math.atan((a.y-b.y)/(a.x-b.x));
					
					if (a.x < b.x) {
						angle += Math.PI;
					}
					if (!forward) {
						angle += Math.PI;
					}
					float xmid = a.x + (b.x-a.x)*(sumTo(lengthList,i)-atLeast)/segmentLength(segments,i);
					float ymid = a.y + (b.y-a.y)*(sumTo(lengthList,i)-atLeast)/segmentLength(segments,i);
					stroke(255);
					//arrowLength = 30*i/segments.size();
					line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle+Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle+Math.PI*3/4)));
					line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle-Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle-Math.PI*3/4)));
					atLeast += arrowSpacing*30;
				}
				
			}
		}
		else if (arrowMode == 3) {
			ArrayList<Integer> drawOn = new ArrayList<Integer>();
			for (ArrayList<Integer> c : crossings) {
				if (c.get(0) == knotNum) {
					drawOn.add(c.get(1) + 1);
					drawOn.add(c.get(1) - 1);
				}
				if (c.get(2) == knotNum) {
					drawOn.add(c.get(3) + 1);
					drawOn.add(c.get(3) - 1);
				}
			}
			
			//float atLeast = 0;
			for (int i = 1; i < segments.size(); i += 1) {
				if (drawOn.contains(i)) {
					Segment a = segments.get(i);
					Segment b = segments.get(i-1);
					float angle = (float)Math.atan((a.y-b.y)/(a.x-b.x));
					
					if (a.x < b.x) {
						angle += Math.PI;
					}
					if (!forward) {
						angle += Math.PI;
					}
					float xmid = a.x + (b.x-a.x)/2;
					float ymid = a.y + (b.y-a.y)/2;
					stroke(255);
					//arrowLength = 30*i/segments.size();
					line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle+Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle+Math.PI*3/4)));
					line(xmid,ymid,(float)(xmid+arrowLength*Math.cos(angle-Math.PI*3/4)),(float)(ymid+arrowLength*Math.sin(angle-Math.PI*3/4)));
					
				}
				
			}
		}
	}
	public PVector crossingPoint(ArrayList<Integer> i) {
//		float m1 = (segments.get(i.get(0)).y-segments.get(i.get(0)-1).y)/((segments.get(i.get(0)).x-segments.get(i.get(0)-1).x));
//		
//		float m2 = (segments.get(i.get(1)).y-segments.get(i.get(1)-1).y)/((segments.get(i.get(1)).x-segments.get(i.get(1)-1).x));
//		float b1 = -1*m1 * segments.get(i.get(0)).x + segments.get(i.get(0)).y;
//		float b2 = -1*m2 * segments.get(i.get(1)).x + segments.get(i.get(1)).y;
//		float crossx = (b2 - b1) / (m1 - m2);
//		float crossy = m1 * ((b2 - b1) / (m1 - m2)) + b1;
//		return new PVector(crossx,crossy);
		
		float m1 = (knots.get(i.get(0)).get(i.get(1)).y-knots.get(i.get(0)).get(i.get(1)-1).y)/((knots.get(i.get(0)).get(i.get(1)).x-knots.get(i.get(0)).get(i.get(1)-1).x));
		
		float m2 = (knots.get(i.get(2)).get(i.get(3)).y-knots.get(i.get(2)).get(i.get(3)-1).y)/((knots.get(i.get(2)).get(i.get(3))).x-knots.get(i.get(2)).get(i.get(3)-1).x);
		
		
		float b1 = -1*m1 * knots.get(i.get(0)).get(i.get(1)).x + knots.get(i.get(0)).get(i.get(1)).y;
		float b2 = -1*m2 * knots.get(i.get(2)).get(i.get(3)).x + knots.get(i.get(2)).get(i.get(3)).y;
		float crossx = (b2 - b1) / (m1 - m2);
		float crossy = m1 * ((b2 - b1) / (m1 - m2)) + b1;
		if (m1 == Float.POSITIVE_INFINITY || m1 == Float.NEGATIVE_INFINITY) {
			crossx = knots.get(i.get(0)).get(i.get(1)).x;
			crossy = m2 * crossx + b2;
			//System.out.println(new PVector(crossx,crossy)+"AA"+b1+" "+b2);
		}
		if (m2 == Float.POSITIVE_INFINITY || m2 == Float.NEGATIVE_INFINITY) {
			crossx = knots.get(i.get(2)).get(i.get(3)).x;
			crossy = m1 * crossx + b1;
			//System.out.println(new PVector(crossx,crossy)+"BB"+b1+" "+b2);
		}
		//System.out.println(crossings.size());
		return new PVector(crossx,crossy);
	}
	public void equalizeSegments(int number) {
		
		//get full knot "length" (sum of all segments)
		//divide by number of segments
		//for each segment, get its distance from the origin
		//place it on either the line to the last one or to the next one (could be multiple next) at that distance
		int totalSegments = countSegments();
		int prevNumber = number;
		for (ArrayList<Segment> segments : knots) {
			number = prevNumber / totalSegments * segments.size();
			float fullLength = 0;
			ArrayList<Float> lengthList = new ArrayList<Float>();
			for (int i = 1; i < segments.size(); i++) {
				fullLength += segmentLength(segments,i);
				lengthList.add(segmentLength(segments,i));
			}
			//System.out.println(lengthList);
			float per = fullLength / number;
			ArrayList<Segment> newSegments = new ArrayList<Segment>();
			newSegments.add(new Segment(segments.get(0).x, segments.get(0).y));
			for (int i = 1; i < number-1; i++) {
				float shouldBe = i*per;
				int upper = cumulativeAtLeast(lengthList, shouldBe)+1;
				float upperDistance = sumTo(lengthList, upper);
				float lowerDistance = sumTo(lengthList, upper - 1); // could be optimized
				
				float difference = upperDistance - lowerDistance;
				// go to upper-1 position + (upperVector - lowerVector) * (shouldBe-lowerDistance) / difference
				float x = segments.get(upper-1).x + (segments.get(upper).x-segments.get(upper-1).x) * (shouldBe - lowerDistance) / difference;
				float y = segments.get(upper-1).y + (segments.get(upper).y-segments.get(upper-1).y) * (shouldBe - lowerDistance) / difference;
				newSegments.add(new Segment(x, y));
				//System.out.println(i);
				//float shouldBe = sumTo(lengthList, i);
				
			}
			newSegments.add(segments.get(segments.size()-1));
			segments = newSegments;
			
		}
		calculateCrossings();
	}
	public void beautifyCrossings(int s, float distance) {
		//may have to cover edge cases if the single segment for the crossing is more than distance away
		// will probably cause problems if the segment intersects the circle twice
		for (int u = 0; u < 2; u++) {
			ArrayList<Segment> segments = knots.get(s);
			int l = crossings.size();
			for (int p = 0; p < l; p++) {
				ArrayList<Integer> c = crossings.get(p);
				if (c.get(0) == s) {
					PVector point = crossingPoint(c);
					int i = c.get(1);
					
					ArrayList<Integer> where1 = new ArrayList<Integer>();
					PVector intersect1;
					while (true) {
						intersect1 = segmentIntersectsCircle(segments.get(i).x,segments.get(i).y,segments.get(i-1).x,segments.get(i-1).y,point.x,point.y,distance);
						if (intersect1 != null || Math.abs(Math.sqrt(Math.pow(segments.get(i).x-point.x, 2)+Math.pow(segments.get(i).y-point.y, 2))-distance)<0.01) {
							
							where1.add(s);
							where1.add(i);
							where1.add(0);
							break;
							//that's the last segment we want
						}
						i--;
					}
					i = c.get(1);
					ArrayList<Integer> where2 = new ArrayList<Integer>();
					PVector intersect2;
					while (true) {
						intersect2 = segmentIntersectsCircle(segments.get(i-1).x,segments.get(i-1).y,segments.get(i).x,segments.get(i).y,point.x,point.y,distance);
						if (intersect2 != null || Math.abs(Math.sqrt(Math.pow(segments.get(i).x-point.x, 2)+Math.pow(segments.get(i).y-point.y, 2))-distance)<0.01 && intersect2 != intersect1) {
							
							where2.add(s);
							where2.add(i+1);
							where2.add(0);
							break;
							//that's the last segment we want
						}
						i++;
					}
					insertSegment(intersect1.x, intersect1.y, where1);
					insertSegment(intersect2.x, intersect2.y, where2);
					for (int z = 0; z < where2.get(1)-where1.get(1)-1; z++) {
						segments.remove(where1.get(1)+1);
					}
					calculateCrossings();
				}
				c = crossings.get(p);
				if (c.get(2) == s) {
					PVector point = crossingPoint(c);
					int i = c.get(3);
					ArrayList<Integer> where1 = new ArrayList<Integer>();
					PVector intersect1;
					while (true) {
						intersect1 = segmentIntersectsCircle(segments.get(i).x,segments.get(i).y,segments.get(i-1).x,segments.get(i-1).y,point.x,point.y,distance);
						if (intersect1 != null || Math.abs(Math.sqrt(Math.pow(segments.get(i).x-point.x, 2)+Math.pow(segments.get(i).y-point.y, 2))-distance)<0.01) {
							
							where1.add(s);
							where1.add(i);
							where1.add(0);
							break;
							//that's the last segment we want
						}
						i--;
					}
					i = c.get(3);
					ArrayList<Integer> where2 = new ArrayList<Integer>();
					PVector intersect2;
					while (true) {
						intersect2 = segmentIntersectsCircle(segments.get(i-1).x,segments.get(i-1).y,segments.get(i).x,segments.get(i).y,point.x,point.y,distance);
						if (intersect2 != null || Math.abs(Math.sqrt(Math.pow(segments.get(i).x-point.x, 2)+Math.pow(segments.get(i).y-point.y, 2))-distance)<0.01 && intersect2 != intersect1) {
							
							where2.add(s);
							where2.add(i+1);
							where2.add(0);
							break;
							//that's the last segment we want
						}
						i++;
					}
					insertSegment(intersect1.x, intersect1.y, where1);
					insertSegment(intersect2.x, intersect2.y, where2);
					for (int z = 0; z < where2.get(1)-where1.get(1)-1; z++) {
						segments.remove(where1.get(1)+1);
					}
					calculateCrossings();
				}
				
			}
			
		}
		
		colorKnots();
	}
	public PVector segmentIntersectsCircle(float x1, float y1, float x2, float y2, float x3, float y3, float r) {
		// y = ((y2-y1)/(x2-x1))*x + y1 - ((y2-y1)/(x2-x1))*x1
		// Math.pow((x-x3),2)+Math.pow((y-y3),2)=Math.pow(r,2)
		// Math.pow((x-x3),2)+Math.pow((((y2-y1)/(x2-x1))*x + y1 - ((y2-y1)/(x2-x1))*x1-y3),2)=Math.pow(r,2)
		// x^2-2xx3+x3^2 + ((y2-y1)/(x2-x1))^2*x^2 - 2*((y2-y1)/(x2-x1))*(y1 - ((y2-y1)/(x2-x1))*x1-y3) + y1 - ((y2-y1)/(x2-x1))*x1-y3)^2=r^2
		
		Boolean right = false;
		if (x2 > x1) {
			right = true;
		}
		double b = -2*x3
				+2*(y1 - y3)*(y2-y1)/(x2-x1)
				+(x1) *(-2)* Math.pow((y2-y1)/(x2-x1),2);
		double a = 1 + Math.pow(((y2-y1)/(x2-x1)), 2);
		double c = x3*x3-r*r
				+Math.pow(
						(-x1*(y2-y1)/(x2-x1) + y1 - y3)
						, 2);
		//System.out.println(a+" "+b+" "+c);
		if (b*b-4*a*c >= 0) {
			double px1 = (-1*b+Math.sqrt(Math.pow(b, 2)-4*a*c))/(2*a);
			double py1 = ((y2-y1)/(x2-x1))*px1 + y1 - ((y2-y1)/(x2-x1))*x1;
			double px2 = (-1*b-Math.sqrt(Math.pow(b, 2)-4*a*c))/(2*a);
			double py2 = ((y2-y1)/(x2-x1))*px2 + y1 - ((y2-y1)/(x2-x1))*x1;
			System.out.println("technically");
			
			if (right) {
				if (((x1 <= px1 && px1 <= x2) || (x1 >= px1 && px1 >= x2)) && ((y1 <= py1 && py1 <= y2) || (y1 >= py1 && py1 >= y2))) {
					System.out.println(new PVector((float)px1,(float)py1));
					return new PVector((float)px1,(float)py1);
				}
			}
			else {
				if (((x1 <= px2 && px2 <= x2) || (x1 >= px2 && px2 >= x2)) && ((y1 <= py2 && py2 <= y2) || (y1 >= py2 && py2 >= y2))) {
					System.out.println(new PVector((float)px2,(float)py2));
					return new PVector((float)px2,(float)py2);
				}
			}
			
			return null;
		}
		else {
			//System.out.println("discerror");
			return null;
		}
		
		
		
		
		
	}
	public float sumTo(ArrayList<Float> list, int i) {
		float total = 0;
		for (int z = 0; z < i; z++) {
			total += list.get(z);
		}
		return total;
	}
	public int whereIsAtLeast(ArrayList<Float> list, float a) {
		//LIST MUST BE ORDERED SMALL -> LARGE
		for (int z = 0; z < list.size(); z++) {
			if (list.get(z) > a) {
				return z;
			}
		}
		return -1;
	}
	public int cumulativeAtLeast(ArrayList<Float> list, float a) {
		//LIST MUST BE ORDERED SMALL -> LARGE
		float total = 0;
		for (int z = 0; z < list.size(); z++) {
			total += list.get(z);
			if (total > a) {
				return z;
			}
		}
		return -1;
	}
	public float segmentLength(ArrayList<Segment> segments, int index) {
		return (float)Math.sqrt(Math.pow((segments.get(index).x-segments.get(index-1).x),2)+Math.pow((segments.get(index).y-segments.get(index-1).y),2));
	}
	public void keyPressed() {
		if (key == 'a') { // calculate crossings manually
			calculateCrossings();
		}
		if (keyCode == SHIFT) { // switch the crossing type at mousepos
			for (Integer i : highlightedCrossings) {
				if (crossings.get(i).get(4) == 1) {
					crossings.get(i).set(4, 2);
				}
				else {
					crossings.get(i).set(4, 1);
				}
			}
		}
		if (keyCode == BACKSPACE) { // unselect
			firstCut = null;
			colorKnots();
		}
		if (key == 'e') { // equalize segments for all knots
			equalizeSegments(countSegments()*2);
		}
		if (key == ' ') { // start or end a cut
			cut(mouseX, mouseY);
		}
		if (key == 'd') { // data
			System.out.println(knots.get(currentKnot).get(knots.get(currentKnot).size()-1).x);
		}
		if (key == 's') { // select a knot
			selectKnot(mouseX, mouseY);
		}
	}
	public void deleteData() {
		knots = new ArrayList<ArrayList<Segment>>();
		crossings = new ArrayList<ArrayList<Integer>>();
		highlightedCrossings = new ArrayList<Integer>();
		currentKnot = -1;
		firstCut = null;
		endOfStrand = null;
		knotData = new ArrayList<ArrayList<Integer>>();
	}
	public void selectKnot(float x, float y) {
		ArrayList<Integer> n = nearestSegment(x,y);
		selectedKnot = n.get(0);
	}
	public void cut(float x, float y) {
		if (firstCut == null) {
			firstCut = nearestSegment(x,y);
			knots.get(firstCut.get(0)).get(firstCut.get(1)).color = Color.blue;
		}
		else {
			ArrayList<Integer> n = nearestSegment(x,y);
			if (firstCut.get(0) == n.get(0)) {
				if (firstCut.get(1) > n.get(1)) {
					for (int i = 0; i < firstCut.get(1)-n.get(1); i++) {
						knots.get(n.get(0)).remove((int)n.get(1));
						System.out.println("done");
					}
					knots.get(n.get(0)).get(n.get(1)).end = true;
				}
				else {
					for (int i = 0; i < n.get(1)-firstCut.get(1); i++) {
						knots.get(n.get(0)).remove((int)firstCut.get(1));
						System.out.println("done");
					}
					knots.get(n.get(0)).get(firstCut.get(1)).end = true;
				}
			}
			calculateCrossings();
			firstCut = null;
		}
		//find the nearest segment by iteration and make the cut there
		// if there's already a cut instead cut from a to b (or b to a)
	}
	public ArrayList<Integer> nearestSegment(float x, float y){
		ArrayList<Integer> current = new ArrayList<Integer>();
		current.add(0);
		current.add(0);
		double closest = Math.sqrt(Math.pow(x-knots.get(0).get(0).x, 2)+Math.pow(y-knots.get(0).get(0).y, 2));
		int a = 0;
		int b = 0;
		for (ArrayList<Segment> s : knots) {
			for (Segment i : s) {
				if (Math.sqrt(Math.pow(x-i.x, 2)+Math.pow(y-i.y, 2)) < closest) {
					current.set(0, a);
					current.set(1, b);
					closest = Math.sqrt(Math.pow(x-i.x, 2)+Math.pow(y-i.y, 2));
				}
				b++;
			}
			a++;
			b = 0;
		}
		System.out.println(current);
		return current;
	}
	public int countSegments() {
		int i = 0;
		for (ArrayList<Segment> s : knots) {
			i += s.size();
		}
		return i;
	}
	public void mousePressed() {
		if (mouseX < displayWidth - 397 && mouseX > 0 && mouseY > 0 && mouseY < displayHeight-65) {
			ArrayList<Integer> found = new ArrayList<Integer>();
			for (int a = 0; a < knots.size(); a++) {
				for (int b = 1; b < knots.get(a).size()-1; b++) {
					Segment s = knots.get(a).get(b);
					if (Math.sqrt(Math.pow((mouseX-s.x),2)+Math.pow((mouseY-s.y),2)) < finishDistance) {
						if (s.end) {
							found.add(a);
							found.add(b);
							found.add(1);
							break;
						}
						if (knots.get(a).get(b+1).end) {
							found.add(a);
							found.add(b);
							found.add(0);
							break;
						}
					}
				}
				if (found.size() > 0) {
					break;
				}
				
			}
			if (found.size() > 0) {
				endOfStrand = found;
				
				insertSegment(mouseX,mouseY,endOfStrand);
				
				//knots.get(found.get(0)).get(found.get(1)-1).end = false;
				if (found.get(2) == 0) {
					endOfStrand.set(1, endOfStrand.get(1)+1);
					knots.get(found.get(0)).remove(found.get(1)-1);
				}
				else {
					//knots.get(found.get(0)).remove(found.get(1)+1);

				}
				drawing = true;
			}
			else {
				currentKnot++;
				knots.add(new ArrayList<Segment>());
				knotData.add(new ArrayList<Integer>());
				knotData.get(currentKnot).add(0);
				drawing = true;
			}
		}
		else {
			checkButtonClicks();
		}
		
	}
	public void mouseDragged() {
		if (mouseX < displayWidth - 397 && mouseX > 0 && mouseY > 0 && mouseY < displayHeight-65) {
			if (endOfStrand != null) {
				insertSegment(mouseX,mouseY,endOfStrand);
				
				if (endOfStrand.get(2) == 0) {
					endOfStrand.set(1, endOfStrand.get(1)+1);
					
				}
				
			}
			else {
				addSegment(mouseX,mouseY);
				if (knots.get(currentKnot).size() == 1 || newEnd) {
					knots.get(currentKnot).get(knots.get(currentKnot).size()-1).end = true;
					newEnd = false;
				}
			}
			
		}
		
		
	}
	public void insertSegment(float x, float y, ArrayList<Integer> fC) {
		knots.get(fC.get(0)).add(fC.get(1),new Segment(x,y));
		if (fC.get(2) == 1) {
			knots.get(fC.get(0)).get(fC.get(1)).end = true;
			knots.get(fC.get(0)).get(fC.get(1)+1).end = false;
		}
		for (ArrayList<Integer> c : crossings) {
			if (c.get(0) == fC.get(0) && c.get(1) > fC.get(1)) {
				c.set(1,c.get(1)+1);
			}
			if (c.get(2) == fC.get(0) && c.get(3) > fC.get(1)) {
				c.set(3,c.get(3)+1);
			}
		}
	}
	public void addSegment(float x, float y) {
		knots.get(currentKnot).add(new Segment(x, y));
		
	}
	public void calculateCrossings() {
		crossings = new ArrayList<ArrayList<Integer>>();
		if (knots.size() == 0 || knots.get(0).size() <= 1) {
			return;
		}
		//System.out.println(knots.size());
		ArrayList<ArrayList<Integer>> avoidList = new ArrayList<ArrayList<Integer>>();
		float distanceTraveled = 0;
		float closenessDistance = gapDistance*2;
		for (int i = 0; i < knots.size(); i++) {
			ArrayList<Segment> segments = knots.get(i);
			for (int a = 1; a < segments.size()-1; a++) {
				distanceTraveled += segmentLength(segments,a);
				for (int z = 0; z < knots.size(); z++) {
					ArrayList<Segment> segments2 = knots.get(z);
					for (int b = 1; b < segments2.size()-1; b++) {
						if ((i==z && b!= a && b!= a+1 && b != a-1) || i!= z) {
							
							Boolean found = false;
							for (ArrayList<Integer> v : avoidList) {
								if (v.get(0) == z && v.get(1) == b && v.get(2) == i && v.get(3) == a) {
									found = true;
								}
							}
							if (!found) {
								
								Line2D line1 = new Line2D.Float(segments.get(a).x, segments.get(a).y, segments.get(a-1).x, segments.get(a-1).y);
								
								Line2D line2 = new Line2D.Float(segments2.get(b).x, segments2.get(b).y, segments2.get(b-1).x, segments2.get(b-1).y);
								if (line2.intersectsLine(line1)) {
									if (distanceTraveled < closenessDistance) {
										continue;
									}
									else {
										distanceTraveled = 0;
									}
									ArrayList<Integer> newCrossing = new ArrayList<Integer>();
									newCrossing.add(i);
									newCrossing.add(a);
									newCrossing.add(z);
									newCrossing.add(b);
									newCrossing.add(1);
									newCrossing.add(-1);
									newCrossing.add(-1);
									newCrossing.add(0);
									avoidList.add(newCrossing);
									//segments.get(a).color = new Color(segments.get(a).color.getRed(),segments.get(a).color.getGreen(),segments.get(a).color.getBlue(),0);
									crossings.add(newCrossing);
									break;
								}
							}
							
						}
					}
				}
				
			}
		}
		
	}
	public void refreshCrossings() {
		//requires # of crossings to be the same
		ArrayList<PVector> save = new ArrayList<PVector>();
		ArrayList<Integer> directions = new ArrayList<Integer>();
		for (int c = 0; c < crossings.size(); c++) {
			save.add(crossingPoint(crossings.get(c)));
			directions.add(getParity(crossings.get(c)));
		}
		calculateCrossings();
		
		for (int c = 0; c < crossings.size(); c++) {
			for (int p = 0; p < save.size(); p++) {
				if (PVector.dist(crossingPoint(crossings.get(c)),save.get(p)) < highlightDistance) {
					if (getParity(crossings.get(c)) != directions.get(p)) {
						crossings.get(c).set(4, crossings.get(c).get(4)%2 + 1);
					}
				}
			}
		}
	}
	public Integer getParity(ArrayList<Integer> c) {
		PVector overFrom = new PVector(knots.get(c.get(-2*(c.get(4)-2))).get(c.get(-2*(c.get(4)-2)+1)-1).x,knots.get(c.get(-2*(c.get(4)-2))).get(c.get(-2*(c.get(4)-2)+1)-1).y);
		PVector overTo = new PVector(knots.get(c.get(-2*(c.get(4)-2))).get(c.get(-2*(c.get(4)-2)+1)).x,knots.get(c.get(-2*(c.get(4)-2))).get(c.get(-2*(c.get(4)-2)+1)).y);
		PVector underTo = new PVector(knots.get(c.get(2*(c.get(4)-1))).get(c.get(2*(c.get(4)-1)+1)).x,knots.get(c.get(2*(c.get(4)-1))).get(c.get(2*(c.get(4)-1)+1)).y);
		PVector v1 = new PVector(overTo.x-overFrom.x,overTo.y-overFrom.y);
		PVector v2 = new PVector(underTo.x-overFrom.x, underTo.y-overFrom.y);
		if (v1.cross(v2).z < 0) {
			return 1;
		}
		return -1;
		
	}
	public void mouseReleased() {
		//have to handle what happens if you release on the menu
		if (mouseX < displayWidth - 397 && mouseX > 0 && mouseY > 0 && mouseY < displayHeight-65) {
			if (knots.get(currentKnot).size() == 0) {
				knots.remove(currentKnot);
				currentKnot--;
				return;
			}
			Boolean found = false;
			Segment foundS = null;
			for (int a = 0; a < knots.get(currentKnot).size()-1; a++) {
				Segment s = knots.get(currentKnot).get(a);
				if ((s.end || knots.get(currentKnot).get(a+1).end) && Math.sqrt(Math.pow((mouseX-s.x),2)+Math.pow((mouseY-s.y),2)) < finishDistance) {
					found = true;
					foundS = s;
					break;
				}
			}
			if (!found) {
				knots.get(currentKnot).get(knots.get(currentKnot).size()-1).end = true;
			}
			else {
				if (endOfStrand != null) {
					System.out.println(endOfStrand);
					System.out.println(foundS.x+" "+foundS.y);
					endOfStrand.set(1,endOfStrand.get(1));
					knots.get(endOfStrand.get(0)).get(endOfStrand.get(1)).end = false;
					//insertSegment(foundS.x, foundS.y, endOfStrand);
					endOfStrand = null;
				}
				else {
					addSegment(foundS.x, foundS.y);
	
				}
				
				//foundS.end = false;
				newEnd = true;
			}
			calculateCrossings();
			colorKnots();
		}
		drawing = false;
	}
	public void dowkerLabels(int s) {
		//check alternating, this assumes alternating
		Boolean alt = isAlternating(s);
		ArrayList<Segment> segments = knots.get(s);
		int cNumber = 1;
		for (int i = 0; i < segments.size(); i++) {
			for (ArrayList<Integer> c : crossings) {
				if (c.get(0) == s && c.get(1) == i) {
					if (!alt && cNumber % 2 == 0 && c.get(4) == 1) {
						c.set(5,-cNumber);
					}
					else {
						c.set(5,cNumber);
					}
					
					cNumber++;
				}
				else if (c.get(2) == s && c.get(3) == i && c.get(5) != -1) {
					if (!alt && cNumber % 2 == 0 && c.get(4) == 2) {
						c.set(6,-cNumber);
					}
					else {
						c.set(6,cNumber);
					}
					cNumber++;
				}
			}
		}
	}
	public void parityLabels(int s) {
		for (ArrayList<Integer> c : crossings) {
			c.set(7, getParity(c));
			
		}
	}
	public ArrayList<Integer> crossingWithSegment(int k, int s){
		for (ArrayList<Integer> c : crossings) {
			if ((c.get(0) == k && c.get(1) == s) || (c.get(2) == k && c.get(3) == s)) {
				return c;
			}
		}
		return null;
	}
	public void colorKnots() {
		int i = 1;
		for (ArrayList<Segment> s : knots) {
			int n = (int) Math.ceil(Math.log10(i)/Math.log10(2));
			Color c = Color.getHSBColor((float) (1/(Math.pow(2, n))+(i-n)/(Math.pow(2, n-1))), 1, 1);
			for (Segment p : s) {
				p.color = c;
			}
			i++;
		}
		firstCut = null;
	}
	public void switchArrows(int s) {
		ArrayList<Segment> oldS = knots.get(s);
		ArrayList<Segment> newS = new ArrayList<Segment>();
		for (int i = oldS.size()-1; i >= 0; i--) {
			newS.add(oldS.get(i));
		}
		newS.get(0).end = true;
		newS.get(newS.size()-1).end = false;
		knots.set(s,newS);
		refreshCrossings();
		for (ArrayList<Integer> c : crossings) {
			if (c.get(4) == 1) {
				c.set(4, 2);
			}
			else {
				c.set(4, 1);
			}
		}
	}
	public void setKnotStart(ArrayList<Integer> segment) {
		ArrayList<Segment> oldS = knots.get(segment.get(0));
		ArrayList<Segment> newS = new ArrayList<Segment>();
		for (int i = segment.get(1); i < oldS.size(); i++) {
			newS.add(oldS.get(i));
		}
		//newS.get(oldS.size()-segment.get(1)-1).end = false;
		for (int i = 1; i <= segment.get(1); i++) {
			newS.add(oldS.get(i));
		}
		
		knots.set(segment.get(0), newS);
		//newS.get(oldS.size()-1).end = true;
		calculateCrossings();
	}
	public Boolean isAlternating(int k) {
		int lastCrossing = -1;
		for (int i = 0; i < knots.get(k).size(); i++) {
			ArrayList<Integer> c = crossingWithSegment(k,i);
			if (c != null) {
				//System.out.println("second "+knots.get(k).get(i).x+" "+c.get(4)+ " "+lastCrossing);
				if (c.get(0) == k && c.get(1) == i) {
					if (c.get(4) == lastCrossing) {
						//System.out.println("first "+knots.get(k).get(i).x);
						return false;
					}
					else {
						lastCrossing = c.get(4);
					}
				}
				else if (c.get(2) == k && c.get(3) == i) {
					if ((c.get(4) == 2 && lastCrossing == 1) || (c.get(4) == 1 && lastCrossing == 2)) {
						
						return false;
					}
					else {
						if (c.get(4) == 1) {
							lastCrossing = 2;
						}
						else {
							lastCrossing = 1;
						}
					}
				}
			}
		}
		for (int i = 0; i < knots.get(k).size(); i++) {
			ArrayList<Integer> c = crossingWithSegment(k,i);
			if (c != null) {
				//System.out.println("second "+knots.get(k).get(i).x+" "+c.get(4)+ " "+lastCrossing);
				if (c.get(0) == k && c.get(1) == i) {
					if (c.get(4) == lastCrossing) {
						//System.out.println("first "+knots.get(k).get(i).x);
						return false;
					}
					else {
						return true;
					}
				}
				else if (c.get(2) == k && c.get(3) == i) {
					if ((c.get(4) == 2 && lastCrossing == 1) || (c.get(4) == 1 && lastCrossing == 2)) {
						
						return false;
					}
					else {
						if (c.get(4) == 1) {
							return true;
						}
						else {
							return true;
						}
					}
				}
			}
		}
		return true;
		/*int lastCrossing = -1;
		for (ArrayList<Integer> c : crossings) {
			if (c.get(0) == k) {
				
				if (lastCrossing != c.get(4)) {
					if (isR1(c)) {
						continue;
					}
					if (c.get(4) == 0) {
						lastCrossing = 0;
					}
					else if (c.get(4) == 1) {
						lastCrossing = 1;
					}
					
				}
				else {
					return false;
				}
			}
			else if (c.get(2) == k) {
				if (lastCrossing != c.get(4)) {
					return false;
				}
			}
		}
		return true;*/
	}
	public Boolean isR1(ArrayList<Integer> crossing) {
		if (crossing.get(0) != crossing.get(2)) {
			return false;
		}
		for (int i = crossing.get(1)+1; i < crossing.get(3); i++) {
			if (crossingWithSegment(crossing.get(0),i) != null) {
				return false;
			}
		}
		return true;
	}
	public void checkButtonClicks() {
		for (Button b : buttons) {
			if (b.click(mouseX, mouseY)) {
				switch (b.id) {
				case "eqs":
					System.out.println("got here");
					break;
				
				case "sarrows":
					if (firstCut != null) {
						switchArrows(firstCut.get(0));
					}
					
					break;
				case "amode":
					arrowMode = (arrowMode+1)%5;
					if (arrowMode == 0) {
						getButtonById("amode").text = "Arrows: None";
					}
					else if (arrowMode == 1) {
						getButtonById("amode").text = "Arrows: Constant";
					}
					else if (arrowMode == 2) {
						getButtonById("amode").text = "Arrows: Even";
					}
					else if (arrowMode == 3) {
						getButtonById("amode").text = "Arrows: Smart";
					}
					else if (arrowMode == 4) {
						getButtonById("amode").text = "Arrows: Animated";
					}
					break;
				case "aleft":
					if (arrowSpacing > 1) {
						arrowSpacing --;
					}
					getFieldById("aspacing").text = String.valueOf(arrowSpacing);
					break;
				case "aright":
					if (arrowSpacing < 200) {
						arrowSpacing ++;
					}
					getFieldById("aspacing").text = String.valueOf(arrowSpacing);
					break;
				case "beautify":
					for (int s = 0; s < knots.size(); s++) {
						try {
							beautifyCrossings(s, gapDistance);

						}
						catch(Exception i) {
							System.out.println("Beautify Failed.");
						}
					}
					break;
				case "clearCrossings":
					clearUnderCrossings = ! clearUnderCrossings;
					break;
				case "altWhite":
					alternateWhite = ! alternateWhite;
					break;
				case "findDowker":
					dowkerLabels(currentKnot);
					break;
				case "knotData":
					System.out.println(isAlternating(currentKnot));
					break;
				case "changeStart":
					setKnotStart(firstCut);
					break;
				case "orientation":
					if (getButtonById("orientation").text == "Show Orientation") {
						getButtonById("orientation").text = "Hide Orientation";
						orientation = true;
					}
					else {
						getButtonById("orientation").text = "Show Orientation";
						orientation = false;
					}
					break;
				case "delete":
					deleteData();
					break;
				case "getParity":
					if (firstCut != null) {
						parityLabels(firstCut.get(0));
					}
					break;
				case "showParity":
					if (getButtonById("showParity").text == "Show Parity") {
						getButtonById("showParity").text = "Hide Parity";
						showParity = true;
					}
					else {
						getButtonById("showParity").text = "Show Parity";
						showParity = false;
					}
					break;
				}
			}
		}
		
	}
	public Button getButtonById(String id) {
		for (Button b : buttons) {
			if (b.id.equals(id)) {
				return b;
			}
		}
		return null;
	}
	public DisplayField getFieldById(String id) {
		for (DisplayField b : fields) {
			if (b.id.equals(id)) {
				return b;
			}
		}
		return null;
	}
}