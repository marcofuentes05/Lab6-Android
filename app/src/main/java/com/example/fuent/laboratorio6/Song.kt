package com.example.fuent.laboratorio6

/**
 * Created by Marco Fuentes on 14/02/2019.
 */

class Song {
    private var id : Long =0L
    private var title : String =" "
    private var artist : String = ""

    constructor(i : Long, ti : String, ar : String){
        id = i
        title = ti
        artist = ar
    }

    fun getTitle(): String{
        return this.title
    }
    fun getArtist():String{
        return artist
    }

}