package com.media.audiovideoplayer.service;

import static com.media.audiovideoplayer.activity.PlayerActivity.playerActivity;
import static com.media.audiovideoplayer.adapter.AudioPlayerAdapter.audioData;
import static com.media.audiovideoplayer.adapter.AudioPlayerAdapter.audioPlayerAdapter;
import static com.media.audiovideoplayer.adapter.AudioPlayerAdapter.selectedPosition;
import static com.media.audiovideoplayer.adapter.VideoPlayerAdapter.videoDataArrayList;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.activity.PlayerActivity;
import com.media.audiovideoplayer.adapter.AudioPlayerAdapter;
import com.media.audiovideoplayer.constants.AudioVideoConstants;
import com.media.audiovideoplayer.constants.AudioVideoEnum;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlayerService extends MediaBrowserServiceCompat {
    public static SimpleExoPlayer exoPlayer;
    public SharedPreferences sharedPreferences;
    private String title;
    private String filePath;
    private int index;
    private long duration;
    public static MediaSessionCompat mediaSession;
    public static MediaControllerCompat mediaControllerCompat;
    private PlaybackStateCompat.Builder playbackStateCompat;
    private MediaMetadataCompat.Builder mediaMetaDataBuilder;
    private PendingIntent stopIntent;
    public static long currentPosition;
    private String source;
    private boolean isPlaying = false;
    public static boolean isPaused = false;
    public static boolean fullscreen = false;
    private boolean isNextSkipped = false;
    private static final String NOTIFICATION_CHANNEL_ID = "AudioVideoPlayer";
    private static final String NOTIFICATION_CHANNEL_NAME = "AudioVideoNotification";
    private DefaultDataSourceFactory mediaSourceFactory;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        if (intent.getAction().equals(AudioVideoConstants.START_FOREGROUND)) {
            sharedPreferences.edit().putBoolean("serviceStatus", true).apply();
            try {
                startForeground();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(AudioVideoConstants.STOP_FOREGROUND)) {
            if (sharedPreferences.getBoolean("serviceStatus", false)) {
                sharedPreferences.edit().putBoolean("serviceStatus", false).apply();
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.seekTo(0);
                exoPlayer.stop();
                if (null != audioPlayerAdapter) {
                    updateMusicRecyclerViewGraphics(false);
                }
                updatePlaybackState(
                        PlaybackStateCompat.STATE_STOPPED,
                        exoPlayer.getCurrentPosition(),
                        false
                );
                stopForeground(true);
                if (playerActivity != null) {
                    playerActivity.finishAndRemoveTask();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = Preferences.getSharedPreferences(this);
        title = sharedPreferences.getString("title", "def");
        filePath = sharedPreferences.getString("filePath", "def");
        index = sharedPreferences.getInt("index", 0);
        duration = sharedPreferences.getLong("duration", 0);
        source = sharedPreferences.getString("source", "def");
        ComponentName mediaButtonReceiver = new ComponentName(
                this,
                MediaButtonReceiver.class
        );
        mediaSession =
                new MediaSessionCompat(this, "MediaSession", mediaButtonReceiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pendingIntent);
        mediaSession.setCallback(mediaSessionCallBack);
        Intent intent = new Intent(this, PlayerActivity.class);
        mediaSession.setSessionActivity(
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        setSessionToken(mediaSession.getSessionToken());
        try {
            mediaControllerCompat = new MediaControllerCompat(this, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
     /*   if (!isNoisyRecieverRegistered) {
            val radioService: RadioService = this@RadioService
                    radioService.registerReceiver(
                    radioService.becomingNoisyReceiver,
                    IntentFilter("android.media.AUDIO_BECOMING_NOISY")
            )
            isNoisyRecieverRegistered = true
            ErrorHandler.logDebug("isNoisyRecieverRegistered $isNoisyRecieverRegistered")
        }*/
        playbackStateCompat = new PlaybackStateCompat.Builder();
        mediaMetaDataBuilder = new MediaMetadataCompat.Builder();
        playbackStateCompat.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO);
        initiateMedia(filePath);
    }

    MediaSessionCompat.Callback mediaSessionCallBack = new MediaSessionCompat.Callback() {

        @Override
        public void onPause() {
            super.onPause();
            isPlaying = false;
            isPaused = true;
            try {
                NotificationManagerCompat.from(getApplicationContext()).notify(1, getNotification());
                exoPlayer.setPlayWhenReady(false);
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, exoPlayer.getCurrentPosition(), true);
                currentPosition = exoPlayer.getCurrentPosition();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlay() {
            super.onPlay();
            if (isPaused) {
                isPlaying = true;
                isPaused = false;
                try {
                    filePath = sharedPreferences.getString("filePath", "def");
                    initiateMedia(filePath);
                    //updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, exoPlayer.getCurrentPosition(), true);
                    startForeground();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            int currentIndex = sharedPreferences.getInt("index", 0);
            int size;
            switch (AudioVideoEnum.valueOf(sharedPreferences.getString("source", "def"))) {
                case AUDIO:
                    size = audioData.size() - 1;
                    index = currentIndex + 1;
                    index = (index > size) ? 0 : index;
                    index = (index < 0) ? size : index;
                    sharedPreferences.edit()
                            .putString("title", audioData.get(index).getMusicTitle())
                            .putString("filePath", audioData.get(index).getFileUrl())
                            .putLong("duration", audioData.get(index).getDuration())
                            .putString("action", "AUDIO_NEXT")
                            .putInt("index", index).apply();
                    if (null != audioPlayerAdapter) {
                        updateMusicRecyclerViewGraphics(true);
                    }
                    if(null!=playerActivity)
                        playerActivity.recreate();
                    break;
                case VIDEO:
                    size = videoDataArrayList.size() - 1;
                    index = currentIndex + 1;
                    index = (index > size) ? 0 : index;
                    index = (index < 0) ? size : index;
                    sharedPreferences.edit()
                            .putString("title", videoDataArrayList.get(index).getTitle())
                            .putString("filePath", videoDataArrayList.get(index).getUrl())
                            .putLong("duration", videoDataArrayList.get(index).getDuration())
                            .putInt("index", index)
                            .putString("action", "VIDEO_NEXT")
                            .apply();
                    if (null != audioPlayerAdapter) {
                        updateMusicRecyclerViewGraphics(false);
                    }
                    break;
            }
            isPaused = true;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            int currentIndex = sharedPreferences.getInt("index", 0);
            int size;
            switch (AudioVideoEnum.valueOf(sharedPreferences.getString("source", "def"))) {
                case AUDIO:
                    size = audioData.size() - 1;
                    index = currentIndex - 1;
                    index = (index > size) ? 0 : index;
                    index = (index < 0) ? size : index;
                    sharedPreferences.edit()
                            .putString("title", audioData.get(index).getMusicTitle())
                            .putString("filePath", audioData.get(index).getFileUrl())
                            .putLong("duration", audioData.get(index).getDuration())
                            .putString("action", "AUDIO_PREV")
                            .putInt("index", index).apply();
                    if (null != audioPlayerAdapter) {
                        updateMusicRecyclerViewGraphics(true);
                    }
                    if(null!=playerActivity)
                        playerActivity.recreate();
                    break;
                case VIDEO:
                    size = videoDataArrayList.size() - 1;
                    index = currentIndex - 1;
                    index = (index > size) ? 0 : index;
                    index = (index < 0) ? size : index;
                    sharedPreferences.edit()
                            .putString("title", videoDataArrayList.get(index).getTitle())
                            .putString("filePath", videoDataArrayList.get(index).getUrl())
                            .putLong("duration", videoDataArrayList.get(index).getDuration())
                            .putString("action", "VIDEO_PREV")
                            .putInt("index", index).apply();
                    if (null != audioPlayerAdapter) {
                        updateMusicRecyclerViewGraphics(false);
                    }
                    break;
            }
            isPaused = true;
            onPlay();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            exoPlayer.seekTo(pos);
        }
    };

    public void startForeground() throws ExecutionException, InterruptedException {
        title = sharedPreferences.getString("title", "def");
        filePath = sharedPreferences.getString("filePath", "def");
        index = sharedPreferences.getInt("index", 0);
        duration = sharedPreferences.getLong("duration", 0);
        source = sharedPreferences.getString("soruce", "def");
        Intent intentstop = new Intent(this, PlayerService.class);
        intentstop.setAction(AudioVideoConstants.STOP_FOREGROUND);
        stopIntent =
                PendingIntent.getService(this, 0, intentstop, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // build notification channel
            manager.createNotificationChannel(chan);
        }
        getGraphicsBasedOnVideo();
        Bitmap notificationIcon = getBitmap(filePath);
        updateMetadata(title, title, notificationIcon, duration);
        startForeground(1, getNotification());
    }


    public Notification getNotification() throws ExecutionException, InterruptedException {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.exo_icon_play)
                .setWhen(0L)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1, 2)
                                .setMediaSession(mediaControllerCompat.getSessionToken())
                )
                .setContentIntent(mediaControllerCompat.getSessionActivity())
                .addAction(
                        new NotificationCompat.Action(
                                R.drawable.exo_controls_previous, "PREV",
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                )
                        )
                )
                .addAction(
                        (isPlaying) ? new NotificationCompat.Action(
                                R.drawable.exo_icon_pause, "Pause",
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_PAUSE
                                )
                        ) : new NotificationCompat.Action(
                                R.drawable.exo_icon_play, "Play",
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_PLAY
                                )
                        )
                )
                .addAction(
                        new NotificationCompat.Action(
                                R.drawable.exo_controls_next, "Next",
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                )
                        )
                )
                .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel, "STOP" +
                                "", stopIntent
                );
        return notificationBuilder.build();

    }

    void getGraphicsBasedOnVideo() {
        if (AudioVideoEnum.valueOf(sharedPreferences.getString("source", "def")) == AudioVideoEnum.VIDEO) {
            if (null != audioPlayerAdapter) {
                updateMusicRecyclerViewGraphics(false);
            }
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    public Bitmap getBitmap(String url) throws ExecutionException, InterruptedException {
        Bitmap icon = null;
        MediaMetadataRetriever retriever;
        switch (AudioVideoEnum.valueOf(sharedPreferences.getString("source", "def"))) {
            case AUDIO:
                try {
                    retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(url);
                    byte[] data = retriever.getEmbeddedPicture();
                    if (data != null) {
                        icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                    } else {
                        icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.music_image);
                    }
                } catch (Exception e) {
                    icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.music_image);
                }
                break;
            case VIDEO:
                icon = new GenerateIconForVideo().execute(url).get();
                break;
        }
        return icon;
    }

    public void initiateMedia(String url) {
        exoPlayer = initiateExoPlayer();
        //DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, "AudioVideoPlayer");
        Uri uri = Uri.parse(url);
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri).build();
        MediaSource mediaSource= new DefaultMediaSourceFactory(this).createMediaSource(mediaItem);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
        if (currentPosition > 0L) {
            exoPlayer.seekTo(currentPosition);
            currentPosition = 0L;
        }
        if (exoPlayer.getPlayWhenReady()) {
            isPlaying = true;
            isPaused = false;
        }
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        isPlaying = true;
                        isNextSkipped = false;
                        updatePlaybackState(
                                PlaybackStateCompat.STATE_PLAYING,
                                exoPlayer.getCurrentPosition(),
                                true);
                        break;
                    case Player.STATE_ENDED:
                        isPlaying = false;
                        if (!isNextSkipped) {
                            mediaControllerCompat.getTransportControls().skipToNext();
                            isNextSkipped = true;
                        }
                        break;
                    case Player.STATE_IDLE:
                        isPlaying = false;
                        isNextSkipped = false;
                        updatePlaybackState(
                                PlaybackStateCompat.STATE_STOPPED,
                                exoPlayer.getCurrentPosition(),
                                false);
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                isPlaying = false;
                updatePlaybackState(
                        PlaybackStateCompat.STATE_STOPPED,
                        exoPlayer.getCurrentPosition(),
                        false);
            }
        });
    }

    private SimpleExoPlayer initiateExoPlayer() {
        return (exoPlayer == null) ? new SimpleExoPlayer.Builder(this).build() : exoPlayer;
    }

    public void updateMetadata(String title, String artist, Bitmap bitmap, Long duration) {
        this.duration = duration;
        sharedPreferences.edit().putLong("duration", duration).apply();
        mediaMetaDataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        mediaMetaDataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist);
        mediaMetaDataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap);
        mediaMetaDataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, duration);
        if (mediaMetaDataBuilder != null) {
            mediaSession.setMetadata(mediaMetaDataBuilder.build());
        } else {
            mediaSession.setMetadata(null);
        }
    }

    public void updatePlaybackState(
            Integer state, Long currentPosition,
            Boolean isActive
    ) {
        playbackStateCompat.setState(state, currentPosition, 1f);
        mediaSession.setPlaybackState(playbackStateCompat.build());
        mediaSession.setActive(isActive);
    }


    public class GenerateIconForVideo extends AsyncTask<String, Void, Bitmap> {
        Bitmap videoIcon;

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                videoIcon = Glide.with(getApplicationContext()).asBitmap().load(strings[0]).submit().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return videoIcon;
        }
    }

    public void updateMusicRecyclerViewGraphics(boolean isValidSongIndex) {
        selectedPosition = (isValidSongIndex) ? sharedPreferences.getInt("index", -1) : -1;
        AudioPlayerAdapter.audioPlayerAdapter.notifyDataSetChanged();
    }
}
