package com.thomascook.instructorpricecomparison

import com.thomascook.core.CoreUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 * Wrapper for managing disposable is less verbose manner. Use provided constructor to specify
 * [CompositeDisposable] to be used for managing internal disposable
 */
class DisposableWrapper(private val composite: CompositeDisposable) {

    //Returns true if the disposable is still pending
    val isNotDisposed: Boolean
        get() {
            val tmpDisposable = disposable
            return (tmpDisposable !== null && !tmpDisposable.isDisposed)
        }

    //Returns true if disposable is not used
    val isDisposed: Boolean
        get() {
            val tmpDisposable = disposable
            return (tmpDisposable === null || tmpDisposable.isDisposed)
        }

    var disposable: Disposable? = null
        set(value) {
            if (value == null) {
                val oldValue = field
                field = null
                if (oldValue !== null) {
                    if (!oldValue.isDisposed)
                        oldValue.dispose()

                    //Remove disposable from the queue
                    composite.remove(oldValue)
                }
            } else {
                //Add disposable to the queue in the composite disposables
                composite.add(value)
                field = value
            }
        }
}

inline fun <reified T : Any> getTypedListener(args: List<Any?>): T? =
    CoreUtils.getType(CoreUtils.ListenerProvider::class, args)?.getListenerForType(T::class)