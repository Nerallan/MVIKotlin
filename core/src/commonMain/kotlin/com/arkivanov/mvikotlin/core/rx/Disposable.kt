package com.arkivanov.mvikotlin.core.rx

/**
 * Represents a disposable resource
 */
interface Disposable {

    /**
     * Returns whether the instance is disposed or not
     */
    val isDisposed: Boolean

    /**
     * Disposes the instance
     */
    fun dispose()
}
