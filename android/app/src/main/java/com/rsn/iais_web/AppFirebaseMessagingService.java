package com.rsn.iais_web;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rsn.iais_web.Util.UserProp;
import com.rsn.iais_web.Util.Utility;
import com.rsn.iais_web.api.ServerApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.rsn.iais_web.BuildConfig.END_POINT;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Log.e("arsen", "PushMessagingService onNewToken: " + token);
        UserProp.saveFcmToken(getApplicationContext(), token);
//        Utility.sendRegistrationToServer(token, this);
//        super.onNewToken(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.e("arsen", "onMessageReceived: "+remoteMessage.getNotification().getTitle());


        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), "http://202.157.177.195:8090/members/lawviapples@gmail.com/notification/invitation", null);

        //super.onMessageReceived(remoteMessage);
    }


    private void showNotification(String notificationTitle,
                                  String notificationMessage,
                                  String notificationLink,
                                  Bitmap notificationPicture) {
        if (TextUtils.isEmpty(notificationTitle) || TextUtils.isEmpty(notificationMessage)) {
            return;
        }

        // Proceed to push notification
        //Intent intent = new Intent(this, DarkModeActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);



        //notification action: direct url
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(notificationLink));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //notification action: open app
        Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;
//        if (notificationLink.contains(END_POINT)){ //url goes to pfs, open app directly
//            Log.e("arsen", "showNotification: ke OG");
//            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
//        }else{
            //url goes to external source
            Log.e("arsen", "showNotification: ke ext");
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
//        }


        // Create custom contentView
        RemoteViews rvNotification = new RemoteViews(getPackageName(), R.layout.notification_content_view);
//        rvNotification.setImageViewBitmap(R.id.iv_notification, notificationPicture);
//        String shorten = Html.fromHtml(notificationMessage).toString();
//        rvNotification.setTextViewText(R.id.notif_text, shorten);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        //.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationMessage)
                        //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                        .setCustomContentView(rvNotification)
//                        .setCustomBigContentView(rvNotification)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "fcm_channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
