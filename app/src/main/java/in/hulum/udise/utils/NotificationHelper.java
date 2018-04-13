package in.hulum.udise.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import in.hulum.udise.R;

/**
 * Created by Irshad on 28-03-2018.
 * This class contains various methods for implementing
 * Oreo compatible notifications.
 *
 * Currently, we are supporting two types of notification channels
 * - Notification without Alerts (Progress Channel): This channel
 *   is used for displaying progress percentage notifications while
 *   excel files are being imported. Since we do not want alerts
 *   (vibration and sounds) for each progress percentage notification,
 *   therefore, this channel turns off the vibration and audio alerts.
 *
 * - Notification with Alerts (Udise Alerts Channel): This channel
 *   is used for displaying error notifications or alert messages that
 *   need immediate user attention. As such, this channel uses both
 *   vibration as well as audio alerts for notifications. Important
 *   notifications must use this channel only.
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

        /*
         * Notification channels are used by Oreo and above only.
         * So check for the same.
         */
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
            progressChannel.setDescription("UDISE Notification Channel for progress update notifications.");
            progressChannel.enableLights(false);
            progressChannel.enableVibration(false);
            progressChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            progressChannel.setShowBadge(false);
            getManager().createNotificationChannel(progressChannel);
        }
    }

    public NotificationManager getManager(){
        if(manager==null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    /**
     * This method creates a notification with alerts (using notification channel
     * with alerts). This method is used for creating important notifications, like the ones
     * used for displaying error in the notification, making alerts to get noticed
     * @param title Title of the notification
     * @param body Detailed message of the notification
     * @return NotificationCompat.Builder object
     */
    public NotificationCompat.Builder getNotificationWithAlerts(String title, String body){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_WITH_ALERTS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                              .bigText(body))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }

    /**
     * This method is used for creating the notification with alerts (vibration
     * as well as sound). This notification is displayed only when the excel
     * file import process is completed. The only difference between this method
     * and the {@link NotificationHelper#getNotificationWithAlerts} method is
     * that this method takes a pending intent.
     * @param title Title of the notification
     * @param body Detailed message of the notification
     * @param pendingIntent pendingIntent object containing info about
     *                      the activity to be started when the notification
     *                      is clicked
     * @return NotificationCompat.Builder object
     */

    public NotificationCompat.Builder getFinalNotificationWithAlerts(String title, String body, PendingIntent pendingIntent){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_WITH_ALERTS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    /**
     * This method creates a notification without alerts (using notification channel
     * without alerts). This method is used for creating silent notifications, like the ones
     * displaying "Loading, Please Wait..." in the notification without making any noise
     * @param title Title of the notification
     * @param body Detailed message of the notification
     * @return NotificationCompat.Builder object
     */

    public NotificationCompat.Builder getNotificationWithoutAlerts(String title, String body){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_PROGRESS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setProgress(0,0,false) //Hide the progress indicator
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }

    /**
     * This method also creates silent notifications. The only difference between this and
     * the above method {@link NotificationHelper#getNotificationWithoutAlerts(String, String)}
     * is that this method also creates a progress bar in the notification panel
     * @param title Title of the notification
     * @param body Detailed message
     * @param progress value for progress percentage to be displayed in the progress bar
     *                 in the notification
     * @return NotificationCompat.Builder object
     */

    public NotificationCompat.Builder getNotificationWithProgress(String title, String body,int progress){
        return new NotificationCompat.Builder(getApplicationContext(),UDISE_PROGRESS_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_import_notification)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setProgress(100,progress,false) //Hide the progress indicator
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }
}
