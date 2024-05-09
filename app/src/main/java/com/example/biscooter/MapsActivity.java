package com.example.biscooter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompatSideChannelService;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.example.biscooter.databinding.ActivityMapsBinding;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Button buttonSuruseBasla;
    private AlertDialog.Builder izinVerMesaji;
    private AlertDialog.Builder konumAcMesaji;
    private LocationManager lm;
    //Değişkenlerin tanımlandığı yer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sürüşe Başla düğmesi tanımlama
        buttonSuruseBasla = findViewById(R.id.surusDugmesi);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//Harita hazır olunca çalışacak kodlar
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(18);

        final int istekKodu=1;
        //İzinleri önceden kontrol etme
        //Verilmemişse izinleri iste
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED){
            //Bilgilendirme izinVerMesajiı
            izinVerMesaji=new AlertDialog.Builder(this);
            izinVerMesaji.setMessage("Uygulamanın çalışabilmesi için tam konum izni gereklidir");
            izinVerMesaji.setCancelable(false);
//TODO: Şuraya Recursion eklemek diyalog sorununu çözebilir
            izinVerMesaji.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Bilgilendirme izinVerMesajiındaki Tamam'a basınca konum erişim izni isteme
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},istekKodu);
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},istekKodu+1);
                    if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_DENIED
                            && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_DENIED){
                        izinVerMesaji.create().show();
                    }

                }
            });
                izinVerMesaji.create().show();

        }
        //Mavi noktayı açtık
        mMap.setMyLocationEnabled(true);
        //İzinler verildiğinde
        //Konumu kontrol et, açık değilse açtır
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Konum kapalıysa
        if(!gps_enabled){
            //TODO: Konum aç mesajı için tamam seçeneği ekle buraya
            konumAcMesaji=new AlertDialog.Builder(this);
           konumAcMesaji.setMessage("Uygulamanın çalışması için konumunuzu açmanız gerek");
           konumAcMesaji.setTitle("Konumunuzu açın");
           konumAcMesaji.setCancelable(false);
           konumAcMesaji.setPositiveButton("Konumu aç", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                   //Konumu açtır
                   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   //Eğer konum hala açılmamışsa
                   if(!gps_enabled){
                       konumAcMesaji.create().show();
                   }
               }
           });
                konumAcMesaji.create().show();
        }
        //Konum açık, konum değişikliğini gösterme sıklığı süresi vs.'yi ayarladık
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,20,this);


    }

//Konum güncellemesi, kamerayı oynatma
    @Override
    public void onLocationChanged(@NonNull Location location) {
       final double lat=location.getLatitude();
        final double lg=location.getLongitude();
        LatLng anlıkKonum=new LatLng(lat,lg);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(anlıkKonum));



    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    /**
     * @param provider
     * @param status
     * @param extras
     * @deprecated
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /* deprecated olmasına rağmen implement etmek gerekiyordu ama deprecated
        olduğu için çalışırken de sıkıntı çıkarıyordu içini boşaltınca sorun çözüldü
         */
    }
//Konum açıksa diyaloga tamam düğmesi eklenir, diyalog sorunu çözülür
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        konumAcMesaji.setNegativeButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        konumAcMesaji.create().show();
    }
    //Uygulama içinde konum kapatılırsa konumAc mesajını göster
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        konumAcMesaji.create().show();
    }
//Kamerayı açma kodu
    public void qrGec(View view){
        final int kamera_istek_kodu=3;
        //Kamera izni var mı kontrol et
        final boolean KAMERA_IZNI_VAR_MI=kameraIznıDenetle();
        if(KAMERA_IZNI_VAR_MI) {
        //TODO: Kamera hallolana kadar Scooter'ların konum bilgisine nasıl ulaşacağını araştır
        }
        else {
            //Kamera erişim izni iste
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CAMERA}, kamera_istek_kodu);
        }


}
//Kamera izni olup olmadığını denetler
private boolean kameraIznıDenetle(){
        //İzin yoksa
   if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
       return false;
   }
   else
       return true;
}
}