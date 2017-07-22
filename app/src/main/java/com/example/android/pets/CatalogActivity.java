/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.adapters.PetCursorAdapter;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int PETS_CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = getClass().getSimpleName();
    private PetCursorAdapter petCursorAdapter;
    private ListView mPetsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        this.petCursorAdapter = new PetCursorAdapter(this, null);
        getSupportLoaderManager().initLoader(PETS_CURSOR_LOADER_ID, null, this);
        initializeActivityViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                addDummyPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllPetsDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PetEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        petCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        petCursorAdapter.swapCursor(null);
    }

    private void showDeleteAllPetsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CatalogActivity.this);
        alertDialogBuilder.setMessage("Delete all pets?");
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface!=null){
                    dialogInterface.dismiss();
                }
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllPets();
            }
        });
        AlertDialog deleteAllPetsAlertDialog = alertDialogBuilder.create();
        deleteAllPetsAlertDialog.show();
    }
    private void deleteAllPets() {
        getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
    }

    private void addDummyPet() {
        ContentValues petValues = makeDummyPetValues();
        getContentResolver().insert(PetEntry.CONTENT_URI, petValues);
    }

    private ContentValues makeDummyPetValues() {
        ContentValues petValues = new ContentValues();

        petValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        petValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        petValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        petValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        return petValues;
    }

    private void initializeActivityViews() {
        initializeFAB();
        initializeListView();
    }

    private void initializeListView() {
        mPetsListView = (ListView) findViewById(R.id.pets_list_view);
        mPetsListView.setAdapter(petCursorAdapter);
        mPetsListView.setEmptyView(findViewById(R.id.view_empty_list));
        mPetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri petUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                openEditorActivity(petUri);
            }
        });
    }

    private void initializeFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditorActivity(null);
            }
        });
    }

    private void openEditorActivity(Uri petUri) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        intent.setData(petUri);
        startActivity(intent);
    }
}
