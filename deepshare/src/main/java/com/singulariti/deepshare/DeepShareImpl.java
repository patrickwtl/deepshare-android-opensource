package com.singulariti.deepshare;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.singulariti.deepshare.listeners.DSFailListener;
import com.singulariti.deepshare.listeners.DSInappDataListener;
import com.singulariti.deepshare.listeners.NewUsageFromMeListener;
import com.singulariti.deepshare.protocol.ServerMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.ChangeValueByMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.ClearUsageMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.CloseMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.InstallMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.NewUsageMessage;
import com.singulariti.deepshare.protocol.httpsendmessages.OpenMessage;
import com.singulariti.deepshare.transport.ServerHttpConnection;
import com.singulariti.deepshare.utils.Util;

import java.util.HashMap;

/**
 * Created by joy on 15/9/3.
 */
public class DeepShareImpl {
    private static final String TAG = "DeepShareImpl";

    final Configuration config;
    private final Context context;


    DeepShareImpl(Context context, String appId) {
        this.context = context;
        this.config = Configuration.getInstance(context.getApplicationContext());

        ServerMessageMgr.getInstance().registerHandler(handler);

        config.setAppKey(appId);
    }


    private UiServerMessageHandler handler = new UiServerMessageHandler() {
        @Override
        protected void processEvent(ServerMessage msg) {
            msg.processResponse();
        }
    };

    boolean initSession(DSInappDataListener callback, boolean isReferrable, Uri data, String key) {
        Util.initUtil();
        Util.startTicks = System.currentTimeMillis();

        boolean uriHandled = false;
        if (data != null && data.isHierarchical()) {
            if (data.getQueryParameter("click_id") != null) {
                uriHandled = true;
                config.setClickId(data.getQueryParameter("click_id"));
            }
            if (data.getQueryParameter("worker_id") != null) {

            }
        } else {
            config.setClickId("");
        }

        ServerHttpConnection.reset(1);

        if (hasUser()) {
            ServerHttpConnection.send(new OpenMessage(context, callback));
        } else {
            ServerHttpConnection.send(new InstallMessage(context, callback));
        }

        config.setInitKey("Initialized");

        if (key != null) {
            config.setAppKey(key);
        }

        return uriHandled;
    }

    boolean hasUser() {
        return !TextUtils.isEmpty(config.getInitKey());
    }

    void close(){
        ServerHttpConnection.send(new CloseMessage(context));
    }

    void getNewUsageFromMe(NewUsageFromMeListener callback){
        ServerHttpConnection.send(new NewUsageMessage(context, callback));
    }

    void clearUsageFromMe(DSFailListener callback){
        ServerHttpConnection.send(new ClearUsageMessage(context, callback));
    }

    void changeValueBy(HashMap<String, Integer> tagToValue, DSFailListener callback) {
        //JSONObject tagToValues = new JSONObject(tagToValue);
        Util.startTicks = System.currentTimeMillis();
        ServerHttpConnection.send(new ChangeValueByMessage(context, tagToValue, callback));
    }


}
