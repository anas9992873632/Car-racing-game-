package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM car_status")
    fun getAllCars(): Flow<List<CarStatusEntity>>

    @Query("SELECT * FROM car_status WHERE carId = :carId LIMIT 1")
    suspend fun getCarSync(carId: String): CarStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCar(car: CarStatusEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCarsIfMissing(cars: List<CarStatusEntity>)

    @Query("SELECT * FROM track_records")
    fun getAllTracks(): Flow<List<TrackRecordEntity>>

    @Query("SELECT * FROM track_records WHERE trackId = :trackId LIMIT 1")
    suspend fun getTrackSync(trackId: String): TrackRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrack(track: TrackRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracksIfMissing(tracks: List<TrackRecordEntity>)

    @Query("SELECT * FROM missions")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMission(mission: MissionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissionsIfMissing(missions: List<MissionEntity>)
}
