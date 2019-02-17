package com.example.fuent.laboratorio6

import android.app.Service
import java.util.ArrayList
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import java.util.Random
import android.app.Notification
import android.app.PendingIntent
import android.os.Build
import android.support.annotation.RequiresApi


/**
 * Created by Marco Fuentes on 14/02/2019.
 */

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private var songTitle : String = ""
    private var NOTIFY_ID = 1
    private var player : MediaPlayer = MediaPlayer()
    private var songs : ArrayList <Song> = arrayListOf()
    private var songPosn: Int = 0
    private var musicBind : IBinder = MusicBinder()
    private var shuffle = false
    private var rand : Random = Random()

    fun setShuffle(){
        shuffle = !shuffle
    }

    fun setList(theSongs: ArrayList<Song>){
        songs = theSongs
    }

    //binder
    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onCreate(){
        super.onCreate()
        songPosn = 0
        player = MediaPlayer()
        initMusicPlayer()
    }

    fun initMusicPlayer(){
        player.setWakeMode(applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)

        //Fijar Listeners
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }


    override fun onPrepared(mp: MediaPlayer) {
        //Comenzar el playback
        mp.start()

        //Notificacion
        var notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendInt : PendingIntent = PendingIntent.getActivity(this,0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var builder : Notification.Builder = Notification.Builder(this)

        builder.setContentIntent (pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)
        var not : Notification = builder.build()
        startForeground(NOTIFY_ID, not)

    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.v("MUSIC PLAYER", "Playback Error")
        mp.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if(player.currentPosition > 0){
            mp.reset()
            playNext()
        }
    }

    override fun onDestroy(){
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun setSong(songIndex : Int){
        songPosn = songIndex
    }


    fun playSong(){
        // reproducir
        player.reset()
        //conseguir la cancion
        var playSong = songs[songPosn]
        songTitle = playSong.getTitle()
        //conseguir el identificador
        var currSong : Long = playSong.getID()
        //Fijar el URI
        val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync()
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosn = songPosn-1
        if(songPosn < 0){
            songPosn = songs.size-1
        }
        playSong()
    }

    fun playNext(){
        if (shuffle){
            var newSong : Int = songPosn
            while (newSong == songPosn){
                newSong=rand.nextInt(songs.size)
            }
            songPosn = newSong
        }else {
            songPosn = songPosn + 1
            if (songPosn >= songs.size) {
                songPosn = 0
            }
        }
        playSong()
    }



}