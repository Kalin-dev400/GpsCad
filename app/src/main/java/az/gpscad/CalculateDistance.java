package az.gpscad;

import org.osmdroid.util.GeoPoint;

public class CalculateDistance {
    public static double CalculateDistance(GeoPoint FirstPoint, GeoPoint SecondPoint){
        double result;
        XYCoordinate PointOne = CoordTransform.FromWGS(FirstPoint.getLongitude(),FirstPoint.getLatitude());
        XYCoordinate PointTwo = CoordTransform.FromWGS(SecondPoint.getLongitude(),SecondPoint.getLatitude());

        double dX = PointTwo.X - PointOne.X;
        double dY = PointTwo.Y - PointOne.Y;

        result = Math.sqrt(Math.pow(dX,2)+Math.pow(dY,2));
        return result;
    }
}
