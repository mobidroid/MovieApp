package com.hasan.android.movieapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hasan.android.movieapp.Adapter.ImageAdapter;
import com.hasan.android.movieapp.DataParser.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getSimpleName();

    public MovieListFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_movie_list, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            //check if network connection is there, otherwise return error

            if (isNetworkAvailable()) {
                // fetch data
                new DiscoverMoviesTask().execute();
            } else {
                // display error
                openAlert();

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void  openAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());

        builder.setMessage("Your WiFi is not enabled.");
        builder.setPositiveButton("Enable WiFi",
            new DialogInterface.OnClickListener() {
            @Override
                public void onClick(
                    final DialogInterface dialogInterface, final int i) {
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                     }
            });
        builder.setNegativeButton("Continue without WiFi", null);
        builder.create().show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        if (isNetworkAvailable()) {
            // fetch data
            new DiscoverMoviesTask().execute();
        } else {
            // display error
            openAlert();
        }


        return rootView;
    }


    private class DiscoverMoviesTask extends AsyncTask<Void, Void, JSONObject> {


        private final String LOG_TAG=this.getClass().getSimpleName();

        private final String EXTRA_MESSAGE="MovieDetails";

        ProgressDialog myPd_bar;

        @Override
        protected void onPreExecute() {

            myPd_bar=new ProgressDialog(getActivity());
            myPd_bar.setMessage("Loading....");
            myPd_bar.setTitle("Fetching Movies");
            myPd_bar.show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            // URL for calling the API is needed
            String baseURL="api.themoviedb.org";

            // API Key I am using to build this APP
            String apiKey="499f158bf8fd92d83ec15bd47fb82fbc";

            //SharedPreference for the Selection
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by=sharedPref.getString(getString(R.string.pref_sort_title), null);

            // Uri
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(baseURL)
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("sort_by",sort_by);
            String url=builder.toString();
            //fetch URL data
            //String result=downloadUrl(url);
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
            JSONObject json = jParser.getJSONFromUrl(url);

            return json; //the return value will be used by onPostExecute to update UI
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(final JSONObject json) {
            myPd_bar.dismiss();

            List<String> poster_paths=new ArrayList<String>();
            JSONArray movies_list_array;
            try{

                movies_list_array = json.getJSONArray("results");
                for(int i=0;i<movies_list_array.length();i++){
                    JSONObject movie = movies_list_array.getJSONObject(i);
                    poster_paths.add(movie.getString("poster_path"));
                }
            }catch(JSONException e){
                Log.e(LOG_TAG, "Error parsing JSON:", e);
            }

           GridView gridview = (GridView) getActivity().findViewById(R.id.movies_list_grid);
            gridview.setAdapter(new ImageAdapter(getActivity(),poster_paths));

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {

                    //getting the movie details for the selected item
                    try {
                        JSONObject movieDetails= json.getJSONArray("results").getJSONObject(position);
                        Intent intent = new Intent(getActivity(), MovieDetails.class);
                        intent.putExtra(EXTRA_MESSAGE, movieDetails.toString());
                        startActivity(intent);
                    }catch (JSONException e){
                        Log.e(LOG_TAG,"Error parsing JSON: ",e);
                    }
                }
            });

        }
    }
}
