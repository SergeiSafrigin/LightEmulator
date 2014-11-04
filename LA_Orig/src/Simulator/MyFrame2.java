package Simulator;

import geom3D.Ang_Vector;
import geom3D.Point3D;
import geom3D.Segment3D;
import geom3D.Vector3D;
import geomLights.GIS_Light;
import geomLights.GIS_Lights;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import kcg.core.light.ImageConfig;
import lights.FrameReader;
import Light_Algorithm.Particle;
import Light_Algorithm.Particle_Algo;
import Light_Algorithm.Position_State;


/**
this class is the window manager of the simple IndoogGo Light algorithm simulator 
including all GUI parts: paint method, data Editable (Drawable container),GUI, mouse manager,
 */

public class MyFrame2 extends JFrame implements ActionListener	{ 	
	private static final long serialVersionUID = -2502427053989256885L;
	private static final float ROOM_OFFSET = 0.2f;
	
	//lights map
	private static String lights_map = "data/lab/lights_map.csv";
	//file with recorded frames
	private static String record_file = "data/lab/3";

	//List of points that represent the path taken
	private ArrayList<Point3D> _path;

	private FrameReader frameReader;

	private static final int ROOM_WIDTH = 500;
	private static final int ROOM_HEIGHT = 500;

	private Point3D min;
	private Point3D max;

	private Cords W2P;

	//GUI - shift to center the room
	private static final int SHIFT_X = 400, SHIFT_Y = 80;

	private GIS_Lights _map = null;
	private int NUMBER_OF_PARTICLES = 100;
	private Particle_Algo _algo = null; 
	private double _time=0;
	private Position_State _solution = null;
	private Point3D _solve, _particleBestsolution;
	
	private Point3D screenShift;
	private float lastTouchCoordX, lastTouchCoordY;
	private boolean recordStartingPoint = false;
	private boolean startingPointSelected = false;

	public static void main(String[] a) {
		System.out.println("InDoorGo Simulation: K&CG");
		MyFrame2 win = new MyFrame2();
		win.start();
	}

	// *** text area ***
	public MyFrame2() {
		this.setTitle("K&CG - IndoorGo Lights Simulator");
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		init();
		addWindowListener(new WindowAdapter() { public void
			windowClosing(WindowEvent e) { System.exit(0); } } );
	}

	private void init() {
		_time = 0;
		
		_map = MapParser.parse(lights_map);
		
		setMinAndMaxPoints();
		W2P = new Cords(min, max, new Point3D(0, ROOM_HEIGHT, 2.55), new Point3D(ROOM_WIDTH, 0, 2.55));
		
		frameReader = new FrameReader(record_file);
		frameReader.step(0);
		
		_path = new ArrayList<Point3D>();
		
		Point3D start = new Point3D(max.x - min.x, max.y - min.y, max.z - min.z);
		_solution = new Position_State(start);
		
		_algo = new Particle_Algo(NUMBER_OF_PARTICLES);
		_algo.init(min,max,NUMBER_OF_PARTICLES);
		_solve = null;
	}

	private void step() {
		if (_time <= 1) {
			_time++;
			return;
		}

		ArrayList<Ang_Vector> lts = frameReader.step((int)_time);

		_algo.update(lts, _map);
		_particleBestsolution = _algo.best().get_pos();
		
		addLocationToPath(frameReader.getCurrVisualOdometryLocation());

		_time = _time + 1;
	}

	public void paint(Graphics g) { 
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.clearRect(SHIFT_X, SHIFT_Y, ROOM_WIDTH, ROOM_HEIGHT);
		drawFrame(g);
		g.setColor(Color.orange);

		int[] currImage = frameReader.getCurrImage();
		int frameWidth = frameReader.getFrameWidth();
		int frameHeight = frameReader.getFrameHeight();

		frameReader.drawImage(currImage, frameWidth, frameHeight, 20, 80, getGraphics());
		frameReader.drawLightEdges(frameWidth, frameHeight, 20, frameHeight+100, getGraphics());
		frameReader.drawGeometryLights(20, frameHeight+100, getGraphics());

		drawPath();

		for(int i=0; i<_map.size(); i++) {
			GIS_Light p = _map.at(i);

			Point3D p1 = W2P.w2s(p.getCenter());
			double r = p.get_diameter();
			int x1 = (int)p1.x();
			int y1 = (int)p1.y();
			int rad = 10;

			g.fillOval(SHIFT_X + x1-rad, SHIFT_Y + y1-rad,2*rad ,2*rad);
		}
		
		if(_solution !=null)
			drawSolution(_solution, g);
		
		if(_algo!=null)
			drawParticles(g);

		if(_solve !=null) {
			g.setColor(Color.GREEN);
			drawPoint(_solve, g);
		}
	}

	private void drawParticles(Graphics g) {
		Particle best = _algo.best();
		double dd = best.get_w();

		for(int i=0; i<_algo.size(); i++) {
			Particle p = _algo.get(i);
			int c = (int)(255*p.get_w()/dd);
			setcolor(c,g);

			if(g.getColor() == Color.BLACK)
				drawPoint(p.get_pos(), 6, true, g);
			else 
				drawPoint(p.get_pos(), 6, false, g);
		}

		g.setColor(Color.BLUE);
		drawPoint(best.get_pos(),12, false,g);
	}
	
	private void drawSolution(Position_State pos, Graphics g) {
		g.setColor(Color.RED);
		//		Point3D cen = pos.get_pos();
		Point3D cen = _particleBestsolution;
		if (cen != null){
			drawPoint(cen,g);
			//		double ori =  pos.get_ori()._azm;
			double ori = frameReader.getCurrYaw();
			
			ImageConfig config = frameReader.getConfig();

			double[] aa = Cords.azmDist2cords(ori-config.gethAngle()/2, 5);
			double[] bb = Cords.azmDist2cords(ori+config.gethAngle()/2, 5);
			Point3D a1 = new Point3D(cen); a1.add(aa[0], aa[1], 0);
			Point3D b1 = new Point3D(cen); b1.add(bb[0], bb[1], 0);
			drawLine(cen, a1,g);
			drawLine(cen, b1,g);
		}
	}

	private void setcolor(int c, Graphics g) {
		if(c<20) g.setColor(Color.white);
		else if(c<40) g.setColor(Color.yellow);
		else if(c<70) g.setColor(Color.orange);
		else if(c<100) g.setColor(Color.red);
		else if(c<150) g.setColor(Color.CYAN);
		else if(c<180) g.setColor(Color.blue);
		else if(c<210) g.setColor(Color.green);
		else g.setColor(Color.black);
	}

	private void drawFrame(Graphics g) {
		g.setColor(Color.black);
		g.drawRect(SHIFT_X, SHIFT_Y, ROOM_WIDTH, ROOM_HEIGHT);
	}

	private void drawLine(Point3D pos, Vector3D ori, Graphics g){
		Point3D dir = new Point3D(pos); dir.add(ori.toPoint3D());
		Point3D p0 = W2P.w2s(pos);
		Point3D p1 = W2P.w2s(dir);
		int x0 = (int)p0.x();
		int y0 = (int)p0.y();
		int x1 = (int)p1.x();
		int y1 = (int)p1.y();
		g.drawLine(SHIFT_X + x0, SHIFT_Y + y0, SHIFT_X + x1, SHIFT_Y + y1);
	}

	private void drawLine(Point3D a, Point3D b, Graphics g){
		Point3D p0 = W2P.w2s(a);
		Point3D p1 = W2P.w2s(b);
		int x0 = (int)p0.x();
		int y0 = (int)p0.y();
		int x1 = (int)p1.x();
		int y1 = (int)p1.y();
		g.drawLine(SHIFT_X + x0, SHIFT_Y + y0, SHIFT_X + x1, SHIFT_Y + y1);
	}

	private void drawPoint(Point3D ps, Graphics g) {
		drawPoint(ps, 12, false, g);
	}

	private void drawPoint(Point3D ps, int rad, boolean fill,Graphics g) {
		Point3D p = W2P.w2s(ps);
		int x = (int)p.x();
		int y = (int)p.y();

		if(!fill)
			g.drawOval(SHIFT_X + x-rad, SHIFT_Y + y-rad,rad*2 ,rad*2);
		else
			g.fillOval(SHIFT_X + x-rad, SHIFT_Y + y-rad,rad*2 ,rad*2);
	}
	
	private void start() {	
		show(); 
		dialog();
	}


	private void dialog() {
		MenuBar menuBar = new MenuBar();

		Menu menu = new Menu("Menu");
		
		MenuItem startPoint;
		startPoint = new MenuItem("Select Starting Point");
		startPoint.addActionListener(this);
		
		menu.add(startPoint);
		
		MenuItem clearPath = new MenuItem("Clear Path");
		clearPath.addActionListener(this);
		menu.add(clearPath);
		
		MenuItem reset = new MenuItem("Reset");
		reset.addActionListener(this);
		menu.add(reset);
		
		menuBar.add(menu);

		setMenuBar(menuBar);	
		addMouseListener(new MouseManeger());
		addKeyListener(new KeyboardManeger());
	}

	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		if (arg.equals("Select Starting Point"))
			recordStartingPoint = true;
		else if (arg.equals("Clear Path")) {
			startingPointSelected = false;
			screenShift = null;
			_path.clear();
			repaint();
		} else if (arg.equals("Reset")) {
			recordStartingPoint = false;
			startingPointSelected = false;
			screenShift = null;
			_path.clear();
			frameReader.step(0);
			Point3D start = new Point3D(max.x - min.x, max.y - min.y, max.z - min.z);
			_solution = new Position_State(start);
			_time = 0;
			repaint();
		}
	}

	class KeyboardManeger extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
			if (startingPointSelected) {
				step();
				repaint();
			}
		}
	}

	class MouseManeger extends MouseAdapter{   // inner class!!										 	
		public void mousePressed(MouseEvent e) {
			if (recordStartingPoint) {
				lastTouchCoordX = e.getX();
				lastTouchCoordY = e.getY();
				recordStartingPoint = false;
				startingPointSelected = true;
				_path.clear();
				_algo.init(min, max, NUMBER_OF_PARTICLES);
				repaint();
			}
		}
	}
	
	private void addLocationToPath(Point3D worldLocation) {
		worldLocation.divideByScalar(100);
		Point3D screenLocation = W2P.w2s(worldLocation);
		if (screenShift == null) {
			screenShift = new Point3D(screenLocation.x - lastTouchCoordX, screenLocation.y - lastTouchCoordY, 0);
		}
		
		screenLocation.substract(screenShift);
		_path.add(screenLocation);
	}

	public void drawPath(){
		Graphics2D g2d = (Graphics2D) getGraphics();

		g2d.setStroke(new BasicStroke(2));
		
		for(int i = 0; i < _path.size() - 1; i++) {
			Point3D p1 = _path.get(i);
			Point3D p2 = _path.get(i + 1);
			
			g2d.drawLine((int)(p1.x + 0.5), (int)(p1.y + 0.5), (int)(p2.x + 0.5), (int)(p2.y + 0.5));
		}
	}
	
	
	private void setMinAndMaxPoints(){
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = 0;

		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		double maxZ = Double.MIN_VALUE;
		
		for(int i = 0; i < _map.size(); i++) {
			GIS_Light light = _map.at(i);
			
			Point3D center = light.getCenter();
			Segment3D segment = light.get_seg();
			Point3D seg1 = segment.p1_ref();
			Point3D seg2 = segment.p2_ref();

			//x
			if (center.x() < minX)
				minX = center.x();
			if (seg1.x() < minX)
				minX = seg1.x();
			if (seg2.x() < minX)
				minX = seg2.x();

			if (center.x() > maxX)
				maxX = center.x();
			if (seg1.x() > maxX)
				maxX = seg1.x();
			if (seg2.x() > maxX)
				maxX = seg2.x();

			//y
			if (center.y() < minY)
				minY = center.y();
			if (seg1.y() < minY)
				minY = seg1.y();
			if (seg2.y() < minY)
				minY = seg2.y();

			if (center.y() > maxY)
				maxY = center.y();
			if (seg1.y() > maxY)
				maxY = seg1.y();
			if (seg2.y() > maxY)
				maxY = seg2.y();

			//z			
			if (center.z() > maxZ)
				maxZ = center.z();
			if (seg1.z() > maxZ)
				maxZ = seg1.z();
			if (seg2.z() > maxZ)
				maxZ = seg2.z();
		}

		minX -= ROOM_OFFSET;
		minY -= ROOM_OFFSET;

		maxX += ROOM_OFFSET;
		maxY += ROOM_OFFSET;

		min = new Point3D(minX, minY, minZ);
		max = new Point3D(maxX, maxY, maxZ);
	}
}

