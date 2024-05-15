package com.frontegg.flutter.stateListener

import android.util.Log
import androidx.lifecycle.Lifecycle
import com.frontegg.android.FronteggAuth
import com.frontegg.flutter.ActivityPluginBindingGetter
import com.frontegg.flutter.toReadableMap
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FronteggStateListenerImpl(
    val activityPluginBindingGetter: ActivityPluginBindingGetter
) : FronteggStateListener {
    private var disposable: Disposable? = null
    private var state: FronteggState? = null

    private var eventSink: EventChannel.EventSink? = null

    override fun dispose() {
        disposable?.dispose()
    }

    override fun setEventSink(eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
        if (this.disposable != null) {
            notifyChanges()
        }
    }

    override fun subscribe(result: MethodChannel.Result) {
        this.disposable?.dispose()
        this.disposable = null

        this.disposable = Observable.mergeArray(
            FronteggAuth.instance.accessToken.observable,
            FronteggAuth.instance.refreshToken.observable,
            FronteggAuth.instance.user.observable,
            FronteggAuth.instance.isAuthenticated.observable,
            FronteggAuth.instance.isLoading.observable,
            FronteggAuth.instance.initializing.observable,
            FronteggAuth.instance.showLoader.observable,
        ).subscribe {
            notifyChanges()
        }
        notifyChanges()
        result.success(null)
    }


    private fun notifyChanges() {
        val lifecycle = activityPluginBindingGetter.getActivityPluginBinding()?.lifecycle
        if (lifecycle != null && lifecycle is HiddenLifecycleReference && lifecycle.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return
        }

        state = FronteggState(
            accessToken = FronteggAuth.instance.accessToken.value,
            refreshToken = FronteggAuth.instance.refreshToken.value,
            user = FronteggAuth.instance.user.value?.toReadableMap(),
            isAuthenticated = FronteggAuth.instance.isAuthenticated.value,
            isLoading = FronteggAuth.instance.isLoading.value,
            initializing = FronteggAuth.instance.initializing.value,
            showLoader = FronteggAuth.instance.showLoader.value,
        )

        state?.let {
            sendState(it)
        }
    }

    private fun sendState(state: FronteggState) {
        GlobalScope.launch(Dispatchers.Main) {
            eventSink?.success(state.toMap())
        }
    }
}