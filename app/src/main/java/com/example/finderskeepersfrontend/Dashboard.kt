package com.example.finderskeepersfrontend

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.finderskeepersfrontend.ui.theme.Bathroom
import com.example.finderskeepersfrontend.ui.theme.Bedroom
import com.example.finderskeepersfrontend.ui.theme.FindersGreen
import com.example.finderskeepersfrontend.ui.theme.FindersKeepersFrontendTheme
import com.example.finderskeepersfrontend.ui.theme.HomeOffice
import com.example.finderskeepersfrontend.ui.theme.KeepersOrange
import com.example.finderskeepersfrontend.ui.theme.Kitchen
import com.example.finderskeepersfrontend.ui.theme.LivingArea
import com.example.finderskeepersfrontend.ui.theme.Veranda
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindersKeepersFrontendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        DashboardScreen()
                    }
                }
            }
        }
    }
}

private object DashboardRoutes {
    const val home = "home"
    const val selectRoom = "add_item/select_room"
    const val addItemDetails = "add_item/details"
    const val captureLocation = "add_item/capture_location"
    const val success = "add_item/success"
    const val itemDetail = "item_detail/{itemId}"
}

private val samplePhotoOptions = listOf(
    R.drawable.nightstand,
    R.drawable.refrigerator_cabinet,
    R.drawable.behind_door
)

@Composable
fun DashboardScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = DashboardRoutes.home) {
        composable(DashboardRoutes.home) {
            var searchQuery by remember { mutableStateOf("") }
            var pendingDeleteItem by remember { mutableStateOf<ItemEntity?>(null) }
            val items = addItemViewModel.items

            FindersKeepersHomeScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                items = items,
                onAddClick = {
                    navController.navigate(DashboardRoutes.selectRoom)
                },
                onItemClick = { item ->
                    navController.navigate("item_detail/${item.id}")
                },
                onEditItemClick = { item ->
                    addItemViewModel.startEditing(item)
                    navController.navigate(DashboardRoutes.selectRoom)
                },
                onDeleteItemClick = { item ->
                    pendingDeleteItem = item
                }
            )

            pendingDeleteItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { pendingDeleteItem = null },
                    title = { Text("Delete item?") },
                    text = {
                        Text("This will permanently remove \"${item.itemName.ifBlank { "this item" }}\" from FindersKeepers.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            addItemViewModel.deleteItem(item)
                            pendingDeleteItem = null
                        }) {
                            Text("Delete", color = Color(0xFFC9752F), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteItem = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        composable(DashboardRoutes.selectRoom) {
            SelectRoomScreen(
                navController = navController,
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.addItemDetails) {
            AddItemScreen(
                onNextClick = { navController.navigate(DashboardRoutes.captureLocation) },
                onBackClick = { navController.popBackStack() },
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.captureLocation) {
            CaptureLocationScreen(
                onConfirmSave = {
                    navController.navigate(DashboardRoutes.success) {
                        popUpTo(DashboardRoutes.home)
                    }
                },
                navController = navController,
                viewModel = addItemViewModel
            )
        }
        composable(DashboardRoutes.success) {
            SuccessScreen(
                item = addItemViewModel.lastSavedItem,
                onBackToHomeClick = {
                    navController.navigate(DashboardRoutes.home) {
                        popUpTo(DashboardRoutes.home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = DashboardRoutes.itemDetail,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            val item = addItemViewModel.items.find { it.id == itemId }

            ItemDetailScreen(
                item = item,
                onFinishClick = { navController.popBackStack() }
            )
        }
    }
}

// Home Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FindersKeepersHomeScreen(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    items: List<ItemEntity> = emptyList(),
    onAddClick: () -> Unit = {},
    onItemClick: (ItemEntity) -> Unit = {},
    onEditItemClick: (ItemEntity) -> Unit = {},
    onDeleteItemClick: (ItemEntity) -> Unit = {}
) {
    val query = searchQuery.trim()
    val isSearching = query.isNotEmpty()
    val visibleItems = remember(items, query) {
        if (query.isEmpty()) {
            items
        } else {
            items.filter {
                it.itemName.contains(query, ignoreCase = true) ||
                        it.room.contains(query, ignoreCase = true) ||
                        it.storageLocation.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log a new item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            AppTitleBar()

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            SectionHeader(text = if (isSearching) "MATCHES" else "RECENTLY STORED")

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            if (visibleItems.isEmpty()) {
                Text(
                    text = if (items.isEmpty())
                        "Nothing stored yet — tap + to log your first item."
                    else
                        "No items match \"$query\".",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                    visibleItems.forEach { item ->
                        RecentItemCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onEditClick = { onEditItemClick(item) },
                            onDeleteClick = { onDeleteItemClick(item) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isSearching) {
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "No more", fontSize = 12.sp, color = Color(0xFFB9B9B9))
                }
            } else {
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
                TapToLogPromptCard()
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        }
    }
}

@Composable
private fun RecentItemCard(
    item: ItemEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
                modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size))
            ) {
                ItemThumbnail(item = item)
            }
            Spacer(Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName.ifBlank { "Unnamed item" },
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
                Text(
                    text = "${item.room} • ${item.storageLocation.ifBlank { "No details" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit ${item.itemName}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete ${item.itemName}",
                    tint = Color(0xFFC9752F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemThumbnail(item: ItemEntity) {
    when {
        item.capturedImage != null -> {
            Image(
                bitmap = item.capturedImage.asImageBitmap(),
                contentDescription = item.itemName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        item.imageRes != null -> {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.itemName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppTitleBar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Finders",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = FindersGreen
        )
        Text(
            text = "Keepers",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = KeepersOrange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_pill)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 18.dp)
        ) {
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Search item", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF444444))
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = Color(0xFF8A8A8A)
    )
}

@Composable
private fun TapToLogPromptCard() {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
                modifier = Modifier.size(dimensionResource(R.dimen.thumbnail_size))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tap the plus to log",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Store an item simply by using your microphone to log.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Shared header used by every step of the Add Item flow
@Composable
private fun AddItemFlowHeader(onBackClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF444444))
        }
        Spacer(Modifier.width(4.dp))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }
    }
}

// Add Item & Store — Select Room Screen
@Composable
private fun SelectRoomScreen(
    navController: NavHostController,
    viewModel: AddItemViewModel
) {
    val selectedRoom = viewModel.selectedRoom

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = {
            viewModel.discardInProgress()
            navController.popBackStack()
        })

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text(
            text = "Add Item & Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "SELECT A ROOM")

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        FloorPlanRoomPicker(
            selectedRoom = selectedRoom,
            onRoomSelected = { room -> viewModel.selectRoom(room) }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Selected Location:",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(12.dp))
            SelectedRoomChip(room = selectedRoom)
        }

        Spacer(Modifier.weight(1f))

        StepDots(activeIndex = 0, totalSteps = 3)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = {
                navController.navigate(DashboardRoutes.addItemDetails)
            },
            enabled = selectedRoom != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

@Composable
private fun FloorPlanRoomPicker(
    selectedRoom: String?,
    onRoomSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.floor_plan_height))
            .clip(RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.simple_house_map),
            contentDescription = "Simple Floor Plan",
            modifier = Modifier.fillMaxSize()
        )

        RoomButton(
            label = "Home Office",
            color = HomeOffice,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 5.dp, y = 20.dp)
        )
        RoomButton(
            label = "Bedroom",
            color = Bedroom,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 50.dp)
        )
        RoomButton(
            label = "Bathroom",
            color = Bathroom,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 45.dp)
        )
        RoomButton(
            label = "Living Area",
            color = LivingArea,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 35.dp)
        )
        RoomButton(
            label = "Kitchen",
            color = Kitchen,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-30).dp, y = 10.dp)
        )
        RoomButton(
            label = "Veranda",
            color = Veranda,
            selectedRoom = selectedRoom,
            onClick = onRoomSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-10).dp, y = (-35).dp)
        )
    }
}

@Composable
private fun RoomButton(
    label: String,
    color: Color,
    selectedRoom: String?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedRoom == label
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = { onClick(label) },
            color = color,
            contentColor = Color.White,
            shape = RoundedCornerShape(40),
            shadowElevation = if (isSelected) 1.dp else 0.dp,
            modifier = modifier.border(
                width = if (isSelected) 1.dp else (-1).dp,
                color = Color(0xFFFFC677),
                shape = RoundedCornerShape(40)
            )
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SelectedRoomChip(room: String?) {
    Surface(
        color = Color(0x33FFE6C2),
        shape = RoundedCornerShape(20),
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFFFC677),
                shape = RoundedCornerShape(20)
            )
    ) {
        Text(
            text = room ?: "None selected",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFFFFC677),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

// Item & Store — Item Details Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    var activeVoiceField by remember { mutableStateOf<VoiceField?>(null) }
    var pendingVoiceField by remember { mutableStateOf<VoiceField?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        } else {
            null
        }
        when (activeVoiceField) {
            VoiceField.ITEM_NAME -> spokenText?.let(viewModel::onItemNameChange)
            VoiceField.STORAGE_LOCATION -> spokenText?.let(viewModel::onStorageLocationChange)
            null -> {}
        }
        activeVoiceField = null
    }

    fun launchSpeechRecognizer(field: VoiceField) {
        activeVoiceField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
        }
        speechLauncher.launch(intent)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingVoiceField?.let { launchSpeechRecognizer(it) }
        }
        pendingVoiceField = null
    }

    fun requestMicAndLaunch(field: VoiceField) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSpeechRecognizer(field)
        } else {
            pendingVoiceField = field
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = onBackClick)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text(
            text = "Add Item & Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "ITEM DETAILS")

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        OutlinedTextField(
            value = viewModel.itemName,
            onValueChange = viewModel::onItemNameChange,
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(onClick = { requestMicAndLaunch(VoiceField.ITEM_NAME) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speak item name", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        OutlinedTextField(
            value = viewModel.storageLocation,
            onValueChange = viewModel::onStorageLocationChange,
            label = { Text("Storage Location") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(onClick = { requestMicAndLaunch(VoiceField.STORAGE_LOCATION) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speak storage location", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Tap the mic on either field to speak instead of typing.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        StepDots(activeIndex = 1, totalSteps = 3)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = onNextClick,
            enabled = viewModel.itemName.isNotBlank() && viewModel.storageLocation.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

// Add Item & Store — Choose a Photo Screen
@Composable
private fun CaptureLocationScreen(
    onConfirmSave: () -> Unit,
    navController: NavHostController,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    val selectedImageRes = viewModel.selectedImageRes
    val capturedImage = viewModel.capturedImage
    val hasPhoto = selectedImageRes != null || capturedImage != null
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            if (bitmap != null) {
                viewModel.selectCapturedImage(bitmap)
            } else {
                Toast.makeText(context, "Couldn't load the photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        pendingUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is needed to take a photo.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCameraAndLaunch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal))
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
        AddItemFlowHeader(onBackClick = { navController.popBackStack() })

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Text(
            text = "Add Item & Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        SectionHeader(text = "CHOOSE A PHOTO")

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        ImagePreviewCard(imageRes = selectedImageRes, capturedImage = capturedImage)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        OutlinedButton(
            onClick = { requestCameraAndLaunch() },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0x33FFE6C2),
                contentColor = Color(0xFFC9752F)
            ),
            border = BorderStroke(1.dp, Color(0xFFFFC677)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_pill)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = if (capturedImage == null) "Take Image" else "Retake Image",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.weight(1f))

        StepDots(activeIndex = 2, totalSteps = 3)

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Button(
            onClick = {
                viewModel.saveItem()
                onConfirmSave()
            },
            enabled = hasPhoto,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text(text = "Confirm & Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

private fun createImageUri(context: android.content.Context): Uri {
    val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("item_photo_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
private fun ImagePreviewCard(imageRes: Int?, capturedImage: android.graphics.Bitmap? = null) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.image_preview_height))
    ) {
        when {
            capturedImage != null -> {
                Image(
                    bitmap = capturedImage.asImageBitmap(),
                    contentDescription = "Captured storage location photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            imageRes != null -> {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Selected storage location photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "No photo yet — take one or pick a sample", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StepDots(activeIndex: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until totalSteps) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i == activeIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

// Add Item & Store — Success Screen
@Composable
private fun SuccessScreen(
    item: ItemEntity?,
    onBackToHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE9DDBF)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Text(
            text = "Item saved successfully",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))) {
                SuccessDetailRow(label = "Item Name", value = item?.itemName?.ifBlank { "—" } ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(label = "Room", value = item?.room ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(
                    label = "Storage Location",
                    value = item?.storageLocation?.ifBlank { "—" } ?: "—"
                )
            }
        }

        if (item != null && (item.imageRes != null || item.capturedImage != null)) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = "Attached Image:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                ItemThumbnail(item = item)
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Button(
            onClick = onBackToHomeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.finderskeepersfrontend.ui.theme.Beige,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_pill)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }
}

@Composable
private fun SuccessDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

// Item Details Screen (reached by tapping a Dashboard row)
@Composable
private fun ItemDetailScreen(
    item: ItemEntity?,
    onFinishClick: () -> Unit
) {
    var showFullScreenImage by remember { mutableStateOf(false) }
    val hasImage = item != null && (item.imageRes != null || item.capturedImage != null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.screen_padding_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Row {
            Text(
                text = "Finders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = FindersGreen
            )
            Text(
                text = "Keepers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = KeepersOrange
            )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
        Text(
            text = "Item Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))) {
                SuccessDetailRow(label = "Item Name", value = item?.itemName?.ifBlank { "—" } ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(label = "Room", value = item?.room ?: "—")
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SuccessDetailRow(
                    label = "Storage Location",
                    value = item?.storageLocation?.ifBlank { "—" } ?: "—"
                )
            }
        }

        if (item != null && hasImage) {
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = "Attached Image:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Box {
                Surface(
                    onClick = { showFullScreenImage = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_medium)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    ItemThumbnail(item = item)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .clickable { showFullScreenImage = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInFull,
                        contentDescription = "View image full-screen",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_large)))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onFinishClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height))
        ) {
            Text(text = "Finish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
    }

    if (showFullScreenImage && item != null && hasImage) {
        FullScreenImageViewer(
            item = item,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
private fun FullScreenImageViewer(item: ItemEntity, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
        ) {
            when {
                item.capturedImage != null -> {
                    Image(
                        bitmap = item.capturedImage.asImageBitmap(),
                        contentDescription = "Attached storage location photo, full screen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                item.imageRes != null -> {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = "Attached storage location photo, full screen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.spacing_medium))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}