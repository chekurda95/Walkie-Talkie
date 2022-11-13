package com.chekurda.common.base_fragment

/**
 * Базовая реализация презентера [BasePresenter].
 */
abstract class BasePresenterImpl<VIEW> : BasePresenter<VIEW> {

    protected var view: VIEW? = null

    override fun attachView(view: VIEW) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onDestroy() = Unit
}