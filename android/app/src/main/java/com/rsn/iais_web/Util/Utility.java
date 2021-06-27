package com.rsn.iais_web.Util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rsn.iais_web.api.ServerApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Utility {
    public static void sendRegistrationToServer(String token, String username, Context context) {
        // TODO: Implement this method to send token to your app server.
        //insertUpdateFcmToken
        ServerApi.insertUpdateFcmToken(
                username,
                Build.MODEL,
                "",
                Build.VERSION.RELEASE,
                Build.MANUFACTURER,
                token,
                context,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Sales", "LoginActivity insertFcmToken onResponse:" +
                                "\n\t" + response.toString() + "\n_");
                        try {
                            int error = response.getInt("error");
                            if (error == 0) {
                                //success insert or update fcm
                            }else {
                                Log.e("Sales", "onResponse: fail");
                            }
                        } catch (JSONException e) {
                            Log.e("Sales", "onResponse: fail try catch");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Sales", "LoginActivity insertFcmToken onErrorResponse:" +
                                "\n\t" + error.toString() + "\n_");
                        if (error.networkResponse != null) {
                            try {
                                String body = new String(error.networkResponse.data,"UTF-8");
                                JSONObject jsonObj = new JSONObject(body);
                                Log.d("Sales", "LoginActivity insertFcmToken onErrorResponse: " + error.toString() +
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

    public static void updateVersiApp(String token, String ver_app, Context context) {
        // TODO: Implement this method to send token to your app server.
        //insertUpdateFcmToken
        ServerApi.updateVersiApp(
                token,
                ver_app,
                context,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("arsen", "LoginActivity updateVersiApp onResponse:" +
                                "\n\t" + response.toString() + "\n_");
                        try {
                            int error = response.getInt("error");
                            if (error == 0) {
                                //success insert or update fcm
                            }else {
                                Log.e("Sales", "onResponse: fail");
                            }
                        } catch (JSONException e) {
                            Log.e("Sales", "onResponse: fail try catch");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("arsen", "LoginActivity updateVersiApp onErrorResponse:" +
                                "\n\t" + error.toString() + "\n_");
                        if (error.networkResponse != null) {
                            try {
                                String body = new String(error.networkResponse.data,"UTF-8");
                                JSONObject jsonObj = new JSONObject(body);
                                Log.d("Sales", "LoginActivity insertFcmToken onErrorResponse: " + error.toString() +
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
}
