package com.example.fuent.laboratorio6

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.widget.ArrayAdapter
import java.util.*
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.example.fuent.laboratorio6.MusicService






class MainActivity : AppCompatActivity() {
    var songList: ArrayList<Song> = ArrayList()

    var musicSrv = MusicService()
    var playIntent = Intent()
    var musicBound : Boolean = false

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
      songView!!.setAdapter(songAdt)
      //setController()
      musicSrv.setList(songList)
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
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()))
        musicSrv.playSong()
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
        }//shuffle
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy(){
        stopService(playIntent)
        musicSrv = null!!
        super.onDestroy()
    }

}