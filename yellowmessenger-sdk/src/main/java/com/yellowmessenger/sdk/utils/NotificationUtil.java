package com.yellowmessenger.sdk.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rashid on 02/03/17.
 */
public class NotificationUtil {
    public static void sendDeviceTokenToServer (String deviceId, String username, final String authorizationToken, Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://notifications.botplatform.io/push/registerDevice";
        deviceId = FirebaseInstanceId.getInstance().getToken();
        JSONObject body = new JSONObject();
        try {
            body.put ("deviceId",deviceId);
            body.put ("profileId",username);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, body,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")) {

                                } else {

                                }
                            } catch (Exception e) {

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap <String, String> headers = new HashMap<>();
                    headers.put("bp-auth-token",authorizationToken);
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
