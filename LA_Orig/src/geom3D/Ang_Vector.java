package geom3D;

import geomLights.GIS_Light;
import geomLights.GIS_Lights;

import java.util.ArrayList;

import Light_Algorithm.Position_State;

/**
 * this class represrnts a vector in angular cordinations
 * @author boaz
 *
 */
public class Ang_Vector {
	private final double EPS = 1e-7;
	private final int FLOAT_NUMBER_PRECISSION = 6;
	public double _azm, _elev, _dist;
	public Ang_Vector(Point3D p1, Point3D p2) {this(new Vector3D(p1,p2));}
	public Ang_Vector(double azm, double elev) {this(azm,elev,1.0);}
	public Ang_Vector(double azm, double elev, double dist){
		this._azm = azm;
		this._elev = elev;
		this._dist = dist;
	}
	public Ang_Vector(Vector3D p){
		_dist = p.length();
		if(p.x==0 && p.y==0) {
			_azm=0; 
			_elev=90;
			if(p.z<0) _elev = -90;	
		}
		else {
			double distXY=Math.sqrt(p.x*p.x+p.y*p.y);
			_elev = Math.atan2(p.z, distXY);
			_elev = Math.toDegrees(_elev);
			_azm = Math.atan2(p.y, p.x);
			_azm = Math.toDegrees(_azm);
			if(_azm<=90 && _azm>-180) {_azm = 90-_azm;}
			else {if(_azm>90 && _azm<180) {_azm = 450-_azm;}
			else{throw new RuntimeException("ERR in angular atan2");}
			}
		}
	}
	public Vector3D toVector() {
		return Geom_Algo.azm2vector(_azm, _elev, _dist);
	}

	public String toString() {
		String pcssn = String.format("%%8.%df",FLOAT_NUMBER_PRECISSION);
		String format = String.format("[%s,%s,%s]",pcssn,pcssn,pcssn);
		return String.format(format, _azm,_elev,_dist);
	}
	
	////////////// PRIVATE ///////////////
}