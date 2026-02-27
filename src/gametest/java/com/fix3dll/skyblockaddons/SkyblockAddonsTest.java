package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.test.FeatureSettingTest;
import com.fix3dll.skyblockaddons.test.FeatureTest;
import com.fix3dll.skyblockaddons.test.SkyblockAddonsTestSuite;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.util.List;

/**
 * Master entry point for all SkyblockAddons client gametests.
 * This class orchestrates various test modules and reports all errors at once.
 */
@SuppressWarnings("UnstableApiUsage")
public class SkyblockAddonsTest implements FabricClientGameTest {

    private final List<SkyblockAddonsTestSuite> suites = List.of(
            new FeatureTest(),
            new FeatureSettingTest()
    );

    @Override
    public void runTest(ClientGameTestContext context) {
        System.out.println("Starting SkyblockAddons client game tests...");

        List<String> allErrors = suites.stream()
                .flatMap(suite -> suite.run(context).stream())
                .toList();

        if (!allErrors.isEmpty()) throw new AssertionError(
                "SkyblockAddons Test Suite failed with %d error(s):\n- %s".formatted(
                        allErrors.size(), String.join("\n- ", allErrors)
                )
        );

        System.out.println("All game tests completed successfully.");
    }

}