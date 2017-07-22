package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by Bassam on 7/12/2017.
 */
public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    public static final int PETS = 100;
    public static final int PETS_ID = 101;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        mUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    private PetDbHelper mPetDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mPetDbHelper = new PetDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;

        SQLiteDatabase database = mPetDbHelper.getReadableDatabase();

        int operationUriCode = mUriMatcher.match(uri);

        switch (operationUriCode) {
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{Long.toString(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query this URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        checkInsertPetValuesValidity(contentValues);

        int operationUriCode = mUriMatcher.match(uri);

        switch (operationUriCode) {
            case PETS:
                return insertPet(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion not supported for this uri: " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.size() == 0) {
            return 0;
        }

        checkUpdatePetsValidity(contentValues);

        int operationCode = mUriMatcher.match(uri);

        switch (operationCode) {
            case PETS:
                return updatePets(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePets(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for: " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int operationCode = mUriMatcher.match(uri);

        switch (operationCode) {
            case PETS:
                return deletePets(uri, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deletePets(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion not supported for: " + uri);
        }

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {

        int operationCode = mUriMatcher.match(uri);

        switch (operationCode) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {

        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        long id = database.insert(PetEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Error inserting the row for uri: " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private int updatePets(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri, null);
        return database.update(PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    private int deletePets(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri, null);
        return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
    }

    private void checkInsertPetValuesValidity(ContentValues contentValues) {

        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        int gender = contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        int weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);

        checkNameValidity(name);
        checkGenderValidity(gender);
        checkWeightValidity(weight);

    }

    private void checkUpdatePetsValidity(ContentValues contentValues) {

        if (contentValues.containsKey(PetEntry.COLUMN_PET_NAME)) {
            checkNameValidity(contentValues.getAsString(PetEntry.COLUMN_PET_NAME));
        }
        if (contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            checkGenderValidity(contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER));
        }
        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            checkWeightValidity(contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT));
        }
    }

    private void checkNameValidity(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        }
    }

    private void checkGenderValidity(int gender) {
        if (!PetEntry.isGenderValid(gender)) {
            throw new IllegalArgumentException("Invalid gender integer!");
        }
    }

    private void checkWeightValidity(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be a negative value!");
        }
    }
}