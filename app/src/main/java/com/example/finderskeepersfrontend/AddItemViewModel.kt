package com.example.finderskeepersfrontend

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class VoiceField { ITEM_NAME, STORAGE_LOCATION }

class AddItemViewModel : ViewModel() {
    val items = mutableStateListOf(
        ItemEntity(
            id = 1,
            itemName = "Passport",
            room = "Bedroom",
            storageLocation = "Top drawer of the nightstand",
            imageRes = R.drawable.nightstand
        ),
        ItemEntity(
            id = 2,
            itemName = "Blender",
            room = "Kitchen",
            storageLocation = "Cabinet above the fridge",
            imageRes = R.drawable.refrigerator_cabinet
        ),
        ItemEntity(
            id = 3,
            itemName = "Umbrella",
            room = "Home Office",
            storageLocation = "Behind the door",
            imageRes = R.drawable.behind_door
        )
    )

    private var nextId = items.size + 1

    var selectedRoom by mutableStateOf<String?>(null)
        private set
    var itemName by mutableStateOf("")
        private set
    var storageLocation by mutableStateOf("")
        private set
    var selectedImageRes by mutableStateOf<Int?>(null)
        private set
    var capturedImage by mutableStateOf<Bitmap?>(null)
        private set

    var lastSavedItem by mutableStateOf<ItemEntity?>(null)
        private set

    private var editingItemId: Int? = null

    fun selectRoom(room: String) {
        selectedRoom = room
    }

    fun onItemNameChange(value: String) {
        itemName = value
    }

    fun onStorageLocationChange(value: String) {
        storageLocation = value
    }

    fun selectImage(res: Int) {
        selectedImageRes = res
        capturedImage = null
    }

    fun selectCapturedImage(bitmap: Bitmap) {
        capturedImage = bitmap
        selectedImageRes = null
    }

    fun startEditing(item: ItemEntity) {
        editingItemId = item.id
        selectedRoom = item.room
        itemName = item.itemName
        storageLocation = item.storageLocation
        selectedImageRes = item.imageRes
        capturedImage = item.capturedImage
    }

    fun discardInProgress() {
        editingItemId = null
        selectedRoom = null
        itemName = ""
        storageLocation = ""
        selectedImageRes = null
        capturedImage = null
    }

    fun saveItem() {
        val id = editingItemId
        val newItem = ItemEntity(
            id = id ?: nextId,
            itemName = itemName,
            room = selectedRoom ?: "",
            storageLocation = storageLocation,
            imageRes = selectedImageRes,
            capturedImage = capturedImage
        )
        if (id != null) {
            val index = items.indexOfFirst { it.id == id }
            if (index != -1) items[index] = newItem
        } else {
            items.add(newItem)
            nextId++
        }
        lastSavedItem = newItem
        discardInProgress()
    }

    fun deleteItem(item: ItemEntity) {
        items.removeAll { it.id == item.id }
    }
}

val addItemViewModel = AddItemViewModel()