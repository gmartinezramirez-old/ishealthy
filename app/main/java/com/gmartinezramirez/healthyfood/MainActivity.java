package com.gmartinezramirez.healthyfood;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmartinezramirez.healthyfood.ai.CustomVisionAPI;
import com.gmartinezramirez.healthyfood.ai.CustomVisionPrediction;
import com.gmartinezramirez.healthyfood.ai.CustomVisionResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private ProgressDialog mDialog;
    private FoodAdapter mAdapter;
    private ArrayList<Food> mFoods;
    private File mPhotoFile;
    private FoodLab mFoodLab;
    private Food mFood;

    private int REQUEST_IMAGE_CAPTURE = 0;

    private static final String FILE_PROVIDER = "com.gmartinezramirez.healthyfood.fileprovider";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFoodLab = FoodLab.get(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFood = new Food();
                mPhotoFile = mFoodLab.getPhotoFile(mFood);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Uri uri = FileProvider.getUriForFile(MainActivity.this,
                        FILE_PROVIDER, mPhotoFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = MainActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    MainActivity.this.grantUriPermission(activity.activityInfo.toString(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

            }
        });
        updateUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            mDialog = ProgressDialog.show(this, "", getString(R.string.progress_dialog), true);

            Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER, mPhotoFile);
            this.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            try {
                PictureUtils.compressBitmap(mPhotoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            callAPI();
        }
    }

    private void printError() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.api_error), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.progress_dialog), true);
                        callAPI();
                    }
                });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    deleteFoodFileFromDisk(mFood);
                    updateUI();
                }
            }
        });

        snackbar.show();
    }

    public void updateUI() {
        mFoods = mFoodLab.getFoods();
        mEmptyView = (TextView) findViewById(R.id.no_foods);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (mFoods.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }

        if (mAdapter == null) {
            mAdapter = new FoodAdapter(mFoods);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
            ItemTouchHelper swipeToDismiss = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    Food food = mFoods.get(viewHolder.getAdapterPosition());
                    deleteFoodFileFromDisk(food);
                    updateUI();
                }
            });
            swipeToDismiss.attachToRecyclerView(mRecyclerView);
        } else {
            mAdapter.setFoods(mFoods);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_about) {
            return about();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean about() {
        AlertDialog.Builder aboutDialog = new AlertDialog.Builder(MainActivity.this);
        aboutDialog.setMessage(R.string.dialog_about)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
        return true;
    }

    private void deleteFoodFileFromDisk(Food food) {
        File photoFile = mFoodLab.getPhotoFile(food);
        Uri photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER, photoFile);
        getContentResolver().delete(photoUri, null, null);
        FoodLab.get(this).deleteFood(food.getId());
    }

    private class FoodHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTitleTextView;

        public FoodHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.card, parent, false));
            mTitleTextView = (TextView) itemView.findViewById(R.id.title);
            mImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }

        public void bind(Food food) {
            File photoFile = mFoodLab.getPhotoFile(food);
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
            mImageView.setImageBitmap(bitmap);
            mTitleTextView.setText(food.getTitle(MainActivity.this));

            if (!food.isFood()) {
                return;
            }

            if (food.isHealthy()) {
                mTitleTextView.setTextColor(getResources().getColor(R.color.healthyText));
            } else {
                mTitleTextView.setTextColor(getResources().getColor(R.color.junkText));
            }
        }
    }

    private class FoodAdapter extends RecyclerView.Adapter<FoodHolder> {
        private List<Food> mFoods;

        public FoodAdapter(List<Food> foods) {
            mFoods = foods;
        }

        @Override
        public FoodHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            return new FoodHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(FoodHolder holder, int position) {
            Food food = mFoods.get(position);
            holder.bind(food);
        }

        @Override
        public int getItemCount() {
            return mFoods.size();
        }

        public void setFoods(List<Food> foods) {
            mFoods = foods;
        }
    }
}
