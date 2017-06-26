package com.example.movietwo.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.Uri;
import android.util.Log;

import com.example.movietwo.interfaces.DBUpdateListener;

public class FavoriteDB {

    private static final String TAG = FavoriteDB.class.getSimpleName();

    private Context mContext;
    private Movie mMovie;
    private DBUpdateListener mDBUpdateListener;

    public static final int ADDED_TO_FAVORITE = 1;
    public static final int REMOVED_FROM_FAVORITE = 2;

    public FavoriteDB(Context context, Movie movie, DBUpdateListener updateListener) {
        mDBUpdateListener = updateListener;
        mContext = context;
        mMovie = movie;
    }

    @Override
    protected Void doInBackground(Void... params) {
        deleteOrSaveFavoriteMovie();
        return null;
    }

    /**
     * Method to handle deletion of a favorite movie if it exists in the favourite movie database
     * or to insert it if doesn't exist
     */
    private void deleteOrSaveFavoriteMovie() {
        //Check if the movie with this movie_id  exists in the db

        Log.d(TAG, MovieContract.MovieEntry.CONTENT_URI.getAuthority());

        Cursor favMovieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovie.getId())},
                null);

        // If it exists, delete the movie with that movie id
        if (favMovieCursor.moveToFirst()) {
            int rowDeleted = mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(mMovie.getId())});

            if (rowDeleted > 0) {
                mDBUpdateListener.onSuccess(REMOVED_FROM_FAVORITE);
            } else {
                mDBUpdateListener.onFailure();
            }

        } else {
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues values = new ContentValues();

            //Then add the data, along with the corresponding name of the data type,
            //so the content provider knows what kind of value is being inserted.
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
            values.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, mMovie.getPosterPath());
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
            values.put(MovieContract.MovieEntry.COLUMN_AVERAGE_RATING, mMovie.getVoteAverage());
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            values.put(MovieContract.MovieEntry.COLUMN_BACKDROP_IMAGE, mMovie.getBackdropPath());


            // Finally, insert movie data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    values);

            // The resulting URI contains the ID for the row.  Extract the movie rowId from the Uri.
            long movieRowId = ContentUris.parseId(insertedUri);

            if (movieRowId > 0) {
                mDBUpdateListener.onSuccess(ADDED_TO_FAVORITE);
            } else {
                mDBUpdateListener.onFailure();
            }
        }
        favMovieCursor.close();
    }
}
