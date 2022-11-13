package com.chekurda.common.base_fragment.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chekurda.common.base_fragment.BasePresenterFragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Реализация делегата вью-модели для фрагмента.
 */
internal class ArchDelegate<VIEW_MODEL>(
    private val fragment: Fragment,
    private var creatingMethod: (() -> VIEW_MODEL)?,
    private val doOnCleared: (VIEW_MODEL) -> Unit = {}
) : ReadOnlyProperty<LifecycleOwner, VIEW_MODEL> {

    private val value: VIEW_MODEL by lazy(::getViewModel)

    override operator fun getValue(
        thisRef: LifecycleOwner,
        property: KProperty<*>
    ): VIEW_MODEL =
        value

    private fun getViewModel(): VIEW_MODEL {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val instance = creatingMethod!!.invoke()
                creatingMethod = null
                @Suppress("UNCHECKED_CAST")
                return (PresenterHolder(instance, doOnCleared) as T)
            }
        }
        val holder = getHolder(ViewModelProvider(fragment, factory))
        @Suppress("UNCHECKED_CAST")
        return holder.presenter as VIEW_MODEL
    }

    private fun getHolder(provider: ViewModelProvider) =
        provider.get(this::class.java.canonicalName!!, PresenterHolder::class.java)
}

/**
 * Вспомогательный класс для работы по сохранению презентера
 * @property presenter презентер
 * @see [BasePresenterFragment]
 */
private class PresenterHolder<PRESENTER>(
    var presenter: PRESENTER?,
    private val doOnCleared: (PRESENTER) -> Unit = {}
) : ViewModel() {

    override fun onCleared() {
        doOnCleared.invoke(presenter!!)
        presenter = null
    }
}