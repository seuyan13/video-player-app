package com.example.quickvideoplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.text.TextUtils;

public class FavoriteSharedPreferences {

    private static FavoriteSharedPreferences instance = null;
    private final SharedPreferences prefs;


    private FavoriteSharedPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    static FavoriteSharedPreferences getInstance(Context context) {
        if (instance == null) {
            SharedPreferences prefs =
                    context.getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID + ".favoriteprefs", Context.MODE_PRIVATE);
            instance = new FavoriteSharedPreferences(prefs);
        }

        return instance;
    }

    void setFavorite(long mediaId, boolean isEnabled) {
        if (isEnabled) {
            prefs.edit().putBoolean(String.valueOf(mediaId), true).commit();

        } else {
            prefs.edit().remove(String.valueOf(mediaId)).commit();
        }
    }

    boolean getFavorite(long mediaId) {
        return prefs.getBoolean(String.valueOf(mediaId), false);
    }

    String getFavoriteSelection() {
        return MediaStore.Files.FileColumns._ID + " IN (" + TextUtils.join(", ", prefs.getAll().keySet()) + ")";
    }
}
