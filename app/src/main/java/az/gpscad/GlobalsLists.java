package az.gpscad;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import cad.formats.CadContour;
import cad.formats.CadLine;
import cad.formats.CadPoint;

public class GlobalsLists {
    public static List<CadLine> GlobalLines = new ArrayList<>();
    public static List<CadPoint> GlobalPoints = new ArrayList<>();
    public static List<CadContour> GlobalCountours = new ArrayList<>();
    public static XYCoordinate CoordStart = new XYCoordinate();
    public static String EKATTE;
    public static List<String> InputFileArray;
    public static GeoPoint PathStartPoint;
    public static GeoPoint PathEndPoint;
    public static boolean UpdatePath = false;
}
