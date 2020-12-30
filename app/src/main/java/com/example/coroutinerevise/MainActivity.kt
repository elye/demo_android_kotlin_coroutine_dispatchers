package com.example.coroutinerevise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking(block = launchesFunction("Nothing"))
        runBlocking(Dispatchers.IO, launchesFunction("IO"))
        runBlocking(Dispatchers.Default, launchesFunction("Default"))
        runBlocking(Dispatchers.Unconfined, launchesFunction("Unconfined"))
        runBlocking(newSingleThreadContext("MyOwnThread"), launchesFunction("MyOwnThread"))
    }

    private fun launchesFunction(message: String): suspend CoroutineScope.() -> Job? = {
        printThread("Starting : $message", coroutineContext)
        launch {
            launchPrint("Nothing    ", coroutineContext)
        }
        launch(Dispatchers.Default) {
            launchPrint("IO         ", coroutineContext)
        }
        launch(Dispatchers.Default) {
            launchPrint("Default    ", coroutineContext)
        }
        launch(Dispatchers.Unconfined) {
            launchPrint("Unconfined ", coroutineContext)
        }
        launch(newSingleThreadContext("MyOwnThread")) {
            launchPrint("MyOwnThread", coroutineContext)
        }
        delay(100)
        printThread("Ending : $message", coroutineContext)
        coroutineContext[Job]
    }

    private suspend fun launchPrint(message: String, coroutineContext: CoroutineContext) {
        printThread("  pre-$message", coroutineContext)
        delay(10)
        printThread("  pos-$message", coroutineContext)
    }

    private fun printThread(message: String, coroutineContext: CoroutineContext) {
        Log.d("Track", "  $message : $coroutineContext:${Thread.currentThread()}")
    }
}