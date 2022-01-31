package com.aaronfodor.android.songquiz.model.repository.dataclasses

data class Track (
        var id: String,
        val name: String = "",
        val artists: List<String> = arrayListOf(),
        val album: String = "",
        val durationMs: Int = 0,
        val uri: String = "",
        val popularity: Int = 0,
        val previewUri: String = "",
        val type: String = "",
)