package com.example.pawan.whatsAppcleaner.tabs.Voice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawan.whatsAppcleaner.adapters.innerAdapeters.InnerDetailsAdapter_audio;
import com.example.pawan.whatsAppcleaner.datas.FileDetails;
import com.example.pawan.whatsAppcleaner.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


public class voice extends AppCompatActivity implements  InnerDetailsAdapter_audio.OnCheckboxListener {


    RecyclerView recyclerView;
    Button button;
    private InnerDetailsAdapter_audio innerDetailsAdapterAudio;
    private ArrayList<FileDetails> innerdatalist = new ArrayList<>();

    private AdView mAdView;
    private static final long GiB = 1024 * 1024 * 1024;
    private static final long MiB = 1024 * 1024;
    private static final long KiB = 1024;
    private ArrayList<FileDetails> filesToDelete = new ArrayList<>();
    private TextView no_ads;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doc_activity);

        recyclerView = findViewById(R.id.recycler_view);
        button = findViewById(R.id.delete);
        ImageView no_files = findViewById(R.id.nofiles);
        no_ads = findViewById(R.id.ads_not_loaded);
        mAdView = findViewById(R.id.adView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(voice.this)
                        .setMessage("Are you sure you want to delete selected files?")
                        .setCancelable(true)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int success = -1;
                                ArrayList<FileDetails> deletedFiles = new ArrayList<>();

                                for (FileDetails details : filesToDelete) {
                                    File file = new File(details.getPath());
                                    if (file.exists()) {
                                        if (file.delete()) {
                                            deletedFiles.add(details);
                                            if (success == 0) {
                                                return;
                                            }
                                            success = 1;
                                        } else {
                                            Log.e("TEST", "" + file.getName() + " delete failed");
                                            success = 0;
                                        }
                                    } else {
                                        Log.e("TEST", "" + file.getName() + " doesn't exists");
                                        success = 0;
                                    }
                                }

                                filesToDelete.clear();

                                for (FileDetails deletedFile : deletedFiles) {
                                    innerdatalist.remove(deletedFile);
                                }
                                innerDetailsAdapterAudio.notifyDataSetChanged();
                                if (success == 0) {
                                    Toast.makeText(voice.this, "Couldn't delete some files", Toast.LENGTH_SHORT).show();
                                } else if (success == 1) {
                                    Toast.makeText(voice.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                                button.setText(R.string.delete_items_blank);
                                button.setTextColor(Color.parseColor("#A9A9A9"));
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Network",0);
        boolean status = sharedPreferences.getBoolean("Status",false);

        if (status){
            no_ads.setVisibility(View.INVISIBLE);


            mAdView.loadAd(new AdRequest.Builder().addTestDevice("623B1B7759D51209294A77125459D9B7").build());

            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdClosed() {
                    if (mAdView.isLoading()){
                        mAdView.loadAd(new AdRequest.Builder().addTestDevice("623B1B7759D51209294A77125459D9B7").build());
                        no_ads.setVisibility(View.VISIBLE);
                        no_ads.setText(R.string.no_ads);
                    }

                }
            });

            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    Log.e("Loaded","Loaded");
                }
            });

            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdFailedToLoad(int i) {
                    Log.e("Bannercode", String.valueOf(i));
                    no_ads.setVisibility(View.VISIBLE);
                    no_ads.setText(R.string.no_ads);
                }
            });

        }
        else {
            no_ads.setVisibility(View.VISIBLE);
            no_ads.setText(R.string.no_ads);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Need to ask permission again or close the app
        } else {
            String path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Voice Notes";


            File directory = new File(path);

            ArrayList<FileDetails> fileList1 = new ArrayList<>();

            File[] results = directory.listFiles();
            if (results != null) {
                for (File file : results) {
                    if (file.isDirectory()) {
                        File[] res = file.listFiles();
                        Log.e("Files", String.valueOf(res.length));

                        for (File re : res) {

                            FileDetails fileDetails = new FileDetails();
                            fileDetails.setName(re.getName());
                            fileDetails.setPath(re.getPath());
                            fileDetails.setImage(R.drawable.voice);
                            fileDetails.setColor(R.color.orange);
                            fileDetails.setSize("" + getFileSize(re));
                            innerdatalist.add(fileDetails);
                        }
                    }
                }
               /// innerdatalist = fileList1;
                Log.e("Files", "files found: " + fileList1.toString());
            } else { Log.e("Files", "No files found in " + directory.getName());
            }
            if (innerdatalist.isEmpty()){
                no_files.setVisibility(View.VISIBLE);
                no_files.setImageResource(R.drawable.file);
            }
        }
        innerDetailsAdapterAudio = new InnerDetailsAdapter_audio(this, innerdatalist, this);
        recyclerView.setAdapter(innerDetailsAdapterAudio);
    }

    @Override
    public void onCheckboxClicked(View view, ArrayList<FileDetails> pos) {
        filesToDelete.clear();

        for (FileDetails details : pos) {
            if (details.isSelected()) {
                filesToDelete.add(details);
            }
        }

        if (filesToDelete.size() > 0) {

            long totalFileSize = 0;

            for (FileDetails details : filesToDelete) {
                File file = new File(details.getPath());
                totalFileSize += file.length();
            }

            String size = Formatter.formatShortFileSize(voice.this, totalFileSize);
            button.setText("Delete Selected Items (" + size + ")");
            button.setTextColor(Color.parseColor("#C103A9F4"));
        } else {
            button.setText(R.string.delete_items_blank);
            button.setTextColor(Color.parseColor("#A9A9A9"));
        }
    }
    private String getFileSize(File file) {
        NumberFormat format = new DecimalFormat("#.##");
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        final double length = file.length();

        if (file.isFile()) {
            if (length > GiB) {
                return format.format(length / GiB) + " GB";
            } else if (length > MiB) {
                return format.format(length / MiB) + " MB";
            } else if (length > KiB) {
                return format.format(length / KiB) + " KB";
            }else
                return format.format(length) + " B";
        } else {
        }
        return "";
    }

}