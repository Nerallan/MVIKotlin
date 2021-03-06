package com.arkivanov.mvikotlin.logging.store

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.StoreEventType
import com.arkivanov.mvikotlin.logging.logger.LoggerWrapper
import com.arkivanov.mvikotlin.logging.logger.log

internal class LoggingExecutor<in Intent : Any, in Action : Any, State : Any, Result : Any, Label : Any>(
    private val delegate: Executor<Intent, Action, State, Result, Label>,
    private val logger: LoggerWrapper,
    private val storeName: String
) : Executor<Intent, Action, State, Result, Label> by delegate {

    override fun init(callbacks: Executor.Callbacks<State, Result, Label>) {
        delegate.init(
            object : Executor.Callbacks<State, Result, Label> {
                override val state: State get() = callbacks.state

                override fun onResult(result: Result) {
                    logger.log(storeName = storeName, eventType = StoreEventType.RESULT, value = result)
                    callbacks.onResult(result)
                }

                override fun onLabel(label: Label) {
                    logger.log(storeName = storeName, eventType = StoreEventType.LABEL, value = label)
                    callbacks.onLabel(label)
                }
            }
        )
    }

    override fun handleAction(action: Action) {
        logger.log(storeName = storeName, eventType = StoreEventType.ACTION, value = action)
        delegate.handleAction(action)
    }

    override fun handleIntent(intent: Intent) {
        logger.log(storeName = storeName, eventType = StoreEventType.INTENT, value = intent)
        delegate.handleIntent(intent)
    }
}
