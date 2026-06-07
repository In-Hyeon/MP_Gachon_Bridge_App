package com.example.gachonbridge;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;

    private FrameLayout mapContainer;
    private LinearLayout placeChoicePanel;
    private TextView placeChoiceTitle;
    private LinearLayout placeChoiceButtonContainer;
    private LinearLayout buildingSearchPanel;
    private EditText buildingSearchInput;
    private TextView buildingSearchButton;
    private TextView buildingSearchResetButton;
    private TextView statusText;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location latestLocation;
    private String lastStatus = "";

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Handler mainHandler;
    private boolean isRequestingLocationUpdates = false;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor accelerometerSensor;
    private boolean isDarkComfortMode = false;
    private long lastShakeTime = 0L;

    private SharedPreferences preferences;

    private Polygon gachonPolygon;

    private final List<MapZone> buildingZones = new ArrayList<>();
    private final List<MapZone> smokingZones = new ArrayList<>();
    private final List<PlaceInfo> placeInfos = new ArrayList<>();
    private final List<PlaceMarkerGroup> placeMarkerGroups = new ArrayList<>();
    private final Map<Polygon, MapZone> zoneByPolygon = new HashMap<>();

    private long holdStatusUntilMillis = 0L;
    private String selectedBuildingName = "";
    private PlaceInfo selectedPlaceInfo = null;

    private static final String PREF_NAME = "gachon_map_prefs";
    private static final String PREF_LAST_PLACE = "last_selected_place";

    private static final LatLngBounds FIXED_MAP_BOUNDS = new LatLngBounds(
            new LatLng(37.445944, 127.126558),
            new LatLng(37.458243, 127.137084)
    );

    private static final int START_BOUNDS_PADDING_DP = 12;
    private static final float BUILDING_SEARCH_ZOOM = 16.0f;
    private static final float PLACE_SEARCH_ZOOM = 17.0f;
    private static final float BUILDING_ZONE_TOLERANCE_METERS = 22f;

    private final List<LatLng> gachonBoundary = Arrays.asList(
            new LatLng(37.452315, 127.126902),
            new LatLng(37.449065, 127.126872),
            new LatLng(37.448981, 127.129740),
            new LatLng(37.450862, 127.130833),
            new LatLng(37.451815, 127.132483),
            new LatLng(37.452567, 127.134395),
            new LatLng(37.454802, 127.136046),
            new LatLng(37.456826, 127.136066),
            new LatLng(37.456607, 127.135956),
            new LatLng(37.456041, 127.132663),
            new LatLng(37.453775, 127.132919),
            new LatLng(37.453036, 127.130354),
            new LatLng(37.452306, 127.127420)
    );

    private static final int SOFT_TEXT_LIGHT = Color.WHITE;

    private static final int STATUS_DARK = Color.argb(225, 44, 53, 64);
    private static final int STATUS_GREEN = Color.argb(225, 70, 130, 91);
    private static final int STATUS_BLUE = Color.argb(225, 65, 116, 190);
    private static final int STATUS_RED = Color.argb(230, 198, 70, 70);
    private static final int STATUS_GRAY = Color.argb(225, 90, 90, 90);
    private static final int STATUS_ORANGE = Color.argb(235, 218, 132, 57);

    private static final int CAMPUS_STROKE = Color.rgb(218, 76, 76);
    private static final int CAMPUS_FILL = Color.argb(24, 218, 76, 76);

    private static final int BUILDING_STROKE = Color.rgb(76, 132, 214);
    private static final int BUILDING_FILL = Color.argb(58, 76, 132, 214);
    private static final int BUILDING_SELECTED_FILL = Color.argb(135, 76, 132, 214);
    private static final int BUILDING_SEARCH_STROKE = Color.rgb(185, 135, 0);
    private static final int BUILDING_SEARCH_FILL = Color.argb(190, 255, 193, 7);

    private static final int SMOKING_STROKE = Color.rgb(0, 0, 0);
    private static final int SMOKING_FILL = Color.argb(185, 0, 0, 0);
    private static final int SMOKING_SELECTED_FILL = Color.argb(230, 0, 0, 0);

    private static final int RESTAURANT_COLOR = Color.rgb(229, 139, 48);
    private static final int BILLIARD_COLOR = Color.rgb(112, 86, 190);
    private static final int MULTI_PLACE_COLOR = Color.rgb(229, 139, 48);
    private static final int SEARCH_SELECTED_YELLOW = Color.rgb(255, 193, 7);

    private static final String DAY_MAP_STYLE_JSON = "["
            + "{\"featureType\":\"poi\",\"stylers\":[{\"visibility\":\"off\"}]},"
            + "{\"featureType\":\"road\",\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#f0ede6\"}]},"
            + "{\"featureType\":\"road\",\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#747474\"}]},"
            + "{\"featureType\":\"landscape\",\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#f7f7f2\"}]},"
            + "{\"featureType\":\"water\",\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#d8eef7\"}]},"
            + "{\"featureType\":\"transit\",\"elementType\":\"labels.icon\",\"stylers\":[{\"visibility\":\"off\"}]}"
            + "]";

    private static final String DARK_MAP_STYLE_JSON = "["
            + "{\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#263238\"}]},"
            + "{\"elementType\":\"labels.text.fill\",\"stylers\":[{\"color\":\"#cfd8dc\"}]},"
            + "{\"elementType\":\"labels.text.stroke\",\"stylers\":[{\"color\":\"#263238\"}]},"
            + "{\"featureType\":\"poi\",\"stylers\":[{\"visibility\":\"off\"}]},"
            + "{\"featureType\":\"road\",\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#39464e\"}]},"
            + "{\"featureType\":\"water\",\"elementType\":\"geometry\",\"stylers\":[{\"color\":\"#203a43\"}]}"
            + "]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapContainer = findViewById(R.id.mapContainer);
        placeChoicePanel = findViewById(R.id.placeChoicePanel);
        placeChoiceTitle = findViewById(R.id.placeChoiceTitle);
        placeChoiceButtonContainer = findViewById(R.id.placeChoiceButtonContainer);
        buildingSearchPanel = findViewById(R.id.searchPanel);
        buildingSearchInput = findViewById(R.id.buildingSearchEditText);
        buildingSearchButton = findViewById(R.id.buildingSearchButton);
        buildingSearchResetButton = findViewById(R.id.buildingSearchResetButton);
        statusText = findViewById(R.id.statusText);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        preparePlaceChoicePanel();
        prepareBuildingSearchPanel();

        setStatusCard(
                "가천대 생활지도",
                "건물, 식당, 카페, 편의시설을 검색해보세요",
                STATUS_DARK
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void preparePlaceChoicePanel() {
        if (placeChoicePanel == null) return;

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(255, 253, 248));
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), RESTAURANT_COLOR);
        placeChoicePanel.setBackground(background);
        placeChoicePanel.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            placeChoicePanel.setElevation(dp(10));
        }
    }

    private void prepareBuildingSearchPanel() {
        if (buildingSearchPanel == null || buildingSearchInput == null
                || buildingSearchButton == null || buildingSearchResetButton == null) {
            return;
        }

        GradientDrawable panelBackground = new GradientDrawable();
        panelBackground.setColor(Color.argb(238, 255, 253, 248));
        panelBackground.setCornerRadius(dp(18));
        panelBackground.setStroke(dp(1), Color.argb(120, 44, 53, 64));
        buildingSearchPanel.setBackground(panelBackground);

        GradientDrawable inputBackground = new GradientDrawable();
        inputBackground.setColor(Color.WHITE);
        inputBackground.setCornerRadius(dp(12));
        inputBackground.setStroke(dp(1), Color.argb(90, 44, 53, 64));
        buildingSearchInput.setBackground(inputBackground);

        /*
         * 여기 때문에 화면에 "건물명 검색" 대신 "검색"으로 뜹니다.
         * 그래도 "건물명 검색"이 계속 보이면 activity_map.xml의
         * android:hint="건물명 검색"도 android:hint="검색"으로 바꿔주세요.
         */
        buildingSearchInput.setHint("검색");
        buildingSearchInput.setSingleLine(true);
        buildingSearchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        buildingSearchButton.setText("검색");
        buildingSearchResetButton.setText("초기화");

        styleSearchButton(buildingSearchButton, STATUS_BLUE);
        styleSearchButton(buildingSearchResetButton, STATUS_GRAY);

        buildingSearchButton.setOnClickListener(v -> performBuildingSearch());
        buildingSearchResetButton.setOnClickListener(v -> clearBuildingSearch());
        buildingSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            performBuildingSearch();
            return true;
        });
    }

    private void styleSearchButton(TextView button, int color) {
        if (button == null) return;

        button.setTextColor(Color.WHITE);
        button.setTextSize(12.5f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setClickable(true);

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(12));
        button.setBackground(background);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        createZones();
        setupFixedMap();
        drawAllZones();
        drawPlaceMarkers();
        setupMapEvents();
        showSavedOpeningMessage();

        registerNetworkCallback();
        startMapServices();
    }

    private void setupFixedMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        applyComfortableMapStyle(false);

        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);

        /*
         * 지도 범위 제한은 유지합니다.
         * 이 범위 안에서만 좌우/상하 이동됩니다.
         */
        mMap.setLatLngBoundsForCameraTarget(FIXED_MAP_BOUNDS);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        FIXED_MAP_BOUNDS.getCenter(),
                        15.0f
                )
        );

        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(17.0f);

        /*
         * 기존 false였던 부분을 true로 변경했습니다.
         * 이제 사용자가 지도를 좌우/상하로 움직일 수 있습니다.
         */
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        mMap.setOnMapLoadedCallback(() -> {
            mMap.setPadding(0, 0, 0, 0);
            fitGachonBoundaryToScreen();
        });
    }

    private void applyComfortableMapStyle(boolean darkMode) {
        if (mMap == null) return;

        try {
            if (darkMode) {
                mMap.setMapStyle(new MapStyleOptions(DARK_MAP_STYLE_JSON));
            } else {
                mMap.setMapStyle(new MapStyleOptions(DAY_MAP_STYLE_JSON));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createZones() {
        buildingZones.clear();
        smokingZones.clear();
        zoneByPolygon.clear();

        addBuildingZone("비전타워", Arrays.asList(
                new LatLng(37.450173, 127.127043),
                new LatLng(37.449082, 127.126984),
                new LatLng(37.449082, 127.127853),
                new LatLng(37.450164, 127.127413)
        ));

        addBuildingZone("반도체", Arrays.asList(
                new LatLng(37.451312, 127.126871),
                new LatLng(37.451440, 127.126909),
                new LatLng(37.451457, 127.127300),
                new LatLng(37.450771, 127.127333)
        ));

        addBuildingZone("글로벌센터", Arrays.asList(
                new LatLng(37.451685, 127.126941),
                new LatLng(37.451673, 127.127304),
                new LatLng(37.452156, 127.127394),
                new LatLng(37.452171, 127.126970)
        ));

        addBuildingZone("공과대학1", Arrays.asList(
                new LatLng(37.451679, 127.127606),
                new LatLng(37.451414, 127.127750),
                new LatLng(37.451353, 127.128386),
                new LatLng(37.451389, 127.128643),
                new LatLng(37.451840, 127.128481)
        ));

        addBuildingZone("공과대학2", Arrays.asList(
                new LatLng(37.449152, 127.128013),
                new LatLng(37.449152, 127.129073),
                new LatLng(37.449399, 127.129010),
                new LatLng(37.449423, 127.128018)
        ));

        addBuildingZone("AI도서관", Arrays.asList(
                new LatLng(37.450649, 127.127655),
                new LatLng(37.450372, 127.128664),
                new LatLng(37.450926, 127.129089),
                new LatLng(37.451046, 127.128653),
                new LatLng(37.450919, 127.128609),
                new LatLng(37.451132, 127.127837)
        ));

        addBuildingZone("바이오 연구원", Arrays.asList(
                new LatLng(37.450150, 127.127987),
                new LatLng(37.449513, 127.127983),
                new LatLng(37.449504, 127.128193),
                new LatLng(37.450147, 127.128193)
        ));

        addBuildingZone("한의과대학", Arrays.asList(
                new LatLng(37.450212, 127.128292),
                new LatLng(37.449990, 127.128230),
                new LatLng(37.449939, 127.128414),
                new LatLng(37.449862, 127.128937),
                new LatLng(37.450008, 127.128992)
        ));

        addBuildingZone("산학협력관", Arrays.asList(
                new LatLng(37.449117, 127.129261),
                new LatLng(37.449056, 127.129523),
                new LatLng(37.449118, 127.129552),
                new LatLng(37.449109, 127.129600),
                new LatLng(37.449319, 127.129687),
                new LatLng(37.449339, 127.129639),
                new LatLng(37.449502, 127.129699),
                new LatLng(37.449495, 127.129761),
                new LatLng(37.449713, 127.129847),
                new LatLng(37.449780, 127.129855),
                new LatLng(37.449848, 127.129570)
        ));

        addBuildingZone("가천관", Arrays.asList(
                new LatLng(37.450155, 127.129482),
                new LatLng(37.450811, 127.129755),
                new LatLng(37.450700, 127.130127),
                new LatLng(37.450055, 127.129862)
        ));

        addBuildingZone("예음관", Arrays.asList(
                new LatLng(37.451573, 127.129115),
                new LatLng(37.451516, 127.129418),
                new LatLng(37.451573, 127.129467),
                new LatLng(37.451516, 127.129580),
                new LatLng(37.451484, 127.129728),
                new LatLng(37.451462, 127.130180),
                new LatLng(37.451634, 127.130256),
                new LatLng(37.451792, 127.129990),
                new LatLng(37.451917, 127.129453),
                new LatLng(37.451730, 127.129169),
                new LatLng(37.451685, 127.129368)
        ));

        addBuildingZone("바이오나노대학", Arrays.asList(
                new LatLng(37.451106, 127.128974),
                new LatLng(37.451224, 127.129021),
                new LatLng(37.451373, 127.129429),
                new LatLng(37.451288, 127.129800),
                new LatLng(37.451433, 127.129865),
                new LatLng(37.451378, 127.130071),
                new LatLng(37.451149, 127.129978),
                new LatLng(37.451090, 127.130202),
                new LatLng(37.450897, 127.130124),
                new LatLng(37.451031, 127.129532)
        ));

        /*
         * 예술대학2는 존재하지 않는다고 해서 넣지 않았습니다.
         * "예" 검색 시 예음관, 예술대학1이 후보로 뜹니다.
         */
        addBuildingZone("예술대학1", Arrays.asList(
                new LatLng(37.452567, 127.128749),
                new LatLng(37.452262, 127.128834),
                new LatLng(37.452268, 127.128924),
                new LatLng(37.452150, 127.128952),
                new LatLng(37.452130, 127.128875),
                new LatLng(37.452116, 127.128877),
                new LatLng(37.452196, 127.129353),
                new LatLng(37.452092, 127.129379),
                new LatLng(37.452016, 127.128961),
                new LatLng(37.452000, 127.128772),
                new LatLng(37.452026, 127.128702),
                new LatLng(37.452521, 127.128548)
        ));

        addBuildingZone("가천대 교육대학원", Arrays.asList(
                new LatLng(37.451461, 127.131586),
                new LatLng(37.451495, 127.131344),
                new LatLng(37.451923, 127.131482),
                new LatLng(37.452161, 127.132100),
                new LatLng(37.451988, 127.132206)
        ));

        addBuildingZone("중앙도서관", Arrays.asList(
                new LatLng(37.452376, 127.132558),
                new LatLng(37.452405, 127.132549),
                new LatLng(37.452684, 127.133252),
                new LatLng(37.452229, 127.133528)
        ));

        addBuildingZone("총학생회", Arrays.asList(
                new LatLng(37.453071, 127.133741),
                new LatLng(37.452687, 127.134251),
                new LatLng(37.453063, 127.134711),
                new LatLng(37.453439, 127.134273)
        ));

        /*
         * "공" 검색 시 AI 공학관도 후보로 뜹니다.
         * "AI 공학과"라고 잘못 쳐도 검색되도록 별칭도 아래 검색 함수에 넣어두었습니다.
         */
        addBuildingZone("AI 공학관", Arrays.asList(
                new LatLng(37.455038, 127.133333),
                new LatLng(37.455108, 127.134315),
                new LatLng(37.455472, 127.134285),
                new LatLng(37.455193, 127.133303)
        ));

        addBuildingZone("제3기숙사", Arrays.asList(
                new LatLng(37.455846, 127.132714),
                new LatLng(37.456087, 127.133522),
                new LatLng(37.455681, 127.133727),
                new LatLng(37.455419, 127.132915)
        ));

        addBuildingZone("제2기숙사", Arrays.asList(
                new LatLng(37.455807, 127.133792),
                new LatLng(37.456136, 127.133629),
                new LatLng(37.456451, 127.134628),
                new LatLng(37.456021, 127.134676)
        ));

        addBuildingZone("제1기숙사", Arrays.asList(
                new LatLng(37.456127, 127.135585),
                new LatLng(37.456112, 127.135396),
                new LatLng(37.456327, 127.134737),
                new LatLng(37.456460, 127.134719),
                new LatLng(37.456492, 127.135558)
        ));

        addBuildingZone("종합운동장", Arrays.asList(
                new LatLng(37.454492, 127.134955),
                new LatLng(37.455452, 127.134827),
                new LatLng(37.455495, 127.135371),
                new LatLng(37.454527, 127.135468)
        ));

        addSmokingZone("흡연구역", Arrays.asList(
                new LatLng(37.455036, 127.133171),
                new LatLng(37.455046, 127.133237),
                new LatLng(37.455129, 127.133218),
                new LatLng(37.455117, 127.133171)
        ));

        addSmokingZone("흡연구역", Arrays.asList(
                new LatLng(37.450641, 127.126929),
                new LatLng(37.450712, 127.126938),
                new LatLng(37.450701, 127.127177),
                new LatLng(37.450633, 127.127280)
        ));

        addSmokingZone("흡연구역", Arrays.asList(
                new LatLng(37.455658, 127.134841),
                new LatLng(37.455611, 127.134854),
                new LatLng(37.455660, 127.134937),
                new LatLng(37.455741, 127.134925)
        ));


        addSmokingZone("흡연구역", Arrays.asList(
                new LatLng(37.451613, 127.126976),
                new LatLng(37.451664, 127.126984),
                new LatLng(37.451660, 127.127131),
                new LatLng(37.451620, 127.127168)
        ));

        addSmokingZone("흡연구역", Arrays.asList(
                new LatLng(37.449841, 127.128404),
                new LatLng(37.449628, 127.128435),
                new LatLng(37.449636, 127.128767),
                new LatLng(37.449811, 127.128702)
        ));
    }

    private void addBuildingZone(String name, List<LatLng> points) {
        buildingZones.add(new MapZone(name, points, false));
    }

    private void addSmokingZone(String name, List<LatLng> points) {
        smokingZones.add(new MapZone(name, points, true));
    }

    private void drawAllZones() {
        gachonPolygon = mMap.addPolygon(new PolygonOptions()
                .addAll(gachonBoundary)
                .strokeColor(CAMPUS_STROKE)
                .strokeWidth(5f)
                .fillColor(CAMPUS_FILL)
                .clickable(true)
                .zIndex(1f));

        for (MapZone zone : buildingZones) {
            zone.polygon = mMap.addPolygon(new PolygonOptions()
                    .addAll(zone.points)
                    .strokeColor(BUILDING_STROKE)
                    .strokeWidth(4f)
                    .fillColor(BUILDING_FILL)
                    .clickable(true)
                    .zIndex(2f));

            zoneByPolygon.put(zone.polygon, zone);
        }

        for (MapZone zone : smokingZones) {
            zone.polygon = mMap.addPolygon(new PolygonOptions()
                    .addAll(zone.points)
                    .strokeColor(SMOKING_STROKE)
                    .strokeWidth(4f)
                    .fillColor(SMOKING_FILL)
                    .clickable(true)
                    .zIndex(3f));

            zoneByPolygon.put(zone.polygon, zone);
        }
    }

    private void drawPlaceMarkers() {
        placeInfos.clear();
        placeMarkerGroups.clear();

        LatLng globalCenterB2 = new LatLng(37.449450, 127.127819);

        PlaceInfo jesoonRestaurant = new PlaceInfo(
                "제순식당",
                "식당",
                "B2",
                "지하 2층",
                "글로벌센터 근처",
                globalCenterB2,
                RESTAURANT_COLOR
        );

        PlaceInfo qBilliard = new PlaceInfo(
                "큐빌리어드 당구장",
                "당구장",
                "B2",
                "지하 2층",
                "제순식당과 같은 위치",
                globalCenterB2,
                BILLIARD_COLOR
        );

        PlaceInfo jjeongiSnack = new PlaceInfo(
                "쩡이분식",
                "식당",
                "B1",
                "지하 1층",
                "쩡이떡볶이",
                new LatLng(37.449918, 127.127653),
                RESTAURANT_COLOR
        );

        PlaceInfo pascucciGachon = new PlaceInfo(
                "파스쿠찌 가천대점",
                "카페",
                "1F",
                "1층",
                "파스쿠찌",
                new LatLng(37.451195, 127.127730),
                RESTAURANT_COLOR
        );

        // 투썸플레이스, 올리브영, 차이나스푼, 봉구스 밥버거, 포밥인 뉴욕을 하나의 점으로 묶기
        LatLng visionTowerFoodArea = new LatLng(37.449716, 127.127745);

        PlaceInfo oliveYoung = new PlaceInfo(
                "올리브영",
                "편의시설",
                "B3",
                "지하 3층",
                "올리브영",
                visionTowerFoodArea,
                RESTAURANT_COLOR
        );

        PlaceInfo chinaSpoon = new PlaceInfo(
                "차이나스푼",
                "식당",
                "B3",
                "지하 3층",
                "올리브영과 같은 위치",
                visionTowerFoodArea,
                RESTAURANT_COLOR
        );

        PlaceInfo twosomePlace = new PlaceInfo(
                "투썸플레이스",
                "카페",
                "B3",
                "지하 3층",
                "올리브영, 차이나스푼과 같은 위치",
                visionTowerFoodArea,
                RESTAURANT_COLOR
        );

        PlaceInfo bongousseBobBurger = new PlaceInfo(
                "봉구스 밥버거",
                "식당",
                "B1",
                "지하 1층",
                "투썸플레이스와 같은 위치",
                visionTowerFoodArea,
                RESTAURANT_COLOR
        );

        PlaceInfo pobabInNewYork = new PlaceInfo(
                "포밥인 뉴욕",
                "식당",
                "B2",
                "지하 2층",
                "투썸플레이스와 같은 위치",
                visionTowerFoodArea,
                RESTAURANT_COLOR
        );

        PlaceInfo dunkinDonuts = new PlaceInfo(
                "던킨 도넛",
                "카페",
                "B3",
                "지하 3층",
                "던킨 도넛",
                new LatLng(37.449491, 127.127375),
                RESTAURANT_COLOR
        );

        placeInfos.add(jesoonRestaurant);
        placeInfos.add(qBilliard);
        placeInfos.add(jjeongiSnack);
        placeInfos.add(pascucciGachon);
        placeInfos.add(oliveYoung);
        placeInfos.add(chinaSpoon);
        placeInfos.add(twosomePlace);
        placeInfos.add(bongousseBobBurger);
        placeInfos.add(pobabInNewYork);
        placeInfos.add(dunkinDonuts);

        Map<String, PlaceMarkerGroup> groupMap = new HashMap<>();

        for (PlaceInfo place : placeInfos) {
            addPlaceToGroupMap(groupMap, place);
        }

        for (PlaceMarkerGroup group : placeMarkerGroups) {
            addPlaceMarkerGroup(group);
        }
    }

    private void addPlaceToGroupMap(Map<String, PlaceMarkerGroup> groupMap, PlaceInfo place) {
        String key = buildPlaceLocationKey(place.position);
        PlaceMarkerGroup group = groupMap.get(key);

        if (group == null) {
            group = new PlaceMarkerGroup(place.position);
            groupMap.put(key, group);
            placeMarkerGroups.add(group);
        }

        group.places.add(place);
    }

    private String buildPlaceLocationKey(LatLng position) {
        long latKey = Math.round(position.latitude * 1000000d);
        long lngKey = Math.round(position.longitude * 1000000d);
        return latKey + ":" + lngKey;
    }

    private void addPlaceMarkerGroup(PlaceMarkerGroup group) {
        BitmapDescriptor icon = createPlaceGroupDotIcon(
                getGroupAccentColor(group),
                group.places.size()
        );

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(group.position)
                .title("편의시설")
                .snippet("주황색 편의시설을 눌러 선택")
                .icon(icon)
                .anchor(0.5f, 0.5f)
                .zIndex(20f));

        if (marker != null) {
            marker.setTag(group);
            group.marker = marker;

            for (PlaceInfo place : group.places) {
                place.marker = marker;
            }
        }
    }

    private int getGroupAccentColor(PlaceMarkerGroup group) {
        if (group.places.size() == 1) {
            return group.places.get(0).accentColor;
        }

        return MULTI_PLACE_COLOR;
    }

    private String getGroupSummaryTitle(PlaceMarkerGroup group) {
        if (group.places.size() == 1) {
            return group.places.get(0).category;
        }

        if (containsCategory(group, "식당")) {
            return "편의시설";
        }

        return "편의시설";
    }

    private boolean containsCategory(PlaceMarkerGroup group, String category) {
        for (PlaceInfo place : group.places) {
            if (category.equals(place.category)) {
                return true;
            }
        }

        return false;
    }

    private BitmapDescriptor createPlaceGroupDotIcon(int accentColor, int placeCount) {
        int size = dp(30);
        float center = size / 2f;
        float outerRadius = dp(10);
        float innerRadius = dp(7);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(75, 0, 0, 0));
        canvas.drawCircle(center, center + dp(1), outerRadius, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(center, center, outerRadius, paint);

        paint.setColor(accentColor);
        canvas.drawCircle(center, center, innerRadius, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor createSelectedPlaceDotIcon() {
        int size = dp(44);
        float center = size / 2f;
        float outerRadius = dp(15);
        float innerRadius = dp(11);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(95, 0, 0, 0));
        canvas.drawCircle(center, center + dp(2), outerRadius, paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(center, center, outerRadius, paint);

        paint.setColor(SEARCH_SELECTED_YELLOW);
        canvas.drawCircle(center, center, innerRadius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(80, 60, 0));
        canvas.drawCircle(center, center, innerRadius, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void setupMapEvents() {
        mMap.setInfoWindowAdapter(null);

        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();

            if (tag instanceof PlaceMarkerGroup) {
                PlaceMarkerGroup group = (PlaceMarkerGroup) tag;

                if (group.places.size() == 1) {
                    hidePlaceChoicePanel();
                    showSelectedPlaceStatus(group.places.get(0));
                } else {
                    showPlaceChoicePanel(group);
                    setStatusCard(
                            "편의시설",
                            "선택창에서 원하는 장소를 선택하세요",
                            STATUS_ORANGE
                    );
                }

                return true;
            }

            return false;
        });

        mMap.setOnMapClickListener(latLng -> {
            hidePlaceChoicePanel();
            holdStatusUntilMillis = 0L;
        });

        mMap.setOnPolygonClickListener(polygon -> {
            hidePlaceChoicePanel();

            MapZone zone = zoneByPolygon.get(polygon);

            /*
             * 중요 수정 부분
             * 예전 코드에서는 건물을 한 번 선택한 뒤 다시 터치하면
             * showCurrentLocationFromBuildingTap()이 실행되면서 카메라가
             * 현재 위치로 갑자기 이동했습니다.
             * 그래서 그 분기를 제거하고, 건물/흡연구역 터치는 항상
             * 아래 정보 글(statusText)만 바꾸도록 했습니다.
             */
            holdStatusUntilMillis = System.currentTimeMillis() + 10000L;
            resetAllPlaceMarkerIcons();
            resetZoneColors();

            if (zone != null) {
                selectedBuildingName = zone.name;
                selectedPlaceInfo = null;

                highlightZone(zone);

                if (zone.isSmokingZone) {
                    setStatusCard("흡연구역", "회색 영역으로 표시됩니다", STATUS_GRAY);
                } else {
                    // 건물을 터치하면 그 건물 쪽으로만 이동합니다.
                    // 현재 위치로 튀는 기능은 실행하지 않습니다.
                    moveCameraToZone(zone);
                    setStatusCard(zone.name, "선택한 건물을 파란색으로 표시했습니다", STATUS_BLUE);
                }

                return;
            }

            if (polygon.equals(gachonPolygon)) {
                selectedBuildingName = "";
                selectedPlaceInfo = null;
                setStatusCard("가천대학교", "빨간 선은 캠퍼스 전체 범위입니다", STATUS_GREEN);
            }
        });
    }

    /*
     * 통합 검색:
     * 건물 이름 + 식당/카페/편의시설 이름 + 카테고리를 전부 검색합니다.
     *
     * 예:
     * "예" → 예음관, 예술대학1
     * "공" → AI 공학관, 공과대학1, 공과대학2
     * "식당" → 제순식당, 쩡이분식, 차이나스푼, 봉구스 밥버거, 포밥인 뉴욕
     * "봉구스" → 봉구스 밥버거
     */
    private void performBuildingSearch() {
        if (mMap == null || buildingSearchInput == null) return;

        String query = buildingSearchInput.getText() == null
                ? ""
                : buildingSearchInput.getText().toString().trim();

        if (query.isEmpty()) {
            clearBuildingSearch();
            return;
        }

        ArrayList<SearchCandidate> results = findSearchCandidates(query);

        if (results.isEmpty()) {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            setStatusCard("검색 실패", "검색어가 들어간 장소를 찾을 수 없습니다", STATUS_RED);
            return;
        }

        hideKeyboard();

        if (results.size() == 1) {
            selectSearchCandidate(results.get(0), true);
        } else {
            showSearchChoicePanel(query, results);
        }
    }

    private ArrayList<SearchCandidate> findSearchCandidates(String query) {
        ArrayList<SearchCandidate> results = new ArrayList<>();
        String normalizedQuery = normalizeSearchText(query);

        if (normalizedQuery.isEmpty()) {
            return results;
        }

        int index = 0;

        for (MapZone zone : buildingZones) {
            String searchText = getZoneSearchText(zone);

            if (normalizeSearchText(searchText).contains(normalizedQuery)) {
                results.add(SearchCandidate.forBuilding(zone, searchText, index));
                index++;
            }
        }

        for (PlaceInfo place : placeInfos) {
            String searchText = getPlaceSearchText(place);

            if (normalizeSearchText(searchText).contains(normalizedQuery)) {
                results.add(SearchCandidate.forPlace(place, searchText, index));
                index++;
            }
        }

        Collections.sort(results, new Comparator<SearchCandidate>() {
            @Override
            public int compare(SearchCandidate a, SearchCandidate b) {
                int rankA = getSearchRank(a, normalizedQuery);
                int rankB = getSearchRank(b, normalizedQuery);

                if (rankA != rankB) {
                    return rankA - rankB;
                }

                int lengthA = normalizeSearchText(a.displayName).length();
                int lengthB = normalizeSearchText(b.displayName).length();

                if (lengthA != lengthB) {
                    return lengthA - lengthB;
                }

                if (a.typeOrder != b.typeOrder) {
                    return a.typeOrder - b.typeOrder;
                }

                return a.displayName.compareTo(b.displayName);
            }
        });

        return results;
    }

    private int getSearchRank(SearchCandidate candidate, String normalizedQuery) {
        String normalizedName = normalizeSearchText(candidate.displayName);
        String normalizedSearchText = normalizeSearchText(candidate.searchText);

        if (normalizedName.equals(normalizedQuery)) {
            return 0;
        }

        if (normalizedName.contains(normalizedQuery)) {
            return 1;
        }

        if (normalizedSearchText.contains(normalizedQuery)) {
            return 2;
        }

        return 9;
    }

    private String getZoneSearchText(MapZone zone) {
        if (zone == null || zone.name == null) return "";

        String text = zone.name;

        if ("공과대학1".equals(zone.name)) {
            text += " 공과대학 1 공대1 공대 1 공학관1";
        } else if ("공과대학2".equals(zone.name)) {
            text += " 공과대학 2 공대2 공대 2 공학관2";
        } else if ("AI 공학관".equals(zone.name)) {
            text += " AI공학관 AI 공학과 AI공학과 인공지능 공학관";
        } else if ("예음관".equals(zone.name)) {
            text += " 예 음 예음";
        } else if ("예술대학1".equals(zone.name)) {
            text += " 예술대학 1 예대1 예대 1 예술관1";
        } else if ("AI도서관".equals(zone.name)) {
            text += " AI 도서관 에이아이도서관";
        }

        return text;
    }

    private String getPlaceSearchText(PlaceInfo place) {
        if (place == null) return "";

        return safe(place.name)
                + " " + safe(place.category)
                + " " + safe(place.floorShortName)
                + " " + safe(place.floorFullName);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeSearchText(String value) {
        if (value == null) return "";

        return value
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "")
                .replace("/", "")
                .replace("·", "")
                .toLowerCase(Locale.KOREAN)
                .trim();
    }

    private void showSearchChoicePanel(String query, ArrayList<SearchCandidate> results) {
        if (placeChoicePanel == null || placeChoiceTitle == null || placeChoiceButtonContainer == null) {
            return;
        }

        holdStatusUntilMillis = System.currentTimeMillis() + 15000L;

        placeChoiceTitle.setText("'" + query + "' 검색 결과 " + results.size() + "개");
        placeChoiceButtonContainer.removeAllViews();

        for (SearchCandidate candidate : results) {
            TextView button = createSearchChoiceButton(candidate);
            placeChoiceButtonContainer.addView(button);
        }

        placeChoicePanel.setVisibility(View.VISIBLE);
        moveChoicePanelToSearchArea();

        setStatusCard(
                "검색 결과",
                "후보 중 원하는 장소를 선택하세요",
                STATUS_ORANGE
        );
    }

    private TextView createSearchChoiceButton(SearchCandidate candidate) {
        TextView button = new TextView(this);

        button.setText(candidate.buttonText);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dp(150));
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setSingleLine(false);
        button.setClickable(true);

        GradientDrawable background = new GradientDrawable();

        if (candidate.zone != null) {
            background.setColor(STATUS_BLUE);
        } else {
            background.setColor(STATUS_ORANGE);
        }

        background.setCornerRadius(dp(12));
        button.setBackground(background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(6);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> selectSearchCandidate(candidate, true));

        return button;
    }

    private void moveChoicePanelToSearchArea() {
        if (placeChoicePanel == null || mapContainer == null) return;

        placeChoicePanel.post(() -> {
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) placeChoicePanel.getLayoutParams();

            params.leftMargin = dp(10);
            params.topMargin = dp(80);

            placeChoicePanel.setLayoutParams(params);
        });
    }

    private void selectSearchCandidate(SearchCandidate candidate, boolean fromSearch) {
        if (candidate == null) return;

        hidePlaceChoicePanel();

        if (buildingSearchInput != null) {
            buildingSearchInput.setText(candidate.displayName);
            buildingSearchInput.setSelection(buildingSearchInput.getText().length());
        }

        if (candidate.zone != null) {
            showBuildingSearchResult(candidate.zone);
        } else if (candidate.place != null) {
            showPlaceSearchResult(candidate.place, fromSearch);
        }
    }

    private void showBuildingSearchResult(MapZone selectedZone) {
        if (selectedZone == null || selectedZone.polygon == null) return;

        selectedBuildingName = selectedZone.name;
        selectedPlaceInfo = null;
        lastStatus = "";
        holdStatusUntilMillis = System.currentTimeMillis() + 15000L;

        hidePlaceChoicePanel();
        restoreAllMapOverlays();
        resetAllPlaceMarkerIcons();
        resetZoneColors();

        highlightSearchBuilding(selectedZone);
        moveCameraToZone(selectedZone);

        setStatusCard(
                selectedZone.name,
                "검색한 건물을 노란색으로 표시했습니다",
                STATUS_BLUE
        );
    }

    private void showPlaceSearchResult(PlaceInfo place, boolean fromSearch) {
        if (place == null) return;

        selectedBuildingName = "";
        selectedPlaceInfo = place;
        lastStatus = "";
        holdStatusUntilMillis = System.currentTimeMillis() + 15000L;

        hidePlaceChoicePanel();
        restoreAllMapOverlays();
        resetZoneColors();
        resetAllPlaceMarkerIcons();

        highlightSearchPlaceMarker(place);
        moveCameraToPlace(place);

        preferences.edit()
                .putString(PREF_LAST_PLACE, place.name + " " + place.floorShortName)
                .apply();

        String message;
        if (fromSearch) {
            message = place.floorFullName + " · 검색한 장소를 노란색으로 표시했습니다";
        } else {
            message = place.floorFullName + " · 선택한 장소를 노란색으로 표시했습니다";
        }

        setStatusCard(
                place.name,
                message,
                STATUS_ORANGE
        );
    }

    private void highlightSearchPlaceMarker(PlaceInfo place) {
        if (place == null || place.marker == null) return;

        PlaceMarkerGroup group = findMarkerGroupByPlace(place);

        if (group == null || group.marker == null) {
            return;
        }

        group.marker.setIcon(createSelectedPlaceDotIcon());
        group.marker.setTitle(place.name);
        group.marker.setSnippet(place.floorFullName);
        group.marker.setZIndex(40f);
        group.marker.setVisible(true);
        group.marker.showInfoWindow();
    }

    private PlaceMarkerGroup findMarkerGroupByPlace(PlaceInfo place) {
        if (place == null) return null;

        for (PlaceMarkerGroup group : placeMarkerGroups) {
            for (PlaceInfo item : group.places) {
                if (item == place) {
                    return group;
                }
            }
        }

        return null;
    }

    private void resetAllPlaceMarkerIcons() {
        for (PlaceMarkerGroup group : placeMarkerGroups) {
            if (group.marker != null) {
                group.marker.setIcon(createPlaceGroupDotIcon(
                        getGroupAccentColor(group),
                        group.places.size()
                ));
                group.marker.setTitle("편의시설");
                group.marker.setSnippet("주황색 편의시설을 눌러 선택");
                group.marker.setZIndex(20f);
            }
        }
    }

    private void clearBuildingSearch() {
        selectedBuildingName = "";
        selectedPlaceInfo = null;
        holdStatusUntilMillis = 0L;
        lastStatus = "";

        hidePlaceChoicePanel();

        if (buildingSearchInput != null) {
            buildingSearchInput.setText("");
            buildingSearchInput.setHint("검색");
        }

        restoreAllMapOverlays();
        resetAllPlaceMarkerIcons();
        resetZoneColors();
        fitGachonBoundaryToScreen();

        setStatusCard("가천대 생활지도", "검색이 초기화되었습니다", STATUS_DARK);
    }

    private boolean isBuildingSearchActive() {
        return selectedBuildingName != null && !selectedBuildingName.trim().isEmpty();
    }

    private void restoreAllMapOverlays() {
        if (gachonPolygon != null) {
            gachonPolygon.setVisible(true);
        }

        for (MapZone zone : buildingZones) {
            if (zone.polygon != null) {
                zone.polygon.setVisible(true);
            }
        }

        for (MapZone zone : smokingZones) {
            if (zone.polygon != null) {
                zone.polygon.setVisible(true);
            }
        }

        for (PlaceMarkerGroup group : placeMarkerGroups) {
            if (group.marker != null) {
                group.marker.setVisible(true);
            }
        }
    }

    private void moveCameraToZone(MapZone zone) {
        if (mMap == null || zone == null || zone.points == null || zone.points.isEmpty()) return;

        LatLngBounds bounds = getBoundsFromPoints(zone.points);

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        bounds.getCenter(),
                        BUILDING_SEARCH_ZOOM
                )
        );
    }

    private void moveCameraToPlace(PlaceInfo place) {
        if (mMap == null || place == null || place.position == null) return;

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        place.position,
                        PLACE_SEARCH_ZOOM
                )
        );
    }

    private void showCurrentLocationFromBuildingTap(MapZone tappedZone) {
        if (!isNetworkAvailable()) {
            setStatusCard("네트워크 연결 필요", "인터넷 연결을 확인해 주세요", STATUS_RED);
            return;
        }

        if (!hasLocationPermission()) {
            setStatusCard("위치 권한 필요", "권한을 허용해야 현재 위치를 확인할 수 있습니다", STATUS_RED);
            startLocationCheck();
            return;
        }

        holdStatusUntilMillis = System.currentTimeMillis() + 10000L;

        if (latestLocation != null) {
            showCurrentLocationStatus(latestLocation);
        } else {
            setStatusCard(tappedZone.name, "현재 위치를 확인하는 중입니다", STATUS_DARK);
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            latestLocation = location;
                            showCurrentLocationStatus(location);
                        } else {
                            setStatusCard("현재 위치 확인 중", "잠시 후 다시 건물을 터치해 주세요", STATUS_DARK);
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void showCurrentLocationStatus(Location location) {
        if (location == null) return;

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        String subMessage = buildCurrentLocationMessage(currentLocation, location);

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
        setStatusCard("현재 나의 위치", subMessage, STATUS_DARK);
        lastStatus = "현재 나의 위치" + subMessage;
    }

    private String buildCurrentLocationMessage(LatLng currentLocation, Location rawLocation) {
        MapZone currentSmokingZone = findCurrentZone(currentLocation, smokingZones);
        if (currentSmokingZone != null) {
            return "흡연구역에 있습니다";
        }

        MapZone currentBuildingZone = findCurrentBuildingZone(currentLocation, rawLocation);
        if (currentBuildingZone != null) {
            return currentBuildingZone.name + "에 있습니다";
        }

        if (isPointInsidePolygon(currentLocation, gachonBoundary)) {
            return "캠퍼스 내부에 있습니다";
        }

        return "캠퍼스 경계 밖에 있습니다";
    }

    private void hideKeyboard() {
        if (buildingSearchInput == null) return;

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(buildingSearchInput.getWindowToken(), 0);
        }

        buildingSearchInput.clearFocus();
    }

    private void showPlaceChoicePanel(PlaceMarkerGroup group) {
        if (placeChoicePanel == null || placeChoiceTitle == null || placeChoiceButtonContainer == null) {
            return;
        }

        holdStatusUntilMillis = System.currentTimeMillis() + 10000L;

        placeChoiceTitle.setText("편의시설");
        placeChoiceButtonContainer.removeAllViews();

        for (PlaceInfo place : group.places) {
            TextView button = createPlaceChoiceButton(place);
            placeChoiceButtonContainer.addView(button);
        }

        placeChoicePanel.setVisibility(View.VISIBLE);
        moveChoicePanelNearMarker(group);
    }

    private TextView createPlaceChoiceButton(PlaceInfo place) {
        TextView button = new TextView(this);

        button.setText(place.name);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dp(132));
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setSingleLine(true);
        button.setClickable(true);

        GradientDrawable background = new GradientDrawable();
        background.setColor(place.accentColor);
        background.setCornerRadius(dp(12));
        button.setBackground(background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(6);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            hidePlaceChoicePanel();
            showSelectedPlaceStatus(place);
        });

        return button;
    }

    private void moveChoicePanelNearMarker(PlaceMarkerGroup group) {
        if (mMap == null || mapContainer == null || placeChoicePanel == null || group == null) {
            return;
        }

        Point point = mMap.getProjection().toScreenLocation(group.position);

        placeChoicePanel.post(() -> {
            int panelWidth = placeChoicePanel.getWidth();
            int panelHeight = placeChoicePanel.getHeight();
            int containerWidth = mapContainer.getWidth();
            int containerHeight = mapContainer.getHeight();

            int margin = dp(8);
            int rightOffset = dp(24);

            int left = point.x + rightOffset;
            int top = point.y - panelHeight / 2;

            if (left + panelWidth > containerWidth - margin) {
                left = point.x - panelWidth - rightOffset;
            }

            if (left < margin) left = margin;
            if (top < margin) top = margin;

            if (top + panelHeight > containerHeight - margin) {
                top = containerHeight - panelHeight - margin;
            }

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) placeChoicePanel.getLayoutParams();

            params.leftMargin = left;
            params.topMargin = top;
            placeChoicePanel.setLayoutParams(params);
        });
    }

    private void hidePlaceChoicePanel() {
        if (placeChoicePanel != null) {
            placeChoicePanel.setVisibility(View.GONE);
        }
    }

    private void showSelectedPlaceStatus(PlaceInfo place) {
        showPlaceSearchResult(place, false);
    }

    private void showSavedOpeningMessage() {
        String lastPlace = preferences.getString(PREF_LAST_PLACE, "");

        if (lastPlace == null || lastPlace.trim().isEmpty()) {
            setStatusCard(
                    "가천대 생활지도",
                    "건물, 식당, 카페, 편의시설을 검색해보세요",
                    STATUS_DARK
            );
        } else {
            setStatusCard(
                    "가천대 생활지도",
                    "최근 확인: " + lastPlace + " · 검색을 사용할 수 있어요",
                    STATUS_DARK
            );
        }
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null || networkCallback != null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                mainHandler.post(() -> {
                    setStatusCard("위치 확인 중", "네트워크가 연결되었습니다", STATUS_DARK);
                    startMapServices();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                mainHandler.post(() -> {
                    if (!isNetworkAvailable()) {
                        stopLocationUpdates();
                        resetZoneColors();
                        resetAllPlaceMarkerIcons();
                        hidePlaceChoicePanel();
                        lastStatus = "";
                        setStatusCard("네트워크 연결 필요", "인터넷 연결을 확인해 주세요", STATUS_RED);
                        Toast.makeText(MapActivity.this, "인터넷 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMapServices() {
        if (!isNetworkAvailable()) {
            stopLocationUpdates();
            setStatusCard("네트워크 연결 필요", "인터넷 연결을 확인해 주세요", STATUS_RED);
            return;
        }

        startLocationCheck();
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) return false;

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    private void startLocationCheck() {
        if (!isNetworkAvailable()) {
            setStatusCard("네트워크 연결 필요", "인터넷 연결을 확인해 주세요", STATUS_RED);
            return;
        }

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        startLocationUpdates();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission() || !isNetworkAvailable() || isRequestingLocationUpdates) return;

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocationStatus(location);
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
        )
                .setMinUpdateIntervalMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                if (location != null) {
                    updateLocationStatus(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            isRequestingLocationUpdates = true;
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        isRequestingLocationUpdates = false;
    }

    private void updateLocationStatus(Location location) {
        latestLocation = location;

        if (System.currentTimeMillis() < holdStatusUntilMillis) return;
        if (isBuildingSearchActive()) return;

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        resetZoneColors();

        boolean isInsideGachon = isPointInsidePolygon(currentLocation, gachonBoundary);
        MapZone currentSmokingZone = findCurrentZone(currentLocation, smokingZones);
        MapZone currentBuildingZone = findCurrentBuildingZone(currentLocation, location);

        String displayName;
        String subMessage;
        int backgroundColor;

        if (currentSmokingZone != null) {
            highlightZone(currentSmokingZone);
            displayName = "흡연구역";
            subMessage = "회색 영역 안에 있습니다";
            backgroundColor = STATUS_GRAY;

        } else if (currentBuildingZone != null) {
            highlightZone(currentBuildingZone);
            displayName = currentBuildingZone.name;
            subMessage = "";
            backgroundColor = STATUS_BLUE;

        } else if (isInsideGachon) {
            displayName = "가천대학교";
            subMessage = "캠퍼스 내부에 있습니다";
            backgroundColor = STATUS_GREEN;

        } else {
            displayName = "학교 밖";
            subMessage = "캠퍼스 경계 밖에 있습니다";
            backgroundColor = STATUS_RED;
        }

        String currentStatusKey = displayName + subMessage;
        if (!currentStatusKey.equals(lastStatus)) {
            setStatusCard(displayName, subMessage, backgroundColor);
            lastStatus = currentStatusKey;
        }
    }

    private MapZone findCurrentZone(LatLng point, List<MapZone> zones) {
        for (MapZone zone : zones) {
            if (isPointInsidePolygon(point, zone.points)) {
                return zone;
            }
        }
        return null;
    }

    private MapZone findCurrentBuildingZone(LatLng point, Location location) {
        MapZone exactZone = findCurrentZone(point, buildingZones);
        if (exactZone != null) return exactZone;

        float toleranceMeters = BUILDING_ZONE_TOLERANCE_METERS;
        if (location != null && location.hasAccuracy()) {
            toleranceMeters = Math.min(
                    30f,
                    Math.max(BUILDING_ZONE_TOLERANCE_METERS, location.getAccuracy() * 0.45f)
            );
        }

        MapZone nearestZone = null;
        float nearestDistance = Float.MAX_VALUE;

        for (MapZone zone : buildingZones) {
            float distance = distanceToPolygonMeters(point, zone.points);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestZone = zone;
            }
        }

        if (nearestZone != null && nearestDistance <= toleranceMeters) {
            return nearestZone;
        }

        return null;
    }

    private float distanceToPolygonMeters(LatLng point, List<LatLng> polygon) {
        if (polygon == null || polygon.size() < 2) return Float.MAX_VALUE;

        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < polygon.size(); i++) {
            LatLng start = polygon.get(i);
            LatLng end = polygon.get((i + 1) % polygon.size());
            float distance = distanceToSegmentMeters(point, start, end);

            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        return minDistance;
    }

    private float distanceToSegmentMeters(LatLng point, LatLng start, LatLng end) {
        double baseLat = Math.toRadians(point.latitude);
        double metersPerDegreeLat = 110540.0;
        double metersPerDegreeLng = 111320.0 * Math.cos(baseLat);

        double startX = (start.longitude - point.longitude) * metersPerDegreeLng;
        double startY = (start.latitude - point.latitude) * metersPerDegreeLat;
        double endX = (end.longitude - point.longitude) * metersPerDegreeLng;
        double endY = (end.latitude - point.latitude) * metersPerDegreeLat;

        double dx = endX - startX;
        double dy = endY - startY;

        if (dx == 0.0 && dy == 0.0) {
            return (float) Math.sqrt(startX * startX + startY * startY);
        }

        double t = -((startX * dx) + (startY * dy)) / ((dx * dx) + (dy * dy));
        t = Math.max(0.0, Math.min(1.0, t));

        double closestX = startX + t * dx;
        double closestY = startY + t * dy;

        return (float) Math.sqrt(closestX * closestX + closestY * closestY);
    }

    private void resetZoneColors() {
        if (gachonPolygon != null) {
            gachonPolygon.setStrokeColor(CAMPUS_STROKE);
            gachonPolygon.setFillColor(CAMPUS_FILL);
            gachonPolygon.setStrokeWidth(5f);
            gachonPolygon.setZIndex(1f);
        }

        for (MapZone zone : buildingZones) {
            if (zone.polygon != null) {
                zone.polygon.setStrokeColor(BUILDING_STROKE);
                zone.polygon.setFillColor(BUILDING_FILL);
                zone.polygon.setStrokeWidth(4f);
                zone.polygon.setZIndex(2f);
            }
        }

        for (MapZone zone : smokingZones) {
            if (zone.polygon != null) {
                zone.polygon.setStrokeColor(SMOKING_STROKE);
                zone.polygon.setFillColor(SMOKING_FILL);
                zone.polygon.setStrokeWidth(4f);
                zone.polygon.setZIndex(3f);
            }
        }
    }

    private void highlightZone(MapZone zone) {
        if (zone.polygon == null) return;

        if (zone.isSmokingZone) {
            zone.polygon.setFillColor(SMOKING_SELECTED_FILL);
            zone.polygon.setStrokeWidth(8f);
            zone.polygon.setZIndex(5f);
        } else {
            zone.polygon.setFillColor(BUILDING_SELECTED_FILL);
            zone.polygon.setStrokeWidth(8f);
            zone.polygon.setZIndex(5f);
        }
    }

    private void highlightSearchBuilding(MapZone zone) {
        if (zone == null || zone.polygon == null) return;

        zone.polygon.setStrokeColor(BUILDING_SEARCH_STROKE);
        zone.polygon.setFillColor(BUILDING_SEARCH_FILL);
        zone.polygon.setStrokeWidth(9f);
        zone.polygon.setZIndex(6f);
    }

    private void fitGachonBoundaryToScreen() {
        if (mMap == null) return;

        LatLngBounds bounds = getBoundsFromPoints(gachonBoundary);

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        dp(START_BOUNDS_PADDING_DP)
                )
        );
    }

    private LatLngBounds getBoundsFromPoints(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng point : points) {
            builder.include(point);
        }

        return builder.build();
    }

    private void setStatusCard(String title, String subMessage, int backgroundColor) {
        if (statusText == null) return;

        if (subMessage == null || subMessage.trim().isEmpty()) {
            statusText.setText(title);
        } else {
            statusText.setText(title + "\n" + subMessage);
        }

        statusText.setTextColor(SOFT_TEXT_LIGHT);
        statusText.setTextSize(14.5f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setSingleLine(false);
        statusText.setMaxLines(Integer.MAX_VALUE);
        statusText.setIncludeFontPadding(true);
        statusText.setLineSpacing(dp(2), 1.0f);
        statusText.setMinHeight(dp(88));
        statusText.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(dp(18));

        statusText.setBackground(background);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean isPointInsidePolygon(LatLng point, List<LatLng> polygon) {
        boolean inside = false;

        double x = point.longitude;
        double y = point.latitude;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i).longitude;
            double yi = polygon.get(i).latitude;
            double xj = polygon.get(j).longitude;
            double yj = polygon.get(j).latitude;

            boolean intersect =
                    ((yi > y) != (yj > y))
                            && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) return;

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            handleLightSensor(event.values[0]);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleShakeSensor(event.values);
        }
    }

    private void handleLightSensor(float lux) {
        boolean shouldUseDarkMode = lux < 15f;

        if (shouldUseDarkMode != isDarkComfortMode) {
            isDarkComfortMode = shouldUseDarkMode;
            applyComfortableMapStyle(isDarkComfortMode);
        }
    }

    private void handleShakeSensor(float[] values) {
        if (values == null || values.length < 3) return;

        float x = values[0];
        float y = values[1];
        float z = values[2];
        double force = Math.sqrt(x * x + y * y + z * z);

        long now = System.currentTimeMillis();
        if (force > 22.0 && now - lastShakeTime > 1800) {
            lastShakeTime = now;
            hidePlaceChoicePanel();
            selectedBuildingName = "";
            selectedPlaceInfo = null;

            if (buildingSearchInput != null) {
                buildingSearchInput.setText("");
                buildingSearchInput.setHint("검색");
            }

            if (gachonPolygon != null) {
                gachonPolygon.setVisible(true);
            }

            for (MapZone zone : buildingZones) {
                if (zone.polygon != null) {
                    zone.polygon.setVisible(true);
                }
            }

            for (MapZone zone : smokingZones) {
                if (zone.polygon != null) {
                    zone.polygon.setVisible(true);
                }
            }

            for (PlaceMarkerGroup group : placeMarkerGroups) {
                if (group.marker != null) {
                    group.marker.setVisible(true);
                }
            }

            resetAllPlaceMarkerIcons();
            resetZoneColors();
            fitGachonBoundaryToScreen();
            setStatusCard("지도 화면 초기화", "휴대폰 흔들림을 감지했습니다", STATUS_DARK);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (hasLocationPermission()) {
                startMapServices();
            } else {
                setStatusCard("위치 권한 필요", "권한을 허용해야 위치 확인이 가능합니다", STATUS_RED);
                Toast.makeText(this, "위치 권한을 허용해야 위치 확인이 가능합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();

        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MapZone {
        String name;
        List<LatLng> points;
        boolean isSmokingZone;
        Polygon polygon;

        MapZone(String name, List<LatLng> points, boolean isSmokingZone) {
            this.name = name;
            this.points = points;
            this.isSmokingZone = isSmokingZone;
        }
    }

    private static class PlaceMarkerGroup {
        LatLng position;
        List<PlaceInfo> places = new ArrayList<>();
        Marker marker;

        PlaceMarkerGroup(LatLng position) {
            this.position = position;
        }
    }

    private static class PlaceInfo {
        String name;
        String category;
        String floorShortName;
        String floorFullName;
        String description;
        LatLng position;
        int accentColor;
        Marker marker;

        PlaceInfo(
                String name,
                String category,
                String floorShortName,
                String floorFullName,
                String description,
                LatLng position,
                int accentColor
        ) {
            this.name = name;
            this.category = category;
            this.floorShortName = floorShortName;
            this.floorFullName = floorFullName;
            this.description = description;
            this.position = position;
            this.accentColor = accentColor;
        }
    }

    private static class SearchCandidate {
        String displayName;
        String buttonText;
        String searchText;
        int typeOrder;
        int originalIndex;
        MapZone zone;
        PlaceInfo place;

        static SearchCandidate forBuilding(MapZone zone, String searchText, int originalIndex) {
            SearchCandidate candidate = new SearchCandidate();
            candidate.zone = zone;
            candidate.place = null;
            candidate.displayName = zone.name;
            candidate.buttonText = "[건물] " + zone.name;
            candidate.searchText = searchText;
            candidate.typeOrder = 0;
            candidate.originalIndex = originalIndex;
            return candidate;
        }

        static SearchCandidate forPlace(PlaceInfo place, String searchText, int originalIndex) {
            SearchCandidate candidate = new SearchCandidate();
            candidate.zone = null;
            candidate.place = place;
            candidate.displayName = place.name;
            candidate.buttonText = "[" + place.category + "] " + place.name + " · " + place.floorShortName;
            candidate.searchText = searchText;
            candidate.typeOrder = 1;
            candidate.originalIndex = originalIndex;
            return candidate;
        }
    }
}