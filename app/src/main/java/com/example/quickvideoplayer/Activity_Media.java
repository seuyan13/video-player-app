package com.example.quickvideoplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class Activity_Media extends AppCompatActivity {

    private MenuItem videoScreen;
    private MenuItem imageScreen;
    private MenuItem favoriteScreen;

    private AdapterMediaList adapterMediaList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        initializeViews();
        checkPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_screen, menu);
        videoScreen = menu.findItem(R.id.video_screen);
        imageScreen = menu.findItem(R.id.image_screen);
        favoriteScreen = menu.findItem(R.id.favorite_screen);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.video_screen:
                setScreenType(0);
                break;

            case R.id.image_screen:
                setScreenType(1);
                break;

            case R.id.favorite_screen:
                setScreenType(2);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_videos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapterMediaList = new AdapterMediaList(this);
        recyclerView.setAdapter(adapterMediaList);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            } else {
                // loadVideos(0);
                loadMedia(0);
            }
        } else {
            loadMedia(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMedia(0);
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @param screenType 0: video, 1: image, 2: favorite
     */
    private void setScreenType(int screenType) {
        if (videoScreen == null || imageScreen == null || favoriteScreen == null) {
            return;
        }

        switch (screenType) {
            case 0:
                videoScreen.setChecked(true);
                imageScreen.setChecked(false);
                favoriteScreen.setChecked(false);
                break;

            case 1:
                videoScreen.setChecked(false);
                imageScreen.setChecked(true);
                favoriteScreen.setChecked(false);
                break;

            case 2:
                videoScreen.setChecked(false);
                imageScreen.setChecked(false);
                favoriteScreen.setChecked(true);
                break;
        }

        loadMedia(screenType);
    }

    private void loadMedia(final int screenType) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.MEDIA_TYPE};
                String selection = null;
                switch (screenType) {
                    case 0:
                        selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                        break;

                    case 1:
                        selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                        break;

                    case 2:
                        selection = FavoriteSharedPreferences.getInstance(Activity_Media.this).getFavoriteSelection();
                        break;
                }

                String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

                Uri contentUri = MediaStore.Files.getContentUri("external");

                Cursor cursor = getApplication().getContentResolver().query(contentUri, projection, selection, null, sortOrder);
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);

                    final ArrayList<ModelMedia> mediaList = new ArrayList<>();

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String title = cursor.getString(titleColumn);
                        String mediaType = cursor.getString(mediaTypeColumn);
                        String duration_formatted = "";

                        Uri data = ContentUris.withAppendedId(contentUri, id);

                        final ModelMedia media = new ModelMedia(id, mediaType, data, title);

                        if (media.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(Activity_Media.this, data);

                            try {
                                int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                                int sec = (duration / 1000) % 60;
                                int min = (duration / (1000 * 60)) % 60;
                                int hrs = duration / (1000 * 60 * 60);

                                if (hrs == 0) {
                                    duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                                } else {
                                    duration_formatted = String.valueOf(hrs).concat(":".concat(String.format(Locale.UK, "%02d", min).concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                                }

                                media.setDuration(duration_formatted);

                            } catch (Exception ignore) {

                            }
                        }

                        mediaList.add(media);
                    }

                    cursor.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapterMediaList.mediaList = mediaList;
                            adapterMediaList.notifyDataSetChanged();
                        }
                    });
                }

            }
        }.start();
    }
}
