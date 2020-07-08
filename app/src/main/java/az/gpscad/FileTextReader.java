package az.gpscad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTextReader {
    public static List<String> FileTextReader(String inputFilePath){
        List<String> output = new ArrayList<>();
        File inputFile = new File(inputFilePath);

        try {

            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }

        /*
        try {
            // open file to read
            Scanner scanner = new Scanner(inputFile);



            // read until end of file (EOF)
            while (scanner.hasNextLine()) {
                //System.out.println(scanner.nextLine());
                output.add(new String(scanner.nextLine()));
            }

            // close the scanner
            scanner.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        */

        return output;
    }
}
