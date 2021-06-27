package com.rsn.iais_web.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerApi {

    private static final int SOCKET_TIME_OUT = 50000; //30 seconds - change to what you want

    public static final String SERVER_URL = "https://core.indonesiaai.org";

    // Instantiate the RequestQueue.
    private static RequestQueue mRequestQueue;

    public static void insertUpdateFcmToken(String username,
                                            String model,
                                            String serial_num,
                                            String os,
                                            String manufacturer,
                                            String fcm_token,
                                            Context context,
                                            Response.Listener<JSONObject> successListener,
                                            Response.ErrorListener errorListener) {

        String endPoint = SERVER_URL + "/api/v1/activities";
        Log.e("arsen", "insertUpdateFcmToken: "+endPoint);

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", "0");
        params.put("user_email", username);
        params.put("versi_app", com.rsn.iais_web.BuildConfig.VERSION_NAME);
        params.put("tipe_device", model);
        params.put("serial_num", serial_num);
        params.put("sistem_operasi", os);
        params.put("manufacturer", manufacturer);
        params.put("token", fcm_token);
        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                endPoint, jsonObj,
                successListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

        };

        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        mRequestQueue.add(jsonObjectRequest);
    }


    public static void getUserDataByToken(String fcm_token,
                                            Context context,
                                            Response.Listener<JSONObject> successListener,
                                            Response.ErrorListener errorListener) {

        String endPoint = SERVER_URL + "/api/v1/get_activities/?token=" + fcm_token;
        Log.e("arsen", "getUserDataByToken: "+endPoint);

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", "0");
//        params.put("user_email", username);
        params.put("versi_app", BuildConfig.VERSION_NAME);
        params.put("token", fcm_token);
        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                endPoint, jsonObj,
                successListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

        };

        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        mRequestQueue.add(jsonObjectRequest);
    }

    public static void updateVersiApp(String fcm_token,
                                          String ver_app,
                                          Context context,
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {

        String endPoint = SERVER_URL + "/api/v1/put_activities/?token=" + fcm_token + "&versi_app=" + ver_app;
        Log.e("arsen", "updateVersiApp: "+endPoint);

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", "0");
//        params.put("user_email", username);
//        params.put("versi_app", BuildConfig.VERSION_NAME);
//        params.put("token", fcm_token);
        JSONObject jsonObj = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                endPoint, jsonObj,
                successListener,
                errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

        };

        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        mRequestQueue.add(jsonObjectRequest);
    }

}