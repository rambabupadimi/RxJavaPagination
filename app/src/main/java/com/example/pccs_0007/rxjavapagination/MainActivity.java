package com.example.pccs_0007.rxjavapagination;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    LinearLayoutManager layoutManager;
    ArrayList<PhotoInsideModel> photoModelArrayList = new ArrayList<>();
    DisplayPhotosAdapter displayPhotosAdapter;
    int firstVisibleItemPosition;
    int visibleItemCount;
    private boolean loading = false;
    private boolean isLastPage= false;
    private int pageNumber = 1;
    private final int VISIBLE_THRESHOLD = 1;
    private int lastVisibleItem, totalItemCount;
    Gson gson=new Gson();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PublishProcessor<Integer> paginator = PublishProcessor.create();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseObjects();
        initialiseRecylerView();
       // https://jsonplaceholder.typicode.com/photos/
        initialiseScrollListener();
        subscribeForData();
    }

    private void initialiseObjects()
    {
        recyclerView = findViewById(R.id.recylerview);
        layoutManager   =new LinearLayoutManager(this);
        progressBar = findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(layoutManager);

    }
    private void initialiseRecylerView()
    {
        displayPhotosAdapter = new DisplayPhotosAdapter(this,photoModelArrayList);
        recyclerView.setAdapter(displayPhotosAdapter);
    }


    private void initialiseScrollListener()
    {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                visibleItemCount = layoutManager.getChildCount();
                if(dy>0) {
                    if (!loading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            pageNumber++;
                            paginator.onNext(pageNumber);
                            loading = true;

                        }
                    }

                }
            }
        });
    }


    private void subscribeForData() {

        Disposable disposable = paginator
                .onBackpressureDrop()
                .concatMap(new Function<Integer, Publisher<List<PhotoInsideModel>>>() {
                    @Override
                    public Publisher<List<PhotoInsideModel>> apply(@NonNull Integer page) throws Exception {
                        loading = true;
                        progressBar.setVisibility(View.VISIBLE);
                        return dataFromNetwork(page);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PhotoInsideModel>>() {
                    @Override
                    public void accept(@NonNull List<PhotoInsideModel> items) throws Exception {

                        displayPhotosAdapter.addItems(items);
                        //    agentCommonListAdapter.notifyDataSetChanged();
                        loading = false;
                        progressBar.setVisibility(View.GONE);
                    }
                });

        compositeDisposable.add(disposable);

        paginator.onNext(pageNumber);

    }

    private Flowable<List<PhotoInsideModel>> dataFromNetwork(final int page) {
        return Flowable.just(true)
                .delay(4, TimeUnit.SECONDS)
                .map(new Function<Boolean, List<PhotoInsideModel>>() {
                    @Override
                    public List<PhotoInsideModel> apply(@NonNull Boolean value) throws Exception {
                         final List<PhotoInsideModel> items = new ArrayList<>();

                        Rx2AndroidNetworking.get("https://api.themoviedb.org/3/movie/top_rated")
                                .addQueryParameter("api_key", "ec01f8c2eb6ac402f2ca026dc2d9b8fd")
                                .addQueryParameter("language", "en-US")
                                .addQueryParameter("page", ""+page+"")
                                .build()
                                .getJSONObjectObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())

                                .subscribe(new Observer<JSONObject>() {

                                    @Override
                                    public void onError(Throwable e) {
                                        // handle error
                                        Log.i("tag","response is error");
                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.i("tag","response is complete");
                                    }

                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        Log.i("tag","response is disposale");
                                    }

                                    @Override
                                    public void onNext(JSONObject response) {
                                        //do anything with response

                                         PhotoModel photoModel =   gson.fromJson(response.toString(),PhotoModel.class);
                                        Log.i("tag","response is"+gson.toJson(photoModel));
                                        items.addAll(photoModel.results);
                                        displayPhotosAdapter.addItems(items);
                                        //    agentCommonListAdapter.notifyDataSetChanged();
                                        loading = false;
                                        progressBar.setVisibility(View.GONE);

                                    }
                                });


/*
                        int val = page*15;
                        pageWithSelectedItemsSize = val;
                        ArrayList<OrderModel> orderModelList =orderDAO.getOrderDataPagination("running",null,val);
                        orderListSize = orderModelList.size();

                        if(totalItemCount==orderListSize){
                            isLastPage = true;
                        }*/
                            return items;
                    }
                });
    }

}
