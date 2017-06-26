package com.example.movietwo.uiux;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.movietwo.R;
import com.example.movietwo.uiux.DetailFragment;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);if (savedInstanceState != null) {
            return;
        } else {
            // Add the Detail Fragment to the 'detail_container' FrameLayout
            addDetailFragment();
        }
    }

    public void addDetailFragment() {
        if (!isFinishing()) {
            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            DetailFragment detailFragment = DetailFragment.newInstance(getIntent().getExtras());

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }
}
