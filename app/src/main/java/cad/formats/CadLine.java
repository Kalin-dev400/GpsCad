package cad.formats;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class CadLine {
    public int uniqueN;
    public List<GeoPoint> Points;

    public CadLine(int uniqueN,List<GeoPoint> Points) {
        this.uniqueN = uniqueN;
        this.Points = Points;
    }

}
