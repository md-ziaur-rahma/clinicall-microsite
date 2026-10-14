package com.example.nativemicrosite

import com.example.nativemicrosite.retrofit.ApiClient
import com.example.nativemicrosite.retrofit.TokenRequest
import com.example.nativemicrosite.retrofit.TokenResponse
import retrofit2.Response
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.nativemicrosite.storage.TokenStorage
import com.example.nativemicrosite.ui.theme.NativeMicrositeTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME, MICROSITE
}

class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )

    private var permissionGranted by mutableStateOf(false)
    private var token by mutableStateOf("")
    private var isLoading by mutableStateOf(false)
    private var currentScreen by mutableStateOf(AppScreen.HOME)

    // Permission launcher
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.values.all { it }

            if (allGranted) {
                permissionGranted = true
                initAppFlow()
            } else {
                Toast.makeText(this, "Camera & Mic permission needed", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkPermissions()
    }

    // Permissions check
    private fun checkPermissions() {
        val notGranted = permissions.any {
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (notGranted) {
            permissionLauncher.launch(permissions)
        } else {
            permissionGranted = true
            initAppFlow()
        }
    }

    // Load saved token and UI
    private fun initAppFlow() {
        token = TokenStorage.getToken(this)
        Log.d("Native Microsite Log", "Loaded Token: $token")

        if (token.isEmpty()) regenerateToken()

        buildUI()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun buildUI() {
        setContent {
            NativeMicrositeTheme {

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFE91E63),   // Blue background
                            titleContentColor = Color.White       // Title white color
                        ),
                            modifier = Modifier.shadow(elevation = 8.dp),
                                    title = {
                                Text(
                                    if (currentScreen == AppScreen.HOME)
                                        "Native Microsite"
                                    else
                                        "Microsite"
                                )
                            },
                            navigationIcon = {
                                if (currentScreen == AppScreen.MICROSITE) {
                                    IconButton(onClick = { currentScreen = AppScreen.HOME }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                        )
                    }
                ) { padding ->

                    when (currentScreen) {

                        AppScreen.HOME -> {
                            HomeScreen(
                                onOpenMicrosite = {
                                    currentScreen = AppScreen.MICROSITE
                                },
                                modifier = Modifier.padding(padding)
                            )
                        }

                        AppScreen.MICROSITE -> {
                            if (!isLoading && token.isNotEmpty()) {
                                Log.d("Native Microsite Log", "Passed Token: $token")
                                MicrositeWebView(
                                    micrositeUrl = "https://microsite.theclinicall.com/",
//                                    micrositeUrl = "http://192.168.10.45:3002/",
                                  token = token,
                                    onTokenExpired = {
                                        regenerateToken()
                                    },
                                    onCloseWebView = {
                                        currentScreen = AppScreen.HOME
                                    },
                                    modifier = Modifier.padding(padding)
                                )
                            }

                            if (isLoading) FullScreenLoader()
                        }
                    }

                    // Handle BACK for Microsite → Home
                    BackHandler(enabled = currentScreen == AppScreen.MICROSITE) {
                        currentScreen = AppScreen.HOME
                    }
                }
            }
        }
    }

    private fun regenerateToken() {
        Log.d("Native Microsite Log", "Generating token…")

        lifecycleScope.launch {
            isLoading = true

            try {
                val requestBody = TokenRequest(
                    countryCode = "BD",
                    dialCode = "880",
//                    phone = "1987089980",// bkash test number
                    phone = "1401840479", // badhon test number
                    // for live token old
//                    accessKey = "DJ4AZ25ZGYMOA5K3PDP6MGIRIWC9IEIHGZ5XRTT2WI0798TO80OJT7FT5D54G34Z"
                    accessKey = "C9IEIHGZ5XRTT24ZDJ4AZ25ZGYMOA5K3PDP6MGIRIWWI0798TO80OJT7FT5D54G3"
//                    accessKey = "P3X9MN2K5R7T8W4Q6A1Z0YBCDEFGHIJLSUVWO9K4M2N8"
//                    accessKey = "YASIDG57BIIRYBE4FCGWCKQ6MW4IOG9FFXKQV2BT2MRQCMKU9HDGILAUU9BMHL63"
                )

                val response = ApiClient.service.regenerateToken(requestBody)
                val newToken = response.body()?.data?.accessToken

                Log.e("API_ERROR", response.errorBody()?.string() ?: "No errorBody")

                if (response.isSuccessful && !newToken.isNullOrEmpty()) {
                    token = newToken
                    TokenStorage.saveToken(this@MainActivity, newToken)
                    Log.d("Native Microsite Log", "Token Saved: $newToken")
                } else {
                    Log.d("Native Microsite Log", "Token Api Failed")
                    Toast.makeText(this@MainActivity, "Token API Failed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.d("Native Microsite Log", "Error: ${e.message}")
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    @Composable
    fun FullScreenLoader() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun HomeScreen(
        onOpenMicrosite: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onOpenMicrosite() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63),      // Button background color
                    contentColor = Color.White        // Text color
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,       // Normal state elevation
                    pressedElevation = 12.dp,      // Button চাপলে elevation
                    focusedElevation = 10.dp,
                    hoveredElevation = 10.dp
                )
            ) {
                Text(
                    "Open Microsite",
                    color = Color.White               // Text color (optional, কারণ উপরে contentColor দেওয়া আছে)
                )
            }
        }
    }

}
