package cad.reader;

public class CheckContourType {
    public static String CheckContourType(int i){
        String result;

        switch(i) {
            case 1:
                result = "Кадастрален район";
                break;
            case 2:
                result = "ПИ";
                break;
            case 3:
                result = "Сграда";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }
}
