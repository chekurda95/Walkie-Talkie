package com.chekurda.common

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.SerialDisposable

fun Disposable.storeIn(disposer: CompositeDisposable) {
    disposer.add(this)
}

fun Disposable.storeIn(disposer: SerialDisposable) {
    disposer.set(this)
}