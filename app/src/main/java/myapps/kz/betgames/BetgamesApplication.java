package myapps.kz.betgames;

import android.app.Application;
import android.content.res.Configuration;

import com.onesignal.OneSignal;

import myapps.kz.betgames.notifications.BetgamesNotificationOpenedHandler;

/**
 * Created by rauan on 26.07.17.
 */

public class BetgamesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new BetgamesNotificationOpenedHandler(getApplicationContext()))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
