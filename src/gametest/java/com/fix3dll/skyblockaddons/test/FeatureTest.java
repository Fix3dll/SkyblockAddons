package com.fix3dll.skyblockaddons.test;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.util.StringUtil;

import java.util.HashSet;

/**
 * Integration tests for {@link Feature}.
 */
@SuppressWarnings("UnstableApiUsage")
public class FeatureTest extends SkyblockAddonsTestSuite {

    @Override
    protected void execute() {
        verifyUniqueFeatureIds();
        verifyFeatureMessages();
    }

    /**
     * Validates that no two non-dummy features share the same ID.
     * Features with ID {@code -1} are considered dummy entries and are excluded
     * from uniqueness checks to prevent false duplicate failures.
     */
    private void verifyUniqueFeatureIds() {
        HashSet<Integer> seen = new HashSet<>();

        for (Feature feature : Feature.values()) {
            int id = feature.getId();

            if (id != -1 && !seen.add(id)) {
                fail("Duplicate feature ID %d found on: %s".formatted(id, feature.name()));
            }
        }
    }

    /**
     * Verifies that every {@link Feature} constant resolves to a non-blank translation string.
     * {@link Feature#getMessage} is guaranteed to return a non-null value, so only blank
     * strings are checked.
     */
    private void verifyFeatureMessages() {
        for (Feature feature : Feature.values()) {
            try {
                assertFalse(
                        StringUtil.isBlank(feature.getMessage()),
                        "Feature '%s' returned a blank translation message.".formatted(feature.name())
                );
            } catch (Exception e) {
                fail("Feature '%s' threw an exception during getMessage(): %s"
                        .formatted(feature.name(), e.getMessage()));
            }
        }
    }
}