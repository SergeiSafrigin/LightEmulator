package Simulator;

import geom3D.Ang_Vector;
import geom3D.Point3D;
import geom3D.Vector3D;
import geomLights.GIS_Light;
import geomLights.GIS_Lights;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JFrame;

import kcg.core.light.GeometryLight;
import kcg.core.light.ImageConfig;
import kcg.core.light.ImageConfig.DistanceType;
import kcg.core.light.filter.LightFilter;
import kcg.core.light.filter.VisualOdometryFilter;
import kcg.datarecorder.main.Config;
import kcg.datarecorder.recorder.Data;
import kcg.datarecorder.recorder.FrameData;
import lights.FrameReader;
import Light_Algorithm.Particle;
import Light_Algorithm.Particle_Algo;
import Light_Algorithm.Position_State;


/**
this class is the window manager of the simple IndoogGo Light algorithm simulator 
including all GUI parts: paint method, data Editable (Drawable container),GUI, mouse manager,
 */

public class MyFrame2 extends JFrame implements ActionListener	{ 

	public static void main(String[] a) {
		System.out.println("InDoorGo Simulation: K&CG");
		MyFrame2 win = new MyFrame2();
		win.start();
	}

	//List of points that represent the path taken
	private ArrayList<Point3D> _path;

	//GUI - Path box metrics
	private static final int PATH_BOX_WIDTH = 200, PATH_BOX_HEIGHT = 200;

	private Cords _pathW2P = new Cords(
			new Point3D(-500, -500, -100),
			new Point3D(500, 500, 300),
			new Point3D(0, PATH_BOX_HEIGHT, 2.55),
			new Point3D(PATH_BOX_WIDTH, 0, 2.55)
			);

	private FrameReader frameReader;

	private static final int ROOM_WIDTH = 500;
	private static final int ROOM_HEIGHT = 500;

	public static Point3D min = new Point3D(0, 2, 2.55);
	public static Point3D max = new Point3D(5.2, 10, 2.55);

	private Cords W2P = new Cords(
			min,
			max,
			new Point3D(0, ROOM_HEIGHT, 2.55),
			new Point3D(ROOM_WIDTH, 0, 2.55)
			);

	private int _ink;
	private int _stage;

	//GUI - shift to center the room
	private static final int SHIFT_X = 400, SHIFT_Y = 80;

	//file with recorded frames
	private String Lights_File="data/lab/3";

	private Point3D _p1,_p2; // tmp Points for selection
	private GIS_Lights _map = null;
	private int NUMBER_OF_PARTICLES = 50;
	private Particle_Algo _algo = null; 
	public double _time=0;
	public Position_State _solution = null;
	public Point3D _solve, _particleBestsolution;

	public static double max_speed = 2.0; // m/s

	public static double min_elev = -0, max_elev = 50;
	public static double min_z = 0.5, max_z = 1.5;
	public static int fps=25;
	public static double sim_time = 60*1000; // mili-seconds
	//	public static String light_map = "data/Light3.txt";

	//lights map
	public static String light_map = "data/lab/lights_map.csv";
	public double max_ang_speed = 90; // deg/s
	public static boolean DEBUG=true;
	public static double NORM_ERR = 1.0;  // [-2,2] - yaw, [-1,1] pitch
	// ****************************************

	// *** text area ***
	public MyFrame2() {
		this.setTitle("K&CG - IndoorGo Lights Simulator");
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		init();
		addWindowListener(new WindowAdapter() { public void
			windowClosing(WindowEvent e) { System.exit(0); } } );
	}

	private void init() {
		_stage = Const.Gen; 
		_ink = Const.blue;
		_p1=null;
		_time = 0;

		frameReader = new FrameReader(Lights_File);
		
		_map = MapParser.parse(light_map);

		_path = new ArrayList<Point3D>();

		_algo = new Particle_Algo(NUMBER_OF_PARTICLES);
		Point3D start = new Point3D(2.6, 6, 1);
		_solution = new Position_State(start);
		_algo.init(min,max,NUMBER_OF_PARTICLES);
		_solve = null;
		//	_algo.get(0).update(start, 0.99, 40);

	}

	private void step(double dt) {
		ArrayList<Ang_Vector> lts = frameReader.step((int)_time);
		
		if(lts.size()>=2) {
			//_solve = simpleTest(lts);
			_algo.update(lts, _map);
			// Boaz 6/10
			//Point3D best = _algo.best().get_pos();
			_particleBestsolution = _algo.best().get_pos();
			_path.add(new Point3D(frameReader.getCurrVisualOdometryLocation()));
			// double dist1 = _solve.distance3D(_solution.get_pos());
			//			double dist = _particleBestsolution.distance3D(_solution.get_pos());
			//			System.out.println("Dist: "+dist+"  pp: "+_particleBestsolution+"   real: "+_solution.get_pos());
			//Particle best = _algo.best();
			//	double dist = best.get_pos().distance3D(_solution.get_pos());
			//	if(DEBUG) System.out.println(", "+best.get_pos()+", ERR: "+dist+"  w: "+best.get_w());
		}

		_time = _time + 1;
	}

	//	private Point3D simpleTest( ArrayList<Ang_Vector> lts) {
	//		Point3D l0=null, l1=null, v0=null, v1 = null;
	//		for(int i=0;l1==null && i<_map.size();i++) {
	//			GIS_Light c = _map.get_lights().get(i);
	//			if(c.is_vis()) {
	//				Point3D t = c.getCenter();
	//				if(l0==null) l0=t;
	//				else l1=t;
	//			}
	//		}
	//		v0 = getPoint(l0,lts.get(0)); v1 = getPoint(l1,lts.get(1));
	//		Point3D pp0 = Geom_Algo.CenterOfMidSegmentBetweenTwoLines(l0, v0, l1, v1);
	//		v0 = getPoint(l0,lts.get(1)); v1 = getPoint(l1,lts.get(0));
	//		Point3D pp1 = Geom_Algo.CenterOfMidSegmentBetweenTwoLines(l0, v0, l1, v1);
	//
	//		Point3D ans = pp0;
	//		if(_solution.get_pos().distance3D(pp0)>_solution.get_pos().distance3D(pp1)) {
	//			ans = pp1;
	//		}
	//		return ans;
	//	}

	//	private Point3D getPoint(Point3D p, Ang_Vector v) {
	//		Point3D ans = new Point3D(p);
	//		Vector3D vec = v.toVector();
	//		ans.add(vec.toPoint3D());
	//		return ans;
	//	}

	//	private void move() {
	//		Point3D p = _solution.get_pos();
	//		double r = 0.2;
	//		double x = rand(-r,r);
	//		double y = rand(-r,r);
	//		double z = rand(-r,r);		
	//
	//		if(p.z()<=0.5) z=Math.abs(z); if(p.z()>=1.5) z=-Math.abs(z);
	//		if(p.x()<= r + min.x()) x=Math.abs(x); if(p.x()>= max.x()-r) x=-Math.abs(x);
	//		if(p.y() <= r + min.y()) y=Math.abs(y); if(p.y()>= max.y()-r) y=-Math.abs(y);
	//
	//		p.add(x, y, z);
	//
	//		_solution.set_pos(p);
	//		Ang_Vector av = _solution.get_ori();
	//		double da = rand(-3,5);
	//		double de = rand(-2,2);
	//		av._azm+=da;
	//		av._elev+=de;
	//		if( av._azm<0) { av._azm+=360;}
	//		if( av._azm>=360) { av._azm-=360;}
	//		if(av._elev>=90) {av._elev=45;}
	//		if(av._elev<=-10) {av._elev=0;}
	//
	//		_solution.set_ori(av);
	//	}

	//	private static double rand(double min, double max){
	//		double d = Math.random();
	//		double dx = max-min;
	//		return min+dx*d;
	//	}
	//	
	//	private static double azmDiff(double a1, double a2) {
	//		double max = Math.max(a1, a2);
	//		double min = Math.min(a1, a2);
	//		double da = max-min;
	//		if(da>=180) {
	//			da = Math.abs((360-max)-min);
	//		}
	//		return da;
	//	}

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
			//			if(p.is_vis())
			g.fillOval(SHIFT_X + x1-rad, SHIFT_Y + y1-rad,2*rad ,2*rad);
			//			else
			//				g.drawOval(SHIFT_X + x1-rad, SHIFT_Y + y1-rad,2*rad ,2*rad);
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
	public void start() {	
		this.show(); 
		Dialog();}


	public void Dialog()
	{  MenuBar mbar = new MenuBar();

	Menu m = new Menu("File");
	MenuItem m1;
	m1 = new MenuItem("Open");
	m1.addActionListener(this);
	m.add(m1);            
	m1 = new MenuItem("Save");
	m1.addActionListener(this);
	m.add(m1); 
	m1 = new MenuItem("PROJ_DIR");
	m1.addActionListener(this);
	m.add(m1); 
	m1 = new MenuItem("Load_Image");
	m1.addActionListener(this);
	m.add(m1); 
	MenuItem m6 = new MenuItem("Clear");
	m6.addActionListener(this);
	m.add(m6);        
	MenuItem m2 = new MenuItem("Exit");
	m2.addActionListener(this);
	m.add(m2);            
	mbar.add(m);

	m = new Menu("Input");
	MenuItem m3 = new MenuItem("WayPoint");
	m3.addActionListener(this);
	m.add(m3);            
	m3 = new MenuItem("Obstacle");
	m3.addActionListener(this);
	m.add(m3);  
	mbar.add(m);

	m = new Menu("Run");
	m1 = new MenuItem("Mission1");
	m1.addActionListener(this);
	m.add(m1); 
	m1 = new MenuItem("Mission2");
	m1.addActionListener(this);
	m.add(m1); 
	mbar.add(m);

	m = new Menu("Edit");         
	m1 = new MenuItem("Delete");
	m1.addActionListener(this);
	m.add(m1);  
	m1 = new MenuItem("Red");
	m1.addActionListener(this);
	m.add(m1);  
	m1 = new MenuItem("Blue");
	m1.addActionListener(this);
	m.add(m1);  
	m1 = new MenuItem("Fill");
	m1.addActionListener(this);
	m.add(m1);  
	m1 = new MenuItem("Empty");
	m1.addActionListener(this);
	m.add(m1);   

	m1 = new MenuItem("Info");
	m1.addActionListener(this);
	m.add(m1);  
	mbar.add(m);   	

	setMenuBar(mbar);	
	this.addMouseListener(new mouseManeger());
	this.addKeyListener(new keyboardManeger());
	}

	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		if (arg.equals("Open"))
			openProj();
		//   else if (arg.equals("Save")) saveAll();
		//  else if (arg.equals("PROJ_DIR")) this.chooseDir();
		// else if (arg.equals("Load_Image")) this.chooseDir();

		else if(arg.equals("Clear")) {
			init();
			repaint();
		} else if(arg.equals("Exit"))
			System.exit(209);
		else if(arg.equals("WayPoint"))
			_stage = Const.Circle1;
		else if(arg.equals("Obstacle"))
			_stage = Const.Rect1;
		else if(arg.equals("Red")) 
			_ink = Const.red;
		else if(arg.equals("Blue"))  
			_ink = Const.blue;
		else if(arg.equals("Mission1")) {  
			//_algo1.startMission(1);
			this._stage = Const.Move1;
		}
		else if(arg.equals("Mission2")) {  
			//	_algo1.startMission(2);
			this._stage = Const.Move1;
		}
	}

	// ********** Private methodes (open,save...) ********

	private void openProj()  {
		_stage = Const.Gen;
		init();
		FileDialog d = new FileDialog(this,"Project file loader", FileDialog.LOAD);
		d.show();
		String dr = d.getDirectory();
		String fi = d.getFile();
		String dir = dr;
		try{
			//_ps = new WayPoints(dir+"PROJ_wps.txt");
			//_obs = new Obstacles(dir+"PROJ_obs.txt");
			File f = new File(dir+"PROJ.png");
			if(f.exists()) {
				//        image = ImageIO.read(new File(dir+"PROJ.png"));
			}
			System.out.println("*** load Proj: "+d);
			repaint();
		}
		catch(Exception e) {e.printStackTrace();}
	}
	private void openMapFile()  {
		_stage = Const.Gen;
		FileDialog d = new FileDialog(this,"Open text file", FileDialog.LOAD);
		d.show();
		String dr = d.getDirectory();
		String fi = d.getFile();
		if (fi != null) {
			//	this.mapName = dr+fi;
			init();
			System.out.println("*** should load map + init: "+dr+fi);
		}
	}

	class keyboardManeger extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
			if( _stage == Const.Move1 ){
				step(1);
				repaint();
			}
		}
	}

	class mouseManeger extends MouseAdapter{   // inner class!!										 	
		public void mousePressed(MouseEvent e) {
			Graphics g = getGraphics();
			int xx = e.getX();
			int yy = e.getY();

			int len =0; 
			switch(_stage) {
			case (Const.Gen):{
				break;	
			}				

			case (Const.Circle1): {	
				_p1 = new Point3D(xx,yy,0);
				_stage = Const.Circle2;
				break;
			}
			case (Const.Circle2): {

				_stage = Const.Circle1;
				repaint();
				break;
			}		
			case (Const.Rect1): {	
				_p1 = new Point3D(xx,yy,0);
				_stage = Const.Rect2;
				break;
			}
			case (Const.Rect2): {	
				Point3D p2 = new Point3D(xx,yy,0);
				Point3D w1 = Cords.W2P().s2w(_p1);
				Point3D w2 = Cords.W2P().s2w(p2);
				//	Obstacle obs = new Obstacle(w1,w2);
				//	Obstacle obs = new Obstacle(_p1,p2);
				//	_obs.add(obs);
				_stage = Const.Rect1;
				repaint();
				break;
			}		
			case (Const.Move1): {	
				step(1);
				repaint();
				break;
			}
			////////////////		
			}
		}
	}

	public void drawPath(){
		Graphics2D g2d = (Graphics2D) getGraphics();

		Point3D prevP = null;

		int shiftX = 1000;
		int shiftY = 200;

		g2d.clearRect(shiftX, shiftY, PATH_BOX_WIDTH, PATH_BOX_HEIGHT);
		g2d.drawRect(shiftX, shiftY, PATH_BOX_WIDTH, PATH_BOX_HEIGHT);

		g2d.setStroke(new BasicStroke(2));

		for(Point3D p: _path){
			if (prevP != null){
				Point3D p0 = _pathW2P.w2s(prevP);
				Point3D p1 = _pathW2P.w2s(p);


				g2d.drawLine(
						shiftX + (int)(p0.x()),
						shiftY + (int)(p0.y()),
						shiftX + (int)(p1.x()),
						shiftY + (int)(p1.y())
						);
			}
			prevP = p;
		}
	}
}

