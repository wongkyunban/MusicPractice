package com.hoppy.musicpractice
import com.google.gson.annotations.SerializedName

data class NoteChord(
    @SerializedName("payload")
    val payload: List<Note>
)

data class Note(
    @SerializedName("chords")
    val chords: Chords,
    @SerializedName("note")
    val note: String
)

data class Chords(
    @SerializedName("five")
    val five: String,
    @SerializedName("four")
    val four: String,
    @SerializedName("one")
    val one: String,
    @SerializedName("seven")
    val seven: String,
    @SerializedName("six")
    val six: String,
    @SerializedName("three")
    val three: String,
    @SerializedName("two")
    val two: String
)