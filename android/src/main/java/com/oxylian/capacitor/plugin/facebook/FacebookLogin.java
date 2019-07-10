package com.oxylian.capacitor.plugin.facebook;

import android.content.Intent;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@NativePlugin(requestCodes = {FacebookLogin.FACEBOOK_SDK_REQUEST_CODE_OFFSET})
public class FacebookLogin extends Plugin {
    CallbackManager callbackManager;

    public static final int FACEBOOK_SDK_REQUEST_CODE_OFFSET = 0xface;

    /**
     * Convert date to ISO 8601 format.
     */
    private String dateToJson(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return simpleDateFormat.format(date);
    }

    private JSArray collectionToJson(Collection<String> list) {
        JSArray json = new JSArray();

        for (String item: list) {
            json.put(item);
        }

        return json;
    }

    private JSObject accessTokenToJson(AccessToken accessToken) {
        JSObject ret = new JSObject();
        ret.put("applicationId", accessToken.getApplicationId());
        ret.put("declinedPermissions", collectionToJson(accessToken.getDeclinedPermissions()));
        ret.put("expires", dateToJson(accessToken.getExpires()));
        ret.put("lastRefresh", dateToJson(accessToken.getLastRefresh()));
        ret.put("permissions", collectionToJson(accessToken.getPermissions()));
        ret.put("token", accessToken.getToken());
        ret.put("userId", accessToken.getUserId());
        ret.put("isExpired", accessToken.isExpired());

        return ret;
    }

    @Override
    public void load() {
        Log.d(getLogTag(), "Entering load()");

        this.callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(getLogTag(), "LoginManager.onSuccess");

                        PluginCall savedCall = getSavedCall();

                        if (savedCall == null) {
                            Log.e(getLogTag(), "LoginManager.onSuccess: no plugin saved call found.");
                        } else {
                            JSObject ret = new JSObject();
                            ret.put("accessToken", accessTokenToJson(loginResult.getAccessToken()));
                            ret.put("recentlyGrantedPermissions", collectionToJson(loginResult.getRecentlyGrantedPermissions()));
                            ret.put("recentlyDeniedPermissions", collectionToJson(loginResult.getRecentlyDeniedPermissions()));

                            savedCall.success(ret);

                            saveCall(null);
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.d(getLogTag(), "LoginManager.onCancel");

                        PluginCall savedCall = getSavedCall();

                        if (savedCall == null) {
                            Log.e(getLogTag(), "LoginManager.onCancel: no plugin saved call found.");
                        } else {
                            JSObject ret = new JSObject();
                            ret.put("accessToken", null);

                            savedCall.success(ret);

                            saveCall(null);
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e(getLogTag(), "LoginManager.onError", exception);

                        PluginCall savedCall = getSavedCall();

                        if (savedCall == null) {
                            Log.e(getLogTag(), "LoginManager.onError: no plugin saved call found.");
                        } else {
                            savedCall.reject(exception.toString());

                            saveCall(null);
                        }
                    }
                });
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getLogTag(), "Entering handleOnActivityResult(" + requestCode + ", " + resultCode + ")");

        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            Log.d(getLogTag(), "onActivityResult succeeded");
        } else {
            Log.w(getLogTag(), "onActivityResult failed");
        }
    }

    @PluginMethod()
    public void login(PluginCall call) {
        Log.d(getLogTag(), "Entering login()");

        PluginCall savedCall = getSavedCall();

        if (savedCall != null) {
            Log.e(getLogTag(), "login: overlapped calls not supported");

            call.reject("Overlapped calls call not supported");

            return;
        }

        JSArray arg = call.getArray("permissions");

        Collection<String> permissions;

        try {
            permissions = arg.toList();
        } catch (Exception e) {
            Log.e(getLogTag(), "login: invalid 'permissions' argument", e);

            call.reject("Invalid permissions argument");

            return;
        }

        LoginManager.getInstance().logInWithReadPermissions(this.getActivity(), permissions);

        saveCall(call);
    }

    @PluginMethod()
    public void logout(PluginCall call) {
        Log.d(getLogTag(), "Entering logout()");

        LoginManager.getInstance().logOut();

        call.success();
    }

    @PluginMethod()
    public void getCurrentAccessToken(PluginCall call) {
        Log.d(getLogTag(), "Entering getCurrentAccessToken()");

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        JSObject ret = new JSObject();

        if (accessToken == null) {
            Log.d(getLogTag(), "getCurrentAccessToken: accessToken is null");
        } else {
            Log.d(getLogTag(), "getCurrentAccessToken: accessToken found");

            ret.put("accessToken", accessTokenToJson(accessToken));
        }

        call.success(ret);
    }
}
