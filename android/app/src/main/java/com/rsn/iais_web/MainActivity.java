package com.rsn.iais_web;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rsn.iais_web.Util.FileUtils;
import com.rsn.iais_web.Util.JavaScriptInterface;
import com.rsn.iais_web.Util.UserProp;
import com.rsn.iais_web.Util.Utility;
import com.rsn.iais_web.api.ServerApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.rsn.iais_web.BuildConfig.END_POINT;


public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    //private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;

    private RelativeLayout mOfflineLayout;
    private Button mTryButton;

    private boolean fromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        fromNotification = intent.getBooleanExtra("fromNotification",false);
        Log.e("arsen", "mainActivity "+fromNotification);


        // Subscribe topic
        Log.d("PAC", "Subscribe FCM topic");
        Log.e("arsen", "onCreate: FCM token "+ UserProp.getFcmToken(this));

        if (UserProp.getFcmToken(this) != null){
            Log.e("arsen", "onCreate: ada token");
            ServerApi.getUserDataByToken(
                    UserProp.getFcmToken(this),
                    this,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("arsen", "LoginActivity getDataToken onResponse:" +
                                    "\n\t" + response.toString() + "\n_");
                            try {
                                JSONObject data = response.getJSONObject("data");
                                String ver_app = data.getString("versi_app");
                                Log.e("arsen", "ver app dari server: "+ver_app);
                                if (!ver_app.equals(BuildConfig.VERSION_NAME)) {
                                    //if not equal then update versi app on server
                                    Utility.updateVersiApp(UserProp.getFcmToken(MainActivity.this),BuildConfig.VERSION_NAME,MainActivity.this);
                                }else {
                                    Log.e("arsen", "else, berarti versi app di server dan lokal sama");
                                }
                            } catch (JSONException e) {
                                Log.e("Sales", "onResponse: fail try catch");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("arsen", "LoginActivity getDataToken onErrorResponse:" +
                                    "\n\t" + error.toString() + "\n_");
                            if (error.networkResponse != null) {
                                try {
                                    String body = new String(error.networkResponse.data,"UTF-8");
                                    JSONObject jsonObj = new JSONObject(body);
                                    Log.d("arsen", "LoginActivity insertFcmToken onErrorResponse: " + error.toString() +
                                            "\n\theader status: " + error.networkResponse.statusCode +
                                            "\n\tbody: " + body +
                                            "\n\tbody error: " + jsonObj.getString("error") +
                                            "\n\tbody message: " + jsonObj.getString("message")
                                    );
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
        }

        FirebaseMessaging.getInstance()
                .subscribeToTopic("blast")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Sales", "Success to subscibe on topic: broadcast");
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Sales", "Fail to subscibe on topic: broadcast");
                    }
                });

        if (UserProp.getFcmToken(this) == null){
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("arsen", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            UserProp.saveFcmToken(MainActivity.this,token);
                            Log.e("arsen", "onComplete: fcm ready "+token);
//                            Utility.sendRegistrationToServer(token, MainActivity.this);
//                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setBackgroundColor(0xff9a9a9a);

        mOfflineLayout = findViewById(R.id.offline_layout);
        mTryButton = findViewById(R.id.retrybutton);
        mTryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebSource();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.OFF);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        //mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setUserAgentString("Android Mozilla/5.0 AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().supportZoom();

        //mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.setWebViewClient(new PolytronWebViewClient(MainActivity.this));
        //mWebView.setWebViewClient(new WriteHandlingWebViewClient(mWebView));
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        mWebView.setWebChromeClient(new ChromeClient());

        // Handle downloading
        mWebView.setDownloadListener(new WebViewDownloadListener());

        openWebSource();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupTransparentSystemBarsForLmp(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onBackPressed() {
//        if (onBackPressedListener != null){
//            onBackPressedListener.doBack();
//        } else {
            if (mWebView != null && mWebView.canGoBack())
                mWebView.goBack();
            else
                super.onBackPressed();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        //AppLogger.writeDebug("onActivityResult");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
//                super.onActivityResult(requestCode, resultCode, data);
//                return;
//            }
//
//            Uri[] results = null;
//            // Check that the response is a good one
//            if (resultCode == Activity.RESULT_OK) {
//                //AppLogger.writeDebug("onActivityResult Activity.RESULT_OK");
//                if (data == null) {
//                    if (mCameraPhotoPath != null) {
////                        setImageOrietation(Uri.parse(mCameraPhotoPath));
//                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
//                    }
//                } else {
//                    //AppLogger.writeDebug("onActivityResult data != null");
//                    String dataString = data.getDataString();
//                    String converted = FileUtils.getRealPath(MainActivity.this, Uri.parse(dataString));
//                    Log.e("arsen", "data str: "+ converted);
//                    if (dataString != null) {
//                        results = new Uri[]{Uri.parse(dataString)};
//                    }
//                }
//            }
//
//            Log.e("arsen", "onActivityResult: asdasd "+results[0].toString());
//
//            mFilePathCallback.onReceiveValue(results);
//            mFilePathCallback = null;
//        }

        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mFilePathCallback == null) return;
            Log.e("arsen", "onActivityResult: "+data.getDataString());
            mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            mFilePathCallback = null;
        }
    }

    private boolean appInstalled(String app) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(app, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void openWebSource() {
        Log.d("PFS", "openWebSource: URL: " + END_POINT);
        if (!DetectConnection.checkInternetConnection(this)) {
            mOfflineLayout.setVisibility(View.VISIBLE);
        } else {
            mOfflineLayout.setVisibility(View.GONE);
            String final_url = "";
            if (fromNotification){
                String email = UserProp.getUserId(this);
                if (email != null){
                    Log.e("arsen", "openWebSource: "+email);
                    final_url = END_POINT + "members/" + email + "/notification/invitation";
                }else {
                    final_url = END_POINT;
                }
            }else final_url = END_POINT;
            mWebView.loadUrl(final_url);

            //mWebView.loadUrl("https://pfs.staging.polytron.angkasa.id/store");
            //mWebView.loadUrl("https://pfs.polytron.co.id");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "psf_" + timeStamp;
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
//        File imageFile = new File(storageDir + imageFileName + ".jpg");
//        /*
//        File imageFile = File.createTempFile(
//                imageFileName,  // prefix
//                ".jpg",         // suffix
//                storageDir      // directory
//        );
//        */
//
//        mCameraPhotoPath = "file:" + imageFile.getAbsolutePath();
//
//        return imageFile;
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
                //setStatusBarColorMethod.invoke(window, 0xffb71c1c);//Color.TRANSPARENT);
                setStatusBarColorMethod.invoke(window, Color.TRANSPARENT);
                //setNavigationBarColorMethod.invoke(window, 0xbb000000);//Color.TRANSPARENT);
                setNavigationBarColorMethod.invoke(window, Color.BLACK);
            } catch (NoSuchFieldException e) {
            } catch (NoSuchMethodException ex) {
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            } finally {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            //super.onPermissionRequest(request);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                request.grant(request.getResources());
            }
        }

        // For Android 5.0
        //@Override
//        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
//            // Double check that we don't have any existing callbacks
//            Log.e("arsen", "onShowFileChooser: ");
//            if (mFilePathCallback != null) {
//                mFilePathCallback.onReceiveValue(null);
//            }
//            mFilePathCallback = filePath;
//
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            //takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);//android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                // Create the File where the photo should go
//                File photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                } catch (IOException ex) {
//                    // Error occurred while creating the File
//                    Log.e("arsen", "Unable to create Image File", ex);
//                }
//
//                // Continue only if the File was successfully created
//                if (photoFile != null) {
////                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
////
////                    Uri photoURI = FileProvider.getUriForFile(
////                            MainActivity.this,
////                            "com.rsn.iais_web.provider",
////                            photoFile);
////
////                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", photoFile));
//
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", photoFile));
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriFromPath(MainActivity.this, mCameraPhotoPath));
//
//                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                            Uri.fromFile(photoFile));
//                } else {
//                    takePictureIntent = null;
//                }
//            } else {
//                Toast.makeText(MainActivity.this, "Error can not open camera", Toast.LENGTH_LONG).show();
//            }
//
//            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
//            contentSelectionIntent.setType("image/*");
//
//            Intent[] intentArray;
//            if (takePictureIntent != null) {
//                intentArray = new Intent[]{takePictureIntent};
//            } else {
//                intentArray = new Intent[0];
//            }
//
//            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//            chooserIntent.putExtra(Intent.EXTRA_TITLE, getResources().getString(R.string.image_chooser));
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
//
//            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
//
//            return true;
//        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // make sure there is no existing message
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }

            mFilePathCallback = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            } catch (ActivityNotFoundException e) {
                mFilePathCallback = null;
                Toast.makeText(MainActivity.this, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }


        // openFileChooser for Android 3.0+
//        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
//            Log.e("arsen", "openFileChooser: ");
//            mUploadMessage = uploadMsg;
//            // Create AndroidExampleFolder at sdcard
//
//            File imageStorageDir = new File(
//                    Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_PICTURES)
//                    , "AndroidExampleFolder");
//
//            if (!imageStorageDir.exists()) {
//                // Create AndroidExampleFolder at sdcard
//                imageStorageDir.mkdirs();
//            }
//
//            // Create camera captured image file path and name
//            File file = new File(
//                    imageStorageDir + File.separator + "IMG_"
//                            + String.valueOf(System.currentTimeMillis())
//                            + ".jpg");
//
//            /*
//            try {
//                mCapturedImageURI = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", createImageFile());
//            } catch (IOException err) {
//            }
//            */
//            mCapturedImageURI = Uri.fromFile(file);
//
//            // Camera capture image intent
//            final Intent captureIntent = new Intent(
//                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//
//            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
//
//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("image/*");
//
//            // Create file chooser intent
//            Intent chooserIntent = Intent.createChooser(i, getResources().getString(R.string.image_chooser));
//
//            // Set camera intent to file chooser
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
//                    , new Parcelable[] { captureIntent });
//
//            // On select image call onActivityResult method of activity
//            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
//        }
//
//        // openFileChooser for Android < 3.0
//        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
//            openFileChooser(uploadMsg, "");
//        }
//
//        //openFileChooser for other Android versions
//        public void openFileChooser(ValueCallback<Uri> uploadMsg,
//                                    String acceptType,
//                                    String capture) {
//            openFileChooser(uploadMsg, acceptType);
//        }
//
//        @Override
//        public boolean onJsBeforeUnload(WebView view, String url, String message, final JsResult result) {
//            new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme)
//                    .setTitle(R.string.back_dialog_title)
//                    .setMessage(message)
//                    .setPositiveButton(android.R.string.ok,
//                            new DialogInterface.OnClickListener()
//                            {
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    result.confirm();
//                                }
//                            })
//                    .setNegativeButton(android.R.string.cancel,
//                            new DialogInterface.OnClickListener()
//                            {
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    result.cancel();
//                                }
//                            })
//                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialogInterface) {
//                            result.cancel();
//                        }
//                    })
//                    .create()
//                    .show();
//
//            return true;
//        }
    }

    private class WebViewDownloadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            Log.e("arsen", "onDownloadStart: "+url+" "+mimeType);
            if (!url.endsWith(".pdf") && !url.endsWith(".apk") && !url.endsWith(".zip")) {
                mWebView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url));
            }else {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void setImageOrietation(Uri uri) {
        try {
            /*
            // Write exif
            ExifInterface exifInterface = new ExifInterface(uri.getPath());
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    String.valueOf(ExifInterface.ORIENTATION_NORMAL));
            exifInterface.saveAttributes();
            */

            // Read exif
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                //case ExifInterface.ORIENTATION_NORMAL:
                //    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateImage(uri, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    //rotateImage(uri, 180);
                    break;

                default:
                    break;
            }
        } catch (FileNotFoundException err) {
//            AppLogger.writeError( "getExifInfo FileNotFoundException: " + err.toString());
        } catch (IOException err) {
//            AppLogger.writeError( "getExifInfo IOException: " + err.toString());
        }
    }

    private void rotateImage(Uri uri, float degrees) {
        try {
            // Rotate the Bitmap thanks to a rotated matrix. This seems to work.
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            //matrix.postRotate(90);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            // Write back rotated bitmap to current file
            OutputStream os = getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
            //bmp.compress(Bitmap.CompressFormat.PNG,100, os);
        } catch (FileNotFoundException err) {
        } catch (IOException err) {
        } finally {
        }
    }

//    public void setOnBackPressedListener(IOnBackPressListener onBackPressedListener) {
//        this.onBackPressedListener = onBackPressedListener;
//    }
}