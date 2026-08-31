package com.example.finderskeepersfrontend

import android.graphics.Bitmap

data class ItemEntity(
    val id: Int,
    val itemName: String,
    val room: String,
    val storageLocation: String,
    val imageRes: Int? = null,
    val capturedImage: Bitmap? = null
)