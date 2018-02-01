package com.example.jordi.privaliachallange.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.jordi.privaliachallange.R;
import com.example.jordi.privaliachallange.models.Response;
import com.example.jordi.privaliachallange.models.Results;
import com.example.jordi.privaliachallange.others.ListViewAdapter;
import com.example.jordi.privaliachallange.others.ScrollListView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private static ListViewAdapter adapter;
    int actualPage, totalNumOfPages;
    ScrollListView movies_lv;
    ProgressBar loading_pb;
    EditText search_et;
    URL imagesUrl;
    AsyncTask asyncTask;
    String apiKey, apiPath, moviePath, popularPath, searchPath, scheme, authority;
    boolean createNewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading_pb = (ProgressBar) findViewById(R.id.loading_pb);
        search_et = (EditText) findViewById(R.id.search_et);
        movies_lv = (ScrollListView) findViewById(R.id.movies_lv);

        apiKey = "93aea0c77bc168d8bbce3918cefefa45";
        authority = "api.themoviedb.org";
        apiPath = "3";
        moviePath = "movie";
        popularPath = "popular";
        searchPath = "search";
        scheme = "https";

        search_et.addTextChangedListener(searchEditorWatcher);
        movies_lv.setOnBottomReachedListener(onBottomReachedListener);

        try {
            imagesUrl = new URL("http://image.tmdb.org/t/p/w185/");

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(scheme)
                    .authority(authority)
                    .appendPath(apiPath)
                    .appendPath(moviePath)
                    .appendPath(popularPath)
                    .appendQueryParameter("api_key", apiKey);

            createNewAdapter = true;

            if(isOnline())
                asyncTask = new getDataTask().execute(new URL(builder.build().toString()));
            else
                Toast.makeText(this, this.getString(R.string.CHECK_INTERNET), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.toString();
        }
    }

    TextWatcher searchEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            createNewAdapter = true;

            try {
                if(s.length() == 0)
                {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(scheme)
                            .authority(authority)
                            .appendPath(apiPath)
                            .appendPath(moviePath)
                            .appendPath(popularPath)
                            .appendQueryParameter("api_key", apiKey);

                    if (asyncTask != null)
                        asyncTask.cancel(true);

                    if(isOnline())
                        asyncTask = new getDataTask().execute(new URL(builder.build().toString()));
                    else
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.CHECK_INTERNET), Toast.LENGTH_LONG).show();
                } else {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(scheme)
                            .authority(authority)
                            .appendPath(apiPath)
                            .appendPath(searchPath)
                            .appendPath(moviePath)
                            .appendQueryParameter("api_key", apiKey)
                            .appendQueryParameter("query", s.toString());

                    if (asyncTask != null)
                        asyncTask.cancel(true);

                    if(isOnline())
                        asyncTask = new getDataTask().execute(new URL(builder.build().toString()));
                    else
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.CHECK_INTERNET), Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e)
            {
                e.toString();
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    ScrollListView.OnBottomReachedListener onBottomReachedListener = new ScrollListView.OnBottomReachedListener() {
        @Override
        public void onBottomReached() {

            if(actualPage < totalNumOfPages) {
                actualPage++;
                createNewAdapter = false;
                try {
                    if(search_et.getText().length() != 0) {
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme(scheme)
                                .authority(authority)
                                .appendPath(apiPath)
                                .appendPath(searchPath)
                                .appendPath(moviePath)
                                .appendQueryParameter("api_key", apiKey)
                                .appendQueryParameter("page", String.valueOf(actualPage))
                                .appendQueryParameter("query", search_et.getText().toString());

                        if (asyncTask != null)
                            asyncTask.cancel(true);

                        if(isOnline())
                            asyncTask = new getDataTask().execute(new URL(builder.build().toString()));
                        else
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.CHECK_INTERNET), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme(scheme)
                                .authority(authority)
                                .appendPath(apiPath)
                                .appendPath(moviePath)
                                .appendPath(popularPath)
                                .appendQueryParameter("api_key", apiKey)
                                .appendQueryParameter("page", String.valueOf(actualPage));

                        if (asyncTask != null)
                            asyncTask.cancel(true);

                        if(isOnline())
                            asyncTask = new getDataTask().execute(new URL(builder.build().toString()));
                        else
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.CHECK_INTERNET), Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e)
                {
                    e.toString();
                }
            }
        }
    };

    public class getDataTask extends AsyncTask<URL, List<Results>, List<Results>> {

        protected void onPreExecute() {
            loading_pb.setVisibility(View.VISIBLE);
            movies_lv.setOnBottomReachedListener(null);
        }

        protected List<Results> doInBackground(URL... url) {

            loading_pb.setVisibility(View.VISIBLE);
            movies_lv.setOnBottomReachedListener(null);

            HttpURLConnection connection = null;

            try {
                //Create connection
                connection = (HttpURLConnection) url[0].openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

                //Get Response
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringResponse = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    stringResponse.append(line);
                    stringResponse.append('\r');
                }
                rd.close();

                Gson gson = new GsonBuilder().create();
                Response response = gson.fromJson(stringResponse.toString(), Response.class);

                actualPage = Integer.parseInt(response.getPage());
                totalNumOfPages = Integer.parseInt(response.getTotal_pages());

                for (Results result: response.getResults()) {
                    Bitmap bmp;
                    if (result.getPoster_path() == null) {
                        bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image);
                    }
                    else {
                        //Create connection
                        connection = (HttpURLConnection) new URL(imagesUrl.toString() + result.getPoster_path()).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

                        //Get Response
                        bmp = BitmapFactory.decodeStream(connection.getInputStream());
                    }
                    result.setPoster_bitmap(bmp);
                }

                return response.getResults();
            }
            catch (Exception e) {
                e.toString();
                return null;
            }
        }

        protected void onPostExecute(List<Results> results) {
            if (createNewAdapter)
            {
                adapter = new ListViewAdapter(results, getApplicationContext());
                movies_lv.setAdapter(adapter);
            }
            else {
                adapter.updateData(results);  //update adapter's data
                adapter.notifyDataSetChanged(); //notifies any View reflecting data to refresh
            }

            movies_lv.setOnBottomReachedListener(onBottomReachedListener);
            loading_pb.setVisibility(View.GONE);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
