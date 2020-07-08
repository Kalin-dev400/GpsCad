package cad.reader;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import az.gpscad.BLCoordinate;
import az.gpscad.CoordTransform;
import az.gpscad.GlobalsLists;
import cad.formats.CadLine;

public class Lines {
    public static int Line(List<String> InputFile, int i){
        List<GeoPoint> tempPointList = new ArrayList<>();
        String[] initialSplit;
        String[] secondSplit;
        String readLine = InputFile.get(i);
        double tempX;
        double tempY;
        BLCoordinate tempBL;
        initialSplit = readLine.split("\\s+");
        int uniqueN = Integer.valueOf(initialSplit[2]);
        i++;
        while (!InputFile.get(i).contains("L ") && !InputFile.get(i).contains("C ") && !InputFile.get(i).contains("P ") && !InputFile.get(i).contains("T ") &&
               !InputFile.get(i).contains("S ") && !InputFile.get(i).contains("END_LAYER")) {
            //проверка за край на данните за линията
            readLine = InputFile.get(i);
            initialSplit = readLine.split(";");

            for (String splitFirst : initialSplit) {
                if (!splitFirst.trim().isEmpty())
                {
                    secondSplit = splitFirst.trim().split("\\s+");

                    tempX = Double.valueOf(secondSplit[1])+GlobalsLists.CoordStart.X;
                    tempY = Double.valueOf(secondSplit[2])+GlobalsLists.CoordStart.Y;

                    tempBL = CoordTransform.ToWGS(tempX,tempY);
                    //tempPointList.add(new CadPoint(Integer.valueOf(secondSplit[0]),tempBL.B,tempBL.L,""));
                    tempPointList.add(new GeoPoint(tempBL.B,tempBL.L));
                }
            }
            i++;
        }
            GlobalsLists.GlobalLines.add(new CadLine(uniqueN, tempPointList));
        return i-1;
    }
}
