package com.chekurda.common.plugin_manager

class PluginManagerScenario private constructor(private val scenario: () -> Unit) {

    fun run() {
        scenario()
    }

    companion object {
        fun builder(pluginManager: PluginManager = PluginManager()): ScenarioBuilder {
            return Builder(pluginManager)
        }
    }

    interface ScenarioBuilder {
        fun prepare(block: PluginManager.() -> Unit): PreparedScenarioBuilder
    }

    interface PreparedScenarioBuilder {
        fun test(block: PluginManager.() -> Unit): TestedPreparedScenarioBuilder
    }

    interface TestedPreparedScenarioBuilder {
        fun check(block: PluginManager.() -> Unit): PluginManagerScenario
        fun checkDuringExecution(block: (pluginManager: PluginManager, testBlock: () -> Unit) -> Unit): PluginManagerScenario
    }

    private class Builder(private val pluginManager: PluginManager) : ScenarioBuilder,
        PreparedScenarioBuilder, TestedPreparedScenarioBuilder {
        private lateinit var prepareBlock: PluginManager.() -> Unit
        private lateinit var testBlock: PluginManager.() -> Unit

        override fun prepare(block: PluginManager.() -> Unit): PreparedScenarioBuilder {
            prepareBlock = block
            return this
        }

        override fun test(block: PluginManager.() -> Unit): TestedPreparedScenarioBuilder {
            testBlock = block
            return this
        }

        override fun check(block: PluginManager.() -> Unit): PluginManagerScenario {
            return PluginManagerScenario {
                pluginManager.prepareBlock()
                pluginManager.testBlock()
                pluginManager.block()
            }
        }

        override fun checkDuringExecution(block: (pluginManager: PluginManager, testBlock: () -> Unit) -> Unit): PluginManagerScenario {
            return PluginManagerScenario {
                pluginManager.prepareBlock()
                block(pluginManager) {
                    pluginManager.testBlock()
                }
            }
        }
    }

}