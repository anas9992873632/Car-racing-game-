package com.example.game

import com.example.data.TrackDef

enum class SceneryItemType {
    TREE,
    BUILDING,
    STREET_LIGHT,
    ROAD_SIGN,
    BRIDGE_ARCH,
    FINISH_LINE,
    CHECKPOINT_ARCH,
    COIN,
    BOOST_PAD,
    OIL_SLICK,
    BARRIER_CONE
}

data class TrackObstacle(
    val trackDistance: Float, // Distance in meters from start
    val lateralX: Float,      // -1.0 to 1.0 (relative to road width)
    val type: SceneryItemType,
    var isCollected: Boolean = false
)

data class TrackSegment(
    val index: Int,
    val lengthMeters: Float,
    val curve: Float,         // Negative = curves left, Positive = curves right, 0 = straight
    val hill: Float,          // Vertical elevation change
    val roadsideLeft: SceneryItemType?,
    val roadsideRight: SceneryItemType?
)

class GeneratedTrack(
    val def: TrackDef,
    val segments: List<TrackSegment>,
    val totalLengthMeters: Float,
    val items: MutableList<TrackObstacle>
) {
    fun getSegmentAt(distance: Float): TrackSegment {
        val wrappedDist = (distance % totalLengthMeters + totalLengthMeters) % totalLengthMeters
        val segIndex = ((wrappedDist / totalLengthMeters) * segments.size).toInt().coerceIn(0, segments.size - 1)
        return segments[segIndex]
    }
}

object TrackGenerator {
    fun buildTrack(def: TrackDef): GeneratedTrack {
        val totalSegments = 160
        val segmentLength = def.lengthMeters.toFloat() / totalSegments
        val segments = ArrayList<TrackSegment>(totalSegments)
        val items = ArrayList<TrackObstacle>()

        for (i in 0 until totalSegments) {
            val progress = i.toFloat() / totalSegments

            // Generate interesting sequence of turns and hills based on track scenery
            val curve = when {
                progress in 0.12f..0.22f -> 2.2f  // right sweeper
                progress in 0.28f..0.38f -> -2.5f // left sweeper
                progress in 0.45f..0.52f -> 3.5f  // sharp right
                progress in 0.58f..0.68f -> -3.2f // sharp left
                progress in 0.75f..0.85f -> 1.8f  // chicane right
                progress in 0.85f..0.92f -> -1.8f // chicane left
                else -> 0.0f                      // straightaway
            }

            val hill = when {
                progress in 0.20f..0.35f -> 1.5f
                progress in 0.35f..0.50f -> -1.2f
                progress in 0.65f..0.80f -> 1.8f
                progress in 0.80f..0.95f -> -1.5f
                else -> 0.0f
            }

            val (leftItem, rightItem) = when (def.sceneryType) {
                "city", "night_city" -> {
                    if (i % 8 == 0) SceneryItemType.BUILDING to SceneryItemType.BUILDING
                    else if (i % 4 == 0) SceneryItemType.STREET_LIGHT to SceneryItemType.STREET_LIGHT
                    else null to null
                }
                "desert" -> {
                    if (i % 6 == 0) SceneryItemType.TREE to SceneryItemType.TREE // Cacti
                    else if (i % 12 == 0) SceneryItemType.ROAD_SIGN to null
                    else null to null
                }
                "forest" -> {
                    if (i % 4 == 0) SceneryItemType.TREE to SceneryItemType.TREE
                    else null to null
                }
                "mountain", "snow" -> {
                    if (i % 5 == 0) SceneryItemType.TREE to SceneryItemType.TREE
                    else if (i % 14 == 0) SceneryItemType.ROAD_SIGN to null
                    else null to null
                }
                "coastal" -> {
                    if (i % 5 == 0) SceneryItemType.TREE to null // Palms on left
                    else if (i % 10 == 0) null to SceneryItemType.STREET_LIGHT
                    else null to null
                }
                "neon" -> {
                    if (i % 6 == 0) SceneryItemType.BUILDING to SceneryItemType.BUILDING
                    else if (i % 3 == 0) SceneryItemType.STREET_LIGHT to SceneryItemType.STREET_LIGHT
                    else null to null
                }
                else -> null to null
            }

            segments.add(
                TrackSegment(
                    index = i,
                    lengthMeters = segmentLength,
                    curve = curve,
                    hill = hill,
                    roadsideLeft = leftItem,
                    roadsideRight = rightItem
                )
            )

            // Place coins along straights and curves
            val dist = i * segmentLength
            if (i % 7 == 0 && i > 3 && i < totalSegments - 5) {
                // Line of coins
                val coinLane = if (curve > 0) 0.5f else if (curve < 0) -0.5f else 0.0f
                items.add(TrackObstacle(dist, coinLane, SceneryItemType.COIN))
                items.add(TrackObstacle(dist + 12f, coinLane, SceneryItemType.COIN))
                items.add(TrackObstacle(dist + 24f, coinLane, SceneryItemType.COIN))
            }

            // Place boost pads on straights
            if (i % 22 == 10) {
                items.add(TrackObstacle(dist, 0.0f, SceneryItemType.BOOST_PAD))
            }

            // Place obstacles (oil slicks or cones)
            if (i % 18 == 8 && i > 8) {
                val obsLane = if (i % 2 == 0) -0.6f else 0.6f
                items.add(TrackObstacle(dist, obsLane, if (i % 3 == 0) SceneryItemType.OIL_SLICK else SceneryItemType.BARRIER_CONE))
            }

            // Overhead bridge
            if (i == 40 || i == 110) {
                items.add(TrackObstacle(dist, 0.0f, SceneryItemType.BRIDGE_ARCH))
            }

            // Checkpoint arch
            if (i == 80) {
                items.add(TrackObstacle(dist, 0.0f, SceneryItemType.CHECKPOINT_ARCH))
            }
        }

        // Finish line at beginning/end
        items.add(TrackObstacle(0f, 0.0f, SceneryItemType.FINISH_LINE))

        return GeneratedTrack(
            def = def,
            segments = segments,
            totalLengthMeters = def.lengthMeters.toFloat(),
            items = items
        )
    }
}
