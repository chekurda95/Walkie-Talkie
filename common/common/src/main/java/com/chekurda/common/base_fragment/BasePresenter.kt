package com.chekurda.common.base_fragment

import androidx.lifecycle.LifecycleObserver

/**
 * Базовый интерфейс презентера для использования в [BasePresenterFragment].
 * @param VIEW тип вью, которая будет прикрепляться и открепляться от презентера.
 */
interface BasePresenter<VIEW> : LifecycleObserver {

    /**
     * Прикрепление вью к презентеру.
     * Происходит, когда View готова к выполнению команд от презентера на изменение своего состояния.
     * @param view VIEW
     */
    fun attachView(view: VIEW)

    /**
     * Открепление вью от презентера.
     * Происходит, когда View больше не может выполнять команды от презентера на изменение своего состояния.
     */
    fun detachView()

    /**
     * Уничтожение презентера.
     */
    fun onDestroy()
}