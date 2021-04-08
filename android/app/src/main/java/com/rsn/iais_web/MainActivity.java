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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rsn.iais_web.Util.UserProp;
import com.rsn.iais_web.Util.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Subscribe topic
        Log.d("PAC", "Subscribe FCM topic");
        Log.e("arsen", "onCreate: FCM token "+ UserProp.getFcmToken(this));

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
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
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

        mWebView.setWebChromeClient(new ChromeClient());

        // Handle downloading
        mWebView.setDownloadListener(new WebViewDownloadListener());

        openWebSource();
    }

    public class JavaScriptInterface {
        Context mContext;

        // Instantiate the interface and set the context
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        // using Javascript to call the finish activity
        public void closeMyActivity() {
            finish();
        }
        @JavascriptInterface
        public void scanBarcode() { //this
            Log.d("MainActivity","scanBarcode()");
//            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//            integrator.setPrompt("Scan");
//            integrator.setCameraId(0);
//            integrator.setBeepEnabled(false);
//            integrator.setBarcodeImageEnabled(false);
//            integrator.initiateScan();
        }
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
        /*
        if (data == null) {
            AppLogger.writeVerbose("onActivityResult intent data == NULL");
        } else {
        */
        //}
//        IntentResult resultQr = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (resultQr != null) {
//            if (resultQr.getContents() == null) {
//                Log.d("MainActivity", "Cancelled scan");
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
//            } else {
//                Log.d("MainActivity", "Scanned");
//                Toast.makeText(this, "Scanned: " + resultQr.getContents(), Toast.LENGTH_LONG).show();
//                mWebView.evaluateJavascript("javascript:barcodeResult('"+resultQr.getContents()+"');", null);
//            }
//        }

        //AppLogger.writeDebug("onActivityResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                //AppLogger.writeDebug("onActivityResult Activity.RESULT_OK");
                if (data == null) {
//                    AppLogger.writeDebug("onActivityResult data == null");
                    /*
                    File f = new File(FileUtils.getRealPath(MainActivity.this, Uri.parse(mCameraPhotoPath)));
                    if (f != null) {
                        AppLogger.writeVerbose("onActivityResult 05 f != null. f length = " + f.length());
                    }
                    */

                    if (mCameraPhotoPath != null) {
                        setImageOrietation(Uri.parse(mCameraPhotoPath));
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    //AppLogger.writeDebug("onActivityResult data != null");
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), "activity :" + e,
                    //        Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
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
            mWebView.loadUrl(END_POINT);

            //mWebView.loadUrl("https://pfs.staging.polytron.angkasa.id/store");
            //mWebView.loadUrl("https://pfs.polytron.co.id");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "psf_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File imageFile = new File(storageDir + imageFileName + ".jpg");
        /*
        File imageFile = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        */

        mCameraPhotoPath = "file:" + imageFile.getAbsolutePath();

        return imageFile;
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
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
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
//                }
//
//                // Continue only if the File was successfully created
//                if (photoFile != null) {
//                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
//
//                    Uri photoURI = FileProvider.getUriForFile(
//                            MainActivity.this,
//                            "com.hit.pfs.provider",
//                            photoFile);
//
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", photoFile));
//
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", photoFile));
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriFromPath(MainActivity.this, mCameraPhotoPath));
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

            return true;
        }

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
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
        }

        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, final JsResult result) {
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

            return true;
        }
    }

    private class WebViewDownloadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
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