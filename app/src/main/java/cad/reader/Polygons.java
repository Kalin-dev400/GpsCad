package cad.reader;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import az.gpscad.BLCoordinate;
import az.gpscad.CoordTransform;
import az.gpscad.GlobalsLists;
import cad.formats.CadContour;
import cad.formats.CadLine;

public class Polygons {
    public static int Contours(List<String> InputFile, int i){
        List<Integer> tempLinesList = new ArrayList<>();
        List<GeoPoint> tempGeoPoints = new ArrayList<>();
        String[] initialSplit;
        String tempIdentificator;
        String tempType;
        String readLine = InputFile.get(i);
        double TextX;
        double TextY;
        BLCoordinate tempTextBLCord;
        String identText;
        int tempNident;
        boolean firstpoint = true;
        GeoPoint LastPoint = null;

        initialSplit = readLine.split("\\s+");
        if (initialSplit.length < 7){
            return i;
        }

        tempIdentificator = initialSplit[2];
        tempNident = tempIdentificator.split("\\.").length;
        tempType = CheckContourType.CheckContourType(tempNident);
        identText = tempIdentificator.split("\\.")[tempNident-1];

        TextX = Double.valueOf(initialSplit[3])+GlobalsLists.CoordStart.X;
        TextY = Double.valueOf(initialSplit[4])+GlobalsLists.CoordStart.Y;

        tempTextBLCord = CoordTransform.ToWGS(TextX,TextY);
        i++;

        while (!InputFile.get(i).contains("L ") && !InputFile.get(i).contains("C ") && !InputFile.get(i).contains("P ") && !InputFile.get(i).contains("T ") &&
                !InputFile.get(i).contains("S ") && !InputFile.get(i).contains("END_LAYER")) {

            readLine = InputFile.get(i);
            initialSplit = readLine.split("\\s+");

            for (String splitFirst : initialSplit) {
                if (!splitFirst.trim().isEmpty())
                {
                    tempLinesList.add(Integer.valueOf(splitFirst));
                    for (CadLine line:GlobalsLists.GlobalLines) {
                        if (line.uniqueN == Integer.valueOf(splitFirst)){
                            //В следващата част се определя в какъв ред да се запишат точките от линията
                            if(LastPoint != null && !LastPoint.equals(line.Points.get(0)) && LastPoint.equals(line.Points.get(line.Points.size()-1))){
                                for(int j=line.Points.size()-1;j>=0;j--){
                                    tempGeoPoints.add(line.Points.get(j));
                                    if(j==0){
                                        LastPoint = line.Points.get(j);
                                    }
                                }
                            }
                            else{
                                for (GeoPoint point: line.Points) {
                                    tempGeoPoints.add(point);
                                    LastPoint = point;
                                }
                            }
                        }
                    }
                }
            }
            i++;
        }
        GlobalsLists.GlobalCountours.add(new CadContour(tempType,GlobalsLists.EKATTE+ "." + tempIdentificator,tempLinesList,
                tempGeoPoints,new GeoPoint(tempTextBLCord.B,tempTextBLCord.L),identText));
        return i-1;
    }
}
