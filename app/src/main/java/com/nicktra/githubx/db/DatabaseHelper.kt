package com.nicktra.githubx.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.TABLE_NAME

internal class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_NAME = "dbgithubx"

        private const val DATABASE_VERSION = 1

        private val SQL_CREATE_TABLE_NOTE = "CREATE TABLE $TABLE_NAME" +
                " (${DatabaseContract.UserColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DatabaseContract.UserColumns.COLUMN_NAME_USERNAME} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.COLUMN_NAME_AVATAR_URL} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.COLUMN_NAME_USER_ID} TEXT NOT NULL,"+
                " ${DatabaseContract.UserColumns.COLUMN_NAME_USER_URL} TEXT NOT NULL,"+
                " ${DatabaseContract.UserColumns.COLUMN_NAME_USER_HTML_URL} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_NOTE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}