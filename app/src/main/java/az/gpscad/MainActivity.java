package az.gpscad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cad.formats.CadContour;
import cad.formats.CadFile;
import cad.formats.CadLine;
import cad.formats.CadPoint;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MULTIPLE_PERMISSION_REQUEST_CODE = 6;
    private static final String TAG = "2";
    private static final int REQUEST_CODE = 6384;
    private MapView mapView;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    NumberFormat CoordFormat = new DecimalFormat("#0.000");
    public List<cad.formats.CadFile> FoundCadFiles = new ArrayList<>();
    private List<CadFile> input;
    private CadFile SelectedCadFile;
    private int markersCount;
    private List<Integer> markersText = new ArrayList<>();
    private int compassOverlayInt;
    private GeoPoint lastPathLocation;
    private double pathDistance;
    private boolean followActivated = true;
    private int followOverlayPosition;
    private List<CadPoint> pointsFromFile = new ArrayList<>();
    private boolean initialCadLoadZoom = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        checkPermissionsState();
        final ListView listView = (ListView) findViewById(R.id.CadFileListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                Iterator<CadFile> iterator = FoundCadFiles.iterator();
                while (iterator.hasNext()) {
                    CadFile cadfile = iterator.next();
                    if (cadfile.Name.equals(selectedItem)) {
                        SelectedCadFile = cadfile;
                        break;
                    }
                }
                if (SelectedCadFile != null){
                    GlobalsLists.GlobalCountours.clear();
                    GlobalsLists.GlobalLines.clear();
                    GlobalsLists.CoordStart = new XYCoordinate();
                    GlobalsLists.GlobalPoints.clear();
                    GlobalsLists.EKATTE = "";

                    TextView CoordText = (TextView) findViewById(R.id.textView);
                    TextView DistanceText = (TextView) findViewById(R.id.textViewDistance);
                    GlobalsLists.InputFileArray = FileTextReader.FileTextReader(SelectedCadFile.Path);
                    cad.reader.LoadCadFile.LoadCadFile(GlobalsLists.InputFileArray);
                    initialCadLoadZoom = true;
                    DrawCadFile();
                    CoordText.setText("Зареден файл: " + SelectedCadFile.Name);
                    listView.setVisibility(View.GONE);
                    mapView.setVisibility(View.VISIBLE);
                    CoordText.setVisibility(View.VISIBLE);
                    DistanceText.setVisibility(View.VISIBLE);

                }
                // Display the selected item text on TextView
            }
        });

        GridLayout InputPointLayout = (GridLayout) findViewById(R.id.input_NewPointLayout);

        Button InputClose = (Button)findViewById(R.id.input_close);
        InputClose.setOnClickListener(this);

        Button InputCreate = (Button)findViewById(R.id.input_create);
        InputCreate.setOnClickListener((View.OnClickListener) this);

        Spinner InputSpinner = findViewById(R.id.pointTypeSpinner);
        addItemsOnSpinner(InputSpinner);

    }

    private void checkPermissionsState() {
        int internetPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int networkStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);

        int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int wifiStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE);
        int readExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);

        if (internetPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                networkStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                wifiStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                readExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED){

            setupMap();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    MULTIPLE_PERMISSION_REQUEST_CODE);
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean somePermissionWasDenied = false;
                    for (int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            somePermissionWasDenied = true;
                        }
                    }
                    if (somePermissionWasDenied) {
                        Toast.makeText(this, "Cant load maps without all the permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        setupMap();
                    }
                } else {
                    Toast.makeText(this, "Cant load maps without all the permissions granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void setupMap() {

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);

        mapView.setHorizontalScrollBarEnabled(true);
        mapView.setVerticalScrollBarEnabled(true);
        //mapView.getProjection();
        //setContentView(mapView); //displaying the MapView

        mapView.getController().setZoom(12.0); //set initial zoom-level, depends on your need
        //mapView.getController().setCenter(ONCATIVO);
        mapView.setUseDataConnection(true); //keeps the mapView from loading online tiles using network connection.
        mapView.setMaxZoomLevel(23.);
        mapView.setMinZoomLevel(2.);
        mapView.setTileSource(TileSourceFactory.OpenTopo);

        mapView.setMultiTouchControls(true);

        mapView.setBuiltInZoomControls(false);

        readPointsFile();


        final GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(MainActivity.this.getBaseContext());

        gpsMyLocationProvider.setLocationUpdateMinDistance(3); // [m]  // Set the minimum distance for location updates
        //gpsMyLocationProvider.setLocationUpdateMinTime(100);   // [ms] // Set the minimum time interval for location updates


        MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider,mapView);

        //oMapLocationOverlay.enableFollowLocation();

        if(followActivated){
            oMapLocationOverlay.enableMyLocation();
            oMapLocationOverlay.enableFollowLocation();
        }
        else{
            oMapLocationOverlay.disableMyLocation();
            oMapLocationOverlay.disableFollowLocation();
        }

        mapView.getOverlays().add(oMapLocationOverlay);
        markersCount++;
        followOverlayPosition = markersCount-1;



        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.setPointerMode(true);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
        markersCount++;
        compassOverlayInt = markersCount-1;



        if(pointsFromFile.size()>0){
            for (CadPoint tempCadPoint:pointsFromFile) {
                Marker filePoint = new Marker(mapView);
                filePoint.setPosition(new GeoPoint(tempCadPoint.B,tempCadPoint.L));
                filePoint.setInfoWindow(new CustomInfoWindow(mapView,tempCadPoint));
                if(tempCadPoint.Kod.equals("тт")){
                    filePoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangulachna, null));
                }
                else if(tempCadPoint.Kod.equals("рт")){
                    filePoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rabotna, null));
                }
                else{
                    filePoint.setTextIcon(tempCadPoint.Nomer);
                }
                filePoint.setTitle(tempCadPoint.Kod + " " + tempCadPoint.Nomer + "\n\r X: " + tempCadPoint.X + "\n\r Y: " + tempCadPoint.Y + "\n\rH: " + tempCadPoint.H);
                markersCount++;
                mapView.getOverlays().add(filePoint);
                // markersText.add(markersCount-1);
            }
        }


        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);
        markersCount++;

    //    final CadPoint tempCadPoint = new CadPoint(1,54345.544,5432321.432,"21","тт",42.759390,23.433422);

  /*      Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(tempCadPoint.B,tempCadPoint.L));
        marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_astronomichna, null));
        marker.setInfoWindow(new CustomInfoWindow(mapView,tempCadPoint));
        marker.setTitle("21\n\r X: " + tempCadPoint.X + "\n\r Y: " + tempCadPoint.Y);

        mapView.getOverlayManager().add(marker);

        Marker marker2 = new Marker(mapView);
        marker2.setPosition(new GeoPoint(42.759290, 23.433322));
        marker2.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangulachna, null));

        mapView.getOverlayManager().add(marker2);

        RoadManager roadManager = new OSRMRoadManager(this);
*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();//Strict mode for ignore NetworkOnMainExceptions (!for anytime, have to put in threading)
        StrictMode.setThreadPolicy(policy);



        mapView.setMapListener(new DelayedMapListener(new MapListener() {
            public boolean onZoom(final ZoomEvent e) {
                MapView mapView = (MapView) findViewById(R.id.mapview);

                String latitudeStr = "" + mapView.getMapCenter().getLatitude();
                String longitudeStr = "" + mapView.getMapCenter().getLongitude();
                //latitudeStr = "" + gpsMyLocationProvider.getLastKnownLocation().getLatitude();

                String latitudeFormattedStr = latitudeStr.substring(0, Math.min(latitudeStr.length(), 7));
                String longitudeFormattedStr = longitudeStr.substring(0, Math.min(longitudeStr.length(), 7));


                XYCoordinate CurrentLocationXY = new XYCoordinate();
                CurrentLocationXY = CoordTransform.FromWGS(gpsMyLocationProvider.getLastKnownLocation().getLatitude(),gpsMyLocationProvider.getLastKnownLocation().getLongitude());

                //Log.i("zoom", "" + mapView.getMapCenter().getLatitude() + ", " + mapView.getMapCenter().getLongitude());
                TextView latLongTv = (TextView) findViewById(R.id.textView);
                //latLongTv.setText("" + latitudeFormattedStr + ", " + longitudeFormattedStr);
                latLongTv.setText("" + CoordFormat.format(CurrentLocationXY.X) + "\n  " + CoordFormat.format(CurrentLocationXY.Y));

                return true;
            }

            public boolean onScroll(final ScrollEvent e) {
                MapView mapView = (MapView) findViewById(R.id.mapview);

                String latitudeStr = "" + mapView.getMapCenter().getLatitude();
                String longitudeStr = "" + mapView.getMapCenter().getLongitude();

                XYCoordinate CurrentLocationXY = new XYCoordinate();
                try {
                    CurrentLocationXY = CoordTransform.FromWGS(gpsMyLocationProvider.getLastKnownLocation().getLatitude(),gpsMyLocationProvider.getLastKnownLocation().getLongitude());
                    GeoPoint CurrentLocationBL = new GeoPoint(gpsMyLocationProvider.getLastKnownLocation().getLatitude(),gpsMyLocationProvider.getLastKnownLocation().getLongitude());

                    String latitudeFormattedStr = latitudeStr.substring(0, Math.min(latitudeStr.length(), 7));
                    String longitudeFormattedStr = longitudeStr.substring(0, Math.min(longitudeStr.length(), 7));

                    //Log.i("scroll", "" + mapView.getMapCenter().getLatitude() + ", " + mapView.getMapCenter().getLongitude());
                    TextView latLongTv = (TextView) findViewById(R.id.textView);
                    //latLongTv.setText("" + latitudeFormattedStr + ", " + longitudeFormattedStr);
                    latLongTv.setText("" + CoordFormat.format(CurrentLocationXY.X) + "\n  " + CoordFormat.format(CurrentLocationXY.Y));

                    if(lastPathLocation!=null && CalculateDistance.CalculateDistance(CurrentLocationBL,lastPathLocation)>20.0){
                        GlobalsLists.UpdatePath = true;
                    }
                    else if(lastPathLocation!=null && pathDistance>0 && pathDistance<10.0){
                        GlobalsLists.UpdatePath = true;
                    }

                    if (GlobalsLists.UpdatePath==true){
                        DrawPath(GlobalsLists.PathEndPoint, CurrentLocationBL);
                        GlobalsLists.UpdatePath = false;
                    }

                    if(mapView.getZoomLevelDouble()<18.0 && markersText.size()>0){
                        for (int markerint:markersText) {
                            mapView.getOverlayManager().overlays().get(markerint).setEnabled(false);
                        }
                    }
                    else if(mapView.getZoomLevelDouble()>=18.0 && markersText.size()>0){
                        for (int markerint:markersText) {
                            mapView.getOverlayManager().overlays().get(markerint).setEnabled(true);
                        }
                    }
                    if(followActivated){
                        setCenterInMyCurrentLocation();
                    }
                }
                catch(Exception ex){

                }
                return true;
            }

        }, 1200));
    }



    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setCenterInMyCurrentLocation() {
        if (mLastLocation != null) {
            mapView.getController().setCenter(new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Локализиране...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            /*
            String language ="bg";
            Locale locale = null;
            if (language.equalsIgnoreCase("en")) {
                locale = new Locale("en");
            } else if (language.equalsIgnoreCase("bg")) {
                locale = new Locale("bg");
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
            return true;

             */

        } else if (id == R.id.action_locate) {

            final GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(MainActivity.this.getBaseContext());

            //MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider,mapView);

            MyLocationNewOverlay oMapLocationOverlay = (MyLocationNewOverlay) mapView.getOverlayManager().get(followOverlayPosition);
            CompassOverlay compass = (CompassOverlay) mapView.getOverlayManager().get(compassOverlayInt);




            if(followActivated){
                followActivated = false;
                oMapLocationOverlay.disableFollowLocation();
                oMapLocationOverlay.disableMyLocation();
                oMapLocationOverlay.setDrawAccuracyEnabled(false);
                Toast.makeText(this, "Локализирането е спряно", Toast.LENGTH_SHORT).show();
                compass.disableCompass();
                mapView.getOverlayManager().set(compassOverlayInt,compass);
            }
            else{
                followActivated = true;
                oMapLocationOverlay.enableFollowLocation();
                Toast.makeText(this, "Локализирането е пуснато", Toast.LENGTH_SHORT).show();
                oMapLocationOverlay.enableMyLocation();
                oMapLocationOverlay.setDrawAccuracyEnabled(true);
                mapView.getOverlayManager().set(followOverlayPosition,oMapLocationOverlay);
                setCenterInMyCurrentLocation();
                compass.enableCompass();
                mapView.getOverlayManager().set(compassOverlayInt,compass);
                item.setChecked(true);
            }

        }
        else if(id == R.id.action_maptile){
            if(item.isChecked()){
                mapView.setTileSource(TileSourceFactory.ChartbundleENRH);
                item.setChecked(false);
            }
            else{
                mapView.setTileSource(TileSourceFactory.OpenTopo);
                item.setChecked(true);
            }
        }
        else if (id== R.id.action_openfile){
            FoundCadFiles.clear();
            LoopThroughFolder(Environment.getExternalStoragePublicDirectory(""));

            if(FoundCadFiles.size()>0) {

                TextView CoordText = (TextView) findViewById(R.id.textView);
                ListView listView = (ListView) findViewById(R.id.CadFileListView);
                TextView DistanceText = (TextView) findViewById(R.id.textViewDistance);
                if (listView.getVisibility() == View.GONE) {
                    listView.setVisibility(View.VISIBLE);
                    mapView.setVisibility(View.GONE);
                    CoordText.setVisibility(View.GONE);
                    DistanceText.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.GONE);
                    mapView.setVisibility(View.VISIBLE);
                    CoordText.setVisibility(View.VISIBLE);
                    DistanceText.setVisibility(View.VISIBLE);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, compileFileList(FoundCadFiles));
                listView.setAdapter(arrayAdapter);
            }
            else{
                Toast.makeText(this, "Няма открити файлове", Toast.LENGTH_SHORT).show();
            }

        }
        else if (id==R.id.action_openInput){
            GridLayout InputLayout = (GridLayout) findViewById(R.id.input_NewPointLayout);

            if (InputLayout.getVisibility() == View.GONE){
                InputLayout.setVisibility(View.VISIBLE);
            }
            else{
                InputLayout.setVisibility(View.GONE);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void LoopThroughFolder (File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    LoopThroughFolder(file);
                } else {
                    if (file.getName().contains(".cad")){
                        FoundCadFiles.add(new CadFile(file.getName(),file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    private ArrayList<String> compileFileList(List<CadFile> input) {

        ArrayList<String> output = new ArrayList<>();
        final int max = input.size();
        for (int i=0; i<max; i++) {
            output.add(i, input.get(i).Name);
        }
        return output;
    }

    private void DrawCadFile(){
        markersCount = 0;
        markersText.clear();

        double maxB=0.0;
        double maxL=0.0;
        double minB=100.0;
        double minL=100.0;

        mapView.getOverlayManager().clear();

        for (CadLine pline:GlobalsLists.GlobalLines) {

            Polyline line = new Polyline();   //see note below!
            line.setPoints(pline.Points);
            line.setWidth(5);
        /*    line.setOnClickListener(new Polyline.OnClickListener() {
                @Override
                public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                    Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                    return false;
                }
            });  */
            mapView.getOverlayManager().add(line);
            markersCount++;
        }
        for (CadContour contour: GlobalsLists.GlobalCountours) {
            Polygon polygon = new Polygon();    //see note below
            switch(contour.Type){
                case "ПИ":
                    polygon.setFillColor(Color.argb(50,170,170,170));
                    //polygon; //set fill color
                    break;
                case "Сграда":
                    polygon.setFillColor(Color.parseColor("#1EFFE70E")); //set fill color
                    break;
                default:
                    polygon.setFillColor(Color.parseColor("#1ffffff")); //set fill color
                    break;
            }
            XYCoordinate tempXYtextPoint = CoordTransform.FromWGS(contour.TextPoint.getLongitude(),contour.TextPoint.getLatitude());

            CadPoint tempGeoPoint = new CadPoint(Integer.valueOf(contour.IdentText),
                    tempXYtextPoint.X,tempXYtextPoint.Y,contour.identificator,contour.Type,
                    contour.TextPoint.getLatitude(),contour.TextPoint.getLongitude(),0);

            polygon.setPoints(contour.Points);
            polygon.setTitle(contour.identificator);
            polygon.setStrokeWidth(5);
            mapView.getOverlayManager().add(polygon);
            markersCount++;

            Marker ptext = new Marker(mapView);
            ptext.setPosition(contour.TextPoint);
            ptext.setTextLabelBackgroundColor(
                    Color.TRANSPARENT
            );
            ptext.setTextLabelForegroundColor(
                    Color.BLACK
            );
            ptext.setTextLabelFontSize(40);
            ptext.setInfoWindow(new CustomInfoWindow(mapView,tempGeoPoint));
            ptext.setTitle(tempGeoPoint.Kod + ": " + tempGeoPoint.Nomer);
            ptext.setDraggable(false);
            ptext.setIcon(null);
            ptext.setTextIcon(contour.IdentText);
            ptext.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            if(maxB<tempGeoPoint.B){
                maxB = tempGeoPoint.B;
            }
            if(minB>tempGeoPoint.B){
                minB=tempGeoPoint.B;
            }

            if(maxL<tempGeoPoint.L){
                maxL = tempGeoPoint.L;
            }
            if(minL>tempGeoPoint.L){
                minL = tempGeoPoint.L;
            }

            mapView.getOverlays().add(ptext);
            markersCount++;
            markersText.add(markersCount-1);
        }

        for (CadPoint point:GlobalsLists.GlobalPoints){
            Marker pPoint = new Marker(mapView);
            pPoint.setPosition(new GeoPoint(point.B,point.L));
            pPoint.setInfoWindow(new CustomInfoWindow(mapView,point));
            pPoint.setTitle(point.Kod + " " + point.Nomer+"\n\rX: " + point.X + "\n\rY: " + point.Y + "\n\rH: " + point.H);
            pPoint.setDraggable(false);
            if(point.Kod.equals("тт")){
                pPoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangulachna, null));
            }
            else if(point.Kod.equals("рт")){
                pPoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rabotna, null));
            }
            else{
                pPoint.setTextIcon(point.Nomer);
            }
            pPoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            mapView.getOverlays().add(pPoint);
            markersCount++;
        }


        if(pointsFromFile.size()>0){
            for (CadPoint tempCadPoint:pointsFromFile) {
                Marker filePoint = new Marker(mapView);
                filePoint.setPosition(new GeoPoint(tempCadPoint.B,tempCadPoint.L));
                filePoint.setInfoWindow(new CustomInfoWindow(mapView,tempCadPoint));
                if(tempCadPoint.Kod.equals("тт")){
                    filePoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangulachna, null));
                }
                else if(tempCadPoint.Kod.equals("рт")){
                    filePoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rabotna, null));
                }
                else{
                    filePoint.setTextIcon(tempCadPoint.Nomer);
                }
                filePoint.setTitle(tempCadPoint.Kod + " " + tempCadPoint.Nomer + "\n\r X: " + tempCadPoint.X +
                        "\n\r Y: " + tempCadPoint.Y + "\n\rH: " + tempCadPoint.H);
                filePoint.setDraggable(false);
                filePoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                mapView.getOverlays().add(filePoint);
                markersCount++;
                // markersText.add(markersCount-1);
            }
        }

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(MainActivity.this.getBaseContext());
        //gpsMyLocationProvider.setLocationUpdateMinDistance(0); // [m]  // Set the minimum distance for location updates
        //gpsMyLocationProvider.setLocationUpdateMinTime(100);   // [ms] // Set the minimum time interval for location updates

        MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider,mapView);
        //oMapLocationOverlay.enableFollowLocation();

        if(followActivated){
            oMapLocationOverlay.enableMyLocation();
            oMapLocationOverlay.enableFollowLocation();
            oMapLocationOverlay.setDrawAccuracyEnabled(true);
        }
        else{
            oMapLocationOverlay.disableMyLocation();
            oMapLocationOverlay.disableFollowLocation();
            oMapLocationOverlay.setDrawAccuracyEnabled(false);
        }
        mapView.getOverlays().add(oMapLocationOverlay);
        markersCount++;
        followOverlayPosition = markersCount-1;


        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.setPointerMode(true);

        if(followActivated){
            compassOverlay.enableCompass();
        }
        else{
            compassOverlay.disableCompass();
        }

        //compassOverlay.mOrientationProvider.stopOrientationProvider();
        mapView.getOverlays().add(compassOverlay);
        markersCount++;
        compassOverlayInt = markersCount-1;



        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);
        markersCount++;

        if(minB != 100 && minL !=100 & maxB!=0 && maxL!=0 && initialCadLoadZoom == true){
            List<GeoPoint> bounds = new ArrayList<>();

            bounds.add(new GeoPoint(minB-.0007,minL-.0007));
            bounds.add(new GeoPoint(maxB+.0007,maxL+.0007));

            final BoundingBox boundingBox = BoundingBox.fromGeoPoints(bounds);

            mapView.zoomToBoundingBox(boundingBox,true);
            initialCadLoadZoom = false;
        }


    }

    public void DrawPath(final GeoPoint endPoint, final GeoPoint myLocation){
        DrawCadFile();
        final RoadManager roadManager = new OSRMRoadManager(this);

        final ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(); //Build up list of geopoints
        waypoints.add(myLocation);
        waypoints.add(endPoint);
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);// draw route
        roadOverlay.setColor(Color.rgb(0, 0, 255));
        roadOverlay.setWidth(10);
        roadOverlay.getPaint().setStrokeCap(Paint.Cap.ROUND);

        //проверка за грешки
        if (road.mStatus == Road.STATUS_TECHNICAL_ISSUE) {
            Toast.makeText(this, "Технически проблем при изграждане на маршрут", Toast.LENGTH_SHORT).show();
        } else if (road.mStatus > Road.STATUS_TECHNICAL_ISSUE) { //functional issues
            Toast.makeText(this, "Не е възможен маршрут", Toast.LENGTH_SHORT).show();
        }
        TextView DistanceText = (TextView) findViewById(R.id.textViewDistance);
        //проверка за дължина на пътя
        //решава дали да използва маршрут или директна линия
        if (roadOverlay.getDistance() < 100 || CalculateDistance.CalculateDistance(new GeoPoint(myLocation.getLongitude(), myLocation.getLatitude()), endPoint) <= 100.0) {
            Polyline line = new Polyline();
            line.setPoints(waypoints);
            line.setColor(Color.rgb(0, 0, 255));
            line.setWidth(10);
            line.getPaint().setStrokeCap(Paint.Cap.ROUND);
            mapView.getOverlayManager().add(line);
            pathDistance = line.getDistance();
        } else {
            mapView.getOverlays().add(roadOverlay);
            pathDistance = roadOverlay.getDistance();
        }
        DistanceText.setText("" + CoordFormat.format(pathDistance) + " m");
        lastPathLocation = myLocation;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.input_close: {
                GridLayout InputPointLayout = findViewById(R.id.input_NewPointLayout);
                InputPointLayout.setVisibility(View.GONE);
                break;
            }
            case R.id.input_create: {
                EditText InputTextX = findViewById(R.id.inputPointX);
                EditText InputTextY = findViewById(R.id.inputPointY);
                EditText InputTextN = findViewById(R.id.inputPointNumber);
                EditText InputTextH = findViewById(R.id.inputPointH);
                Spinner InputSpinner = findViewById(R.id.pointTypeSpinner);
                boolean create = true;

                double inputX = 0.0;double inputY=0.0; String inputNumber="";String inputType="";
                double inputH = 0.0;

                try{
                inputX = Double.parseDouble(InputTextX.getText().toString());
                }
                catch (Exception ex){
                }
                try{
                    inputY = Double.parseDouble(InputTextY.getText().toString());
                }
                catch (Exception ex){
                }
                try{
                    inputH = Double.parseDouble(InputTextH.getText().toString());
                    inputNumber = InputTextN.getText().toString();
                    inputType = InputSpinner.getSelectedItem().toString();
                }
                catch (Exception ex){
                }
                try{
                    inputH = Double.parseDouble(InputTextH.getText().toString());
                }
                catch (Exception ex){
                }
                try{
                    inputNumber = InputTextN.getText().toString();
                }
                catch (Exception ex){
                }
                try{
                    inputNumber = InputTextN.getText().toString();
                }
                catch (Exception ex){
                }

                if(inputX > 4950000.0 || inputX < 4550000.0){
                    create = false;
                }

                if(inputY > 755000.0 || inputY < 239000.0){
                    create = false;
                }

                if(inputNumber.trim().equals("")){
                    create = false;
                }

                BLCoordinate tempBL = CoordTransform.ToWGS(inputX,inputY);

                if(create == true){
                    CadPoint inputCadPoint = new CadPoint(markersCount+1,inputX,inputY,inputNumber,inputType,tempBL.B,tempBL.L,inputH);

                    Marker InputPoint = new Marker(mapView);
                    InputPoint.setTitle(inputCadPoint.Kod + ": " + inputCadPoint.Nomer + "\n\rX: "
                                        + inputCadPoint.X + "\n\rY: " + inputCadPoint.Y + "\n\rH: " + inputCadPoint.H);

                   InputPoint.setPosition(new GeoPoint(inputCadPoint.B,inputCadPoint.L));

                   switch (inputCadPoint.Kod){
                       case "рт":
                           InputPoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rabotna, null));
                           break;
                       case "тт":
                           InputPoint.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_triangulachna, null));
                           break;
                   }
                    InputPoint.setInfoWindow(new CustomInfoWindow(mapView,inputCadPoint));
                    InputPoint.setDraggable(false);
                    InputPoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    mapView.getOverlayManager().add(InputPoint);

                    markersCount++;
                    pointsFromFile.add(inputCadPoint);

                    GridLayout InputPointLayout = findViewById(R.id.input_NewPointLayout);
                    InputPointLayout.setVisibility(View.GONE);
                    writePointsFile();
                    InputTextH.setText("");
                    InputTextN.setText("");
                    InputTextX.setText("");
                    InputTextY.setText("");
                }
                else{
                    Toast.makeText(this, "Невалидни данни", Toast.LENGTH_SHORT).show();
                }

                // do something for button 2 click
                break;
            }
        }
    }

    public void addItemsOnSpinner(Spinner InputSpinner) {

        List<String> list = new ArrayList<String>();
        list.add("рт");
        list.add("тт");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        InputSpinner.setAdapter(dataAdapter);

    }

    private void writePointsFile() {

        String sBody;

        StringBuffer buffer = new StringBuffer();

        for (CadPoint tempCadPoint:pointsFromFile) {
            buffer.append(tempCadPoint.Kod + " " + tempCadPoint.Nomer + " " + tempCadPoint.X + " " + tempCadPoint.Y + " " + tempCadPoint.H + "\n");
        }

        sBody = buffer.toString();

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "GPSCad");
            if (!root.exists()) {
                root.mkdirs();
            }
            File pointFile = new File(root, "PointsFile.txt");
            FileWriter writer = new FileWriter(pointFile);
            writer.write(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Запис..", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPointsFile(){
        pointsFromFile.clear();
        File root = new File(Environment.getExternalStorageDirectory(), "GPSCad");
        if (!root.exists()) {
            root.mkdirs();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(root, "PointsFile.txt")));
            String line;
            String[] temp;


            while ((line = br.readLine()) != null) {
                temp = line.split("\\s+");
                if(temp.length == 5){
                    BLCoordinate tempBLCoord = CoordTransform.ToWGS(Double.parseDouble(temp[2]),Double.parseDouble(temp[3]));
                    CadPoint tempCadPoint = new CadPoint(0,Double.parseDouble(temp[2]),Double.parseDouble(temp[3]),temp[1],temp[0],
                            tempBLCoord.B,tempBLCoord.L,Double.parseDouble(temp[4]));
                    pointsFromFile.add(tempCadPoint);
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

    }

}
