package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Created by Bassam on 7/8/2017.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "shelter.db";

    public static final String SQL_DATATYPE_INT = " INTEGER ";
    public static final String SQL_DATATYPE_TEXT = " TEXT ";
    public static final String SQL_KEYWORD_PRIMARY_KEY = " PRIMARY KEY ";
    public static final String SQL_KEYWORD_AUTOINCREMENT = " AUTOINCREMENT ";
    public static final String SQL_KEYWORD_NOT_NULL = " NOT NULL ";
    public static final String SQL_KEYWORD_DEFAULT = " DEFAULT ";

    private static final String SQL_CREATE_ENTRY = "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
            PetEntry._ID + SQL_DATATYPE_INT + SQL_KEYWORD_PRIMARY_KEY + SQL_KEYWORD_AUTOINCREMENT + "," +
            PetEntry.COLUMN_PET_NAME + SQL_DATATYPE_TEXT + SQL_KEYWORD_NOT_NULL + "," +
            PetEntry.COLUMN_PET_BREED + SQL_DATATYPE_TEXT + "," +
            PetEntry.COLUMN_PET_GENDER + SQL_DATATYPE_INT + SQL_KEYWORD_NOT_NULL + SQL_KEYWORD_DEFAULT + PetEntry.DEFAULT_GENDER + "," +
            PetEntry.COLUMN_PET_WEIGHT + SQL_DATATYPE_INT + SQL_KEYWORD_NOT_NULL + SQL_KEYWORD_DEFAULT + PetEntry.DEFAULT_WEIGHT + ")";
    private static final String SQL_DELETE_ENTRY = "DROP TABLE IF EXISTS" + PetEntry.TABLE_NAME;


    public PetDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRY);
        onCreate(sqLiteDatabase);
    }
}
