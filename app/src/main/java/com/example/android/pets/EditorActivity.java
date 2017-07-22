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

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int PET_CURSOR_LOADER_ID = 0;
    private final int MODE_EDIT = 0;
    private final int MODE_ADD = 1;
    private final int NAVIGATE_UP = 0;
    private final int NAVIGATE_BACK = 1;
    private int mActivityMode;
    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;
    private int mGender;
    private Uri mPetUri;
    private boolean mPetAttributesChanged;
    private View.OnTouchListener mOnViewTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initializeMembers();
        setActivityMode();
        setActivityTitle();
        setupSpinner();
        if (mActivityMode == MODE_EDIT) {
            getSupportLoaderManager().initLoader(PET_CURSOR_LOADER_ID, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mActivityMode == MODE_ADD) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (arePetAttributesValid()) {
                    Uri petUri = savePet();
                    displayInsertionStatus(petUri);
                    finish();
                } else {
                    displayInsertionStatus(null);
                }
                return true;
            case R.id.action_delete:
                if (mActivityMode == MODE_EDIT) {
                    showDeletePetDialog();
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mPetAttributesChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    showUnsavedChangesDialog(NAVIGATE_UP);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mPetAttributesChanged) {
            super.onBackPressed();
            return;
        } else {
            showUnsavedChangesDialog(NAVIGATE_BACK);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mPetUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateActivityViews(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        populateActivityViews(null);
    }

    private boolean arePetAttributesValid() {
        String name = mNameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    private void initializeMembers() {
        this.mPetUri = getIntent().getData();
        this.mGender = PetEntry.DEFAULT_GENDER;
        this.mPetAttributesChanged = false;
        this.mOnViewTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPetAttributesChanged = true;
                return false;
            }
        };
        callActivityViews();
        setViewsListeners();
    }

    private void setActivityTitle() {
        if (mActivityMode == MODE_ADD) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
        } else if (mActivityMode == MODE_EDIT) {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
        }
    }

    private void setActivityMode() {
        if (mPetUri == null) {
            this.mActivityMode = MODE_ADD;
        } else {
            this.mActivityMode = MODE_EDIT;
        }
    }

    private void callActivityViews() {
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
    }

    private void setViewsListeners() {
        mGenderSpinner.setOnTouchListener(mOnViewTouchListener);
        mWeightEditText.setOnTouchListener(mOnViewTouchListener);
        mBreedEditText.setOnTouchListener(mOnViewTouchListener);
        mNameEditText.setOnTouchListener(mOnViewTouchListener);
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.DEFAULT_GENDER; // Unknown
            }
        });
    }

    private Uri savePet() {
        ContentValues petAttributes = getPetAttributesFromUserInput();
        if (mActivityMode == MODE_ADD) {
            return getContentResolver().insert(PetEntry.CONTENT_URI, petAttributes);
        } else {
            getContentResolver().update(mPetUri, petAttributes, null, null);
            return mPetUri;
        }
    }

    private ContentValues getPetAttributesFromUserInput() {
        ContentValues petAttributes = new ContentValues(4);

        petAttributes = insertName(petAttributes);
        petAttributes = insertBreed(petAttributes);
        petAttributes = insertGender(petAttributes);
        petAttributes = insertWeight(petAttributes);

        return petAttributes;
    }

    private void showUnsavedChangesDialog(final int navigationType) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.unsaved_changes_dialog_msg);
        alertDialogBuilder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (navigationType == NAVIGATE_UP) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                } else if (navigationType == NAVIGATE_BACK) {
                    finish();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDeletePetDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditorActivity.this);
        alertDialogBuilder.setMessage(R.string.delete_dialog_msg);
        alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int deletionStatus = deletePet();
                displayDeletionStatus(deletionStatus);
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void displayDeletionStatus(int deletionStatus) {
        if (deletionStatus == 0) {
            displayToast(getString(R.string.editor_delete_pet_failed));
        } else {
            displayToast(getString(R.string.editor_delete_pet_successful));
        }
    }

    private int deletePet() {
        if (mActivityMode == MODE_EDIT) {
            return getContentResolver().delete(mPetUri, null, null);
        }
        return 0;
    }

    private void displayInsertionStatus(Uri petUri) {
        if (petUri == null) {
            displayToast(getString(R.string.pet_not_saved));
        } else {
            displayToast(getString(R.string.pet_saved));
        }
    }

    private void displayToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void populateActivityViews(Cursor petCursor) {
        if (petCursor != null && petCursor.getCount() != 0) {
            petCursor.moveToNext();

            String name = petCursor.getString(petCursor.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            mNameEditText.setText(name);

            String breed = petCursor.getString(petCursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
            mBreedEditText.setText(breed);

            int gender = petCursor.getInt(petCursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
            mGenderSpinner.setSelection(gender, true);

            int weight = petCursor.getInt(petCursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT));
            mWeightEditText.setText(String.valueOf(weight));
        } else {
            mNameEditText.clearComposingText();
            mBreedEditText.clearComposingText();
            mWeightEditText.clearComposingText();
            mGenderSpinner.setSelection(0, true);
        }
    }

    private ContentValues insertName(ContentValues petAttributes) {
        String name = mNameEditText.getText().toString().trim();
        petAttributes.put(PetEntry.COLUMN_PET_NAME, name);
        return petAttributes;
    }

    private ContentValues insertBreed(ContentValues petAttributes) {
        String breed = mBreedEditText.getText().toString().trim();
        petAttributes.put(PetEntry.COLUMN_PET_BREED, breed);
        return petAttributes;
    }

    private ContentValues insertGender(ContentValues petAttributes) {
        petAttributes.put(PetEntry.COLUMN_PET_GENDER, mGender);
        return petAttributes;
    }

    private ContentValues insertWeight(ContentValues petAttributes) {
        String weight = mWeightEditText.getText().toString().trim();
        if (weight.isEmpty()) {
            weight = "0";
        }
        int weightInt = Integer.parseInt(weight);
        petAttributes.put(PetEntry.COLUMN_PET_WEIGHT, weightInt);
        return petAttributes;
    }
}