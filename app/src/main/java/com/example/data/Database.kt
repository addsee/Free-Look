package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val language: String = "ar",
    val controlMode: Int = 0, // 0: eye tracking, 1: head tracking, 2: hybrid, 3: voice + face
    val gazeDurationMs: Int = 1000,
    val cursorStyle: String = "circle_pulse",
    val cursorSize: Float = 48f,
    val cursorColorHex: String = "#00F0FF",
    val cursorOpacity: Float = 0.8f,
    val glowEnabled: Boolean = true,
    val doubleCursorEnabled: Boolean = true,
    val clickBlockerEnabled: Boolean = true,
    val batterySaverEnabled: Boolean = false,
    val eyeFatigueNotificationEnabled: Boolean = true,
    val speechAssistantEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundFeedbackEnabled: Boolean = true
)

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gestureName: String,
    val actionSequence: String
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val avatarColorHex: String
)

@Entity(tableName = "training")
data class TrainingProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val challengeName: String,
    val score: Int,
    val badgeEarned: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)
}

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY id DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)
}

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training ORDER BY timestamp DESC")
    fun getAllTraining(): Flow<List<TrainingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(progress: TrainingProgressEntity)
}

@Database(
    entities = [
        SettingsEntity::class,
        MacroEntity::class,
        NoteEntity::class,
        ContactEntity::class,
        TrainingProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun macroDao(): MacroDao
    abstract fun notesDao(): NotesDao
    abstract fun contactDao(): ContactDao
    abstract fun trainingDao(): TrainingDao
}
