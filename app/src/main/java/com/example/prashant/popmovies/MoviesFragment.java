package com.example.prashant.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.prashant.popmovies.ImageAdapter;
import com.example.prashant.popmovies.MainActivity;
import com.example.prashant.popmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public  class MoviesFragment extends Fragment {

    static GridView gridview;
    static int width;
    static boolean sortByPop = true;
    static String api_key = "b7f57ee32644eb6ddfdca9ca38b5513e";
    static boolean sortByFavorites;
    HttpURLConnection urlConnection;

    static ArrayList<String> fposters;
    static ArrayList<String> foverview;
    static ArrayList<String> fdate;
    static ArrayList<String> fratings;
    static ArrayList<String> fyoutube;
    static ArrayList<String> fyoutube1;
    static ArrayList<String> ftitle;
    static ArrayList<ArrayList<String>> fcomments;

    static ArrayList<String> overviews;
    static ArrayList<String> titles;
    static ArrayList<String> dates;
    static ArrayList<String> ratings;
    static ArrayList<String> youtube;
    static ArrayList<String> youtube1;
    static ArrayList<String> ids;
    static ArrayList<String> posters;
    static ArrayList<Boolean> favorited;
    static ArrayList<ArrayList<String>> comments;


    public MoviesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        //It is a interface that apps use to talk or interact with the window manager
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();    // Point hold two interger coordinates
        display.getSize(size);

        //to set the poster in gridview
        // 3 poster in each row if table and 2 for the mobile phone
        if (MainActivity.TABLET) {
            width = size.x / 3;
        } else width = size.x / 2;


        if (getActivity() != null) {
            ArrayList<String> array = new ArrayList<String>();
            ImageAdapter adapter = new ImageAdapter(getActivity(), array, width);
            gridview = (GridView) rootView.findViewById(R.id.gridview_poster);

            gridview.setColumnWidth(width);

            //set the posters on gridview optained from imageAdapter class
            gridview.setAdapter(adapter);

        }
        //listen for presses on gridview items
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("THERE IS THE POSITION :", position + "");

                if (!sortByFavorites){
                    favorited = bindFavoritesToMovies();

                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra("overview", overviews.get(position))
                            .putExtra("poster", posters.get(position))
                            .putExtra("title", titles.get(position))
                            .putExtra("date", dates.get(position))
                            .putExtra("rating", ratings.get(position))
                            .putExtra("youtube", youtube.get(position))
                            .putExtra("youtube1", youtube1.get(position))
                            .putExtra("comments", comments.get(position))
                            .putExtra("favorite", favorited.get(position));

                    startActivity(intent);

                }

                else {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra("overview", foverview.get(position))
                            .putExtra("poster", fposters.get(position))
                            .putExtra("title", ftitle.get(position))
                            .putExtra("date", fdate.get(position))
                            .putExtra("rating", fratings.get(position))
                            .putExtra("youtube", fyoutube.get(position))
                            .putExtra("youtube1", fyoutube1.get(position))
                            .putExtra("comments", fcomments.get(position))
                            .putExtra("favorite", favorited.get(position));
                    startActivity(intent);
                }



            }
        });

        return rootView;
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            gridview.setAdapter(null);
            onStart();
        }
    }

    public ArrayList<Boolean> bindFavoritesToMovies() {

        ArrayList<Boolean> result = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            result.add(false);
        }

        for (String favoritedTitles : ftitle ) {

            for (int j = 0; j < titles.size(); j++) {

                if (favoritedTitles.equals(titles.get(j))){
                    result.set(j, true);
                }
            }
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        PreferenceChangeListener listener = new PreferenceChangeListener();
        prefs.registerOnSharedPreferenceChangeListener(listener);
        ;

        if (prefs.getString("sortby", "popularity").equals("popularity")) {
            getActivity().setTitle("Most Popular Movies");
            sortByPop = true;
            sortByFavorites = false;
        } else if (prefs.getString("sortby", "rating").equals("rating")) {
            getActivity().setTitle("Highest Rated Movies");
            sortByPop = false;
            sortByFavorites = false;
        } else if (prefs.getString("sortby", "favorities").equals("favorities")) {
            getActivity().setTitle("Favorited Movies");
            sortByPop = false;
            sortByFavorites = true;
        }

        TextView textView = new TextView(getActivity());
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.linearlayout);
        loadFavoritesteData();
        if (sortByFavorites) {

            if (fposters.size() == 0) {

                textView.setText("You have no Favorite Movies Selected Yet");
                if (layout.getChildCount() == 1) {
                    layout.addView(textView);
                    gridview.setVisibility(GridView.GONE);
                }
            } else {
                gridview.setVisibility(GridView.VISIBLE);
                layout.removeView(textView);
            }

            if (fposters != null && getActivity() != null) {
                ImageAdapter adapter = new ImageAdapter(getActivity(), fposters, width);
                gridview.setAdapter(adapter);
            }
        } else {
            gridview.setVisibility(GridView.VISIBLE);
            layout.removeView(textView);


            if (isNetworkAvailable()) {
                //gridview.setVisibility(GridView.VISIBLE);
                new ImageLoadTask().execute();
            } else {
                //Creating a new textview and setting the gridview invisible , if net is not available
                TextView textview1 = new TextView(getActivity());
                LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.linearlayout);
                textview1.setText("You are not connected to the Internet");
                if (layout1.getChildCount() == 1) {
                    layout1.addView(textview1);
                }
                gridview.setVisibility(GridView.GONE);
            }
        }

    }
    public void loadFavoritesteData() {
        String URL = "content://com.example.prashant.Movies/movies";
        Uri movies = Uri.parse(URL);
        Cursor c = getActivity().getContentResolver().query(movies, null, null, null,"title");

        fposters = new ArrayList<String>();
        foverview = new ArrayList<String>();
        fratings = new ArrayList<String>();
        ftitle = new ArrayList<String>();
        fyoutube = new ArrayList<String>();
        fyoutube1 = new ArrayList<String>();
        fdate = new ArrayList<String>();
        favorited =  new ArrayList<Boolean>();
        fcomments = new ArrayList<ArrayList<String>>();

        if (c == null) return;

        while (c.moveToNext()) {
            fposters.add(c.getString(c.getColumnIndex(MovieProvider.NAME)));
            foverview.add(c.getString(c.getColumnIndex(MovieProvider.OVERVIEW)));
            fdate.add(c.getString(c.getColumnIndex(MovieProvider.DATE)));
            ftitle.add(c.getString(c.getColumnIndex(MovieProvider.TITLE)));
            fratings.add(c.getString(c.getColumnIndex(MovieProvider.RATING)));
            fyoutube.add(c.getString(c.getColumnIndex(MovieProvider.YOUTUBE)));
            fyoutube1.add(c.getString(c.getColumnIndex(MovieProvider.YOUTUBE1)));
            fcomments.add(convertStringToArrayList(c.getString(c.getColumnIndex(MovieProvider.REVIEW))));
            favorited.add(true);
        }

    }

    public ArrayList<String> convertStringToArrayList(String s){
        ArrayList<String> result = new ArrayList<>(Arrays.asList(s.split("@divider@")));
        return result;
    }

    public boolean isNetworkAvailable() {
        //It is a class that answer all the queries about the os network connectivity.
        //also notifies app when connection changes
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        //To get the instance of current network connection
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private final String LOG_TAG = ImageLoadTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            while (true) {
                try {
                    //storing posters into an arraylist poster
                    posters = new ArrayList(Arrays.asList(getPathsFromAPI(sortByPop)));
                    return posters;
                } catch (Exception e) {
                    continue;
                }
            }

        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null && getActivity() != null) {
                ImageAdapter adapter = new ImageAdapter(getActivity(), result, width);
                gridview.setAdapter(adapter);

            }
        }

        public String[] getPathsFromAPI(boolean sortbypop)
        {
            while(true)
            {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JSONResult;

                try {
                    String urlString = null;
                    if (sortbypop) {
                        urlString = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + api_key;
                    } else {
                        urlString = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&vote_count.gte=500&api_key=" + api_key;
                    }
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    //Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    JSONResult = buffer.toString();

                    try {
                        overviews = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult,"overview")));
                        titles = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult,"original_title")));
                        ratings = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult,"vote_average")));
                        dates = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult,"release_date")));
                        ids = new ArrayList<String>(Arrays.asList(getStringsFromJSON(JSONResult,"id")));
                        while(true)
                        {
                            youtube = new ArrayList<String>(Arrays.asList(getYoutubesFromIds(ids,0)));
                            youtube1 = new ArrayList<String>(Arrays.asList(getYoutubesFromIds(ids,1)));
                            int nullCount = 0;
                            for(int i = 0; i<youtube.size();i++)
                            {
                                if(youtube.get(i)==null)
                                {
                                    nullCount++;
                                    youtube.set(i,"no video found");
                                }
                            }
                            for(int i = 0; i<youtube1.size();i++)
                            {
                                if(youtube1.get(i)==null)
                                {
                                    nullCount++;
                                    youtube1.set(i,"no video found");
                                }
                            }
                            if(nullCount>2)continue;
                            break;
                        }
                        comments = getReviewsFromIds(ids);
                        return getPathsFromJSON(JSONResult);

                    } catch (JSONException e) {
                        return null;
                    }
                }catch(Exception e)
                {
                    continue;
                }finally {
                    if(urlConnection!=null)
                    {
                        urlConnection.disconnect();
                    }
                    if(reader!=null)
                    {
                        try{
                            reader.close();
                        }catch(final IOException e)
                        {
                        }
                    }
                }


            }

        }
        public String[] getYoutubesFromIds(ArrayList<String> ids, int position)
        {
            String[] results = new String[ids.size()];
            for(int i =0; i<ids.size(); i++)
            {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String JSONResult;

                try {
                    String urlString = null;
                    urlString = "http://api.themoviedb.org/3/movie/" + ids.get(i) + "/videos?api_key=" + api_key;


                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    //Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    JSONResult = buffer.toString();
                    try {
                        results[i] = getYoutubeFromJSON(JSONResult, position);
                    } catch (JSONException E) {
                        results[i] = "no video found";
                    }
                } catch (Exception e) {

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                        }
                    }
                }
            }
            return results;

        }
        public ArrayList<ArrayList<String>> getReviewsFromIds(ArrayList<String> ids)
        {
            outerloop:
            while(true)
            {
                ArrayList<ArrayList<String>> results = new ArrayList<>();
                for(int i =0; i<ids.size(); i++)
                {
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;
                    String JSONResult;

                    try {
                        String urlString = null;
                        urlString = "http://api.themoviedb.org/3/movie/" + ids.get(i) + "/reviews?api_key=" + api_key;
                        URL url = new URL(urlString);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        //Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line + "\n");
                        }
                        if (buffer.length() == 0) {
                            return null;
                        }
                        JSONResult = buffer.toString();
                        try {
                            results.add(getCommentsFromJSON(JSONResult));
                        } catch (JSONException E) {
                            return null;
                        }
                    } catch (Exception e) {
                        continue outerloop;

                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
                            }
                        }
                    }
                }
                return results;

            }
        }
        public ArrayList<String> getCommentsFromJSON(String JSONStringParam)throws JSONException{
            JSONObject JSONString = new JSONObject(JSONStringParam);
            JSONArray reviewsArray = JSONString.getJSONArray("results");
            ArrayList<String> results = new ArrayList<>();
            if(reviewsArray.length()==0)
            {
                results.add("No reviews found for this movie.");
                return results;
            }
            for(int i = 0; i<reviewsArray.length(); i++)
            {
                JSONObject result = reviewsArray.getJSONObject(i);
                results.add(result.getString("content"));
            }
            return results;

        }
        public String getYoutubeFromJSON(String JSONStringParam, int position) throws JSONException
        {
            JSONObject JSONString = new JSONObject(JSONStringParam);
            JSONArray youtubesArray = JSONString.getJSONArray("results");
            JSONObject youtube;
            String result = "no videos found";
            if(position ==0)
            {
                youtube = youtubesArray.getJSONObject(0);
                result = youtube.getString("key");
            }
            else if(position==1)
            {
                if(youtubesArray.length()>1)
                {
                    youtube = youtubesArray.getJSONObject(1);
                }
                else{
                    youtube = youtubesArray.getJSONObject(0);
                }
                result = youtube.getString("key");
            }
            return result;
        }
        public String[] getStringsFromJSON(String JSONStringParam, String param)  throws JSONException
        {
            JSONObject JSONString = new JSONObject(JSONStringParam);

            JSONArray moviesArray = JSONString.getJSONArray("results");
            String[] result = new String[moviesArray.length()];

            for(int i = 0; i<moviesArray.length();i++)
            {
                JSONObject movie = moviesArray.getJSONObject(i);
                if(param.equals("vote_average"))
                {
                    Double number = movie.getDouble("vote_average");
                    String rating =Double.toString(number)+"/10";
                    result[i]=rating;
                }
                else {
                    String data = movie.getString(param);
                    result[i] = data;
                }
            }
            return result;
        }
        public String[] getPathsFromJSON(String JSONStringParam) throws JSONException{

            JSONObject JSONString = new JSONObject(JSONStringParam);

            JSONArray moviesArray = JSONString.getJSONArray("results");
            String[] result = new String[moviesArray.length()];

            for(int i = 0; i<moviesArray.length();i++)
            {
                JSONObject movie = moviesArray.getJSONObject(i);
                String moviePath = movie.getString("poster_path");
                result[i] = moviePath;
            }
            return result;
        }
    }
}