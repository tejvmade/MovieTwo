package com.example.movietwo.uiux;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.movietwo.R;
import com.example.movietwo.controller.AppController;
import com.example.movietwo.data.FavoriteDB;
import com.example.movietwo.interfaces.DBUpdateListener;
import com.example.movietwo.models.AllMovieReviewResponse;
import com.example.movietwo.models.AllVideoTrailerResponse;
import com.example.movietwo.models.Language;
import com.example.movietwo.models.Movie;
import com.example.movietwo.models.MovieReview;
import com.example.movietwo.models.VideoTrailer;
import com.example.movietwo.networking.DataManager;
import com.example.movietwo.networking.DataRequester;
import com.example.movietwo.util.AlertDialogUtil;
import com.example.movietwo.util.NetworkUtils;
import com.example.movietwo.util.PabloPicasso;
import com.example.movietwo.util.ProgressBarUtil;
import com.example.movietwo.util.SPManagerFavMovies;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.movietwo.data.FavoriteDB.ADDED_TO_FAVORITE;
import static com.example.movietwo.util.Constants.ARG_MOVIE_DETAIL;

public class DetailFragment extends Fragment implements View.OnClickListener, DBUpdateListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.coordinate_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolBar;
    @BindView(R.id.iv_backdrop)
    ImageView mIvBackDrop;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_release)
    TextView mTvReleaseDate;
    @BindView(R.id.tv_rating)
    TextView mTvRating;
    @BindView(R.id.tv_movie_overview)
    TextView mTvOverview;
    @BindView(R.id.iv_poster)
    ImageView mIvPoster;
    @BindView(R.id.fab_favorite)
    FloatingActionButton mButtonFavorite;
    @BindColor(R.color.colorPrimaryDark)
    int primaryDark;
    @BindView(R.id.fab_trailer)
    FloatingActionButton mButtonTrailer;
    @BindView(R.id.fab_share)
    FloatingActionButton mButtonShare;
    @BindView(R.id.review_layout0)
    CardView mReviewLayout0;
    @BindView(R.id.review_layout1)
    CardView mReviewLayout1;


    @BindString(R.string.no_internet_connection)
    String noInternetConnection;
    @BindString(R.string.something_went_wrong)
    String somethingWentWrong;
    @BindString(R.string.movie_trailers_dialog_title)
    String movieTrailerDialogTitle;
    @BindString(R.string.no_internet_connection_to_show_reviews)
    String noInternetConnectionToShowReviews;

    private SPManagerFavMovies mSPManagerFavMovies;
    private Movie mMovie;
    private ProgressBarUtil mProgressBar;
    private DataManager mDataMan;
    private int mViewId;
    private Activity mActivity;
    private boolean mTwoPane;

    public static DetailFragment newInstance(Bundle args) {
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle arg = getArguments();
            mMovie = arg.getParcelable(ARG_MOVIE_DETAIL);

            Log.d(TAG, "Received movie from getArguments() :  " + (mMovie != null ? mMovie.toString() : null));
        }

        mActivity = getActivity();
        mProgressBar = new ProgressBarUtil(mActivity);

        AppController app = ((AppController) mActivity.getApplication());
        mDataMan = app.getDataManager();
        mSPManagerFavMovies = SPManagerFavMovies.getInstance(mActivity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);

        if (getActivity().findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
        }
        setStatusBarColor(primaryDark);
        mCollapsingToolBar.setTitle(mMovie.getOriginalTitle());
        mCollapsingToolBar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mTvTitle.setText(mMovie.getOriginalTitle());

        String sourceDateStr = mMovie.getReleaseDate();
        SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        Date sourceDate = null;
        try {
            sourceDate = sourceDateFormat.parse(sourceDateStr);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        SimpleDateFormat finalDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String finalDateStr = finalDateFormat.format(sourceDate);

        mTvReleaseDate.setText(finalDateStr);
        mTvRating.setText(String.valueOf(mMovie.getVoteAverage()));
        mTvOverview.setText(mMovie.getOverview());

        PabloPicasso.with(mActivity).load(mMovie.getPosterPath()).fit()
                .placeholder(R.drawable.heart_filled)
                .error(R.drawable.heart_filled)
                .into(mIvPoster);

        PabloPicasso.with(mActivity).load(mMovie.getBackdropPath()).error(R.drawable.heart_filled).
                into(mIvBackDrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "applyPalette mTwoPane : " + mTwoPane);
                        if (!mTwoPane && isAdded()) {
                            Bitmap bitmap = ((BitmapDrawable) mIvBackDrop.getDrawable()).getBitmap();
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    applyPalette(palette);
                                }

                            });
                        }

                    }

                    @Override
                    public void onError() {

                    }
                });

        mButtonFavorite.setOnClickListener(this);
        mButtonTrailer.setOnClickListener(this);
        mButtonShare.setOnClickListener(this);


        if (mSPManagerFavMovies.getBoolean(mMovie.getId())) {
            mButtonFavorite.setImageResource(R.drawable.heart_filled);
        } else {
            mButtonFavorite.setImageResource(R.drawable.heart_empty);
        }

        return view;
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        int primary = getResources().getColor(R.color.colorPrimary);
        mCollapsingToolBar.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolBar.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        setStatusBarColor(palette.getDarkMutedColor(primaryDark));
    }

    private void setStatusBarColor(int darkMutedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = mActivity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(darkMutedColor);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //If your Fragment is added to existing activity
        //you should call the SnackBar's method into the onActivityCreated()
        // method of the Fragment.
        if (NetworkUtils.isOnline(mActivity)) {
            mProgressBar.show();
            Log.v(TAG, "Calling : get movie reviews api");
            mDataMan.getMovieReviews(
                    new WeakReference<>(mMovieReviewRequester), mMovie.getId(),
                    Language.LANGUAGE_EN.getValue(), TAG);

        } else {
            NetworkUtils.showSnackbar(mCoordinatorLayout, noInternetConnectionToShowReviews);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_favorite:

                FavoriteDB favouriteMovieDBTask = new FavoriteDB(mActivity, mMovie, this);
                favouriteMovieDBTask.execute();
                break;

            case R.id.fab_trailer:
                mViewId = R.id.fab_trailer;
                if (NetworkUtils.isOnline(mActivity)) {
                    mProgressBar.show();
                    Log.v(TAG, "Calling : get video trailer api");
                    mDataMan.getVideoTrailers(
                            new WeakReference<>(mVideoTrailerRequester), mMovie.getId(),
                            Language.LANGUAGE_EN.getValue(), TAG);

                } else {
                    NetworkUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
                }

                break;

            case R.id.fab_share:
                mViewId = R.id.fab_share;
                if (NetworkUtils.isOnline(mActivity)) {
                    mProgressBar.show();
                    Log.v(TAG, "Calling : get video trailer api to share the first trailer");
                    mDataMan.getVideoTrailers(
                            new WeakReference<>(mVideoTrailerRequester), mMovie.getId(),
                            Language.LANGUAGE_EN.getValue(), TAG);

                } else {
                    NetworkUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
                }
                break;

        }
    }

    @Override
    public void onSuccess(final int operationType) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String operation;
                if (operationType == ADDED_TO_FAVORITE) {
                    operation = "added to favorite";
                    mButtonFavorite.setImageResource(R.drawable.heart_filled);
                    mSPManagerFavMovies.putBoolean(mMovie.getId(), true);
                } else {
                    operation = "removed from favorite";
                    mButtonFavorite.setImageResource(R.drawable.heart_empty);
                    mSPManagerFavMovies.putBoolean(mMovie.getId(), false);
                }

                NetworkUtils.showSnackbar(mCoordinatorLayout, mMovie.getTitle() + " " + operation);
            }
        });
    }

    @Override
    public void onFailure() {
        NetworkUtils.showSnackbar(mCoordinatorLayout, mMovie.getTitle() + " " + somethingWentWrong);
    }


    public DataRequester mVideoTrailerRequester = new DataRequester() {

        @Override
        public void onFailure(Throwable error) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : video trailer onFailure");
            NetworkUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
        }

        @Override
        public void onSuccess(Object respObj) {
            if (!isAdded()) {
                return;
            }


            Log.v(TAG, "Success : video trailer data : " + new Gson().toJson(respObj));
            final AllVideoTrailerResponse response = (AllVideoTrailerResponse) respObj;


            if (response != null && response.getResults() != null && response.getResults().size() > 0) {
                final List<VideoTrailer> videoTrailerList = response.getResults();

                int noOfTrailers = videoTrailerList.size();
                String[] trailerNames = new String[noOfTrailers];
                for (int i = 0; i < noOfTrailers; i++) {
                    trailerNames[i] = videoTrailerList.get(i).getName();
                }
                switch (mViewId) {
                    case R.id.fab_trailer:
                        mProgressBar.hide();
                        AlertDialogUtil.createSingleChoiceItemsAlert(mActivity, movieTrailerDialogTitle,
                                trailerNames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        playVideoTrailer(videoTrailerList.get(which).getKey());
                                        dialog.dismiss();
                                    }
                                });
                        break;
                    case R.id.fab_share:
                        shareVideoTrailer(videoTrailerList.get(0).getKey());
                        break;

                }
            } else {
                mProgressBar.hide();
            }


        }
    };

    private void playVideoTrailer(String key) {
        Uri videoUri = Uri.parse(DataManager.BASE_URL_VIDEO + key);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(videoUri);


        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't play video trailer for key: " + key);
        }
    }

    private void shareVideoTrailer(String key) {
        String videoExtraText = DataManager.BASE_URL_VIDEO + key;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //This flag help you in returning to your app after any app handled the share intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, videoExtraText);

        Intent shareIntent = Intent.createChooser(intent, "Share trailer via");



        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            mProgressBar.hide();
            startActivity(shareIntent);
        } else {
            mProgressBar.hide();
            Log.d(TAG, "Couldn't share Video Trailer for key: " + key);
        }
    }

    private DataRequester mMovieReviewRequester = new DataRequester() {

        @Override
        public void onFailure(Throwable error) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : movie Reviews onFailure");
            NetworkUtils.showSnackbar(mCoordinatorLayout, noInternetConnectionToShowReviews);
        }

        @Override
        public void onSuccess(Object respObj) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Success : movie reviews data : " + new Gson().toJson(respObj));
            final AllMovieReviewResponse response = (AllMovieReviewResponse) respObj;


            if (response != null && response.getResults() != null && response.getResults().size() > 0) {

                ArrayList<MovieReview> mMovieReviewList = response.getResults();

                int noOfReviews = mMovieReviewList.size();

                if (noOfReviews >= 2) {
                    //We can fill two reviews
                    displayReviewLayout(0, mMovieReviewList.get(0));
                    displayReviewLayout(1, mMovieReviewList.get(1));
                } else {
                    //We can fill only one review
                    displayReviewLayout(0, mMovieReviewList.get(0));
                }
            }
        }
    };

    private void displayReviewLayout(int position, MovieReview movieReview) {
        CardView reviewLayout = null;
        if (position == 0) {
            reviewLayout = mReviewLayout0;
            reviewLayout.findViewById(R.id.tv_reviews_text).setVisibility(View.VISIBLE);
            reviewLayout.findViewById(R.id.line_reviews_heading).setVisibility(View.VISIBLE);
        } else if (position == 1) {
            reviewLayout = mReviewLayout1;
        }

        if (reviewLayout != null) {
            reviewLayout.setVisibility(View.VISIBLE);
            String author = movieReview.getAuthor();
            String content = movieReview.getContent();

            ((TextView) reviewLayout.findViewById(R.id.tv_review_author)).setText(author);
            ((TextView) reviewLayout.findViewById(R.id.tv_review_content)).setText(content);
        }
    }
}
