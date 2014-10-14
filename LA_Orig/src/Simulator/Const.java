package Simulator;
import geom3D.Point3D;

import java.awt.*;

import Light_Algorithm.Lights_Algorithms;
import Light_Algorithm.Particle_Algo;
/**
this class contains all the public constants of the Ex6 example. <br>
*/ 
public class Const {
	
	
	public static final int Gen=0, Point=1, Rect1=2, Rect2=3,
		Circle1=4,Circle2=5,T1=6,T2=7,T3=8, Poly1=50, Poly2=51;
	
	public static final int PointS=10,RectS1=11,RectS2=12,
		Move1=13,Move2=14,Copy1=15,Copy2=16,Delete=17,_Color=18,Rescale1=19,Rescale2=20,
			By_area=1,By_perimeter=2,By_2S=0;
	
	public final static int MAX_X = 300;
	public final static int MAX_Y = 300;
	public static String APPLICATION_TITLE = "EX6_Example";
	public static final double _epsilon = 0.000001;
	public final static int black=0,red=1,blue=2,green=3,pink=4,gray=5;
	
	/** the color table - converts int to 'real' (awt) Color.*/
	public static Color color(int i) {
		Color c = Color.black;
		if(i==green) c = Color.green;
		else if(i==red) c = Color.red;
		else if(i==blue) c = Color.blue;
		else if(i==pink) c = Color.pink;
		else if(i==gray) c = Color.gray;
		return c;
	}
}
