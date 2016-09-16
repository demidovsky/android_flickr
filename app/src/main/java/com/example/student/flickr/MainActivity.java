package com.example.student.flickr;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.StrictMode;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements Callback<Result> {

    public final static String PHOTO_URL = "PHOTO_URL";

    private Retrofit retrofit;
    private FlickrService service;
    private int currentPage = 1;
    private String tag = "forest";
    private GridView grid;
    private CursorAdapter adapter;
    private PhotosDBHelper helper;
    private boolean loading = false;
    private final int threshold = 40;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.flickr.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(FlickrService.class);



        adapter = new PhotoAdapter(this, null, 0);

        helper = new PhotosDBHelper(this);



        setupGrid();




        startOver();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top, menu);
        return super.onCreateOptionsMenu(menu);
    }




    // Клик по пунктам главного меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {

            case R.id.search:
                handleSearch(item);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void handleSearch(MenuItem item) {

        final SearchView searchView = (SearchView) item.getActionView();

        item.expandActionView();

        searchView.setQuery(tag, false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                tag = query;
                startOver();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });



//        Toast.makeText(this, searchText.getText(), Toast.LENGTH_SHORT).show();

    }


    private static String createUrl(Photo p) {
        String url = String.format(
                "https://farm%s.staticflickr.com/%s/%s_%s_q.jpg",
                p.getFarm(),
                p.getServer(),
                p.getId(),
                p.getSecret()
        );
        //Log.d("happy", url);
        return url;
    }



    private void startOver() {
        helper.getWritableDatabase().delete(PhotosTable.TABLE_PHOTOS, null, null);
        currentPage = 1;
        loadMore(currentPage, tag);
    }



// https://api.flickr.com/services/rest/
// ?method=flickr.photos.search
// &api_key=dcceac9e627ee62a18f24c610f9f6e38
// &tags=Black+and+White
// &format=json
// &nojsoncallback=1
// &api_sig=cd70c67fd237a4f7f5b2d90d2019ee9a

    /*
            @Query("text") String text,
            @Query("method") String method,
            @Query("format") String format,
            @Query("api_key") String key,
            @Query("nojsoncallback") int flag,
            @Query("page") int page
     */


    private void loadMore(int page, String search) {
        Call <Result> call = service.search(
                search,
                "flickr.photos.search",
                "json",
                "d8e561f63a10489788fe1e37f351d738",
                1,
                page
        );

        loading = true;

        call.enqueue(this);
    }



    @Override
    public void onResponse(Call<Result> call, Response<Result> response) {
        Photos body = response.body().getPhotos();

        currentPage = body.getPage();

        String sql = "insert into "
                + PhotosTable.TABLE_PHOTOS
                + " ( "
                + PhotosTable.COLUMN_URL
                + " ) VALUES ( ? );";

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();

        for (Photo photo : body.getPhoto())
        {
            Log.d("happy", photo.getTitle());
            statement.bindString(1, createUrl(photo));
            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        Cursor cursor = getPhotoCursor();
        updateCursor(cursor);
        loading = false;
    }




    private Cursor getPhotoCursor() {
            return helper.getReadableDatabase().query(
                    PhotosTable.TABLE_PHOTOS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
    }




    private void updateCursor(Cursor cursor) {
        adapter.swapCursor(cursor);
    }




    @Override
    public void onFailure(Call<Result> call, Throwable t) {
        Toast.makeText(this, "Проблемы с загрузкой", Toast.LENGTH_SHORT).show();
    }





    // Настройка сетки: привязка адаптера, обработчик кликов, бесконечный скролл
    private void setupGrid()
    {
        grid = (GridView) findViewById(R.id.grid);


        grid.setAdapter(adapter);


        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Detail.class);

                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                String url = cursor.getString(cursor.getColumnIndex(PhotosTable.COLUMN_URL));

                intent.putExtra(PHOTO_URL, url);

                startActivity(intent);
            }
        });


        grid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE)
                {
                    if (grid.getLastVisiblePosition() >= grid.getCount() - threshold && !loading)
                    {
                        Toast.makeText(getApplicationContext(), "Загрузка...", Toast.LENGTH_SHORT).show();
                        loadMore(currentPage + 1, tag);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }






}
