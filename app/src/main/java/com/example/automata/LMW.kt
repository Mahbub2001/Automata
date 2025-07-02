package com.example.automata

import android.llama.cpp.LLamaAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class LLMW(private val llamaAndroid: LLamaAndroid = LLamaAndroid.instance()) {

    interface MessageHandler {
        fun h(msg: String)
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)

    fun load(path: String, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        ioScope.launch {
            try {
                llamaAndroid.load(path)
                onComplete?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }

    fun unload() {
        ioScope.launch {
            llamaAndroid.unload()
        }
    }

    fun send(message: String, handler: MessageHandler) {
        ioScope.launch {
            llamaAndroid.send(message).collect {
                handler.h(it)
            }
        }
    }
}
