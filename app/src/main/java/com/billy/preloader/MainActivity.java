package com.billy.preloader;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.liwg.android.preloader.PreLoader;
import com.liwg.android.preloader.anno.Subscribe;
import com.liwg.android.preloader.interfaces.DataListener;
import com.liwg.android.preloader.interfaces.DataLoader;
import com.liwg.android.preloader.interfaces.GroupedDataLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * @author billy.qi
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int preLoadBeforeButtonClickId;
    private int id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addOnClickListeners(R.id.btn_open_log
                , R.id.btn_disable_log
                , R.id.btn_pre_load_before_page
                , R.id.btn_pre_load_inside_page
                , R.id.btn_pre_load_before_button_click
                , R.id.btn_pre_load_group_before_page
        );
        id = PreLoader.create(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("success");
                subscriber.onCompleted();
            }
        }));
        getIntent().putExtra("PRELOADER",id);
        getIntent().putExtra("Test1",id);
        getIntent().putExtra("Test2",id);
        //start pre-loader for PreLoadBeforeLaunchActivity
//        preLoadBeforeButtonClickId = preLoadForNextActivity();

//        id = PreLoader.create(Observable.create(new Observable.OnSubscribe<Object>() {
//            @Override
//            public void call(Subscriber<? super Object> subscriber) {
//                subscriber.onNext("----");
//                subscriber.onCompleted();
//            }
//        }));
    }

    @Subscribe(preloadKey = "Test2")
    public void test(Observable<String> observable){
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("--------", "call: "+s );
            }
        });
    }
    @Subscribe(preloadKey = "Test1")
    public void test2(Observable observable){
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("--------", "call: "+s );
            }
        });
    }



    public void addOnClickListeners(@IdRes int... ids) {
        if (ids != null) {
            View view;
            for (int id : ids) {
                view = findViewById(id);
                if (view != null) {
                    view.setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        PreLoader.listenData(id, new DataListener<Observable<String>>() {
            @Override
            public void onDataArrived(Observable<String> observable) {
                observable.subscribe(new Action1<String>() {
                    @Override
                    public void call(final String o) {
                        Toast.makeText(getApplicationContext(), o, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Intent intent;
        int id = v.getId();
        switch (id) {
            case R.id.btn_open_log:
                PreLoader.showLog(true);
                break;
            case R.id.btn_disable_log:
                PreLoader.showLog(false);
                break;
            case R.id.btn_pre_load_inside_page:
                intent = new Intent(this, PreLoadActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_pre_load_before_page:
                intent = new Intent(this, PreLoadBeforeLaunchActivity.class);
                //start pre-loader for PreLoadBeforeLaunchActivity
                intent.putExtra("preLoaderId", preLoadForNextActivity());
                intent.putExtra("option", 1);
                startActivity(intent);
                break;
            case R.id.btn_pre_load_before_button_click:
                intent = new Intent(this, PreLoadBeforeLaunchActivity.class);
                intent.putExtra("preLoaderId", preLoadBeforeButtonClickId);
                intent.putExtra("option", 2);
                startActivity(intent);
                break;
            case R.id.btn_pre_load_group_before_page:
                intent = new Intent(this, PreLoadGroupBeforeLaunchActivity.class);
                //start pre-loader for PreLoadBeforeLaunchActivity
                intent.putExtra("preLoaderId", preLoadGroupForNextActivity());
                intent.putExtra("option", 1);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private int preLoadForNextActivity() {
        // load data before activity launch
        // use PreLoaderPool to do this work
        return PreLoader.create(new Loader());
    }

    private int preLoadGroupForNextActivity() {
        // load a group data before activity launch
        // use PreLoaderPool to do this work
        return PreLoader.create(new Loader1(), new Loader2());
    }

    class Loader implements DataLoader<String> {
        @Override
        public String loadData() {
            TimeWatcher timeWatcher = TimeWatcher.obtainAndStart("DataLoader load data");
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
            return timeWatcher.stopAndPrint();
        }
    }

    class Loader1 implements GroupedDataLoader<String> {
        @Override
        public String loadData() {
            TimeWatcher timeWatcher = TimeWatcher.obtainAndStart("GroupedDataLoader1 load data");
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
            return timeWatcher.stopAndPrint();
        }

        @Override
        public String keyInGroup() {
            return "loader1";
        }
    }

    class Loader2 implements GroupedDataLoader<String> {
        @Override
        public String loadData() {
            TimeWatcher timeWatcher = TimeWatcher.obtainAndStart("GroupedDataLoader2 load data");
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignored) {
            }
            return timeWatcher.stopAndPrint();
        }

        @Override
        public String keyInGroup() {
            return "loader2";
        }
    }
}
