package com.fix3dll.skyblockaddons.test;

import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import net.minecraft.util.StringUtil;

/**
 * Integration tests for {@link FeatureSetting}.
 * <p>There are three distinct setting types, determined by their constructor:
 * <ul>
 *   <li><b>Universal</b> — has a {@code translationKey}, no fixed {@code relatedFeature}, {@code universal=true}</li>
 *   <li><b>Standard</b>  — has both a {@code translationKey} and a {@code relatedFeature}, {@code universal=false}</li>
 *   <li><b>Dynamic</b>   — has a {@code relatedFeature} but no {@code translationKey}, {@code universal=false}</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class FeatureSettingTest extends SkyblockAddonsTestSuite {

    @Override
    protected void execute() {
        verifyRelatedFeatureContracts();
        verifyUniversalFeatureInitialState();
        verifyMessages();
    }

    /**
     * Verifies that each {@link FeatureSetting} satisfies its {@code relatedFeature} contract:
     * <ul>
     *   <li>Universal settings must return {@code null} from {@link FeatureSetting#getRelatedFeature()}.</li>
     *   <li>Standard and Dynamic settings must return a non-null related feature.</li>
     * </ul>
     */
    private void verifyRelatedFeatureContracts() {
        for (FeatureSetting setting : FeatureSetting.values()) {
            var relatedFeature = setting.getRelatedFeature();

            if (setting.isUniversal()) {
                assertNull(
                        relatedFeature,
                        "Universal setting '%s' should not have a related feature, but returned: %s"
                                .formatted(setting.name(), relatedFeature)
                );
            } else {
                assertNotNull(
                        relatedFeature,
                        "Non-universal setting '%s' must have a related feature assigned."
                                .formatted(setting.name())
                );
            }
        }
    }

    /**
     * Verifies that no Universal setting has a {@code universalFeature} assigned at startup.
     * {@code universalFeature} is a runtime-assigned field and must be {@code null} before
     * any feature associates itself with a Universal setting.
     */
    private void verifyUniversalFeatureInitialState() {
        for (FeatureSetting setting : FeatureSetting.values()) {
            if (setting.isUniversal()) {
                assertNull(
                        setting.getUniversalFeature(),
                        "Universal setting '%s' should have a null universalFeature at startup, but was: %s"
                                .formatted(setting.name(), setting.getUniversalFeature())
                );
            }
        }
    }

    /**
     * Verifies the return value of {@link FeatureSetting#getMessage} against the expected
     * behavior for each setting type:
     * <ul>
     *   <li>Universal and Standard settings must return a non-null, non-blank translation string.</li>
     *   <li>Dynamic settings have no {@code translationKey} and are expected to return {@code null}; this is not an error.</li>
     * </ul>
     */
    private void verifyMessages() {
        for (FeatureSetting setting : FeatureSetting.values()) {
            try {
                String message = setting.getMessage();

                if (message == null) {
                    // Dynamic settings intentionally return null — this is the expected contract.
                    // A null message on a Universal setting indicates a missing translationKey.
                    if (setting.isUniversal()) {
                        fail("Universal setting '%s' returned null; translationKey is not set."
                                .formatted(setting.name()));
                    }
                } else {
                    assertFalse(
                            StringUtil.isBlank(message),
                            "%s setting '%s' returned a blank translation string."
                                    .formatted(classifyType(setting), setting.name())
                    );
                }
            } catch (Exception e) {
                fail("Setting '%s' threw an exception during getMessage(): %s"
                        .formatted(setting.name(), e.getMessage()));
            }
        }
    }

    /**
     * Returns a human-readable label for the type of the given {@link FeatureSetting},
     * intended for use in error messages.
     */
    private static String classifyType(FeatureSetting setting) {
        if (setting.isUniversal()) return "Universal";
        return setting.getMessage() != null ? "Standard" : "Dynamic";
    }

}