package in.hulum.udise.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import in.hulum.udise.R;

/**
 * Created by Irshad on 28-03-2018.
 */

public class NotificationHelper extends ContextWrapper {

    private static final String UDISE_WITH_ALERTS_CHANNEL_ID = "in.hulum.udise.notificationchannels.NOTIFICATION_WITH_ALERTS";
    private static final String UDISE_WITH_ALERTS_CHANNEL_NAME = "UDISE Alerts";

    private static final String UDISE_PROGRESS_CHANNEL_ID = "in.hulum.udise.notificationchannels.NOTIFICATION_WITHOUT_ALERTS";
    private static final String UDISE_PROGRESS_CHANNEL_NAME = "UDISE Import Progress";

    private  NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    private void createChannels(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel alertChannel = new NotificationChannel(UDISE_WITH_ALERTS_CHANNEL_ID,UDISE_WITH_ALERTS_CHANNEL_NAME,importance);
            alertChannel.setDescription("UDISE Notification Channel for extremely important notifications.");
            alertChannel.enableLights(true);
            alertChannel.enableVibration(true);
            alertChannel.setLightColor(Notification.COLOR_DEFAULT);
            alertChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            alertChannel.setShowBadge(true);
            getManager().createNotificationChannel(alertChannel);

            int importanceDefault = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel progressChannel = new NotificationChannel(UDISE_PROGRESS_CHANNEL_ID,UDISE_PROGRESS_CHANNEL_NAME,importanceDefault);
            alertChannel.setDescription("UDISE Notification Channel for progress update notifications.");
            alertChannel.enableLights(false);
            alertChannel.enableVibration(false);
            alertChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            alertChannel.setShowBadge(false);
            getManager().createNotificationChannel(progressChannel);
        }
    }

    public NotificationManager getManager(){
        if(manager==null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public NotificationCompat.Builder getNotificationWithAlerts(String title, String body){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_WITH_ALERTS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                              .bigText(body))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
    }

    public NotificationCompat.Builder getNotificationWithoutAlerts(String title, String body){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_PROGRESS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setProgress(0,0,false) //Hide the progress indicator
                .setAutoCancel(true);
    }

    public NotificationCompat.Builder getNotificationWithProgress(String title, String body,int progress){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_PROGRESS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setProgress(100,progress,false) //Hide the progress indicator
                .setAutoCancel(true);
    }
}
