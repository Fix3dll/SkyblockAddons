package com.fix3dll.skyblockaddons.test;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all SkyblockAddons test suites.
 *
 * <p>Subclasses implement {@link #execute()} and record failures via the provided assertion
 * helpers. Failures are accumulated rather than thrown immediately, allowing all checks
 * within a suite to run before results are returned to the orchestrator.
 *
 * <p>{@link net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl#runOnClient}
 * is synchronous — the test thread blocks until the client thread completes the task.
 * As a result, {@link #fail} is always called from a single thread at a time, making
 * the internal error list safe to use without synchronization.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class SkyblockAddonsTestSuite {

    private final ArrayList<String> errors = new ArrayList<>();
    private ClientGameTestContext context;

    /**
     * Runs this suite and returns an unmodifiable view of all recorded failures.
     */
    public final List<String> run(ClientGameTestContext context) {
        this.context = context;
        execute();

        if (errors.isEmpty()) {
            System.out.println(getClass().getSimpleName() + " completed without errors.");
        }

        return Collections.unmodifiableList(errors);
    }

    /**
     * Entry point for subclass test logic. Called once per {@link #run} invocation.
     */
    protected abstract void execute();

    /**
     * Returns the client game test context, available for subclasses that need to
     * dispatch work to the client thread via {@code context().runOnClient(...)}.
     */
    protected ClientGameTestContext context() {
        return context;
    }

    /**
     * Records an unconditional failure with the given message.
     */
    protected void fail(String message) {
        errors.add("[" + getClass().getSimpleName() + "] " + message);
    }

    /**
     * Fails if {@code condition} is {@code false}.
     */
    protected void assertTrue(boolean condition, String message) {
        if (!condition) fail(message);
    }

    /**
     * Fails if {@code condition} is {@code true}.
     */
    protected void assertFalse(boolean condition, String message) {
        if (condition) fail(message);
    }

    /**
     * Fails if {@code value} is not {@code null}.
     */
    protected void assertNull(Object value, String message) {
        if (value != null) fail(message);
    }

    /**
     * Fails if {@code value} is {@code null}.
     */
    protected void assertNotNull(Object value, String message) {
        if (value == null) fail(message);
    }

}