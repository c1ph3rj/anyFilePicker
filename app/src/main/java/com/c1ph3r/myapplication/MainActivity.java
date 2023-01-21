package com.c1ph3r.myapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Initialize variable
    Button btSelect;
    TextView tvUri, tvPath;
    String vala;
    ActivityResultLauncher<Intent> resultLauncher;

    String sPath;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // assign variable
        btSelect = findViewById(R.id.bt_select);
        tvUri = findViewById(R.id.tv_uri);

        // Initialize result launcher
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(
                            ActivityResult result) {
                        // Initialize result data
                        Intent data = result.getData();

                        // check condition
                        if (data != null) {
                            // When data is not equal to empty
                            // Get PDf uri
                            Uri sUri = data.getData();
                            File destinationDirectory=new File(getFilesDir() + "/files/documents");
                            if(!destinationDirectory.exists()){
                                destinationDirectory.mkdirs();
                            }
                            try {
                                DocumentFile pickedFile = DocumentFile.fromSingleUri(MainActivity.this, sUri);
                                File destination = new File(destinationDirectory + "/" + Objects.requireNonNull(pickedFile).getName());
                                Log.d("Destination is ", destination.toString());

                                tvUri.setText(pickedFile.getName());

                                ContentResolver cr = getContentResolver();
                                InputStream inputStream = cr.openInputStream(sUri);

                                FileOutputStream outputStream = new FileOutputStream(destination);
                                byte[] buffer = new byte[1024];
                                int read;
                                while ((read = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, read);
                                }
                                outputStream.flush();
                                outputStream.close();
                                inputStream.close();

                                tvUri.setOnClickListener(onClickTvUri -> {
                                    openFile(destination, pickedFile.getType());
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                });

        // Set click listener on button
        btSelect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check condition
                        if (ActivityCompat.checkSelfPermission(
                                MainActivity.this,
                                Manifest.permission
                                        .READ_EXTERNAL_STORAGE)
                                != PackageManager
                                .PERMISSION_GRANTED) {
                            // When permission is not granted
                            // Result permission
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{
                                            Manifest.permission
                                                    .READ_EXTERNAL_STORAGE},
                                    1);
                        } else {
                            // When permission is granted
                            // Create method
                            selectPDF();
                        }
                    }
                });
    }

    private void openFile(File fileFromList, String type) {
        try {
            // Get the File location and file name.
            File file = fileFromList;
            Log.d("pdfFIle", "" + file);
            // Get the URI Path of file.
            Uri uriPdfPath = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName()+".fileprovider", file);
            // Start Intent to View PDF from the Installed Applications.
            Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
            pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenIntent.setClipData(ClipData.newRawUri("", uriPdfPath));
            pdfOpenIntent.setDataAndType(uriPdfPath, type);
            pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                startActivity(pdfOpenIntent);
            } catch (ActivityNotFoundException activityNotFoundException) {
                Toast.makeText(MainActivity.this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void selectPDF() {
        // Initialize intent
        Intent intent
                = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // set type
        intent.setType("*/*");
        // Launch intent
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        resultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        // check condition
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            // When permission is granted
            // Call method
            selectPDF();
        } else {
            // When permission is denied
            // Display toast
            Toast
                    .makeText(getApplicationContext(),
                            "Permission Denied",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }


}
