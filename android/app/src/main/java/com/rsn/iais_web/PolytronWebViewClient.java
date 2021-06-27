package com.rsn.iais_web;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rsn.iais_web.Util.UserProp;
import com.rsn.iais_web.Util.Utility;

import java.net.MalformedURLException;
import java.net.URL;


public class PolytronWebViewClient extends WebViewClient {

    private Context mContext = null;
    private WebView mWebView = null;

    public PolytronWebViewClient(Context context) {
        this(context, null);
    }

    public PolytronWebViewClient(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
//        AppLogger.writeError("url changed: "+url);
        if (url.endsWith("/sales")){
//            AppLogger.writeError("on the Dashboard Page, back will close the app");
//            ((MainActivity)mContext).setOnBackPressedListener(new IOnBackPressListener() {
//                @Override
//                public void doBack() {
//                    new AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
//                            .setTitle(R.string.exit_dialog)
//                            .setMessage(mContext.getResources().getString(R.string.exit_desc))
//                            .setPositiveButton(android.R.string.ok,
//                                    new DialogInterface.OnClickListener()
//                                    {
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            //Exiting application
//                                            ((MainActivity) mContext).finish();
//                                        }
//                                    })
//                            .setNegativeButton(android.R.string.cancel,
//                                    new DialogInterface.OnClickListener()
//                                    {
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            dialog.dismiss();
//                                        }
//                                    })
//                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                @Override
//                                public void onDismiss(DialogInterface dialogInterface) {
////                                    result.cancel();
//                                }
//                            })
//                            .create()
//                            .show();
//                }
//            });
        }else {
//            ((MainActivity)mContext).setOnBackPressedListener(null);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        // If you use https, than you find this useful:
        // rawCookieHeader = cookieManager.getCookie("https://" + parsedURL.getHost()); and working!

        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager != null) {
            try {
                String rawCookieHeader = null;
                URL parsedURL = new URL(url);
                Log.e("arsen", "onPageStarted: "+url);

                // Extract Set-Cookie header value from Android app CookieManager for this URL
                rawCookieHeader = cookieManager.getCookie(parsedURL.getHost());
                if (rawCookieHeader != null) {
//                    AppLogger.writeError("onPageStarted url:\n\t" + url + "\n\tYeah... Cookie found\n\t" + rawCookieHeader + "\n_");
                } else {
//                    AppLogger.writeError("onPageStarted url:\n\t" + url + "\n\tNo Cookie\n_");
                }
            } catch (MalformedURLException err) {

            }
        }
    }

    /***
     @Nullable
     @SuppressWarnings("deprecation") // From API 21 we should use another overload
     @Override
     public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
     //return super.shouldInterceptRequest(view, url);
     boolean intercept = interceptUrl(url);
     AppLogger.writeError("shouldInterceptRequest BEFORE LOLLIPOP url:\n\t" + url + " : " + intercept + "\n_");
     //if (interceptUrl(url)) {
     //    return handleIntercept(url);
     //}
     return super.shouldInterceptRequest(view, url);
     }
     ***/

    /***
    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        //return super.shouldInterceptRequest(view, request);
        String url = request.getUrl().toString();
        boolean intercept = interceptUrl(url);
        AppLogger.writeError("shouldInterceptRequest LOLLIPOP url:\n\t" + request.getUrl() + " : " + intercept + "\n_");
        if (intercept) {
            return handleIntercept(request);
        }

        return super.shouldInterceptRequest(view, request);
    }
    ***/

    //boolean interceptUrl(@NonNull String url) {
    //    return url.contains("login_submit");
    //}

    //@NonNull
    /***
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private WebResourceResponse handleIntercept(WebResourceRequest request) {
        AppLogger.writeError("handleIntercept" +
                "\n\tmethod: " + request.getMethod() +
                "\n\tbody: " + request.getRequestHeaders() +
                "\n_");

        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request okHttpRequest = new Request.Builder()
                    .url(request.getUrl().toString())
                    //.addHeader("token", UserHelper.getToken()) //add headers
                    .build();

            Response okHttpResponse = okHttpClient.newCall(okHttpRequest).execute();

            AppLogger.writeError("Response: " + okHttpResponse.body().string());
            return null;
                //return new WebResourceResponse(
                //        okHttpResponse.header("content-type", "text/plain"), // You can set something other as default content-type
                //        okHttpResponse.header("content-encoding", "utf-8"),  // Again, you can set another encoding as default
                //        okHttpResponse.body().byteStream()
                //);
        } catch (IOException err) {
            return null;
        }
    }
    ***/

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        AppLogger.writeError("shouldOverrideUrlLoading url:\n\t" + url + "\n_");
        if (!DetectConnection.checkInternetConnection(mContext)) {
            Toast.makeText(mContext.getApplicationContext(), "No Internet!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            if (URLUtil.isNetworkUrl(url)) {
                Log.e("arsen", "shouldOverrideUrlLoading: "+url);
                if (url != null && url.contains("members/") && url.contains("@")){
                    String[] spl = url.split("members/");
                    Log.e("arsen", "tes: "+spl[1]);

                    String userEmail = spl[1].substring(0, spl[1].indexOf("/"));
                    Log.e("arsen", "get Email: "+userEmail);
                    UserProp.saveUserId(mContext, userEmail);

                    if (UserProp.getToken(mContext) == null){
                        //first time, save to db
                        Utility.sendRegistrationToServer(UserProp.getFcmToken(mContext), userEmail, mContext);
                        UserProp.saveToken(mContext,"done");
                    }
                }

                //whatsapp
                if (url != null && url.contains("https://api.whatsapp.com/send")) {
                    view.reload();
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    sendIntent.setAction(Intent.ACTION_VIEW);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.setData(Uri.parse(url));
                    if (sendIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(sendIntent);
                        return true;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=com.whatsapp"));
                        mContext.startActivity(intent);
                        return true;
                    }
                }

                //zoom
                if (url != null && url.contains("https://zoom.us")) {
                    view.reload();
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    sendIntent.setAction(Intent.ACTION_VIEW);
                    sendIntent.setPackage("us.zoom.videomeetings");
                    sendIntent.setData(Uri.parse(url));
                    if (sendIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(sendIntent);
                        return true;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=us.zoom.videomeetings"));
                        mContext.startActivity(intent);
                        return true;
                    }
                }
            }
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

}
