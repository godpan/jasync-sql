package com.github.jasync.sql.db.pool

import com.github.jasync.sql.db.util.ExecutorServiceUtils
import com.github.jasync.sql.db.util.NettyUtils
import com.github.jasync.sql.db.util.isCompleted
import com.github.jasync.sql.db.util.success
import org.junit.Test
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for TimeoutScheduler
 */
class TimeoutSchedulerSpec {

    val TIMEOUT_DID_NOT_PASS = "timeout did not pass"

    private fun createTimeoutScheduler() = TimeoutSchedulerImpl(ExecutorServiceUtils.CommonPool, NettyUtils.DefaultEventLoopGroup, {})

    @Test
    fun `test timeout did not pass`() {
        val timeoutScheduler = createTimeoutScheduler()
        val promise = CompletableFuture<String>()
        val scheduledFuture = timeoutScheduler.addTimeout(promise, Duration.ofMillis(1000))
        Thread.sleep(100)
        assertFalse(promise.isCompleted)
        promise.success(TIMEOUT_DID_NOT_PASS)
        Thread.sleep(1500)
        assertEquals(TIMEOUT_DID_NOT_PASS, promise.get())
        assertTrue(scheduledFuture!!.isCancelled)
        assertEquals(false, timeoutScheduler.isTimeout())
    }

    @Test()
    fun `test timeout passed`() {
        val timeoutMillis: Long = 100
        val promise = CompletableFuture<String>()
        val timeoutScheduler = createTimeoutScheduler()
        val scheduledFuture = timeoutScheduler.addTimeout(promise, Duration.ofMillis(timeoutMillis))
        Thread.sleep(1000)
        assertTrue(promise.isCompleted)
        assertFalse(scheduledFuture!!.isCancelled)
        promise.success(TIMEOUT_DID_NOT_PASS)
        assertEquals(true, timeoutScheduler.isTimeout())
        try {
            promise.get()
        } catch (e: ExecutionException) {
            assertNotNull(e.cause)
            assertTrue(e.cause is TimeoutException)
        }
    }

    @Test
    fun `test no timeout`() {
        val timeoutScheduler = createTimeoutScheduler()
        val promise = CompletableFuture<String>()
        val scheduledFuture = timeoutScheduler.addTimeout(promise, null)
        Thread.sleep(1000)

        assertNull(scheduledFuture)
        assertFalse(promise.isCompleted)
        promise.success(TIMEOUT_DID_NOT_PASS)
        assertEquals(TIMEOUT_DID_NOT_PASS, promise.get())
        assertEquals(false, timeoutScheduler.isTimeout())
    }
}
