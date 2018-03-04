package myapps.kz.betgames.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import myapps.kz.betgames.MainActivity;

/**
 * Created by rauan on 26.07.17.
 */

public class BetgamesNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    private Context context;

    public BetgamesNotificationOpenedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;
        boolean isTest;
        if (data != null) {
            isTest = data.optBoolean("is_test");
            if (isTest == false) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            }
        }
    }
}
