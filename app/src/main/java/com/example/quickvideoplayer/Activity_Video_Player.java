package com.example.quickvideoplayer;

import android.content.ContentUris;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class Activity_Video_Player extends AppCompatActivity {

    private long videoId;

    private MenuItem favoriteMenuItem;
    private MenuItem[] speedMenuItems;

    private PlayerView playerView;
    private SimpleExoPlayer player;

    private FavoriteSharedPreferences favoriteSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        }

        setContentView(R.layout.activity_video_player);

        videoId = getIntent().getExtras().getLong("videoId");

        favoriteSharedPreferences = FavoriteSharedPreferences.getInstance(this);

        initializeViews();
    }

    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    private void initializeViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playerView = findViewById(R.id.playerView);
    }

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setControllerAutoShow(getOrientation() == Configuration.ORIENTATION_LANDSCAPE);

        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            playerView.showController();

        } else {
            playerView.hideController();
        }

        Uri videoUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), videoId);
        MediaSource mediaSource = buildMediaSource(videoUri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_video_player, menu);

        favoriteMenuItem = menu.findItem(R.id.favorite_unchecked);

        MenuItem fullScreen = menu.findItem(R.id.fullscreen);
        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            fullScreen.setIcon(R.drawable.ic_fullscreen_exit);
        }

        MenuItem speed_025MenuItem = menu.findItem(R.id.speed_025);
        MenuItem speed_05MenuItem = menu.findItem(R.id.speed_05);
        MenuItem speed_normalMenuItem = menu.findItem(R.id.speed_normal);
        MenuItem speed_125MenuItem = menu.findItem(R.id.speed_125);
        MenuItem speed_15MenuItem = menu.findItem(R.id.speed_15);
        MenuItem speed_2MenuItem = menu.findItem(R.id.speed_2);

        speedMenuItems = new MenuItem[]{speed_025MenuItem, speed_05MenuItem, speed_normalMenuItem, speed_125MenuItem, speed_15MenuItem, speed_2MenuItem};

        setFavorite(favoriteSharedPreferences.getFavorite(videoId));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.favorite_unchecked:
                setFavorite(!favoriteMenuItem.isChecked());
                break;

            case R.id.fullscreen:
                if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;

            case R.id.speed_025:
                setSpeed(0);
                break;

            case R.id.speed_05:
                setSpeed(1);
                break;

            case R.id.speed_normal:
                setSpeed(2);
                break;

            case R.id.speed_125:
                setSpeed(3);
                break;

            case R.id.speed_15:
                setSpeed(4);
                break;

            case R.id.speed_2:
                setSpeed(5);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else {
            super.onBackPressed();
        }
    }

    private void setFavorite(boolean isEnabled) {
        favoriteSharedPreferences.setFavorite(videoId, isEnabled);

        if (isEnabled) {
            favoriteMenuItem.setChecked(true);
            favoriteMenuItem.setIcon(R.drawable.ic_favorite_checked);

        } else {
            favoriteMenuItem.setChecked(false);
            favoriteMenuItem.setIcon(R.drawable.ic_favorite_unchecked);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void setSpeed(int index) {
        float speed;
        switch (index) {
            case 0:
                speed = 0.25f;
                break;

            case 1:
                speed = 0.5f;
                break;

            case 3:
                speed = 1.25f;
                break;

            case 4:
                speed = 1.5f;
                break;

            case 5:
                speed = 2f;
                break;

            default:
                speed = 1f;
                break;
        }

        player.setPlaybackParameters(new PlaybackParameters(speed));

        for (int i = 0; i < speedMenuItems.length; i++) {
            speedMenuItems[i].setChecked(i == index);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        super.onStop();
    }
}
