package com.arkivanov.mvikotlin.core.view

import com.arkivanov.mvikotlin.core.annotations.MainThread
import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.rx.Observer

/**
 * Represents a source of the `View Events`
 *
 * @see View
 */
interface ViewEvents<out Event : Any> {

    /**
     * Subscribes the provided [Observer] of `View Events`.
     * Emissions are performed on the main thread.
     *
     * @param observer an [Observer] that will receive the `View Events`
     */
    @MainThread
    fun events(observer: Observer<Event>): Disposable
}
