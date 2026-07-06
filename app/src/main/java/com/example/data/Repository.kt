package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val db: AppDatabase) {
    val settingsFlow: Flow<SettingsEntity?> = db.settingsDao().getSettingsFlow()
    val macrosFlow: Flow<List<MacroEntity>> = db.macroDao().getAllMacros()
    val notesFlow: Flow<List<NoteEntity>> = db.notesDao().getAllNotes()
    val contactsFlow: Flow<List<ContactEntity>> = db.contactDao().getAllContacts()
    val trainingFlow: Flow<List<TrainingProgressEntity>> = db.trainingDao().getAllTraining()

    suspend fun getSettings(): SettingsEntity {
        val existing = db.settingsDao().getSettingsDirect()
        if (existing == null) {
            val defaultSettings = SettingsEntity()
            db.settingsDao().insertOrUpdateSettings(defaultSettings)
            return defaultSettings
        }
        return existing
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        db.settingsDao().insertOrUpdateSettings(settings)
    }

    suspend fun addMacro(macro: MacroEntity) {
        db.macroDao().insertMacro(macro)
    }

    suspend fun deleteMacro(macro: MacroEntity) {
        db.macroDao().deleteMacro(macro)
    }

    suspend fun addNote(note: NoteEntity) {
        db.notesDao().insertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        db.notesDao().deleteNote(note)
    }

    suspend fun addContact(contact: ContactEntity) {
        db.contactDao().insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        db.contactDao().deleteContact(contact)
    }

    suspend fun addTrainingProgress(progress: TrainingProgressEntity) {
        db.trainingDao().insertTraining(progress)
    }

    // Prepopulate some starting items if database is empty
    suspend fun prepopulateIfEmpty() {
        if (db.settingsDao().getSettingsDirect() == null) {
            db.settingsDao().insertOrUpdateSettings(SettingsEntity())
        }
        // Prepopulate standard contact items
        val contacts = db.contactDao().getAllContacts().firstOrNull()
        if (contacts.isNullOrEmpty()) {
            db.contactDao().insertContact(ContactEntity(name = "أمي / Mother", phone = "+966501234567", avatarColorHex = "#FF1744"))
            db.contactDao().insertContact(ContactEntity(name = "أبي / Father", phone = "+966507654321", avatarColorHex = "#2979FF"))
            db.contactDao().insertContact(ContactEntity(name = "الطبيب / Doctor", phone = "+966500000000", avatarColorHex = "#00E676"))
        }
        // Prepopulate macros
        val macros = db.macroDao().getAllMacros().firstOrNull()
        if (macros.isNullOrEmpty()) {
            db.macroDao().insertMacro(MacroEntity(gestureName = "نفخ الخدين (Puff Cheeks)", actionSequence = "لقطة شاشة (Screenshot)"))
            db.macroDao().insertMacro(MacroEntity(gestureName = "ابتسامة عريضة (Big Smile)", actionSequence = "كاميرا السيلفي (Selfie Photo)"))
        }
    }
}
