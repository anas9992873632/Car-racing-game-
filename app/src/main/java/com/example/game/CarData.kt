package com.example.game

import com.example.data.CarDef
import com.example.data.CarStatusEntity

data class CarEffectiveStats(
    val topSpeedKmh: Float,     // e.g. 210 to 330 km/h
    val accelerationRate: Float, // m/s^2
    val handlingTurnRate: Float,// lateral sensitivity
    val brakingForce: Float,    // deceleration rate
    val nitroBoostFactor: Float,// speed multiplier when nitro is active
    val nitroDurationSec: Float,// duration of full nitro tank
    val nitroRechargeRate: Float
)

object CarPhysicsCalculator {
    fun calculateStats(def: CarDef, status: CarStatusEntity?): CarEffectiveStats {
        val engLvl = (status?.engineLevel ?: 1) - 1
        val turboLvl = (status?.turboLevel ?: 1) - 1
        val tiresLvl = (status?.tiresLevel ?: 1) - 1
        val brakesLvl = (status?.brakesLevel ?: 1) - 1
        val handLvl = (status?.handlingLevel ?: 1) - 1
        val nitroLvl = (status?.nitroLevel ?: 1) - 1
        val maxSpdLvl = (status?.maxSpeedLevel ?: 1) - 1

        val baseSpeed = def.baseSpeed + (engLvl * 4) + (maxSpdLvl * 5)
        val baseAccel = def.baseAcceleration + (turboLvl * 5) + (engLvl * 3)
        val baseHand = def.baseHandling + (handLvl * 5) + (tiresLvl * 4)
        val baseBrake = def.baseBraking + (brakesLvl * 6) + (tiresLvl * 3)
        val baseNit = def.baseNitro + (nitroLvl * 6)

        // Convert to physical game values
        val topSpeedKmh = 190f + (baseSpeed.coerceIn(40, 130) * 1.35f)
        val accelRate = 7.5f + (baseAccel * 0.12f)
        val turnRate = 1.35f + (baseHand * 0.015f)
        val brakeDecel = 18f + (baseBrake * 0.15f)
        val nitroMultiplier = 1.38f + (baseNit * 0.002f)
        val nitroDuration = 4.5f + (nitroLvl * 0.6f)
        val nitroRecharge = 0.07f + (tiresLvl * 0.01f)

        return CarEffectiveStats(
            topSpeedKmh = topSpeedKmh,
            accelerationRate = accelRate,
            handlingTurnRate = turnRate,
            brakingForce = brakeDecel,
            nitroBoostFactor = nitroMultiplier,
            nitroDurationSec = nitroDuration,
            nitroRechargeRate = nitroRecharge
        )
    }
}
