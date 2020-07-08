package az.gpscad;


public class CoordTransform {


    public static XYCoordinate FromWGS(double Longitute, double Latitute){
        XYCoordinate output = new XYCoordinate();

        double a,alfa, e, e2;				// Константи на елипсоида
        double L0 = 25.5;
        double B1 = 42.0;
        double B2 = 43.3333333333;
        double Y0 = 500000.0;
    	a = 6378137.;
    	alfa = 1/298.257222101;
        e2 = .00669438002290;
        e = Math.sqrt(e2);

        L0 = L0*Math.PI/180;
        B1 = B1*Math.PI/180;
        B2 = B2*Math.PI/180;
        double B = Longitute*Math.PI/180;
        double L = Latitute*Math.PI/180;
        double w1, w2, w0, Q1, Q2, Q0;
        w1 = Math.sqrt(1 - e2*Math.pow(Math.sin(B1),2) );
        w2 = Math.sqrt(1 - e2*Math.pow(Math.sin(B2),2) );
        Q1 = (Math.log((1. + Math.sin(B1))/(1.-Math.sin(B1))) - e*Math.log((1. + e*Math.sin(B1))/(1.-e*Math.sin(B1))))/2;	// изометричната ширина
        Q2 = (Math.log((1. + Math.sin(B2))/(1.-Math.sin(B2))) - e*Math.log((1. + e*Math.sin(B2))/(1.-e*Math.sin(B2))))/2;	// изометричната ширина
        double B0 = Math.asin(Math.log(w2*Math.cos(B1)/w1/Math.cos(B2))/(Q2 - Q1));	//
        Q0 = (Math.log((1. + Math.sin(B0))/(1.-Math.sin(B0))) - e*Math.log((1. + e*Math.sin(B0))/(1.-e*Math.sin(B0))))/2;	// ширина на централния паралел
        w0 = Math.sqrt(1 - e2*Math.pow(Math.sin(B0),2) );
        double Re;		// радиус на образа на екватора, приет за начало на ширините
        Re = a*Math.cos(B1)*Math.exp(Q1*Math.sin(B0))/w1/Math.sin(B0);
        double R0;		// радиус на образа на централния паралел
        R0 = Re/Math.exp(Q0*Math.sin(B0));
        double m0, m2, m4, m6, m8;
        m0 = a*(1-e2);
        m2 = 1.5*e2*m0;
        m4 = 1.25*e2*m2;
        m6 = 7./6.*e2*m4;
        m8 = 1.125*e2*m6;
        double a0, a2, a4, a6;
        a0 = m0 + 0.5*m2 + 0.375*m4 + 0.3125*m6 + 0.2734375*m8;
        a2 = 0.5*m2 + 0.5*m4 + 0.46875*m6 + 0.4375*m8;
        a4 = 0.125*m4 + 0.1875*m6 + 0.21875*m8;
        a6 = 0.03125*m6 + 0.0625*m8;
        double x0;						// абциса на централната точка на проекцията
        x0 = a0*B0 - Math.sin(B0)*Math.cos(B0)*(a2 - a4 + a6 +(2*a4-16*a6/3)*Math.pow(Math.sin(B0),2) + 16*a6*Math.pow(Math.sin(B0),4)/3);
        double R;
        Q0 = (Math.log((1. + Math.sin(B))/(1.-Math.sin(B))) - e*Math.log((1. + e*Math.sin(B))/(1.-e*Math.sin(B))))/2;	// ширина на дадения паралел
        R = Re/Math.exp(Q0*Math.sin(B0));			// определяне на радиуса на образа на текущия паралел
        m0 = (L - L0)*Math.sin(B0);			// определяне на меридианната конвергенция
        output.X = R0 + x0 - R*Math.cos(m0);		// Редукция + север
        output.Y = Y0 + R*Math.sin(m0);				// Редукция + изток


        return output;
    }

    public static BLCoordinate ToWGS(double X, double Y) {
        BLCoordinate output = new BLCoordinate();

        double a,alfa, e, e2;				// Константи на елипсоида
        double L0 = 25.5;
        double B1 = 42.0;
        double B2 = 43.333333333;
        double Y0 = 500000.0;
        a = 6378137.;
        alfa = 1/298.257222101;
        e2 = .00669438002290;
        e = Math.sqrt(e2);


        L0 = L0*Math.PI/180;
        B1 = B1*Math.PI/180;
        B2 = B2*Math.PI/180;
        double B;						// координата X-север, в метрична мярка
        double L;						// координата Y-изток, в метрична мярка
        double w1, w2, w0, Q1, Q2, Q0;
        w1 = Math.sqrt(1 - e2*Math.pow(Math.sin(B1),2) );
        w2 = Math.sqrt(1 - e2*Math.pow(Math.sin(B2),2) );
        Q1 = (Math.log((1. + Math.sin(B1))/(1.-Math.sin(B1))) - e*Math.log((1. + e*Math.sin(B1))/(1.-e*Math.sin(B1))))/2;	  // изометричната ширина
        Q2 = (Math.log((1. + Math.sin(B2))/(1.-Math.sin(B2))) - e*Math.log((1. + e*Math.sin(B2))/(1.-e*Math.sin(B2))))/2;	  // изометричната ширина
        double B0 = Math.asin(Math.log(w2*Math.cos(B1)/w1/Math.cos(B2))/(Q2 - Q1));
        Q0 = (Math.log((1. + Math.sin(B0))/(1.-Math.sin(B0))) - e*Math.log((1. + e*Math.sin(B0))/(1.-e*Math.sin(B0))))/2;	// ширина на централния паралел
        w0 = Math.sqrt(1 - e2*Math.pow(Math.sin(B0),2) );
        double Re; // радиус на образа на екватора, приет за начало на ширините
        Re = a*Math.cos(B1)*Math.exp(Q1*Math.sin(B0))/w1/Math.sin(B0);
        double R0;	// радиус на образа на централния паралел
        R0 = Re/Math.exp(Q0*Math.sin(B0));
        double m0, m2, m4, m6, m8;
        m0 = a*(1-e2);
        m2 = 1.5*e2*m0;
        m4 = 1.25*e2*m2;
        m6 = 7./6.*e2*m4;
        m8 = 1.125*e2*m6;
        double a0, a2, a4, a6;
        a0 = m0 + 0.5*m2 + 0.375*m4 + 0.3125*m6 + 0.2734375*m8;
        a2 = 0.5*m2 + 0.5*m4 + 0.46875*m6 + 0.4375*m8;
        a4 = 0.125*m4 + 0.1875*m6 + 0.21875*m8;
        a6 = 0.03125*m6 + 0.0625*m8;
        double x0;
        x0 = a0*B0 - Math.sin(B0)*Math.cos(B0)*(a2 - a4 + a6 +(2*a4-16*a6/3)*Math.pow(Math.sin(B0),2) + 16*a6*Math.pow(Math.sin(B0),4)/3);
        double R;
        R = Math.sqrt(Math.pow((Y - Y0), 2) + Math.pow((R0 + x0 - X),2));
        Q0 = Math.log(Re/R)/Math.sin(B0);
        m0 = Math.asin((Math.exp(2*Q0)-1)/(Math.exp(2*Q0)+1));
        B = 0;
        while (Math.abs(B - m0) > 1.0e-10)				// Итерация за определяна на ширината
        {
            m0 = B;
            w1 = (Math.log((1. + Math.sin(m0))/(1.-Math.sin(m0))) - e*Math.log((1. + e*Math.sin(m0))/(1.-e*Math.sin(m0))))/2 - Q0;
            w2 = 1/(1-Math.pow(Math.sin(m0),2)) - e2/(1-e2*Math.pow(Math.sin(m0),2));
            B = Math.asin(Math.sin(m0) - w1/w2);				// Географска ширина в РАДИАНИ
        }
        m0 = Math.atan((Y - Y0)/(R0 + x0 - X));
        L = m0/Math.sin(B0) + L0;						// географска дължина в РАДИАНИ
        B = B*180/Math.PI;								// Географска ширина в ГРАДУСНА мярка!
        L = L*180/Math.PI;								// Географска дължина в градуси

        output.B=B;
        output.L=L;

        return output;
    }
}
