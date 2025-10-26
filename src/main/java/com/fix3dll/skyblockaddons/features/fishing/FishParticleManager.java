package com.fix3dll.skyblockaddons.features.fishing;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.mixin.extensions.WakeParticleExtension;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class implements an optimized <b>"Hybrid"</b> approach for detecting fish particle trails,
 * limited to a fixed buffer size (<code>N=MAX_ACTIVE_PARTICLES</code>).
 * </p>
 * <ol>
 * <li>It uses 'Sliding Window' data structures (<code>O(k)</code> maps/deque, <code>k &lt;= N</code>)
 * for efficient storage and time/quantity-based pruning.</li>
 * <li>It uses a 'Comprehensive DFS' (worst-case <code>O(N<sup>2</sup>)</code> due to cap N) algorithm
 * calculating the longest path <i>ending</i> at each node for high accuracy.</li>
 * <li>Includes cycle detection, stable <code>long</code> hashing based on coordinates, and optimized pruning.</li>
 * <li>Directly modifies particles belonging to recently identified trails.</li>
 * </ol>
 * <p>
 * This replaces the original implementation which used bitwise matrix operations
 * (conceptually similar to Bellman-Ford pathfinding) and was strictly limited to <code>N=64</code>
 * due to its 64-bit optimizations.
 * See <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/21166ae994aae813f53dca3ad74ae09b35b860d6/src/main/java/codes/biscuit/skyblockaddons/features/fishParticles/FishParticleManager.java">Original code</a>.
 * </p>
 * <p>
 * This Hybrid approach offers the high accuracy of comprehensive graph traversal (like Graph+DFS models)
 * while leveraging dynamic structures capped at <code>N</code>. Benchmarks show it offers good average
 * performance, potentially faster than fixed-array Graph+DFS at low load, but its worst-case performance
 * (dominated by DFS on hash-based structures) might be slightly less predictable under extreme load,
 * although the <code>MAX_ACTIVE_PARTICLES</code> cap provides stability.
 * </p>
 */
public class FishParticleManager {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final double DIST_EXPECTED = 0.1;
    private static final double DIST_VARIATION = 0.005;
    private static final double ANGLE_EXPECTED = 12.0;
    private static final int TIME_VARIATION = 4;
    private static final double MAX_DISTANCE = 8.0;
    private static final int MIN_TRAIL_LENGTH = 4;
    private static final int MAX_PARTICLE_AGE = 100;
    /**
     * The absolute maximum number of particles to track.
     * This acts as a safety cap to prevent O(k^2) lag spikes
     * when particle density (k) is extremely high.
     */
    private static final int MAX_ACTIVE_PARTICLES = 64;

    /**
     * Internal node class representing a particle's data and position in the graph.
     * <br>Uses stable <code>long</code> hash and overrides <code>hashCode/equals</code>.
     */
    private static class ParticleNode {
        final double x, z;
        final double distance;
        final double angle;
        final long spawnTime;
        final List<WakeParticle> particles;
        final long hash;

        ParticleNode(double x, double z, double dist, double angle, long time, List<WakeParticle> particles, long hash) {
            this.x = x;
            this.z = z;
            this.distance = dist;
            this.angle = angle;
            this.spawnTime = time;
            this.particles = new ArrayList<>(particles);
            this.hash = hash;
        }

        /**
         * Checks if this (new) particle can follow the 'other' (older) particle.
         * (this -> other)
         */
        boolean isCompatibleWith(ParticleNode other, long currentTime) {
            long timeDiff = this.spawnTime - other.spawnTime;
            if (timeDiff <= 0 || timeDiff > TIME_VARIATION) return false;
            if (currentTime - other.spawnTime > MAX_PARTICLE_AGE) return false;

            double anglDiff = Math.abs(this.angle - other.angle) % 360;
            if ((anglDiff > 180 ? 360 - anglDiff : anglDiff) >= ANGLE_EXPECTED) return false;

            double distDiff1 = Math.abs(other.distance - this.distance - DIST_EXPECTED);
            double distDiff2 = Math.abs(other.distance - this.distance - 2 * DIST_EXPECTED);

            return distDiff1 < DIST_VARIATION || distDiff2 < DIST_VARIATION;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.hash);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            return this.hash == ((ParticleNode) obj).hash;
        }
    }

    // Core Data Structures O(k)
    /** <code>O(1)</code> lookup map by stable <code>long</code> hash. */
    private static final Map<Long, ParticleNode> particleMap = new Long2ObjectLinkedOpenHashMap<>();
    /** Time-sorted deque (newest first), capped at <code>MAX_ACTIVE_PARTICLES</code>. */
    private static final Deque<ParticleNode> recentParticles = new ArrayDeque<>();
    /** Set of nodes identified as heads of valid trails in the last calculation. */
    private static final Set<ParticleNode> trailHeads = new HashSet<>();

    // Comprehensive Graph Structure O(k^2)
    /** Stores reverse graph links: <code>(Child Node -> List&lt;Parent Nodes&gt;)</code>. Used by DFS. */
    private static final Map<ParticleNode, List<ParticleNode>> graph = new HashMap<>();

    /** DFS Memoization cache: <code>(Node -> Max Path Length ending at Node)</code>. */
    private static final Map<ParticleNode, Integer> maxPathCache = new Object2IntOpenHashMap<>();

    /**
     * Primary entry point. Called when a new particle spawns.
     * <br>Complexity is dominated by <code>calculateTrails()</code> which is roughly <code>O(N<sup>2</sup>)</code> due to the cap N.
     * @param hook Player's fishing hook.
     * @param fishWakeParticle Newly spawned particle.
     */
    public static void onFishWakeSpawn(FishingHook hook, WakeParticle fishWakeParticle) {
        ClientLevel level = MC.level;
        if (level == null) return;

        // Clear state if hook is absent or just cast
        if (hook == null || hook.tickCount == 0) {
            clearParticleCache();
            return;
        }

        double xCoord = fishWakeParticle.x;
        double zCoord = fishWakeParticle.z;

        // Validate distance (early exit)
        double distToHook = Math.sqrt(
                (xCoord - hook.getX()) * (xCoord - hook.getX()) + (zCoord - hook.getZ()) * (zCoord - hook.getZ())
        );
        if (distToHook > MAX_DISTANCE) return;

        long xBits = Double.doubleToLongBits(xCoord);
        long zBits = Double.doubleToLongBits(zCoord);
        long hash = 31 * (31 * 23 + xBits) + zBits;

        // Particle already exists at this hash.
        ParticleNode existingNode = particleMap.get(hash);
        if (existingNode != null) {
            existingNode.particles.add(fishWakeParticle);
            return; // Don't process as a new node
        }

        long currentTime = level.getGameTime();

        // Prune old/excess particles
        cleanOldParticles(currentTime);

        // Calculate the angle relative to the hook
        double angle = Mth.atan2(xCoord - hook.getX(), zCoord - hook.getZ()) * 180 / Math.PI;

        // Create the new particle node
        ParticleNode newNode = new ParticleNode(
                xCoord, zCoord, distToHook, angle, currentTime, Collections.singletonList(fishWakeParticle), hash
        );

        // Build graph links (Comprehensive O(k))
        List<ParticleNode> parents = new ArrayList<>();
        // Find ALL compatible parents from the sliding window.
        for (ParticleNode candidate : recentParticles) {
            // Check if newNode can follow candidate (candidate is a parent of newNode)
            if (newNode.isCompatibleWith(candidate, currentTime)) {
                parents.add(candidate);
            }
            // Check if candidate can follow newNode (newNode is a parent of candidate)
            if (candidate.isCompatibleWith(newNode, currentTime)) {
                // Add newNode to candidate's parent list in the graph.
                graph.computeIfAbsent(candidate, k -> new ArrayList<>()).add(newNode);
            }
        }

        // Add the new node and its calculated parent links to the graph structures
        particleMap.put(hash, newNode);
        recentParticles.addFirst(newNode); // Add to the front (most recent)
        graph.put(newNode, parents); // Store the parents for newNode

        // Recalculate all trails using DFS (The O(N^2 capped) part)
        calculateTrails();

        // Update particle visuals based on the new trailHeads
        updateOverlay();
    }

    /**
     * Prunes old particles based on TIME and QUANTITY limits. Optimized lazy deletion approach.
     * @param currentTime Current game time (ticks).
     */
    private static void cleanOldParticles(long currentTime) {
        Set<ParticleNode> nodesToEvict = new HashSet<>(); // Collect nodes to remove

        // Prune by age from the end of the time-sorted deque
        while (!recentParticles.isEmpty()) {
            ParticleNode oldest = recentParticles.peekLast();
            if (currentTime - oldest.spawnTime > MAX_PARTICLE_AGE) {
                recentParticles.removeLast(); // Remove from deque
                nodesToEvict.add(oldest);     // Mark for full eviction
            } else {
                break; // Stop when a recent enough particle is found
            }
        }

        // Prune by quantity limit if still exceeding MAX_ACTIVE_PARTICLES
        while (recentParticles.size() > MAX_ACTIVE_PARTICLES) {
            ParticleNode oldest = recentParticles.removeLast(); // Remove excess oldest from deque
            nodesToEvict.add(oldest);                           // Mark for full eviction
        }

        // Exit if no nodes need eviction
        if (nodesToEvict.isEmpty()) return;

        // Evict marked nodes from all relevant data structures
        for (ParticleNode nodeToEvict : nodesToEvict) {
            particleMap.remove(nodeToEvict.hash); // Remove from main lookup map
            graph.remove(nodeToEvict);            // Remove node as a key in the graph (removes outgoing links implicitly)
            trailHeads.remove(nodeToEvict);       // Remove if it was a trail head
            maxPathCache.remove(nodeToEvict);     // Remove from DFS cache
        }
        // Dangling edges (references *to* evicted nodes within graph's List values)
        // are handled implicitly by the DFS validity check. No O(k^2) pruning needed here.
    }

    /** Recalculates trail heads using DFS on active particles. */
    private static void calculateTrails() {
        maxPathCache.clear(); // Clear DFS cache for this run
        trailHeads.clear();   // Clear previous trail heads
        // Tracks nodes covered by DFS starts in *this* run
        Set<ParticleNode> processedNodes = new HashSet<>();

        // Iterate through currently active particles (newest first is efficient for cache hits)
        for (ParticleNode node : recentParticles) {
            // Find the longest path ending at this node using DFS with cycle detection
            int pathLen = getMaxPathLengthDfs(node, processedNodes, Sets.newIdentityHashSet());

            // If path meets minimum length, mark this node as a trail head
            if (pathLen >= MIN_TRAIL_LENGTH) {
                trailHeads.add(node);
            }
        }
    }

    /**
     * Memoized DFS to find the longest path <i>ending</i> at <code>node</code>.
     * Includes validity check for parents and cycle detection.
     * @param node           Node to calculate path length for.
     * @param processedNodes Set tracking nodes processed globally in this calculateTrails run.
     * @param visiting       Set tracking nodes currently in the recursion stack (use IdentityHashSet).
     * @return Length of the longest chain ending at <code>node</code>.
     */
    private static int getMaxPathLengthDfs(ParticleNode node, Set<ParticleNode> processedNodes, Set<ParticleNode> visiting) {
        // 1. Cycle Check using the 'visiting' set (Identity comparison)
        if (!visiting.add(node)) {
            return 0; // Cycle detected along this path, return 0 length.
        }

        // 2. Cache Check (uses node's equals/hashCode based on long hash)
        Integer cachedLength = maxPathCache.get(node);
        if (cachedLength != null) {
            visiting.remove(node); // Backtrack visiting set
            return cachedLength;
        }

        // Mark as processed globally for this calculateTrails run (uses node's equals/hashCode)
        processedNodes.add(node);

        // 3. Get Parents from graph (Node -> List<Parents>)
        List<ParticleNode> parents = graph.get(node);
        int maxParentLength = 0;

        // 4. Recurse through VALID parents
        if (parents != null && !parents.isEmpty()) {
            for (ParticleNode parent : parents) {
                // Validity Check: Ensure parent still exists in the main map (wasn't pruned)
                // Use containsKey for O(1) check based on long hash
                if (particleMap.containsKey(parent.hash)) {
                    // Recursively call DFS for the valid parent
                    int parentLen = getMaxPathLengthDfs(parent, processedNodes, visiting);
                    // Update max length found
                    if (parentLen > maxParentLength) {
                        maxParentLength = parentLen;
                    }
                }
                // Ignore pruned parents
            }
        }

        // 5. Backtrack: Remove node from visiting set
        visiting.remove(node);

        // 6. Cache Result: Length = 1 (self) + longest valid parent path
        int myLength = 1 + maxParentLength;
        maxPathCache.put(node, myLength); // Cache the result
        return myLength;
    }

    /** Updates visual properties of particles belonging to <b>recently active</b> trail heads. */
    private static void updateOverlay() {
        if (trailHeads.isEmpty()) return; // No trails to update.

        int color = Feature.COLORED_FISHING_PARTICLES.getColor();
        float rCol = ARGB.redFloat(color);
        float gCol = ARGB.greenFloat(color);
        float bCol = ARGB.blueFloat(color);
        boolean biggerWake = Feature.COLORED_FISHING_PARTICLES.isEnabled(FeatureSetting.BIGGER_WAKE);

        for (ParticleNode head : trailHeads) {
            for (WakeParticle particle : head.particles) {
                ((WakeParticleExtension) particle).sba$setBlankSprite(true);
                if (biggerWake) particle.scale(1.1F);
                particle.setColor(rCol, gCol, bCol);
            }
        }
    }

    /**
     * Clears all particle data from all tracking structures.
     */
    public static void clearParticleCache() {
        particleMap.clear();
        recentParticles.clear();
        trailHeads.clear();
        graph.clear();
        maxPathCache.clear();
    }

}