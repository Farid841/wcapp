package com.example.wcapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.HEREWeGoTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private String AUTH = "Basic " + Base64.encodeToString("hbella:bella7905Hb@".getBytes(), Base64.NO_WRAP);
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    okhttp3.Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", AUTH)
                            .build();
                    return chain.proceed(newRequest);
                }
            })
            .build();
    private SearchView searchView ;

    private MapView map_view;
    private MapView map;

    private ListView list ;
    private MaterialButton localiser;
    private MaterialButton add;
    private MaterialButton account;
    private EditText adressadd;
    AlertDialog alert_adresse;
    AlertDialog alert_itiniraire;
    private GeoPoint actual_location;


    private IMapController mapController;
    private LocationManager locationManager;

    private Context context;

    private MainActivity mainActivity;

    private ArrayList<GeoPoint> waypoints_iti = new ArrayList<GeoPoint>();

    private Boolean LoggedIn = false;

    private String id_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent_logged = getIntent();
        if(intent_logged.hasExtra("id_user")){

                LoggedIn = true;
                id_user = intent_logged.getStringExtra("id_user");
        }
        if(intent_logged.hasExtra("logout") ){
            LoggedIn = false;
        }
        if(!LoggedIn){
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialiser la vue de la carte

         localiser = findViewById(R.id.localiser);
         add = findViewById(R.id.add);
            account = findViewById(R.id.account);

            account.setOnClickListener(v -> {
              Intent intent = new Intent(MainActivity.this, Account.class);
              intent.putExtra("id_user",id_user);
                startActivity(intent);
            });
//
        map = findViewById(R.id.map);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        searchView = findViewById(R.id.searchview);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        Configuration.getInstance().setUserAgentValue(getPackageName());

        final ITileSource tileSource = TileSourceFactory.MAPNIK;

        map.setTileSource(tileSource);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        //Charger les données de l'API
        FetchData();


//         Obtenir le contrôleur de la carte et définir la position initiale et le niveau de zoom
        mapController = map.getController();
        mapController.setZoom(12.00d);
        GeoPoint startPoint = new GeoPoint(48.7109981, 2.168666);
        mapController.setCenter(startPoint);
        map.setTilesScaledToDpi(true);
        Geocoder geocoder = new Geocoder(MainActivity.this);


//         Initialiser le gestionnaire de localisation
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Demande de permission à l'utilisateur si nécessaire
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        } else {
            // Demander les mises à jour de localisation si la permission a été accordée
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 20, this);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 20, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 20, this);



        add.setOnClickListener((v -> {
            Modal();
        }));

        MainActivity that = this;
        localiser.setOnClickListener((v -> {
            Toast.makeText(this, "Localisation mis à jour", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, this);





        }));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {


                // Récupérer le texte saisi par l'utilisateur
                String searchString = newText.trim();
                Marker marker = new Marker(map);
                // Vérifier si la chaîne de caractères est vide
                if (TextUtils.isEmpty(searchString)) {
                    // Si la chaîne de caractères est vide, annuler la recherche et masquer le marqueur
                    marker.setVisible(false);
                    return false;
                }
                if (TextUtils.isEmpty(searchString)) {
                    // Si la chaîne de caractères est vide, vider l'ArrayAdapter
                    adapter.clear();
                    return false;
                }
                // Utiliser l'objet Geocoder pour effectuer une recherche d'adresse
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(searchString, 1);
                    if (addresses != null && addresses.size() > 0) {
                        // Si une adresse a été trouvée, récupérer les coordonnées de l'adresse
                        Address address = addresses.get(0);
                        for (Address address1 : addresses) {
                            adapter.add(address1.getAddressLine(0));
                        }
                        double latitude = address.getLatitude();
                        double longitude = address.getLongitude();

                        // Créer un objet LatLng pour stocker les coordonnées de l'adresse
                        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                        // Vérifier si le marqueur de l'adresse existe déjà
                        if (marker != null) {
                            // Si le marqueur existe déjà, mettre à jour sa position et le rendre visible
//                            map.getOverlays().clear();
//                            marker.setPosition(geoPoint);
//                            marker.setTitle("Adresse");
//                            map.getOverlays().add(marker);
//                            map.invalidate();
//                            mapController.setZoom(18d);
                        } else {
                            // Si le marqueur n'existe pas, créer un nouveau marqueur et l'ajouter à la carte
//                            map.getOverlays().clear();
//                            marker.setPosition(geoPoint);
//                            marker.setTitle("Adresse");
//                            map.getOverlays().add(marker);
//                            map.invalidate();
//                            mapController.setZoom(18d);
                        }
//
                        // Déplacer la caméra de la carte vers l'adresse trouvée
                        //mapController.setCenter(geoPoint);
                    } else {
                        adapter.clear();
                        // Si aucune adresse n'a été trouvée, masquer le marqueur
                        marker.setVisible(false);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }
            ArrayList<Marker> myMarkers= new ArrayList<>();
            @Override
            public boolean onQueryTextSubmit(String newText) {
                Marker marker = new Marker(map);
                // Récupérer le texte saisi par l'utilisateur
                String searchString = newText.trim();

                // Utiliser l'objet Geocoder pour effectuer une recherche d'adresse
                Geocoder geocoder = new Geocoder(MainActivity.this);
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(searchString, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addresses != null && addresses.size() > 0) {
                    // Si une adresse a été trouvée, récupérer les coordonnées de l'adresse
                    Address address = addresses.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();

                    // Créer un objet LatLng pour stocker les coordonnées de l'adresse
                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                    // Vérifier si le marqueur de l'adresse existe déjà
                    if (marker != null) {
                        // Si le marqueur existe déjà, mettre à jour sa position et le rendre visible
                        //map.getOverlays().clear();
                        marker.setPosition(geoPoint);
                        marker.setTitle(addresses.get(0).getAddressLine(0));
                        Drawable localisation = ResourcesCompat.getDrawable(getResources(), R.drawable.here, null);
                        //Bitmap bitmap = ((BitmapDrawable) localisation).getBitmap();
                       // Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (48.0f * getResources().getDisplayMetrics().density), (int) (48.0f * getResources().getDisplayMetrics().density), true));
                        marker.setIcon(localisation);
                        marker.setId("new localisation");
                        for (Marker m : myMarkers) {
                            if (m.getId().toString() == "new localisation"){
                                map.getOverlays().remove(m);
                            }

                        }

                        //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);


                        mapController.setZoom(11d);
                        myMarkers.add(marker);
                        map.invalidate();

                    } else {
                        // Si le marqueur n'existe pas, créer un nouveau marqueur et l'ajouter à la carte
                        //map.getOverlays().clear();
                        marker.setPosition(geoPoint);
                        marker.setTitle("Adresse");
                        map.getOverlays().add(marker);
                        map.invalidate();
                        mapController.setZoom(11d);

                    }

                    // Déplacer la caméra de la carte vers l'adresse trouvée
                    mapController.setCenter(geoPoint);
                } else {
                    // Si aucune adresse n'a été trouvée, afficher un message d'erreur
                    Toast.makeText(MainActivity.this, "Aucun résultat trouvé", Toast.LENGTH_SHORT).show();
                }

                    // Masquer le clavier virtuel
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                    // Annuler la recherche
                return true;
            }
        });



            }




public  void FetchData(){
    MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {






            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            Itiniraire(p);
            return true;
        }
    };
    MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);


    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://webdev.iut-orsay.fr/~nabitb1/WCapp/")
            .client(client)

            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    WcInterface wcInterface = retrofit.create(WcInterface.class);

    Call<List<WcAPI>> call = wcInterface.getWcAPI();

    call.enqueue(new Callback<List<WcAPI>>() {
        @Override
        public void onResponse(Call<List<WcAPI>> call, retrofit2.Response<List<WcAPI>> response) {
            if (!response.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Code: " + response.code(), Toast.LENGTH_SHORT).show();
                return;
            }
            List<WcAPI> wcAPI = response.body();
            for (WcAPI wc : wcAPI) {
                Marker marker = new Marker(map);
                String content = "";
                content +=  wc.getLongi();
                content +=  wc.getLati();

                double lati = wc.getLati();
                double longi = wc.getLongi();


                marker.setPosition(new GeoPoint(lati,longi));
                marker.setId(wc.getId());
                marker.setTitle(wc.getDescr());
                marker.setSubDescription("Note : " + wc.getNote());
                Geocoder texttoaddress = new Geocoder(MainActivity.this);
                List<Address> addresses = null;
                try {
                    addresses = texttoaddress.getFromLocation(longi, lati, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null && addresses.size() > 0) {
                    // Si une adresse a été trouvée, récupérer les coordonnées de l'adresse
                    Address address = addresses.get(0);
                    marker.setSubDescription(addresses.get(0).getAddressLine(0));
                }
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                Drawable localisation = ResourcesCompat.getDrawable(getResources(), R.drawable.wc, null);
                marker.setIcon(localisation);
                marker.onTouchEvent(null, map);

                map.getOverlays().add(marker);
                map.getOverlays().add(OverlayEvents);
                map.invalidate();


            }
            Toast.makeText(MainActivity.this, "Les WC aux alentours ont chargés", Toast.LENGTH_SHORT).show();

//
        }
        @Override
        public void onFailure(Call<List<WcAPI>> call, Throwable t) {
            Toast.makeText(MainActivity.this, "Erreur lors de la récupération : "+t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}


public void Itiniraire(GeoPoint p){
    AlertDialog.Builder alert_builder = new AlertDialog.Builder(this);
    alert_builder.setView(R.layout.itiniraire)
            .setTitle("Itinéraire")
            .setMessage("Voulez-vous créer un itinéraire ?");



    alert_builder.setPositiveButton("Commencer", (dialogInterface, i) -> {
        RoadManager roadManager = new OSRMRoadManager(MainActivity.this, "itineraire");


        if(actual_location == null){
            Toast.makeText(MainActivity.this, "Veuillez attendre que la localisation soit chargée", Toast.LENGTH_SHORT).show();
            actual_location = new GeoPoint(48.697, 2.176);
        }else {
            map.getOverlays().clear();
            FetchData();
            waypoints_iti.clear();
            GeoPoint debut = actual_location;
            waypoints_iti.add(debut);
            GeoPoint endPoint = p;
            waypoints_iti.add(endPoint);

            Road road = roadManager.getRoad(waypoints_iti);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(Color.RED);
            roadOverlay.setWidth(10);
            map.getOverlays().add(roadOverlay);

            Drawable nodeIcon = getResources().getDrawable(R.drawable.step);
            for (int y=0; y<road.mNodes.size(); y++){
                RoadNode node = road.mNodes.get(y);
                Marker nodeMarker = new Marker(map);
                nodeMarker.setPosition(node.mLocation);
                nodeMarker.setIcon(nodeIcon);
                nodeMarker.setTitle("Step "+y);
                map.getOverlays().add(nodeMarker);
                nodeMarker.setSnippet(node.mInstructions);
                nodeMarker.setSubDescription(Road.getLengthDurationText(MainActivity.this, node.mLength, node.mDuration));
                Drawable icon = getResources().getDrawable(R.drawable.wc);
                nodeMarker.setImage(icon);

            }


            map.invalidate();



            Toast.makeText(MainActivity.this, "Itinéraire débuté", Toast.LENGTH_SHORT).show();

        }
    });
    alert_itiniraire = alert_builder.create();

    alert_itiniraire.show();

    TextView info = (TextView) alert_itiniraire.findViewById(R.id.info);
    Geocoder texttoaddress = new Geocoder(MainActivity.this);
    List<Address> addresses = null;
    try {
        addresses = texttoaddress.getFromLocation(p.getLatitude(), p.getLongitude(), 1);
    } catch (IOException e) {
        e.printStackTrace();
    }
    if (addresses != null && addresses.size() > 0) {
        // Si une adresse a été trouvée, récupérer les coordonnées de l'adresse
        Address address = addresses.get(0);
        info.setText(addresses.get(0).getAddressLine(0));
        info.setTextSize(20);
    }else {
        info.setText("Adresse inconnue");
        info.setTextSize(20);
    }


}



            public void Modal(){
                Geocoder texttoaddress = new Geocoder(MainActivity.this);
                AlertDialog.Builder alert_builder = new AlertDialog.Builder(this);
                alert_builder.setView(R.layout.modal)
                        .setTitle("Ajouter des WC")
                        .setMessage("Entrez l'adresse et une description si nécessaire.");


//
//
//

                alert_builder.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        List<Address> addresses = null;
                        try {
                            adressadd = alert_adresse.findViewById(R.id.adressadd);

                            addresses = texttoaddress.getFromLocationName(adressadd.getText().toString(), 1);
                            Address address = addresses.get(0);
                            double latitude = address.getLatitude();
                            double longitude = address.getLongitude();
                            GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                            Gson gson = new GsonBuilder()
                                    .setLenient()
                                    .create();

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("https://webdev.iut-orsay.fr/~nabitb1/WCapp/")
                                    .client(client)
                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                    .build();

                           WcInterface wcInterface =  retrofit.create(WcInterface.class);

                            WcAPI wcAPI = new WcAPI(
                                    longitude,
                                    latitude,
                                    URLEncoder.encode(adressadd.getText().toString()),
                                    "0",
                                    "null"
                            );

                           Call<List<WcAPI>>call = wcInterface.postWcAPI(longitude, latitude,  URLEncoder.encode(adressadd.getText().toString()),id_user,"0", "null");

                            call.enqueue(new Callback<List<WcAPI>>() {

                                @Override
                                public void onResponse(Call<List<WcAPI>> call, retrofit2.Response<List<WcAPI>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "WC ajouté", Toast.LENGTH_SHORT).show();
                                        Log.i("Post submitted to API.", wcAPI.toString());
                                    } else {
                                        Toast.makeText(MainActivity.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                             public void onFailure(Call<List<WcAPI>> call, Throwable t) {
                                                 Toast.makeText(MainActivity.this,  t.getMessage(), Toast.LENGTH_LONG).show();
                                                 Log.e("ERROR: ", t.getMessage());
                                             }
                                         });



                            Marker newWc = new Marker(map);

                            newWc.setTitle(adressadd.getText().toString());
                            newWc.setPosition(geoPoint);
                            Drawable wc = ResourcesCompat.getDrawable(getResources(), R.drawable.wc, null);
                            newWc.setIcon(wc);

                            map.getOverlays().add(newWc);
                            map.invalidate();

                            // Créer un objet LatLng pour stocker les coordonnées de l'adresse
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//
                    }
                });

                alert_builder.setNegativeButton("Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                alert_adresse = alert_builder.create();
                alert_adresse.show();

            }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // Vérifier si la permission a été accordée
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Demander les mises à jour de localisation si la permission a été accordée
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Demander la permission de localisation à l'utilisateur s'il ne l'a pas encore accordée
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                } else {
                    // Demander les mises à jour de localisation si la permission a été accordée
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
            } else {
                // Afficher un message d'erreur si la permission a été refusée
                Toast.makeText(this, "La permission de localisation est nécessaire pour afficher la carte.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "Localisation mis à jour", Toast.LENGTH_SHORT).show();

        // Mettre à jour la carte avec la nouvelle localisation
       GeoPoint newLocation = new GeoPoint(location);
        actual_location = newLocation;
        mapController.setCenter(newLocation);

        // Afficher un marqueur à la nouvelle localisation
        Marker marker = new Marker(map);
        Drawable localisation = ResourcesCompat.getDrawable(getResources(), R.drawable.here, null);
        marker.setIcon(localisation);


        //Configurer le marqueur
        marker.setPosition(newLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Votre position actuelle");
        //map.getOverlays().clear(); // Effacer tous les marqueurs précédents
        map.getOverlays().add(marker);
        mapController.setZoom(18d);

        map.invalidate(); // Mettre à jour la vue de la carte
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Ne pas utiliser
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Ne pas utiliser
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Ne pas utiliser
    }



}