package io.seal.swarmwear;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.service.SearchVenuesService;

import java.io.File;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class Utils {

    public static boolean isNetworkConnectedOrConnecting(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static File getLogFile() {
        return new File(PhoneApp.getInstance().getFilesDir(), "log.txt");
    }

    public static boolean isPersistentLogEnabled() {
        return BuildConfig.DEBUG;
    }

    public static NotificationCompat.Action getSearchAction(Context context, int notificationId) {

        Intent searchIntent = new Intent(context, SearchVenuesService.class);
        searchIntent.putExtra(Properties.Keys.PASSIVE, false);
        PendingIntent pendingIntent = PendingIntent.getService(context, notificationId, searchIntent,
                FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_action_search,
                context.getString(R.string.search_nearby),
                pendingIntent).build();
    }

    public static boolean isActivityAvailable(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
