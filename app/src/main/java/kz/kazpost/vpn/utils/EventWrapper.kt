package kz.kazpost.vpn.utils

import androidx.lifecycle.Observer

open class EventWrapper<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun get(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peek(): T = content
}

/**
 * Can be used for no-data events
 */
object VoidEvent {
    val WRAPPED: EventWrapper<VoidEvent>
        get() = EventWrapper(VoidEvent)
}

/**
 * An [Observer] for [EventWrapper]s, simplifying the pattern of checking if the [EventWrapper]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [EventWrapper]'s contents has not been handled.
 */
class EventObserver<T>(
    private val onEventUnhandledContent: (T) -> Unit
) : Observer<EventWrapper<T>> {
    override fun onChanged(event: EventWrapper<T>?) {
        event?.get()?.let(onEventUnhandledContent)
    }
}

fun <T : Any> T.wrapEvent() = EventWrapper(this)