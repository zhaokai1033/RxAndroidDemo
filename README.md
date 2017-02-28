# RxAndroid的简单试用

[Reactivex]: http://reactivex.io/intro.html

RxAndroid 的简单试用 包含

1、基本用法

2、简洁订阅

3、操作符 defer、flowable、map、flatmap、zip、filter、sample 的演示

4、threadScheduler 线程的应用

### 示例一 正常调用

##### 1、创建观察者Observer

```java
        //Observer 观察者，他接受事件发生的结果
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe 是否被处理 isDisposed：" + d.isDisposed());
            }

            @Override
            public void onNext(String value) {
               //此处判断是否需要取消订阅
                Log.d(TAG, "onNext " + value );
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
```

##### 2、 创建被观察者Observable

```java
        //Observable 即被观察者，它决定什么时候触发事件以及触发怎样的事件。即发布事件
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
```

##### 3、建立联系

```java
	   //观察者与被观察者联系并确定 事件处理及通知方式 ，此时事件开始执行
        observable
                //默认在什么位置开启 在什么位置处理事件.指定接受位置 则必须指定处理位置
                //事件在哪处理  io 是一个无上限线程池 可复用，newThread 不可复用   
          		.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//在哪通知到
                .subscribeWith(observer);
```

##### 4、链式调用

```java
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception 				{
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Log.d(TAG, "" + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "error"+e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "complete");
            }
        });
```

### 示例二 Consumer

Consumer 是订阅者的简化处理 ，可以选择性的接收

```java
        if (stringList == null) {
            stringList = new ArrayList<>();
        }
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
                        //这里获取订阅关系 方便取消订阅
                        new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                Log.d(TAG, "Disposable:" + disposable.isDisposed());
                            }
                        }
                );
```

### 其他操作符

### 线程控制器

1、Schedulers.trampoline(): 

```java
直接在当前线程运行,这是默认的 Scheduler。 
```

2、Schedulers.single：

```java
单线程支持实例
```

3、Schedulers.newThread(): 

```java
总是启用新线程，并在新线程执行操作。 
```

4、Schedulers.io():

```java
I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。行为模式和 newThread() 差不多，区别在于 io() 的内部实现是用一个无数量上限的线程池，因此多数情况下 io() 比 newThread() 更有效率。不要把计算工作放在 io() 中 
```

5、Schedulers.computation(): 

```java
计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
```

#####  备注

借助 .observeOn(?) 可以实现事件的 多线程分发，但分发是全部的 并不会根据线程进行区分

subscribe、doAfterNext() 是一定会在doNext 之后执行,并且是所有的doNext执行完成之后