package com.chekurda.common.plugin_manager

import android.annotation.SuppressLint
import android.app.Application
import com.chekurda.common.plugin_manager.resolver.FeatureResolver
import com.chekurda.common.plugin_manager.resolver.SimpleFeatureResolver
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureProvider
import com.chekurda.common.plugin_struct.FeatureRegistry
import com.chekurda.common.plugin_struct.Plugin
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Управление плагинами: регистрация, инициализация, поставка и проверка зависимостей.
 *
 * @param resolver разруливатель фич
 * @param underTest использовать только при юнит-тестировании. Позволяет проигнорировать ошибки контроллера, так как нельзя проинициализировать платформу на локальной JVM.
 * @param detectSlowInitProcess флаг активирует анализ схождения процесса ицициализации. Если сходимость процесса медленная, то это скажется на времени старта приложения.
 * @param doAfterInitializeOnBackground флаг активирует выполнение метода 'doAfterInitialize(...)' в фоновом потоке (применяется для всех плагинов).
 */
class PluginManager(
    private val resolver: FeatureResolver = SimpleFeatureResolver(),
    private val underTest: Boolean = false,
    private val detectSlowInitProcess: Boolean = false,
    private val doAfterInitializeOnBackground: Boolean = false
) : FeatureRegistry {

    data class Record<F : Feature>(val supplier: Plugin<*>, val feature: FeatureProvider<F>)

    /**
     * Перехватчик действий с плагином
     */
    fun interface Interceptor {
        fun intercept(plugin: Plugin<*>, action: (Plugin<*>) -> Unit)
    }

    private val plugins: MutableSet<Plugin<*>> = linkedSetOf()
    private val features = mutableMapOf<Class<out Feature>, MutableSet<Record<out Feature>>>()

    fun registerPlugin(plugin: Plugin<*>) {
        plugins += plugin
    }

    fun registerPlugins(vararg plugs: Plugin<*>) {
        plugins.addAll(plugs)
    }

    fun configure(application: Application) {
        if (plugins.isEmpty()) return

        plugins.forEach { plugin ->
            collectApi(application, plugin)
        }

        plugins.forEach { plugin ->
            resolveDependency(plugin)
        }

        doInit(plugins)

        doAfterInit(plugins)
    }

    private fun collectApi(application: Application, plugin: Plugin<*>) {
        with(plugin) { setApplication(application) }

        plugin.api.forEach {
            features.getOrPut(it.type) { mutableSetOf() }.add(Record(plugin, it.provider))
        }
    }

    private fun resolveDependency(plugin: Plugin<*>) {
        plugin.dependency.apply {
            requiredSingle().forEach { (key, inject) ->
                val availableFeatures = features[key]
                if (availableFeatures.isNullOrEmpty()) {
                    throw RequiredDependencyMissingException(key, plugin)
                }
                resolver.resolveRequiredSingle(key, plugin, availableFeatures).let(inject)
            }

            requiredMulti().forEach { (key, inject) ->
                val availableFeatures = features[key]
                if (availableFeatures.isNullOrEmpty()) {
                    throw RequiredDependencySetMissingException(key, plugin)
                }
                resolver.resolveRequiredMulti(key, plugin, availableFeatures).let(inject)
            }

            optionalSingle().forEach { (key, inject) ->
                val availableFeatures = features[key]
                if (!availableFeatures.isNullOrEmpty()) {
                    resolver.resolveOptionalSingle(key, plugin, availableFeatures)?.let(inject)
                }
            }

            optionalMulti().forEach { (key, inject) ->
                val availableFeatures = features[key]
                if (!availableFeatures.isNullOrEmpty()) {
                    resolver.resolveOptionalMulti(key, plugin, availableFeatures)?.let(inject)
                }
            }
        }
    }

    private fun doInit(plugins: Set<Plugin<*>>) {
        var pending = LinkedList(plugins)
        val initErrors = linkedMapOf<Plugin<*>, Throwable>()
        do {
            val nonInitialized = pending
            val initialSize = nonInitialized.size
            pending = LinkedList()

            do {
                val plug = nonInitialized.pop()
                try {
                    plug.initialize()
                } catch (e: Throwable) {
                    initErrors[plug] = e
                    pending.add(plug)
                }
            } while (nonInitialized.isNotEmpty())

            if (initialSize == pending.size) {
                val errorMessage = StringBuilder().apply {
                    append("Не удалось проинициализировать плагинную систему из-за следующих ошибок:")
                    initErrors.onEachIndexed { index, (plug, error)  ->
                        append("\n${index}. ${plug::class.java.canonicalName}: ${error.message}\n${error.stackTraceToString()}")
                    }
                }.toString()
                throw InfiniteInitializationException(errorMessage)
            } else if(detectSlowInitProcess && pending.size > initialSize/2) {
                val errorMessage = StringBuilder().apply {
                    append("Из-за несвоевременного использования зависимостей, поставляемых чужими плагинами, сходимость процесса инициализации очень медленная.")
                    initErrors.onEachIndexed { index, (plug, error)  ->
                        append("\n${index}. ${plug::class.java.canonicalName}: ${error.message}\n${error.stackTraceToString()}")
                    }
                }.toString()
                throw BadInitializationPerformanceException(errorMessage)
            } else {
                initErrors.clear()
            }
        } while (pending.isNotEmpty())
    }

    @SuppressLint("CheckResult")
    private fun doAfterInit(plugins: Set<Plugin<*>>) {
        val afterInitAction = {
            plugins.forEach { plug -> doAfterInit(plug) }
        }
        if (!doAfterInitializeOnBackground) {
            afterInitAction.invoke()
        } else {
            val executorThread = Thread.currentThread()
            val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Single
                .fromCallable(afterInitAction)
                .subscribeOn(Schedulers.single())
                .subscribe({}, { error ->
                    exceptionHandler?.uncaughtException(executorThread, error) ?: throw error
                })
        }
    }

    private fun doAfterInit(plugin: Plugin<*>) {
        try {
            plugin.doAfterInitialize()
        } catch (error: Throwable) {
            if (underTest) {
                when(error) {
                    is UnsatisfiedLinkError,
                    is NoClassDefFoundError -> Unit
                    else -> throw error
                }
            } else {
                throw error
            }
        }
    }

    internal class RequiredDependencyMissingException(
        featureType: Class<out Feature>,
        plugin: Plugin<*>
    ) : RuntimeException("Отсутствует обязательная зависимость ${featureType.canonicalName} для плагина ${plugin::class.java.canonicalName}. Не найдено ни одного поставщика данной фичи.")

    internal class RequiredDependencySetMissingException(
        featureType: Class<out Feature>,
        plugin: Plugin<*>
    ) : RuntimeException("Отсутствует обязательная зависимость Set<${featureType.canonicalName}> для плагина ${plugin::class.java.canonicalName}. Не найдено ни одного поставщика данной фичи.")

    internal class InfiniteInitializationException(
        message: String
    ) : RuntimeException(message)

    internal class BadInitializationPerformanceException(
        message: String
    ) : RuntimeException(message)
}