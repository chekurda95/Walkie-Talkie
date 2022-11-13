package com.chekurda.common.plugin_manager

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chekurda.common.plugin_manager.fake.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.chekurda.common.plugin_manager.resolver.FeatureResolver
import com.chekurda.common.plugin_manager.resolver.FlexibleFeatureResolver

/**
 * @author kv.martyshenko
 */
@RunWith(AndroidJUnit4::class)
@Config(manifest=Config.NONE)
class PluginManagerTest {
    private var _app: Application? = null
    private val app: Application
        get() = requireNotNull(_app)


    @Before
    fun setup() {
        _app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun reset() {
        _app = null
    }

    @Test
    fun `Test configuration is successful, when NO plugins registered`() {
        PluginManagerScenario.builder()
            .prepare { }
            .test { configure(app) }
            .check { Assert.assertTrue(true) }
            .run()
    }

    @Test
    fun `Test configuration is failed, when single required dependency NOT found`() {
        val plugin = Plugin0()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(PluginManager.RequiredDependencyMissingException::class.java) { testBlock() }
                Assert.assertThrows(UninitializedPropertyAccessException::class.java) { plugin.feature1Provider }
            }
            .run()

    }

    @Test
    fun `Test configuration is successful, when single required dependency found`() {
        val plugin0 = Plugin0()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin0)
                registerPlugin(Plugin1())
            }
            .test { configure(app) }
            .check { Assert.assertNotNull(plugin0.feature1Provider) }
            .run()
    }

    @Test
    fun `Test configuration is successful, when single optional dependency NOT found`() {
        val plugin1 = Plugin1()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin1)
            }
            .test { configure(app) }
            .check { Assert.assertNull(plugin1.feature2Provider) }
            .run()
    }

    @Test
    fun `Test configuration is successful, when single optional dependency found`() {
        val plugin1 = Plugin1()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin1)
                registerPlugin(Plugin2())
            }
            .test { configure(app) }
            .check { Assert.assertNotNull(plugin1.feature2Provider) }
            .run()
    }

    @Test
    fun `Test configuration is failed, when required dependency set NOT found`() {
        val plugin = Plugin4()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(PluginManager.RequiredDependencySetMissingException::class.java) { testBlock() }
                Assert.assertThrows(UninitializedPropertyAccessException::class.java) { plugin.feature1Provider }
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when required dependency set found (single provider)`() {
        val plugin = Plugin4()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(Plugin1())
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .check {
                Assert.assertTrue(plugin.feature1Provider.size == 1)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when required dependency set found (multi providers)`() {
        val plugin = Plugin4()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(Plugin1())
                registerPlugin(Plugin3())
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .check {
                Assert.assertTrue(plugin.feature1Provider.size == 3)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when optional dependency set NOT found`() {
        val plugin = Plugin5()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .check {
                Assert.assertNull(plugin.feature1Provider)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when optional dependency set found (single provider)`() {
        val plugin = Plugin5()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin)
                registerPlugin(Plugin1())
            }
            .test { configure(app) }
            .check {
                Assert.assertTrue(plugin.feature1Provider?.size == 1)
            }
            .run()
    }

    @Test
    fun `Test configuration is success, when optional dependency set found (multi providers)`() {
        val plugin = Plugin5()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(Plugin1())
                registerPlugin(Plugin3())
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .check {
                Assert.assertTrue(plugin.feature1Provider?.size == 3)
            }
            .run()
    }

    @Test
    fun `Test configuration is failed, when required single dependency NOT resolved (multi providers)`() {
        val plugin = Plugin0()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(Plugin1())
                registerPlugin(Plugin3())
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(FeatureResolver.SingleDependencyNotResolvedException::class.java) { testBlock() }
                Assert.assertThrows(UninitializedPropertyAccessException::class.java) { plugin.feature1Provider }
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when required single dependency resolved (multi providers)`() {
        val plugin0 = Plugin0()
        val plugin1 = Plugin1()

        val featureResolver = FlexibleFeatureResolver(onResolveRequiredSingle = { featureType, caller, records, fallback ->
            if(featureType == TestFeature1::class.java && caller == plugin0) {
                records.first { it.supplier == plugin1 }.feature
            } else {
                fallback(featureType, caller, records)
            }
        })

        val pluginManager = PluginManager(featureResolver)

        PluginManagerScenario.builder(pluginManager)
            .prepare {
                registerPlugin(plugin1)
                registerPlugin(Plugin3())
                registerPlugin(plugin0)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin0.feature1Provider)
                Assert.assertEquals(plugin0.feature1Provider.get(), plugin1.testFeature1)
            }
            .run()
    }

    @Test
    fun `Test configuration is failed, when optional single dependency NOT resolved (multi providers)`() {
        val plugin = Plugin1()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(Plugin2())
                registerPlugin(Plugin3())
                registerPlugin(plugin)
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(FeatureResolver.SingleDependencyNotResolvedException::class.java) { testBlock() }
                Assert.assertNull(plugin.feature2Provider)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when optional single dependency resolved (multi providers)`() {
        val plugin1 = Plugin1()
        val plugin2 = Plugin2()

        val featureResolver = FlexibleFeatureResolver(onResolveOptionalSingle = { featureType, caller, records, fallback ->
            if(featureType == TestFeature2::class.java && caller == plugin1) {
                records.first { it.supplier == plugin2 }.feature
            } else {
                fallback(featureType, caller, records)
            }
        })

        val pluginManager = PluginManager(featureResolver)

        PluginManagerScenario.builder(pluginManager)
            .prepare {
                registerPlugin(plugin2)
                registerPlugin(Plugin3())
                registerPlugin(plugin1)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin1.feature2Provider)
                Assert.assertEquals(plugin1.feature2Provider?.get(), plugin2.testFeature2)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when optional single dependency resolved - SKIPPED (multi providers)`() {
        val plugin1 = Plugin1()
        val plugin2 = Plugin2()

        val featureResolver = FlexibleFeatureResolver(onResolveOptionalSingle = { featureType, caller, records, fallback ->
            if(featureType == TestFeature2::class.java && caller == plugin1) {
                null
            } else {
                fallback(featureType, caller, records)
            }
        })

        val pluginManager = PluginManager(featureResolver)

        PluginManagerScenario.builder(pluginManager)
            .prepare {
                registerPlugin(plugin2)
                registerPlugin(Plugin3())
                registerPlugin(plugin1)
            }
            .test { configure(app) }
            .check {
                Assert.assertNull(plugin1.feature2Provider)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when required multi dependency resolved (providers filtered)`() {
        val plugin0 = Plugin4()
        val plugin1 = Plugin1()

        val featureResolver = FlexibleFeatureResolver(onResolveRequiredMulti = { featureType, caller, records, fallback ->
            if(featureType == TestFeature1::class.java && caller == plugin0) {
                records.filter { it.supplier == plugin1 }.map { it.feature }.toSet()
            } else {
                fallback(featureType, caller, records)
            }
        })

        val pluginManager = PluginManager(featureResolver)

        PluginManagerScenario.builder(pluginManager)
            .prepare {
                registerPlugin(plugin1)
                registerPlugin(Plugin3())
                registerPlugin(plugin0)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin0.feature1Provider)
                Assert.assertTrue(plugin0.feature1Provider.size == 1)
                Assert.assertEquals(plugin0.feature1Provider.first().get(), plugin1.testFeature1)
            }
            .run()
    }

    @Test
    fun `Test configuration is successful, when optional multi dependency resolved (providers filtered)`() {
        val plugin1 = Plugin5()
        val plugin2 = Plugin1()

        val featureResolver = FlexibleFeatureResolver(onResolveOptionalMulti = { featureType, caller, records, fallback ->
            if(featureType == TestFeature1::class.java && caller == plugin1) {
                records.filter { it.supplier == plugin2 }.map { it.feature }.toSet()
            } else {
                fallback(featureType, caller, records)
            }
        })

        val pluginManager = PluginManager(featureResolver)

        PluginManagerScenario.builder(pluginManager)
            .prepare {
                registerPlugin(plugin2)
                registerPlugin(Plugin3())
                registerPlugin(plugin1)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin1.feature1Provider)
                Assert.assertTrue(plugin1.feature1Provider?.size == 1)
                Assert.assertEquals(plugin1.feature1Provider?.first()?.get(), plugin2.testFeature1)
            }
            .run()
    }

    @Test
    fun `Test initialization is failed, when hard circular dependency found`() {
        val plugin1 = Plugin6()
        val plugin2 = Plugin7()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin1)
                registerPlugin(plugin2)
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(PluginManager.InfiniteInitializationException::class.java) { testBlock() }
                Assert.assertNotNull(plugin1.testFeature2)
                Assert.assertNotNull(plugin2.testFeature1)
            }
            .run()
    }

    @Test
    fun `Test initialization is successful, when wrong initialization order`() {
        val plugin1 = Plugin6()
        val plugin2 = Plugin8()
        PluginManagerScenario.builder()
            .prepare {
                registerPlugin(plugin1)
                registerPlugin(plugin2)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin1.testFeature2)
                Assert.assertEquals(plugin1.testFeature2.get(), plugin2.testFeature2)
            }
            .run()
    }

    @Test
    fun `Test initialization is successful, when monitor init problems and wrong initialization order NOT found (even)`() {
        val plugin6 = Plugin6()
        val plugin8 = Plugin8()
        val plugin9 = Plugin9()
        val plugin10 = Plugin10()
        PluginManagerScenario.builder(PluginManager(detectSlowInitProcess = true))
            .prepare {
                registerPlugin(plugin10)
                registerPlugin(plugin9)
                registerPlugin(plugin8)
                registerPlugin(plugin6)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin8.testFeature2)
                Assert.assertNotNull(plugin6.testFeature2)
                Assert.assertEquals(plugin6.testFeature2.get(), plugin8.testFeature2)
                Assert.assertNotNull(plugin9.testFeature1)
                Assert.assertEquals(plugin9.testFeature1.get(), plugin6.testFeature1)
                Assert.assertNotNull(plugin10.testFeature3)
                Assert.assertEquals(plugin10.testFeature3.get(), plugin9.testFeature3)
            }
            .run()
    }

    @Test
    fun `Test initialization is successful, when monitor init problems and wrong initialization order NOT found (odd)`() {
        val plugin6 = Plugin6()
        val plugin8 = Plugin8()
        val plugin9 = Plugin9()
        val plugin10 = Plugin10()
        val plugin11 = Plugin11()
        PluginManagerScenario.builder(PluginManager(detectSlowInitProcess = true))
            .prepare {
                registerPlugin(plugin11)
                registerPlugin(plugin10)
                registerPlugin(plugin8)
                registerPlugin(plugin6)
                registerPlugin(plugin9)
            }
            .test { configure(app) }
            .check {
                Assert.assertNotNull(plugin8.testFeature2)
                Assert.assertNotNull(plugin6.testFeature2)
                Assert.assertEquals(plugin6.testFeature2.get(), plugin8.testFeature2)
                Assert.assertNotNull(plugin9.testFeature1)
                Assert.assertEquals(plugin9.testFeature1.get(), plugin6.testFeature1)
                Assert.assertNotNull(plugin10.testFeature3)
                Assert.assertEquals(plugin10.testFeature3.get(), plugin9.testFeature3)
                Assert.assertNotNull(plugin11.testFeature4)
                Assert.assertEquals(plugin11.testFeature4.get(), plugin10.testFeature4)
            }
            .run()
    }

    @Test
    fun `Test initialization is failed, when monitor init problems and wrong initialization order detected`() {
        PluginManagerScenario.builder(PluginManager(detectSlowInitProcess = true))
            .prepare {
                registerPlugin(Plugin11())
                registerPlugin(Plugin10())
                registerPlugin(Plugin9())
                registerPlugin(Plugin6())
                registerPlugin(Plugin8())
            }
            .test { configure(app) }
            .checkDuringExecution { _, testBlock ->
                Assert.assertThrows(PluginManager.BadInitializationPerformanceException::class.java) { testBlock() }
            }
            .run()
    }
}