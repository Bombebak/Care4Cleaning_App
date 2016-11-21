package com.example.den_f.mandatoryassignment_care4cleaning;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    public static final int REQUEST_CAMERA = 0;
    public static final int IMAGE_GALLERY_REQUEST = 20;
    public AsyncResponse delegate = null;
    public String pictureName = "";

    static final String TAG = "com.example.StateChange";
    String caseIdStr = "";
    String description = "";
    String command = "";
    String dialogCommandMsg = "";
    String picturePath;
    String ba64;
    String token = "token";
    String username;
    Bitmap photo;
    UploadToServer uploadToServer;
    View mLayout;
    ImageView imgPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.maincontent);

        mLayout = findViewById(R.id.mainContent);
        //get a reference to our components from xml
        final EditText editCaseID = (EditText) findViewById(R.id.txtCaseId);
        final EditText editDescription = (EditText) findViewById(R.id.txtDescription);
        ImageView btnPicture = (ImageView) findViewById(R.id.btnPic);
        Button btnUpload = (Button) findViewById(R.id.btnSubmit);
        ImageView btnFindPic = (ImageView) findViewById(R.id.btnFindPic);
        imgPicture = (ImageView) findViewById(R.id.imagePrev);
        imgPicture.setImageResource(R.drawable.image_placeholder);


        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        Log.e("token ", "-----" + token);
        if (prefs.contains("token")) {
            token = prefs.getString("token", "");
        }
        else {

            createUser();
        }



        btnFindPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFiles();
            }
        });

        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCamera();
            }
        });

        //add a click listener to our save button
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                caseIdStr = editCaseID.getText().toString();
                description = editDescription.getText().toString();
                if (isEmpty(editCaseID)) {
                    Toast.makeText(MainActivity.this, R.string.caseIdEmpty, Toast.LENGTH_SHORT).show();
                    editCaseID.requestFocus();
                }
                if (isEmpty(editDescription)) {
                    Toast.makeText(MainActivity.this, R.string.descriptionEmpty, Toast.LENGTH_SHORT).show();
                    editDescription.requestFocus();
                }

                if (isImageEmpty(photo)) {
                    Toast.makeText(MainActivity.this, R.string.imageEmpty, Toast.LENGTH_SHORT).show();
                }
                else if (!isEmpty(editCaseID) && !isEmpty(editDescription) && photo != null) {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 90, bao);
                    byte[] ba = bao.toByteArray();
                    ba64 = Base64.encodeToString(ba, Base64.NO_WRAP);
                    pictureName = new StringBuilder("").append("cid").append(caseIdStr+"_").append(System.currentTimeMillis()).append(".png").toString();


                    dialogCommandMsg = "Wait image is uploading!";
                    command = "imageUpload";
                    uploadToServer = new UploadToServer(MainActivity.this, MainActivity.this);
                    uploadToServer.setupSSLCertificate();
                    uploadToServer.execute(command, caseIdStr, ba64, token, description, pictureName);


                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    //This method is called before our activity is created
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //ALWAYS CALL THE SUPER METHOD
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");

		/* Here we put code now to save the state */
        final EditText editCaseID = (EditText) findViewById(R.id.txtCaseId);
        final EditText editDescription = (EditText) findViewById(R.id.txtDescription);
        final ImageView imgPicture = (ImageView) findViewById(R.id.imagePrev);
        BitmapDrawable drawable = (BitmapDrawable) imgPicture.getDrawable();

        CharSequence caseIdStr = editCaseID.getText();
        CharSequence description = editDescription.getText();
        Bitmap bitmap = drawable.getBitmap();

        outState.putCharSequence("editCaseID", caseIdStr);
        outState.putCharSequence("editDescription", description);
        outState.putParcelable("image", bitmap);
    }


    //this is called when our activity is recreated, but
    //AFTER our onCreate method has been called
    //EXTREMELY IMPORTANT DETAIL
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        //MOST UI elements will automatically store the information
        //if we call the super.onRestoreInstaceState but other data will be lost.
        super.onRestoreInstanceState(savedState);
        Log.i(TAG, "onRestoreInstanceState");


		/*Here we restore any state */
        mLayout = findViewById(R.id.mainContent);
        //get a reference to our components from xml
        final EditText editCaseID = (EditText) findViewById(R.id.txtCaseId);
        final EditText editDescription = (EditText) findViewById(R.id.txtDescription);
        CharSequence caseID = savedState.getCharSequence("editCaseID");
        CharSequence description = savedState.getCharSequence("editDescription");
        Bitmap bitmap = savedState.getParcelable("image");
        editCaseID.setText(caseID);
        editDescription.setText(description);
        imgPicture.setImageBitmap(bitmap);


        //imgPicture.setImageResource(R.drawable.image_placeholder);
        /*int test = 0;
        test = savedState.getInt("imgPicture");
        imgPicture.setImageResource(test);*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                /*Snackbar.make(mLayout, R.string.permision_available_camera,
                        Snackbar.LENGTH_SHORT).show();*/
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                /*Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();*/

            }
            // END_INCLUDE(permission_result)

        }
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Image gallery permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "Image gallery permission has now been granted. Showing preview.");
                Snackbar.make(mLayout, R.string.permision_available_camera,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Image gallery permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (picturePath != null) {
                    //decoding the file into a bitmap
                    photo = BitmapFactory.decodeFile(picturePath);
                    //setting the bitmap on the image file.
                    imgPicture.setImageBitmap(photo);
                }
            }
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // if we are here, we are hearing back from the image gallery.

                // the address of the image on the SD Card.
                Uri imageUri = data.getData();

                picturePath = imageUri.getPath();


                // declare a stream to read the image data from the SD Card.
                InputStream inputStream;

                // we are getting an input stream, based on the URI of the image.
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // get a bitmap from the stream.
                    photo = BitmapFactory.decodeStream(inputStream);

                    // show the image to the user
                    imgPicture.setImageBitmap(photo);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // show a message to the user indictating that the image is unavailable.
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }

            }
        }

    }

    public void createUser() {
            OkCancelInputDialog dialog = new OkCancelInputDialog(this,"Create user","Choose a username")
            {
                @Override
                public void clickOk() {
                    dialogCommandMsg = "Wait user is being created!";
                    command = "createUser";
                    uploadToServer = new UploadToServer(MainActivity.this, MainActivity.this);
                    uploadToServer.setupSSLCertificate();
                    uploadToServer.execute(command, getUserInput(), token+= System.currentTimeMillis());
                    super.clickOk();

                }
            };
            dialog.show();

        }



    private void clickpic() {
        // Check camera availability
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Open default camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
            try {
                f = setUpPhotoToFile();
                picturePath = f.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

            } catch (IOException e) {
                e.printStackTrace();
                f = null;
                picturePath = null;
            }
            // start the image capture Intent
            startActivityForResult(intent, REQUEST_CAMERA);

        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showCamera() {
        Log.i(TAG, "Show camera button pressed. Checking permission.");
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestCameraPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");
            //showCameraPreview();
            clickpic();
        }
        // END_INCLUDE(camera_permission)

    }

    public void showFiles() {
        Log.i(TAG, "Show files button pressed. Checking permission.");
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestGalleryPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");

            onImageGalleryClicked();
        }
        // END_INCLUDE(camera_permission)

    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    /**
     * Requests the Image gallery permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestGalleryPermission() {
        Log.i(TAG, "Image Gallery permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying image gallery permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    IMAGE_GALLERY_REQUEST);
                        }
                    })
                    .show();
        } else {

            // Image gallery permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    IMAGE_GALLERY_REQUEST);
        }
        // END_INCLUDE(image_gallery_request)
    }

    /**
     * This method is for finding existing images on the phones SD card
     */
    public void onImageGalleryClicked() {
        // invoke the image gallery using an implict intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }


    //Used to check if an editText is empty
    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
    private boolean isImageEmpty(Bitmap photo) {
        return photo == null;
    }

    public void clearFields() {
        final EditText editCaseID = (EditText) findViewById(R.id.txtCaseId);
        final EditText editDescription = (EditText) findViewById(R.id.txtDescription);
        imgPicture = (ImageView) findViewById(R.id.imagePrev);
        editCaseID.setText("");
        editDescription.setText("");
        imgPicture.setImageResource(R.drawable.image_placeholder);
    }

    private File setUpPhotoToFile() throws IOException {
        File f = createImageFile();
        picturePath = f.getAbsolutePath();
        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        picturePath = image.getAbsolutePath();
        return image;
    }
    public String getDialogMessage() {
        return dialogCommandMsg;
    }



}
