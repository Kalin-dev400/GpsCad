package az.gpscad;

import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.Button;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import cad.formats.CadPoint;

public class CustomInfoWindow extends MarkerInfoWindow {
    public CustomInfoWindow(final MapView mapView, final CadPoint cadPoint) {
        super(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, mapView);
        Button btn = (Button)(mView.findViewById(org.osmdroid.bonuspack.R.id.bubble_moreinfo));
        btn.setText(R.string.infowindow_trackbutton);
        btn.setBackground(null);
        btn.setTextSize(10);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GlobalsLists.PathEndPoint = new GeoPoint(cadPoint.B,cadPoint.L);
                GlobalsLists.UpdatePath = true;
                //Toast.makeText(view.getContext(),(cadPoint.Nomer +" X: "+ cadPoint.X + " Y: " + cadPoint.Y), Toast.LENGTH_LONG).show();
                CustomInfoWindow.closeAllInfoWindowsOn(mapView);
                mapView.scrollBy(1,1);
            }
        });
    }
}
