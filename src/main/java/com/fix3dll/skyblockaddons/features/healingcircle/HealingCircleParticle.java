package com.fix3dll.skyblockaddons.features.healingcircle;

import lombok.Getter;

import java.awt.geom.Point2D;

@Getter
public class HealingCircleParticle {

    private final Point2D.Double point;
    private final long creation = System.currentTimeMillis();

    public HealingCircleParticle(double x, double z) {
        point = new Point2D.Double(x, z);
    }
}