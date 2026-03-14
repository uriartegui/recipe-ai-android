package com.guiuriarte.recipeai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guiuriarte.recipeai.data.database.entity.RecipeEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT")
    }
}

@Database(entities = [RecipeEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}

