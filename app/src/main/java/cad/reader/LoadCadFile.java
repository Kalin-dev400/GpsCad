package cad.reader;

import java.util.ArrayList;
import java.util.List;

import az.gpscad.GlobalsLists;
import cad.formats.CadContour;
import cad.formats.CadLine;

public class LoadCadFile {
    public static void LoadCadFile(List<String> inputFile){
        String[] temp;
        for (int i=0;i<inputFile.size();i++)
        {
            if(inputFile.get(i).contains("HEADER")){
                i++;
                while(!inputFile.get(i).contains("END_HEADER")){
                    if(inputFile.get(i).contains("EKATTE")){
                        temp = inputFile.get(i).split("\\s+");
                        GlobalsLists.EKATTE = temp[1];
                    }
                    else if(inputFile.get(i).contains("REFERENCE")){
                        temp = inputFile.get(i).split("\\s+");
                        GlobalsLists.CoordStart.X = Double.valueOf(temp[1]);
                        GlobalsLists.CoordStart.Y = Double.valueOf(temp[2]);
                    }
                    else if(inputFile.get(i).contains("COORDTYPE")){
                        if (!inputFile.get(i).contains("2005")){
                            return;
                        }
                    }
                    i++;
                }
            }
            if(inputFile.get(i).contains("LAYER CADASTER")){
                i++;
                while(!inputFile.get(i).contains("END_LAYER")){
                    if (inputFile.get(i).contains("L "))
                    {
                        i = cad.reader.Lines.Line(inputFile,i);
                    }
                    else if(inputFile.get(i).contains("C ")){
                        i = cad.reader.Polygons.Contours(inputFile,i);
                    }
                    else if(inputFile.get(i).contains("P ")){
                        i = cad.reader.Points.Point(inputFile,i);
                    }
                    i++;
                }
            }
        }

        List<CadLine> TempLinesUnused = new ArrayList<>();
        List<Integer> LinesToBeDrawn = new ArrayList<>();

        for (CadContour contour:GlobalsLists.GlobalCountours) {
            for (Integer lineunique:contour.Lines) {
                if (!LinesToBeDrawn.contains(lineunique)){
                    LinesToBeDrawn.add(lineunique);
                }
            }
        }

        for (CadLine line:GlobalsLists.GlobalLines) {
            if(!LinesToBeDrawn.contains(line.uniqueN)){
                TempLinesUnused.add(line);
            }
        }

        GlobalsLists.GlobalLines = TempLinesUnused;
    }
}
