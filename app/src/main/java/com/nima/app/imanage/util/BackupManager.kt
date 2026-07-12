package com.nima.app.imanage.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nima.app.imanage.data.db.AppDatabase
import com.nima.app.imanage.data.db.entity.PasswordItemEntity
import com.nima.app.imanage.data.model.BackupData
import com.nima.app.imanage.data.model.BackupSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class BackupManager(private val db: AppDatabase) {

    suspend fun export(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bankCards = db.bankCardDao().getAll().first()
            val loans = db.loanDao().getAll().first()
            val noteBoxes = db.noteBoxDao().getAll().first()
            val notes = db.noteDao().getAllOnce()
            val expenseCategories = db.expenseCategoryDao().getAll().first()
            val expenses = db.expenseDao().getAll().first()
            val incomeSources = db.incomeSourceDao().getAll().first()
            val incomes = db.incomeDao().getAll().first()
            val installments = db.installmentDao().getAll().first()
            val installmentItems = db.installmentItemDao().getAllItems().first()
            val assets = db.assetDao().getAll().first()
            val passwords = db.passwordItemDao().getAll().first().map { decryptPassword(it) }
            val trips = db.tripDao().getAll().first()
            val participants = db.participantDao().getAllOnce()
            val tripExpenses = db.tripExpenseDao().getAll().first()
            val tripExpenseSplits = db.tripExpenseSplitDao().getAllOnce()
            val settlements = db.settlementDao().getAllOnce()
            val carServices = db.carServiceDao().getAll().first()

            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val settings = BackupSettings(
                language = prefs.getString("language", "en") ?: "en",
                theme = prefs.getString("theme_mode", "system") ?: "system"
            )

            val appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            } catch (_: Exception) {
                "1.0"
            }

            val backupData = BackupData(
                version = 1,
                appVersion = appVersion,
                timestamp = System.currentTimeMillis(),
                settings = settings,
                bankCards = bankCards,
                loans = loans,
                noteBoxes = noteBoxes,
                notes = notes,
                expenseCategories = expenseCategories,
                expenses = expenses,
                incomeSources = incomeSources,
                incomes = incomes,
                installments = installments,
                installmentItems = installmentItems,
                assets = assets,
                passwords = passwords,
                trips = trips,
                participants = participants,
                tripExpenses = tripExpenses,
                tripExpenseSplits = tripExpenseSplits,
                settlements = settlements,
                carServices = carServices
            )

            val json = exportGson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
            } ?: throw Exception("Could not open output stream")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun import(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw Exception("Could not open input stream")

            val backupData = importGson.fromJson(json, BackupData::class.java)
                ?: throw Exception("Invalid backup file")

            if (backupData.version != 1) {
                throw Exception("Unsupported backup version: ${backupData.version}")
            }

            db.runInTransaction {
                clearAllTables()
                val writableDb = db.openHelper.writableDatabase

                backupData.noteBoxes.forEach { writableDb.insert("note_boxes", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.notes.forEach { writableDb.insert("notes", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.incomeSources.forEach { writableDb.insert("income_sources", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.incomes.forEach { writableDb.insert("incomes", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.expenseCategories.forEach { writableDb.insert("expense_categories", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.expenses.forEach { writableDb.insert("expenses", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.installments.forEach { writableDb.insert("installments", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.installmentItems.forEach { writableDb.insert("installment_items", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.trips.forEach { writableDb.insert("trips", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.participants.forEach { writableDb.insert("trip_participants", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.tripExpenses.forEach { writableDb.insert("trip_expenses", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.tripExpenseSplits.forEach { writableDb.insert("trip_expense_splits", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.settlements.forEach { writableDb.insert("trip_settlements", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.bankCards.forEach { writableDb.insert("bank_cards", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.assets.forEach { writableDb.insert("assets", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.loans.forEach { writableDb.insert("loans", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }
                backupData.carServices.forEach { writableDb.insert("car_services", SQLiteDatabase.CONFLICT_REPLACE, it.toCv()) }

                backupData.passwords.forEach { password ->
                    val reEncrypted = encryptPassword(password)
                    writableDb.insert("passwords", SQLiteDatabase.CONFLICT_REPLACE, reEncrypted.toCv())
                }
            }

            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("language", backupData.settings.language)
                .putString("theme_mode", backupData.settings.theme)
                .apply()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun clearAllTables() {
        val writableDb = db.openHelper.writableDatabase
        writableDb.execSQL("DELETE FROM trip_expense_splits")
        writableDb.execSQL("DELETE FROM trip_expenses")
        writableDb.execSQL("DELETE FROM trip_settlements")
        writableDb.execSQL("DELETE FROM trip_participants")
        writableDb.execSQL("DELETE FROM trips")
        writableDb.execSQL("DELETE FROM installment_items")
        writableDb.execSQL("DELETE FROM installments")
        writableDb.execSQL("DELETE FROM notes")
        writableDb.execSQL("DELETE FROM note_boxes")
        writableDb.execSQL("DELETE FROM incomes")
        writableDb.execSQL("DELETE FROM income_sources")
        writableDb.execSQL("DELETE FROM expenses")
        writableDb.execSQL("DELETE FROM expense_categories")
        writableDb.execSQL("DELETE FROM bank_cards")
        writableDb.execSQL("DELETE FROM assets")
        writableDb.execSQL("DELETE FROM passwords")
        writableDb.execSQL("DELETE FROM car_services")
        writableDb.execSQL("DELETE FROM loans")
    }

    private fun decryptPassword(item: PasswordItemEntity): PasswordItemEntity {
        val decrypted = CryptoUtils.decrypt(item.encryptedPassword)
        return item.copy(encryptedPassword = decrypted)
    }

    private fun encryptPassword(item: PasswordItemEntity): PasswordItemEntity {
        val encrypted = CryptoUtils.encrypt(item.encryptedPassword)
        return item.copy(encryptedPassword = encrypted)
    }

    private fun Any.toCv(): ContentValues {
        val cv = ContentValues()
        val element = exportGson.toJsonTree(this).asJsonObject
        element.entrySet().forEach { (key, value) ->
            when {
                value.isJsonNull -> cv.putNull(key)
                value.asJsonPrimitive.isBoolean -> cv.put(key, value.asBoolean)
                value.asJsonPrimitive.isString -> cv.put(key, value.asString)
                value.asJsonPrimitive.isNumber -> {
                    val numStr = value.asString
                    if (numStr.contains(".") || numStr.contains("E") || numStr.contains("e")) {
                        cv.put(key, value.asDouble)
                    } else {
                        cv.put(key, value.asLong)
                    }
                }
            }
        }
        return cv
    }

    companion object {
        private val exportGson = GsonBuilder().serializeNulls().create()
        private val importGson = Gson()
    }
}
