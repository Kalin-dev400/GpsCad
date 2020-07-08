package cad.formats;

public class CadPoint {
    public double X;
    public double Y;
    public int N;
    public String Nomer;
    public String Kod;
    public double B;
    public double L;
    public double H;

    public CadPoint(int N, double X, double Y, String Nomer,String Kod,double B,double L,double H) {
        this.N = N;
        this.X = X;
        this.Y = Y;
        this.Nomer = Nomer;
        this.Kod = Kod;
        this.B = B;
        this.L = L;
        this.H = H;
    }

}
