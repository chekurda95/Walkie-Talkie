package ru.tensor.sbis.common.testing

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Базовая реализация правила [TestRule] для установки планировщика [scheduler] в
 * - [RxJavaPlugins.setIoSchedulerHandler]
 * - [RxJavaPlugins.setComputationSchedulerHandler]
 * - [RxJavaPlugins.setNewThreadSchedulerHandler]
 * - [RxAndroidPlugins.setInitMainThreadSchedulerHandler]
 */
abstract class SchedulerRule<T : Scheduler> : TestRule {

    /**
     * Планировщик, который будет использоваться в рамках теста
     */
    protected abstract val scheduler: T

    override fun apply(base: Statement, d: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { scheduler }
                RxJavaPlugins.setComputationSchedulerHandler { scheduler }
                RxJavaPlugins.setNewThreadSchedulerHandler { scheduler }
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler }
                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}