package com.example.fuent.laboratorio6

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Marco Fuentes on 14/02/2019.
 */

class SongAdapter : BaseAdapter{

    private var songs : ArrayList<Song> = arrayListOf()
    private var songInf : LayoutInflater

    constructor(c: Context, theSongs : ArrayList<Song>){
        songs = theSongs
        songInf=LayoutInflater.from(c)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //map to song layout
        val songLay = (songInf.inflate(R.layout.song, parent, false)) as LinearLayout
        //get title and artist views
        val songView = songLay.findViewById<View>(R.id.song_title) as TextView
        val artistView = songLay.findViewById<View>(R.id.song_artist) as TextView
        //get song using position
        val currSong = songs[position]
        //get title and artist strings
        songView.text = currSong.getTitle()
        artistView.setText(currSong.getArtist())
        //set position as tag
        songLay.tag = position
        return songLay
    }

    override fun getItem(position: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return songs.size
    }
}