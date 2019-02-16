package com.example.fuent.laboratorio6

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import java.util.*
import android.os.IBinder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.view.MenuItem
import android.view.View
import android.widget.MediaController.MediaPlayerControl

class MainActivity : AppCompatActivity(), MediaPlayerControl {

    var songList: ArrayList<Song> = ArrayList()
    var musicSrv = MusicService()
    var playIntent = Intent()
    var musicBound : Boolean = false
    private var paused : Boolean =false
    private var playbackPaused : Boolean = false
    var controller : MusicController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var songView: ListView = findViewById(R.id.lista)

        getSongList()

        Collections.sort(songList, object : Comparator<Song> {
            override fun compare(a: Song, b: Song): Int {
                return a.getTitle().compareTo(b.getTitle())
            }
        })
        val songAdt = SongAdapter(this, songList)
        songView.adapter = songAdt
        musicSrv.setList(songList)
        setController()
    }



    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller?.hide()
        super.onStop()
    }

    override fun isPlaying(): Boolean {
        if(musicSrv!=null){
        return musicSrv.isPng();}
        else{return false;}
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        if(musicSrv!=null) {
            return musicSrv.getDur();
        }else {return 0;}
    }

    override fun getBufferPercentage(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(pos: Int) {
        musicSrv.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        if(musicSrv!=null)
        return musicSrv.getPosn();
        else {return 0;}
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        musicSrv.go()
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        return true
    }

    private fun setController(){
        var controller = MusicController(this)
        controller.setPrevNextListeners({ playNext() }) { playPrev() }
        controller.setMediaPlayer(this)
        controller.setAnchorView(findViewById(R.id.lista))
        controller.setEnabled(true)
    }



    private val musicConnection = object: ServiceConnection{
        override fun onServiceConnected (name: ComponentName, service: IBinder){
            val binder = service as MusicService.MusicBinder
            // Conseguir servicio
            musicSrv = binder.service
            // Pasar la lista
            musicSrv.setList(songList)
            musicBound = true
        }
        override fun onServiceDisconnected (name : ComponentName) {
            musicBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    fun getSongList() {
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext())
        }
    }

    fun songPicked (view : View ){
        musicSrv!!.setSong(Integer.parseInt(view.getTag().toString()))
        musicSrv!!.playSong()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller!!.show(0)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            R.id.action_shuffle -> {
            }
            R.id.action_end -> {
                stopService(playIntent)
                musicSrv = null!!
                System.exit(0)
            }
            R.id.action_shuffle ->{
                musicSrv.setShuffle()
            }
        }//shuffle
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy(){
        stopService(playIntent)
        musicSrv = null!!
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissionForReadExtertalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun playNext() {
        musicSrv.playNext()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller?.show(0)
    }

    private fun playPrev() {
        musicSrv.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller?.show(0)
    }

    override fun pause() {
        playbackPaused = true
        musicSrv.pausePlayer()
    }
}