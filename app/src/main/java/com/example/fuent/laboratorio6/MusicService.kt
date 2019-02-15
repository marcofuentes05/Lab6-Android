package com.example.fuent.laboratorio6

import android.app.Service
import java.util.ArrayList;
import android.content.ContentUris;
import android.content.Intent
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Marco Fuentes on 14/02/2019.
 */

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private var player = MediaPlayer()
    private var songs : ArrayList <Song> = arrayListOf()
    private var songPosn: Int = 0
    private var musicBind : IBinder = MusicBinder()

    fun setList(theSongs: ArrayList<Song>){
        songs = theSongs
        println("El tamaño es de ${songs.size}")
    }

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
        player.setWakeMode( getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }


    override fun onPrepared(mp: MediaPlayer?) {
        //Comenzar el playback
        mp!!.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        player.reset()
        //conseguir la cancion
        val playSong = songs[songPosn]
        //conseguir el identificador
        val currSong = playSong.getID()
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

    }git

}