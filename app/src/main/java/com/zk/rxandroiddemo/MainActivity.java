package com.zk.rxandroiddemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Toast;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RxJavaSample";
    private Snackbar snackbar;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private ScrollView scrollView;
    private int widowHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setClick();
        initEditText();
//        initRxJava();
    }

    private void initEditText() {
        scrollView = ((ScrollView) findViewById(R.id.scrollView));
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        widowHeight = metric.heightPixels;
        findViewById(R.id.et1).setOnFocusChangeListener(listener);
        findViewById(R.id.et2).setOnFocusChangeListener(listener);
        findViewById(R.id.et3).setOnFocusChangeListener(listener);
        findViewById(R.id.et4).setOnFocusChangeListener(listener);
        findViewById(R.id.et5).setOnFocusChangeListener(listener);
        findViewById(R.id.et6).setOnFocusChangeListener(listener);
        findViewById(R.id.et7).setOnFocusChangeListener(listener);
    }

    public View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                int x = location[0];
                final int y = location[1];
                Log.e(TAG, "x:" + x + "y:" + y + " h:" + widowHeight / 2);
                if (y > widowHeight / 2) {
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int i = (y - widowHeight / 2) + dip2px(30);
                            scrollView.smoothScrollBy(0, i);
                        }
                    }, 200);
                } else {
                    Toast.makeText(MainActivity.this, String.valueOf("x:" + x + "y:" + y + " h:" + widowHeight / 2), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    private void initRxJava() {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {


        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe " + d.toString());
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext " + value + " isMainLoop:" + (Looper.getMainLooper() == Looper.myLooper()));
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete ");
            }
        };

        Subscriber<String> subscriber = new Subscriber<String>() {

            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        };
//        observable
//                .subscribeOn(Schedulers.io())
//               .observeOn(Schedulers.io())
//                .subscribeWith(observer);
//            }
//        }).start();
        Observable
                .just("one", "two", "three", "four", "five")
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e(TAG, "accept " + s + " isMainLoop:" + (Looper.getMainLooper() == Looper.myLooper()));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept " + throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.e(TAG, "Action start ");
                        SystemClock.sleep(5000);
                        Log.e(TAG, "Action end");
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Log.e(TAG, "accept " + disposable.toString());
                    }
                });
    }

    private void setClick() {
        findViewById(R.id.bt1).setOnClickListener(this);
        findViewById(R.id.bt2).setOnClickListener(this);
        findViewById(R.id.bt3).setOnClickListener(this);
        findViewById(R.id.bt4).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt1:
                click1();
                break;
            case R.id.bt2:
                click2();
                break;
            case R.id.bt3:
                consumer();
                break;
            case R.id.bt4:
                flowable();
                break;
        }
    }

    private Subscription flowableSubscription;
    private Toast toast;

    //压测下的 事件处理  （需要Subscription.request(15)触发才会执行，简化订阅 默认触犯Long.MAX_VALUE）
    private void flowable() {
        intList.clear();
        Flowable.create(
                new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> e) throws Exception {
                        for (Integer s : intList) {
                            SystemClock.sleep(500);
                            e.onNext("" + s);
                        }
                        e.onComplete();
                    }
                },
                //压力处理策略
                BackpressureStrategy.ERROR)
                //连接调度
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
//                                SystemClock.sleep(500);
//                                if (toast == null) {
//                                    toast =
//                                } else {
//                                    toast.setText(s);
//                                }
                                if (toast == null) {
//                                    toast.cancel();
//                                } else {
                                    toast = Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
                                }
                                toast.setText(s);
                                toast.show();
                                Log.d(TAG, "accept " + s);
//                                if (integer > 6) {
//                                    //接受数据的 过程中出现错误可以动态取消
//                                    if (flowableSubscription != null) {
//                                        flowableSubscription.cancel();
//                                    }
//                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(TAG, "accept error" + throwable.getMessage());
                            }
                        },
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.d(TAG, "complete ");
                            }
                        },
                        new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {
                                flowableSubscription = subscription;
                            }
                        });

        for (int i = 0; i < 50; i++) {
            intList.add(intList.size());
        }
        if (flowableSubscription != null) {
//            for (int i = 0; i < 6; i++) {
            flowableSubscription.request(15);
//            }
        }
    }

    // 订阅者的简化处理
    private void consumer() {
        stringList.clear();
        stringList.add("这");
        stringList.add("是");
        stringList.add("consumer");
        Observable.fromArray(stringList.toArray(new String[3]))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //接受数据 同之前的onNext
                        new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Log.d(TAG, "accept:" + s);
                            }
                        },
                        //接受错误
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(TAG, "throwable:" + throwable.getMessage());
                            }
                        },
                        //接受结束状态
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.d(TAG, "run:complete");
                            }
                        },
                        //这里获取订阅关系
                        new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                Log.d(TAG, "Disposable:" + disposable.isDisposed());
                            }
                        }
                );
    }

    //延迟调用
    private void click2() {

        defer();
        defer();
    }

    static List<String> stringList = new ArrayList<>();
    static List<Integer> intList = new ArrayList<>();

    //  Defer操作符只有当有Subscriber来订阅的时候才会创建一个新的Observable对象,
    // 也就是说每次订阅都会得到一个刚创建的最新的Observable对象，
    // 这可以确保Observable对象里的数据是最新的
    static Observable<String> sampleDeferObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                //此处可以做耗时操作
                intList.add(intList.size());
                return Observable.just(intList.size() + "");
            }
        });
    }

    private void defer() {

        DisposableObserver<String> disposable = sampleDeferObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")" + intList.size());
                        SystemClock.sleep(2000);
                    }
                });
        disposables.add(disposable);
    }

    private void click1() {
        //建立观察者
        //Observer 观察者，他接受事件发生的结果
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe 是否被处理 isDisposed：" + d.isDisposed());
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext " + value + " isMainLoop:" + (Looper.getMainLooper() == Looper.myLooper()));
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete ");
            }
        };
        // 建立被观察者
        //Observable 即被观察者，它决定什么时候触发事件以及触发怎样的事件。
        Observable<String> observable = new Observable<String>() {
            @Override
            protected void subscribeActual(Observer<? super String> observer) {
                observer.onNext("one");
                Log.d(TAG, "subscribeActual isMainLoop:" + (Looper.getMainLooper() == Looper.myLooper()));
                observer.onNext("two");
//                observer.onError(new Throwable("报错了"));
                observer.onNext("three");
                observer.onComplete();
            }
        };

        //观察者与被观察者联系并确定 事件处理及通知方式
        observable
                //默认在什么位置开启 在什么位置处理事件.指定接受位置 则必须指定处理位置
                .subscribeOn(Schedulers.io())//时间在哪处理  io 是一个线程池 可复用，newThread 不可复用
                .observeOn(AndroidSchedulers.mainThread())//在哪通知到
                .subscribeWith(observer);
    }

    public int dip2px(float dipValue) {
        return (int) (dipValue * getResources().getDisplayMetrics().density + 0.5f);
    }
}
