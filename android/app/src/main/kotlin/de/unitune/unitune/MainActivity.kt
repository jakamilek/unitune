package de.unitune.unitune

import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.googlemobileads.GoogleMobileAdsPlugin

class MainActivity : FlutterActivity() {
    private val CHANNEL = "de.unitune.unitune/intent"
    private var methodChannel: MethodChannel? = null
    private var initialIntent: Map<String, String>? = null
    
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Register Native Ad Factory for Liquid Glass style ads
        GoogleMobileAdsPlugin.registerNativeAdFactory(
            flutterEngine,
            "liquidGlassNative",
            NativeAdFactory(this)
        )
        
        // Create MethodChannel for intent action communication
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel?.setMethodCallHandler { call, result ->
            if (call.method == "getInitialIntent") {
                result.success(initialIntent)
            } else {
                result.notImplemented()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dart registers its MethodChannel handler after the activity is created.
        // Keep the launch intent until Dart explicitly requests it so an initial
        // ACTION_VIEW is not lost during application startup.
        initialIntent = intentInfo(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val intentInfo = intentInfo(intent) ?: return
        
        Log.d("UniTune", "=== Native Intent Handler ===")
        Log.d("UniTune", "Action: ${intentInfo["action"]}")
        Log.d("UniTune", "Data: ${intentInfo["data"]}")
        Log.d("UniTune", "Type: ${intentInfo["type"]}")
        
        // Send intent info to Flutter
        methodChannel?.invokeMethod("onIntent", intentInfo)
    }

    private fun intentInfo(intent: Intent?): Map<String, String>? {
        if (intent == null) return null

        return mapOf(
            "action" to (intent.action ?: ""),
            "data" to (intent.data?.toString() ?: ""),
            "type" to (intent.type ?: "")
        )
    }
    
    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        super.cleanUpFlutterEngine(flutterEngine)
        GoogleMobileAdsPlugin.unregisterNativeAdFactory(flutterEngine, "liquidGlassNative")
        methodChannel = null
    }
}
