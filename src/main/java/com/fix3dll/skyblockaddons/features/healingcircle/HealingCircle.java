package com.fix3dll.skyblockaddons.features.healingcircle;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonClass;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonPlayer;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter @Setter
public class HealingCircle {

    private static final float DEFAULT_DIAMETER = 12;
    private static final float DEFAULT_RADIUS = 12 / 2F;
    private static final long DEFAULT_DURATION = 10000;

    @Getter @Setter private static float radius = 0;
    @Getter private static float diameter = DEFAULT_DIAMETER;
    private static long duration = DEFAULT_DURATION;

    private List<HealingCircleParticle> healingCircleParticles = new ArrayList<>();
    private long creation = System.currentTimeMillis();
    private Point2D.Double cachedCenterPoint = null;

    private double totalX;
    private double totalZ;
    private int totalParticles;

    public HealingCircle(HealingCircleParticle healingCircleParticle) {
        addPoint(healingCircleParticle);
        if (radius == 0) {
            calculateRadius();
        }
    }

    public void addPoint(HealingCircleParticle healingCircleParticle) {
        totalParticles++;
        totalX += healingCircleParticle.getPoint().getX();
        totalZ += healingCircleParticle.getPoint().getY();
        healingCircleParticles.add(healingCircleParticle);
    }

    public double getAverageX() {
        return totalX / (double) totalParticles;
    }

    public double getAverageZ() {
        return totalZ / (double) totalParticles;
    }

    public double getParticlesPerSecond() {
        int particlesPerSecond = 0;
        long now = System.currentTimeMillis();
        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            if (now - healingCircleParticle.getCreation() < 1000) {
                particlesPerSecond++;
            }
        }
        return particlesPerSecond;
    }

    public Point2D.Double getCircleCenter() {

        if (cachedCenterPoint != null) {
            return cachedCenterPoint;
        }

        if (healingCircleParticles.size() < 3) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        // The middle point, which is the first point for consistency. The circle will not appear
        // until two other points exist, one that is left of this one, and one right.
        Point2D.Double middlePoint = healingCircleParticles.get(0).getPoint();

        // The first point, which can be anywhere on the circle as long as its a decent
        // distance away from the middle.
        Point2D.Double firstPoint = null;
        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            Point2D.Double point = healingCircleParticle.getPoint();
            if (point != middlePoint && point.distance(middlePoint) > 2) {
                firstPoint = point;
                break;
            }
        }
        if (firstPoint == null) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        // The second point, which can be anywhere on the circle as long as its a decent
        // distance away from the middle + its on the opposite side of the first point.
        Point2D.Double secondPoint = null;
        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            Point2D.Double point = healingCircleParticle.getPoint();
            if (point != middlePoint && point != firstPoint) {
                double distanceToMiddle = point.distance(middlePoint);
                // Make sure that the point is closer to the middle point than the first
                // point, or else both points will be on the same side.
                if (distanceToMiddle > 2 && point.distance(firstPoint) > distanceToMiddle) {
                    secondPoint = point;
                    break;
                }
            }
        }
        if (secondPoint == null) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        Point2D.Double firstChordMidpoint = new Point2D.Double((middlePoint.x + firstPoint.x) / 2D, (middlePoint.y + firstPoint.y) / 2D);
        Point2D.Double secondChordMidpoint = new Point2D.Double((middlePoint.x + secondPoint.x) / 2D, (middlePoint.y + secondPoint.y) / 2D);

        Point2D.Double firstChordFirst = rotatePoint(middlePoint, firstChordMidpoint, 90);
        Point2D.Double firstChordSecond = rotatePoint(firstPoint, firstChordMidpoint, 90);

        Point2D.Double secondChordFirst = rotatePoint(middlePoint, secondChordMidpoint, 90);
        Point2D.Double secondChordSecond = rotatePoint(secondPoint, secondChordMidpoint, 90);

        Point2D.Double center = lineLineIntersection(firstChordFirst, firstChordSecond, secondChordFirst, secondChordSecond);

        checkIfCenterIsPerfect(center);

        return center;
    }

    public void checkIfCenterIsPerfect(Point2D.Double center) {
        // Not large enough sample size to check
        if (this.totalParticles < 25) {
            return;
        }

        int perfectParticles = 0;

        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            Point2D.Double point = healingCircleParticle.getPoint();

            double distance = point.distance(center);
            if (distance > (diameter - 1) / 2F && distance < (diameter + 1) / 2F) {
                perfectParticles++;
            }
        }

        float percentagePerfect = perfectParticles / (float) totalParticles;

        if (percentagePerfect > 0.75) {
            this.cachedCenterPoint = center;
        }
    }

    private static Point2D.Double rotatePoint(Point2D.Double point, Point2D.Double center, double degrees) {
        double radians = Math.toRadians(degrees);

        double newX = center.getX() + (point.getX() - center.getX()) * Math.cos(radians) - (point.getY() - center.getY()) * Math.sin(radians);
        double newY = center.getY() + (point.getX() - center.getX()) * Math.sin(radians) + (point.getY() - center.getY()) * Math.cos(radians);

        return new Point2D.Double(newX, newY);
    }

    private static Point2D.Double lineLineIntersection(Point2D.Double a, Point2D.Double b, Point2D.Double c, Point2D.Double d) {
        // Line AB represented as a1x + b1y = c1
        double a1 = b.y - a.y;
        double b1 = a.x - b.x;
        double c1 = a1 * (a.x) + b1 * (a.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = d.y - c.y;
        double b2 = c.x - d.x;
        double c2 = a2 * (c.x) + b2 * (c.y);

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            // The lines are parallel.
            return new Point2D.Double(Double.NaN, Double.NaN);
        } else {
            double x = (b2 * c1 - b1 * c2) / determinant;
            double y = (a1 * c2 - a2 * c1) / determinant;
            return new Point2D.Double(x, y);
        }
    }

    public void removeOldParticles() {
        Iterator<HealingCircleParticle> healingCircleParticleIterator = this.healingCircleParticles.iterator();
        while (healingCircleParticleIterator.hasNext()) {
            HealingCircleParticle healingCircleParticle = healingCircleParticleIterator.next();

            if (System.currentTimeMillis() - healingCircleParticle.getCreation() > duration) {
                this.totalX -= healingCircleParticle.getPoint().getX();
                this.totalZ -= healingCircleParticle.getPoint().getY();
                this.totalParticles--;

                healingCircleParticleIterator.remove();
            }
        }
    }

    public boolean hasCachedCenterPoint() {
        return cachedCenterPoint != null;
    }

    private void calculateRadius() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        Object2ObjectOpenHashMap<String, DungeonPlayer> teammates = main.getDungeonManager().getTeammates();
        Pair<DungeonClass, Integer> thePlayerClass = main.getDungeonManager().getThePlayerClass();
        boolean isThePlayerHealer = thePlayerClass != null && thePlayerClass.getLeft() == DungeonClass.HEALER;

        // Formulas: https://wiki.hypixel.net/Healer
        if (teammates.isEmpty() && isThePlayerHealer) { // solo healer run
            int thePlayerClassLevel = thePlayerClass.getRight();
            radius = 5.5F + (float) Math.floor(thePlayerClassLevel / 5F) * 0.5F;
            diameter = radius * 2;
            duration = (10 + (long) Math.floor(thePlayerClassLevel / 5F)) * 1000;
        } else if (!teammates.isEmpty()) {
            int healerCounter = 0;
            DungeonPlayer healerData = null;

            for (DungeonPlayer teammateData : teammates.values()) {
                if (teammateData.getDungeonClass() == DungeonClass.HEALER) {
                    healerCounter++;
                    healerData = teammateData;
                }
            }

            // TODO improve
            if (healerCounter == 1 && !isThePlayerHealer) {
                int healerClassLevel = healerData.getClassLevel();
                radius = 5.5F + (float) Math.floor(healerClassLevel / 5F) * 0.5F;
                diameter = radius * 2;
                duration = (10 + (long) Math.floor(healerClassLevel / 5F)) * 1000;
            } else if (healerCounter == 0 && isThePlayerHealer) {
                int thePlayerClassLevel = thePlayerClass.getRight();
                radius = 5.5F + (float) Math.floor(thePlayerClassLevel / 5F) * 0.5F;
                diameter = radius * 2;
                duration = (10 + (long) Math.floor(thePlayerClassLevel / 5F)) * 1000;
            } else {
                radius = DEFAULT_RADIUS;
                diameter = DEFAULT_DIAMETER;
                duration = DEFAULT_DURATION;
            }
        } else {
            radius = DEFAULT_RADIUS;
            diameter = DEFAULT_DIAMETER;
            duration = DEFAULT_DURATION;
        }
    }
}
