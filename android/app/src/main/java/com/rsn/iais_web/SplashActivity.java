package com.rsn.iais_web;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
//import android.support.annotation.NonNull;666666
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 5000;
    private boolean cancelRunnable = false;
    private boolean fromNotification = false;

    private Handler handler = new Handler();
    private Runnable splash = new Runnable() {
        @Override
        public void run() {
            if (!cancelRunnable) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                if (fromNotification) mainIntent.putExtra("fromNotification", true);
                else mainIntent.putExtra("fromNotification", false);
                mainIntent.setAction(Intent.ACTION_MAIN);
                startActivity(mainIntent);
            }
            SplashActivity.this.finish();
        }
    };

    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        Log.e("arsen", "splash: "+(b == null));
        if (b != null) fromNotification = true;
        else fromNotification = false;

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //getWindow().setStatusBarColor(Color.RED);

        /***
         try {
         Window window = getWindow();
         window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
         | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
         Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
         window.addFlags(drawsSysBackgroundsField.getInt(null));

         Method setStatusBarColorMethod = Window.class.getDeclaredMethod("setStatusBarColor", int.class);
         Method setNavigationBarColorMethod = Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
         setStatusBarColorMethod.invoke(window, Color.TRANSPARENT);
         setNavigationBarColorMethod.invoke(window, Color.TRANSPARENT);
         } catch (NoSuchFieldException e) {
         } catch (NoSuchMethodException ex) {
         } catch (IllegalAccessException e) {
         } catch (IllegalArgumentException e) {
         } catch (InvocationTargetException e) {
         } finally {
         }
         ***/

        setContentView(R.layout.activity_splash);

        if (ActivityCompat.checkSelfPermission(SplashActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    SplashActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, CAMERA_REQUEST_CODE);
        } else {
            handler.postDelayed(splash, SPLASH_DISPLAY_LENGTH);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupTransparentSystemBarsForLmp(getWindow());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cancelRunnable = false;
        handler.postDelayed(splash, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelRunnable = true;
    }

    /**
     * Sets up transparent navigation and status bars in LMP. This method is a
     * no-op for other platform versions.
     */
    @TargetApi(19)
    protected void setupTransparentSystemBarsForLmp(Window window) {
        // Currently we use reflection to access the flags and the API to set
        // the transparency
        // on the System bars.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                Field drawsSysBackgroundsField = WindowManager.LayoutParams.class.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
                window.addFlags(drawsSysBackgroundsField.getInt(null));

                Method setStatusBarColorMethod = Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                Method setNavigationBarColorMethod = Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
                setStatusBarColorMethod.invoke(window, Color.TRANSPARENT);
                setNavigationBarColorMethod.invoke(window, Color.TRANSPARENT);
            } catch (NoSuchFieldException e) {
            } catch (NoSuchMethodException ex) {
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            } finally {
            }
        }
    }

}
