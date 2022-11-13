package com.chekurda.common.plugin_struct

/**
 * Предназначен для формирования перечня обязательных и опциональных зависимостей модуля.
 */
class Dependency private constructor(
    private val requiredSingle: Map<Class<out Feature>, (FeatureProvider<out Feature>) -> Unit>,
    private val optionalSingle: Map<Class<out Feature>, (FeatureProvider<out Feature>?) -> Unit>,
    private val requiredMulti: Map<Class<out Feature>, (Set<FeatureProvider<out Feature>>) -> Unit>,
    private val optionalMulti: Map<Class<out Feature>, (Set<FeatureProvider<out Feature>>?) -> Unit>
) {

    companion object {

        /**
         * Пустой набор зависимостей
         */
        val EMPTY = Dependency(emptyMap(), emptyMap(), emptyMap(), emptyMap())
    }

    @Suppress("UNCHECKED_CAST")
    class Builder {
        private val requiredSingle = mutableMapOf<Class<out Feature>, (FeatureProvider<out Feature>) -> Unit>()
        private val optionalSingle = mutableMapOf<Class<out Feature>, (FeatureProvider<out Feature>?) -> Unit>()
        private val requiredMulti = mutableMapOf<Class<out Feature>, (Set<FeatureProvider<out Feature>>) -> Unit>()
        private val optionalMulti = mutableMapOf<Class<out Feature>, (Set<FeatureProvider<out Feature>>?) -> Unit>()

        fun <F : Feature> require(type: Class<F>, inject: (FeatureProvider<F>) -> Unit): Builder {
            requiredSingle[type] = inject as (FeatureProvider<out Feature>) -> Unit
            return this
        }

        fun <F : Feature> optional(type: Class<F>, inject: (FeatureProvider<F>?) -> Unit): Builder {
            optionalSingle[type] = inject as (FeatureProvider<out Feature>?) -> Unit
            return this
        }

        fun <F : Feature> requireSet(type: Class<F>, inject: (Set<FeatureProvider<F>>) -> Unit): Builder {
            requiredMulti[type] = inject as (Set<FeatureProvider<out Feature>>) -> Unit
            return this
        }

        fun <F : Feature> optionalSet(type: Class<F>, inject: (Set<FeatureProvider<F>>?) -> Unit): Builder {
            optionalMulti[type] = inject as (Set<FeatureProvider<out Feature>>?) -> Unit
            return this
        }

        fun build(): Dependency {
            return Dependency(requiredSingle, optionalSingle, requiredMulti, optionalMulti)
        }

    }

    fun FeatureRegistry.requiredSingle(): Map<Class<out Feature>, (FeatureProvider<out Feature>) -> Unit> = requiredSingle

    fun FeatureRegistry.optionalSingle(): Map<Class<out Feature>, (FeatureProvider<out Feature>?) -> Unit> = optionalSingle

    fun FeatureRegistry.requiredMulti(): Map<Class<out Feature>, (Set<FeatureProvider<out Feature>>) -> Unit> = requiredMulti

    fun FeatureRegistry.optionalMulti(): Map<Class<out Feature>, (Set<FeatureProvider<out Feature>>?) -> Unit> = optionalMulti

}