package cad.formats;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class CadContour {
    public String Type;
    public String identificator;
    public List<Integer> Lines;
    public List<GeoPoint> Points;
    public GeoPoint TextPoint;
    public String IdentText;

    public CadContour(String Type, String identificator, List<Integer> Lines,List<GeoPoint> Points, GeoPoint TextPoint, String IdentText){
        this.Type = Type;
        this.identificator = identificator;
        this.Lines = Lines;
        this.Points = Points;
        this.TextPoint = TextPoint;
        this.IdentText = IdentText;
    }
}
