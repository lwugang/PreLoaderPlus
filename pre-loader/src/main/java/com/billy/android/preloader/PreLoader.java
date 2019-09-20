package com.billy.android.preloader;


import android.content.Intent;

import com.billy.android.preloader.interfaces.DataListener;
import com.billy.android.preloader.interfaces.DataLoader;
import com.billy.android.preloader.interfaces.GroupedDataListener;
import com.billy.android.preloader.interfaces.GroupedDataLoader;
import com.billy.android.preloader.util.ILogger;
import com.billy.android.preloader.util.PreLoaderLogger;

import java.util.List;
import java.util.concurrent.ExecutorService;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Entrance for pre-load data
 *
 * @author billy.qi <a href="mailto:qiyilike@163.com">Contact me.</a>
 */
public class PreLoader {
    static final String LOADER_ID = "loaderId";
    static ILogger logger = new PreLoaderLogger();

    /**
     * set a custom logger
     *
     * @param logger custom logger
     */
    public static void setLogger(ILogger logger) {
        if (logger != null) {
            PreLoader.logger = logger;
        }
    }

    /**
     * enable/disable logger
     *
     * @param show true:enable logger, false:disable logger
     */
    public static void showLog(boolean show) {
        if (logger != null) {
            logger.showLog(show);
        }
    }

    /**
     * set a custom thread pool for all pre-load tasks
     *
     * @param threadPoolExecutor thread pool
     */
    public static void setDefaultThreadPoolExecutor(ExecutorService threadPoolExecutor) {
        Worker.setDefaultThreadPoolExecutor(threadPoolExecutor);
    }

    /**
     * 预加载数据, rxjava 1.0
     *
     * @param loader 加载器
     * @return
     */
    public static <E> int create(DataLoader<E> loader) {
        return PreLoaderPool.getDefault().preLoad(loader);
    }

    /**
     * 预加载数据,rxjava 1.0
     *
     * @param observable
     * @return
     */
    public static <E> int create(final Observable<E> observable) {
        return PreLoaderPool.getDefault().preLoad(new DataLoader<Observable>() {
            E data;

            @Override
            public Observable<E> loadData() {
                final Object lock = new Object();
                observable.subscribe(new Action1<E>() {
                    @Override
                    public void call(E e) {
                        data = e;
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
                if (data == null) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Observable<E> just = Observable.just(data);
                data = null;
                return just;
            }
        });
    }

    /**
     * pre-load data with a group loaders in only one pre-load-id
     * eg. pre-load data in an activity with lots of {@link DataLoader}
     *
     * @param loaders
     * @return
     */
    public static int create(GroupedDataLoader... loaders) {
        return PreLoaderPool.getDefault().preLoadGroup(loaders);
    }


    /**
     * start to listen data by id<br>
     * 调用 {@link #create(DataLoader)} or {@link #create(Observable)} 会返回 loaderId，根据loaderId监听数据
     *
     * @param loaderId     任务id
     * @param dataListener 回调监听
     * @return success or not
     */
    public static <T> boolean listenData(int loaderId, DataListener<T> dataListener) {
        return PreLoaderPool.getDefault().listenData(loaderId, dataListener);
    }

    public static boolean listenData(int id, GroupedDataListener... listeners) {
        return PreLoaderPool.getDefault().listenData(id, listeners);
    }

    /**
     * remove a specified {@link DataListener} for the pre-load task by id
     *
     * @param id           the id returns by {@link #create(DataLoader)}
     * @param dataListener the listener to remove
     * @return success or not
     */
    public static <T> boolean removeListener(int id, DataListener<T> dataListener) {
        return PreLoaderPool.getDefault().removeListener(id, dataListener);
    }

    /**
     * check the pre-load task is exists in singleton {@link PreLoaderPool}
     *
     * @param id the id returns by {@link #create(DataLoader)}
     * @return exists or not
     */
    public static boolean exists(int id) {
        return PreLoaderPool.getDefault().exists(id);
    }

    /**
     * re-load data for all listeners
     *
     * @return success
     */
    public static boolean refresh(int id) {
        return PreLoaderPool.getDefault().refresh(id);
    }

    /**
     * destroy the pre-load task by id.
     * call this method for remove loader and all listeners, and will not accept any listeners
     *
     * @param id the id returns by {@link #create(DataLoader)}
     * @return success or not
     */
    public static boolean destroy(int id) {
        return PreLoaderPool.getDefault().destroy(id);
    }

    /**
     * destroy all pre-load tasks in singleton {@link PreLoaderPool}
     *
     * @return success or not
     */
    public static boolean destroyAll() {
        return PreLoaderPool.getDefault().destroyAll();
    }
}
