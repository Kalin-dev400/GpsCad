package cad.reader;

import java.util.List;

import az.gpscad.BLCoordinate;
import az.gpscad.CoordTransform;
import az.gpscad.GlobalsLists;
import cad.formats.CadPoint;

public class Points {
    public static int Point(List<String> InputFile, int i){
        String[] initialSplit;
        String[] secondSplit;
        String readLine = InputFile.get(i);
        double tempX;   //временни променливи
        double tempY;
        double tempH;
        BLCoordinate tempBL;
        CadPoint tempCadPoint = null;

        initialSplit = readLine.split("\\s+");  //разделя прочетеният ред на части
        //генерализиране на вида на точките на триангулачни или работни
        if(Integer.parseInt(initialSplit[1])>=2 && Integer.parseInt(initialSplit[1])<=8){
            tempX = GlobalsLists.CoordStart.X+Double.parseDouble(initialSplit[3]);
            tempY = GlobalsLists.CoordStart.Y + Double.parseDouble(initialSplit[4]);
            tempH = Double.parseDouble(initialSplit[5]);
            tempBL = CoordTransform.ToWGS(tempX,tempY);
            tempCadPoint = new CadPoint(0,tempX,tempY,initialSplit[2],"тт",tempBL.B,tempBL.L,tempH);
        }
        else if(Integer.parseInt(initialSplit[1])>=9 && Integer.parseInt(initialSplit[1])<=13){
            tempX = GlobalsLists.CoordStart.X+Double.parseDouble(initialSplit[3]);
            tempY = GlobalsLists.CoordStart.Y + Double.parseDouble(initialSplit[4]);
            tempBL = CoordTransform.ToWGS(tempX,tempY);
            tempH = Double.parseDouble(initialSplit[5]);
            tempCadPoint = new CadPoint(0,tempX,tempY,initialSplit[2],"рт",tempBL.B,tempBL.L,tempH);
        }
        if(tempCadPoint!=null){
            GlobalsLists.GlobalPoints.add(tempCadPoint);
        }
        return i;
    }
}
