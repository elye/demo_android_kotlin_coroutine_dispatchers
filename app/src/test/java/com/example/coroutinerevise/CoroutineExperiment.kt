package com.example.coroutinerevise

import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class CoroutineExperiment {
    private val mainThreadSurrogate = newSingleThreadContext("Test Main")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun runAllVariation() {
        runBlocking(block = launchesFunction("Nothing"))
        runBlocking(Dispatchers.Main, launchesFunction("Main"))
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
        launch(Dispatchers.Main) {
            launchPrint("Main       ", coroutineContext)
        }
        launch(Dispatchers.IO) {
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
        println("$message : $coroutineContext:${Thread.currentThread()}")
    }
}