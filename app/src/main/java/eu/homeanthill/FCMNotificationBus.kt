package eu.homeanthill

import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide bus for foreground FCM messages.
 * FCMService emits here; AppNavGraph collects and shows a snackbar.
 * extraBufferCapacity = 8 so tryEmit never drops a message under normal load.
 */
object FCMNotificationBus {
    private val _messages = MutableSharedFlow<RemoteMessage>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun emit(message: RemoteMessage) {
        _messages.tryEmit(message)
    }
}
