package com.example.colorpalette.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.colorpalette.data.AppState
import com.example.colorpalette.data.ColorInfo
import com.example.colorpalette.monitoring.AppMonitor
import com.example.colorpalette.viewmodel.ColorPaletteViewModel

/**
 * Main screen of the Color Palette application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteScreen(
    viewModel: ColorPaletteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackAlreadyShown by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    val appMonitor = remember { AppMonitor.getInstance(context) }
    
    // Show Analytics Screen or Main Screen
    if (showAnalytics) {
        AnalyticsScreen(
            appMonitor = appMonitor,
            onBack = { showAnalytics = false }
        )
    } else {
        MainScreenContent(
            uiState = uiState,
            showFeedbackDialog = showFeedbackDialog,
            onShowFeedbackDialog = { showFeedbackDialog = it },
            onShowAnalytics = { showAnalytics = true },
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    uiState: AppState,
    showFeedbackDialog: Boolean,
    onShowFeedbackDialog: (Boolean) -> Unit,
    onShowAnalytics: () -> Unit,
    viewModel: ColorPaletteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var feedbackAlreadyShown by remember { mutableStateOf(false) }
    
    // Auto-show feedback dialog 5 seconds after successful color extraction
    LaunchedEffect(uiState) {
        if (uiState is AppState.Success && !feedbackAlreadyShown) {
            kotlinx.coroutines.delay(5000) // 5 seconds
            onShowFeedbackDialog(true)
            feedbackAlreadyShown = true
        }
        
        // Reset flag when new image is being processed
        if (uiState is AppState.Loading) {
            feedbackAlreadyShown = false
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processImage(context, it)
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(
                context,
                "Permission denied. Cannot access images.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Palette Extractor") },
                actions = {
                    IconButton(onClick = onShowAnalytics) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "View Analytics"
                        )
                    }
                    IconButton(onClick = { onShowFeedbackDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = "Send Feedback"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AppState.Initial -> {
                    InitialScreen(
                        onSelectImage = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    )
                }
                is AppState.Loading -> {
                    LoadingScreen()
                }
                is AppState.Success -> {
                    SuccessScreen(
                        imageUri = state.imageUri,
                        colors = state.colors,
                        onSelectNewImage = {
                            viewModel.resetState()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        onCopyColor = { hexCode ->
                            copyToClipboard(context, hexCode)
                            Toast.makeText(
                                context,
                                "Copied $hexCode to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
                is AppState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = {
                            viewModel.resetState()
                        }
                    )
                }
            }
        }
        
        // Feedback dialog
        if (showFeedbackDialog) {
            FeedbackDialog(
                onDismiss = { onShowFeedbackDialog(false) },
                onSubmit = { rating, comment ->
                    // Get AppMonitor instance
                    val monitor = AppMonitor.getInstance(context)
                    
                    // Record feedback in AppMonitor (Room database)
                    monitor.recordFeedback(rating, comment, contextInfo = "Manual feedback")
                    
                    // Also record in old Analytics for backward compatibility
                    com.example.colorpalette.utils.Analytics.recordFeedback(rating, comment)
                    
                    // Show confirmation to user
                    Toast.makeText(
                        context,
                        "Thank you for your feedback! Rating: $rating",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // In production, also send to remote analytics:
                    // FirebaseAnalytics.getInstance(context).logEvent("user_feedback") { ... }
                    // Or send to your backend API
                    
                    onShowFeedbackDialog(false)
                }
            )
        }
    }
}

/**
 * Initial screen with instructions
 */
@Composable
fun InitialScreen(
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Extract Color Palette",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Select an image from your gallery to extract its dominant colors and get their hex codes",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onSelectImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select Image",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Features list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FeatureItem("Extract vibrant and muted colors")
                FeatureItem("Get hex codes for each color")
                FeatureItem("Copy colors to clipboard")
                FeatureItem("See color popularity")
            }
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Loading screen
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Extracting colors...",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Success screen showing the image and extracted colors
 */
@Composable
fun SuccessScreen(
    imageUri: Uri,
    colors: List<ColorInfo>,
    onSelectNewImage: () -> Unit,
    onCopyColor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // Button to select new image
        item {
            OutlinedButton(
                onClick = onSelectNewImage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Another Image")
            }
        }
        
        // Colors title
        item {
            Text(
                text = "Extracted Colors (${colors.size})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Color items
        items(colors) { colorInfo ->
            ColorItem(
                colorInfo = colorInfo,
                onCopy = { onCopyColor(colorInfo.hexCode) }
            )
        }
    }
}

/**
 * Individual color item display
 */
@Composable
fun ColorItem(
    colorInfo: ColorInfo,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopy() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorInfo.color)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Color information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = colorInfo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = colorInfo.hexCode,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Population: ${colorInfo.population}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Copy icon
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy hex code",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Error screen
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

/**
 * Feedback dialog for user feedback
 */
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "How would you rate this app?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Star rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i }
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Additional comments (optional):",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Share your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (rating > 0) {
                        onSubmit(rating, comment)
                    }
                },
                enabled = rating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Utility function to copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Color Code", text)
    clipboard.setPrimaryClip(clip)
}

