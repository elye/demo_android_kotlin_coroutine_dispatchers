package com.example.coroutinerevise

import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.*

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
        CoroutineExceptionHandler
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


    @Test
    fun testCoroutineName() {
        runBlocking {
            launch(CoroutineName("My-Own-Coroutine")) {
                println(coroutineContext)
            }
        }
    }

    @Test
    fun testCoroutineExceptionHandler() {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }

        runBlocking {
            MainScope().launch(handler) {
                launch {
                    throw IllegalAccessException("Just testing")
                }
            }.join()
        }
    }

    @Test
    fun testContinuationInterceptor() {
        val interception = object : ContinuationInterceptor {
            override val key: CoroutineContext.Key<*>
                get() = ContinuationInterceptor

            override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
                println("  ## Interception Setup for ${continuation.context[Job]} ##")
                return Continuation(continuation.context) {
                    println("  ~~ Interception for ${continuation.context[Job]} ~~")
                    continuation.resumeWith(it)
                }
            }
        }

        runBlocking(CoroutineName("runBlocker") + interception) {
            println("Started runBlocking")
            launch(CoroutineName("launcher")) {
                println("Started launch")
                delay(10)
                println("End launch")
            }
            delay(10)
            println("End runBlocking")
        }
    }

    @Test
    fun testSwitchContext() {
        newSingleThreadContext("Ctx1").use { ctx1 ->
            newSingleThreadContext("Ctx2").use { ctx2 ->
                runBlocking(ctx1) {
                    println("Started in ctx1 $coroutineContext")
                    withContext(ctx2) {
                            println("Working in ctx2 $coroutineContext")
                    }
                    println("Back to ctx1 $coroutineContext")
                }
            }
        }
    }
}