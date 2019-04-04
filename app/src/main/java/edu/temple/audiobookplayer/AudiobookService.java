package edu.temple.audiobookplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AudiobookService extends Service {

    private static final String TAG = "Audiobook Service";
    MediaPlayer mediaPlayer;
    Notification notification;
    Handler progressHandler;
    Thread progressThread;
    int playingState; //0 - stopped, 1 - playing, 2 - paused
    int startPosition;

    private final String NOTIFICATION_CHANNEL_ID = "media_player_control";
    private final String BOOK_DOWNLOAD_URL = "https://kamorris.com/lab/audlib/download.php?id=";

    public AudiobookService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i(TAG, "Audiobook prepared");
                playingState = 1;
                if (startPosition > 0) {
                    mediaPlayer.seekTo(1000 * startPosition);
                    startPosition = 0;
                }
                mediaPlayer.start();
                progressThread = new NotifyProgress();
                progressThread.start();
                Log.i(TAG, "Audiobook started");

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                progressThread = null;
            }
        });

        String NOTIFICATION_PLAYING_TITLE = getString(R.string.notification_playing_title);
        String NOTIFICATION_PLAYING_DESCRIPTION = getString(R.string.notification_playing_description);

        notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(NOTIFICATION_PLAYING_TITLE)
                .setContentText(NOTIFICATION_PLAYING_DESCRIPTION)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Player bound");
        return new MediaControlBinder();
    }

    private void setHandler (Handler handler) {
        Log.i(TAG, "Handler set");
        progressHandler = handler;
    }

    private void play(int id) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(BOOK_DOWNLOAD_URL + id);
            playingState = 0;
            mediaPlayer.prepareAsync();
            Log.i(TAG, "Audiobook preparing");
            int FOREGROUND_CODE = 1;
            startForeground(FOREGROUND_CODE, notification);
            Log.i(TAG, "Foreground notification started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void play(int id, int position) {
        startPosition = position;
        play(id);
    }

    private void play(File file) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource((new FileInputStream(file)).getFD());
            playingState = 0;
            mediaPlayer.prepareAsync();
            Log.i(TAG, "Audiobook preparing");
            int FOREGROUND_CODE = 1;
            startForeground(FOREGROUND_CODE, notification);
            Log.i(TAG, "Foreground notification started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void play(File file, int position) {
        startPosition = position;
        play(file);
    }

    private void pause () {
        if (playingState == 1) {
            playingState = 2;
            mediaPlayer.pause();
            if (progressThread != null) {
                progressThread.interrupt();
                progressThread = null;
            }
            Log.i(TAG, "Player paused");
        } else if (playingState == 2) {
            playingState = 1;
            mediaPlayer.start();
            progressThread = new NotifyProgress();
            progressThread.start();
            Log.i(TAG, "Player started");
        }
    }

    private void stop() {
        mediaPlayer.stop();
        playingState = 0;
        stopForeground(true);
        if (progressThread != null) {
            progressThread.interrupt();
            progressThread = null;
        }
        Log.i(TAG, "Player stopped");
    }

    private void seekTo(int position) {
        position = position * 1000;
        if (position <= mediaPlayer.getDuration()) {
            mediaPlayer.seekTo((position));
            Log.i(TAG, "Audiobook position changed");
        }
    }

    public class MediaControlBinder extends Binder {

        public void play(int id) {
            AudiobookService.this.play(id);
        }

        public void play(int id, int startPosition) {
            AudiobookService.this.play(id, startPosition);
        }

        public void play(File file) {
            AudiobookService.this.play(file);
        }

        public void play(File file, int startPosition) {
            AudiobookService.this.play(file, startPosition);
        }

        public void pause() {
            AudiobookService.this.pause();
        }

        public void stop() {
            AudiobookService.this.stop();
        }

        public void setProgressHandler(Handler handler) {
            AudiobookService.this.setHandler(handler);
        }

        public void seekTo(int position) {
            AudiobookService.this.seekTo(position);
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        progressHandler = null;
        return super.onUnbind(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    class NotifyProgress extends Thread {

        @Override
        public void run() {
            while (playingState == 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Progress update stopped");
                }
                if (progressHandler != null) {
                    if (playingState == 1)
                        progressHandler.sendEmptyMessage(mediaPlayer.getCurrentPosition() / 1000);
                    else if (playingState == 0)
                        progressHandler.sendEmptyMessage(0);
                }
            }
        }

    }
}