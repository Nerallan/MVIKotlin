package com.arkivanov.mvikotlin.extensions.coroutines

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.utils.assertOnMainThread
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A builder function for the [Binder]
 *
 * @param mainContext a main CoroutineContext, the default value is Dispatchers.Main
 * @param builder the DSL block function
 *
 * @return a new instance of the [Binder]
 */
fun bind(mainContext: CoroutineContext = Dispatchers.Main, builder: BindingsBuilder.() -> Unit): Binder =
    BuilderBinder(mainContext = mainContext)
        .also(builder)

interface BindingsBuilder {

    /**
     * Creates a binding between this [Flow] and the provided `consumer`
     *
     * @receiver a stream of values
     * @param consumer a `consumer` of values
     */
    infix fun <T> Flow<T>.bindTo(consumer: suspend (T) -> Unit)

    /**
     * Creates a binding between this [Flow] and the provided [ViewRenderer]
     *
     * @receiver a stream of the `View Models`
     * @param viewRenderer a [ViewRenderer] that will consume the `View Models`
     */
    infix fun <Model : Any> Flow<Model>.bindTo(viewRenderer: ViewRenderer<Model>)

    /**
     * Creates a binding between this [Flow] and the provided [Store]
     *
     * @receiver a stream of the [Store] `States`
     * @param store a [Store] that will consume the `Intents`
     */
    infix fun <Intent : Any> Flow<Intent>.bindTo(store: Store<Intent, *, *>)
}

private class BuilderBinder(
    private val mainContext: CoroutineContext
) : BindingsBuilder, Binder {
    private val bindings = ArrayList<Binding<*>>()
    private var job: Job? = null

    override fun <T> Flow<T>.bindTo(consumer: suspend (T) -> Unit) {
        bindings += Binding(this, consumer)
    }

    override fun <Model : Any> Flow<Model>.bindTo(viewRenderer: ViewRenderer<Model>) {
        this bindTo {
            assertOnMainThread()
            viewRenderer.render(it)
        }
    }

    override fun <T : Any> Flow<T>.bindTo(store: Store<T, *, *>) {
        this bindTo { store.accept(it) }
    }

    override fun start() {
        job =
            GlobalScope.launch(mainContext) {
                bindings.forEach { binding ->
                    launch { start(binding) }
                }
            }
    }

    private suspend fun <T> start(binding: Binding<T>) {
        binding.source.collect {
            binding.consumer(it)
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}

private class Binding<T>(
    val source: Flow<T>,
    val consumer: suspend (T) -> Unit
)
