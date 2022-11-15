package com.chekurda.common.base_fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Базовая реализация презентера [BasePresenter].
 */
abstract class BasePresenterImpl<VIEW> : BasePresenter<VIEW>, LifecycleObserver {

    protected var view: VIEW? = null

    override fun attachView(view: VIEW) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun viewIsStarted() = Unit
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun viewIsResumed() = Unit
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun viewIsPaused() = Unit
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun viewIsStopped() = Unit

    override fun onDestroy() = Unit
}