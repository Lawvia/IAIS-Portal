package com.rsn.iais_web.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
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

    public static final String SERVER_URL = "http://202.157.177.195:8090";

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

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", "0");
        params.put("user_email", username);
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

}