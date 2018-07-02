package com.zk.rxandroiddemo;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RxJavaSample";
    //订阅管理器
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flowableSubscription != null) {
            flowableSubscription.cancel();
        }
        disposables.clear();
    }

    private void setClick() {
        findViewById(R.id.bt1).setOnClickListener(this);
        findViewById(R.id.bt2).setOnClickListener(this);
        findViewById(R.id.bt3).setOnClickListener(this);
        findViewById(R.id.bt40).setOnClickListener(this);
        findViewById(R.id.bt41).setOnClickListener(this);
        findViewById(R.id.bt5).setOnClickListener(this);
        findViewById(R.id.bt6).setOnClickListener(this);
        findViewById(R.id.bt7).setOnClickListener(this);
        findViewById(R.id.bt8).setOnClickListener(this);
        findViewById(R.id.bt9).setOnClickListener(this);
        findViewById(R.id.bt10).setOnClickListener(this);
        findViewById(R.id.bt11).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt1:
                //正常调用
                normal();
                break;
            case R.id.bt2:
                defer();
                break;
            case R.id.bt3:
                consumer();
                break;
            case R.id.bt40:
                flowable1();
                break;
            case R.id.bt41:
                flowable2();
                break;
            case R.id.bt5:
                link();
                break;
            case R.id.bt6:
                threadScheduler();
                break;
            case R.id.bt7:
                map();
                break;
            case R.id.bt8:
                flatMap();
                break;
            case R.id.bt9:
                zip();
                break;
            case R.id.bt10:
                filter();
                break;
            case R.id.bt11:
                sample();
                break;
        }
    }

    /**
     * 取样符 可以每隔指定时间获取一个事件
     */
    private void sample() {

        Disposable dis = Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; ; i++) {
                            e.onNext(i);
                            SystemClock.sleep(10);
                        }
                    }
                }).sample(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "筛选的值：" + integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        boolean flag = disposables.add(disposable);
                        Log.d(TAG, "isAdd"+flag+"Disposable:" + disposable.isDisposed());
                    }
                });
        boolean flag = disposables.add(dis);
        Log.d(TAG, "isAdd"+flag);
    }


    StringBuffer sb = new StringBuffer();
    int time = 0;
    long localeTime1, localeTime2;

    /**
     * 过滤符 对发送的事件进行过滤
     */
    private void filter() {
        time = 0;
        intList.clear();
        sb.delete(0, sb.length());
        sb.append("这是  1000 以内的质数：");
        Disposable dis = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                localeTime1 = System.currentTimeMillis();
                for (int i = 0; i < 1000; i++) {
                    e.onNext(i);
                }
                e.onComplete();
            }
        }).filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) throws Exception {
                if (integer == 2) {
                    time++;
                    intList.add(integer);
                    return true;
                } else if (integer == 1 || integer % 2 == 0) {
                    time++;
                    return false;
                }
                double s = Math.sqrt(integer);//求取平方根
                for (int i : intList) {
                    time++;
                    if (i <= s) {
                        if (integer % i == 0) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
//                for (int i = 3; i <= s; i++) {
//                    time++;
//                    if (integer % i == 0) {
//                        return false;
//                    }
//                }
                intList.add(integer);
                return true;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                sb.append(",").append(integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.i(TAG, throwable.getLocalizedMessage());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                localeTime2 = System.currentTimeMillis();
                sb.append(intList.toString()).append("\n").append("次数为：").append(time).append("\t个数为:").append(intList.size()).append("\t耗时：").append(localeTime2 - localeTime1);
                Log.i(TAG, sb.toString());
            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                boolean flag = disposables.add(disposable);
                Log.d(TAG, "isAdd"+flag+"Disposable:" + disposable.isDisposed());
            }
        });
        disposables.add(dis);
    }

    /**
     * zip 操作符，会将多个事件合为一个 并且取最少配对
     * 通过对每个被观察者
     */
    private void zip() {
        Observable<String> observable1 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("A");
                SystemClock.sleep(500);
                e.onNext("B");
                SystemClock.sleep(500);
                e.onNext("C");
                SystemClock.sleep(500);
                e.onNext("D");
                SystemClock.sleep(500);
                e.onComplete();
            }
        }).map(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                s = s + "From observable1 ";
                Log.i(TAG, "\nThreadName:" + Thread.currentThread().getName() + "\n" + "位置：加工1 " + "\n数据：" + s);
                return s;
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                SystemClock.sleep(500);
                e.onNext(1);
                SystemClock.sleep(500);
                e.onNext(2);
                SystemClock.sleep(500);
                e.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                String s = "数字：" + integer + "From observable2";
                Log.i(TAG, "\nThreadName:" + Thread.currentThread().getName() + "\n" + "位置：加工2 " + "\n数据：" + s);
                return s;
            }
        }).subscribeOn(Schedulers.io());
        Disposable dis = Observable
                .zip(observable1, observable2, new BiFunction<String, String, String>() {
                    @Override
                    public String apply(String s1, String s2) throws Exception {
                        String s = s1 + " _ " + s2;
                        Log.i(TAG, "\nThreadName:" + Thread.currentThread().getName() + "\n" + "位置：加工3 " + "\n数据：" + s);
                        return s;
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "\nThreadName:" + Thread.currentThread().getName() + "\n" + "位置：接受 " + "\n数据：" + s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "接收 error\n" + throwable.getLocalizedMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "结束 ");
                    }
                });
        disposables.add(dis);

    }

    /**
     * 与map 类似 区别只是返回的多个类型 但不保证返回事件的顺序
     * 如果需要 保证顺序可以使用 concatMap
     */
    private void flatMap() {
        Disposable dis = Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        e.onNext(1);
                        e.onNext(2);
                        e.onNext(3);
                        e.onComplete();
                    }
                })
                //.concatMap
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Integer integer) throws Exception {
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            list.add("这是 " + integer + " 的 " + i + " 样式");
                        }
                        return Observable.fromIterable(list).delay(100, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.single()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "接受的结果：" + s);
                    }
                });
        disposables.add(dis);
    }

    /**
     * map作用就是对上游发送的每一个事件通过指定函数进行变化
     * 函数发生与分发处理在一个线程
     */
    private void map() {
        Disposable dis = Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        Log.d(TAG, "此处分发数据\n" + Thread.currentThread().getName());
                        e.onNext(1);
                        e.onNext(2);
                        e.onComplete();
                    }
                })
                .observeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SystemClock.sleep(1000);
                        Log.d(TAG, "doOnNext 接收 1\n" + Thread.currentThread().getName() + "\n" + integer);
                        if (integer == 2) {
                            throw new Exception("获取 2 报错了");
                        }
                    }
                })
                .observeOn(Schedulers.newThread())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        String s = String.valueOf("这是转换后的数据：" + integer);
                        Log.d(TAG, "此处加工数据\n" + Thread.currentThread().getName() + "\n" + s);
                        return s;
                    }
                })
                .observeOn(Schedulers.newThread())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "doOnNext 接收 2\n" + Thread.currentThread().getName() + "\n" + s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "此处接收数据\n" + Thread.currentThread().getName() + "\n" + s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "此处接收错误\n" + Thread.currentThread().getName() + "\n" + throwable.getLocalizedMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"Map complete");
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        boolean flag = disposables.add(disposable);
                        Log.d(TAG, "Map act isAdd "+flag +" dis_size "+disposables.size());
                    }
                });
        boolean flag = disposables.add(dis);
        Log.d(TAG, "Map isAdd "+flag+" dis_size "+disposables.size());
    }

    /**
     * 线程调度器的使用
     * 借助 .observeOn(?) 可以实现事件的 多线程分发，但分发是全部的 并不会根据线程进行区分
     * subscribe、doAfterNext() 是一定会在doNext 之后执行,并且是所有的doNext执行完成之后
     * 相对于单个接受者来说，事件顺序是一定的
     */
    private void threadScheduler() {
        Disposable dis = Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        Log.d(TAG, "分发：" + "\tThread Name:" + Thread.currentThread().getName());
                        e.onNext(1);
                        SystemClock.sleep(1000);
                        e.onNext(2);
                        SystemClock.sleep(1000);
                        e.onNext(3);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())//切换到子线程接受事件
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SystemClock.sleep(3000);
                        Log.d(TAG, "doOnNext:main\nValue" + integer +
                                "\tThread Name:" + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SystemClock.sleep(1000);
                        Log.d(TAG, "doOnNext:newThread\nValue" + integer +
                                "\tThread Name:" + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.newThread())
                .doAfterNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SystemClock.sleep(1000);
                        Log.d(TAG, "doAfterNext:io\nValue" + integer +
                                "\tThread Name:" + Thread.currentThread().getName());
                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "doAfterTerminate" +
                                "\tThread Name:" + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "subscribe:\nValue" + integer +
                                "\tThread Name:" + Thread.currentThread().getName());
                        SystemClock.sleep(200);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "异常");
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"结束");
                    }
                });
        disposables.add(dis);
    }

    private Subscription flowableSubscription;

    private void flowable2() {
        Log.d(TAG, "Flowable 请求129个");
        if (flowableSubscription != null) {
            flowableSubscription.request(129);
        }
    }

    //压测下的 事件处理  （需要Subscription.request(?)触发才会执行，简化订阅 默认触犯Long.MAX_VALUE）
    private void flowable1() {
        Disposable dis = Flowable.create(
                new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 10000; i++) {
                            e.onNext(i);
                        }
                        e.onComplete();
                        Log.d(TAG, "Flowable 分发完成");
                    }
                },

                //压力处理策略
                //DROP :丢掉超出缓存的 事件 只存储 128个
                //LATEST:保持事件最新 超出的覆盖之前的  127 之后是 最新值; 存129个值
                //BUFFER:保存所有 的  会产生OOM
                BackpressureStrategy.BUFFER)
                //连接调度
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Integer>() {
                            @Override
                            public void accept(Integer s) throws Exception {

                                Log.d(TAG, "accept " + s);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(TAG, "accept error" + throwable);
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
        Log.d(TAG, "Flowable 连接完成");
        disposables.add(dis);

    }

    // 订阅者的简化处理
    private void consumer() {
        if (stringList == null) {
            stringList = new ArrayList<>();
        }
        stringList.clear();
        stringList.add("这");
        stringList.add("是");
        stringList.add("consumer");
        Disposable dis = Observable.fromArray(stringList.toArray(new String[3]))
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
                                boolean flag = disposables.add(disposable);
                                Log.d(TAG, "isAdd"+flag+"Disposable:" + disposable.isDisposed());
                            }
                        }
                );
        disposables.add(dis);
    }

    //延迟调用

    static List<String> stringList = new ArrayList<>();
    static List<Integer> intList = new ArrayList<>();

    //  Defer操作符只有当有Subscriber来订阅的时候才会创建一个新的Observable对象,
    // 也就是说每次订阅都会得到一个刚创建的最新的Observable对象，
    // 这可以确保Observable对象里的数据是最新的
    public Observable<String> sampleDeferObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                //此处可以做耗时操作
                return Observable.just(intList.size() + "")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        });
    }

    private void defer() {

        intList.add(intList.size());

        DisposableObserver<String> observer = new DisposableObserver<String>() {

            @Override
            protected void onStart() {
                super.onStart();
            }

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
        };
        //订阅
        sampleDeferObservable().subscribe(observer);

        disposables.add(observer);
    }

    //RxAndroid 的基本使用
    private void normal() {
        Log.d(TAG, "click ThreadName:" + Thread.currentThread().getName());
        //建立观察者
        //Observer 观察者，他接受事件发生的结果
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposables.add(d);
                Log.d(TAG, "onSubscribe 是否需要取消订阅 isDisposed：" + d.isDisposed());
            }

            @Override
            public void onNext(String value) {

                //此处判断是否需要取消订阅
                Log.d(TAG, "onNext " + value);
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
                Log.d(TAG, "subscribeActual ThreadName:" + Thread.currentThread().getName());
                observer.onNext("two");
//                observer.onError(new Throwable("报错了"));
                observer.onNext("three");
                observer.onComplete();
            }
        };

        //观察者与被观察者联系并确定 事件处理及通知方式
        Observer<String> result = observable
                //默认在什么位置开启 在什么位置处理事件.指定接受位置 则必须指定处理位置
                .subscribeOn(Schedulers.computation())//事件在哪处理 线程控制器 一般使用 io (无上限线程池)
                .observeOn(AndroidSchedulers.mainThread())//在哪通知到 线程控制器
                .subscribeWith(observer);
    }

    // 链式调用
    private void link() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                boolean flag = disposables.add(d);
                Log.d(TAG, "subscribe isAdd " + flag);
            }

            @Override
            public void onNext(Integer value) {
                Log.d(TAG, "" + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "error" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "complete");
            }
        });
    }
}
