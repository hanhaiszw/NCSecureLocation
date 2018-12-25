package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池
 */
public class MyThreadPool {
    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private MyThreadPool() {
    }

    //执行任务
    public static void execute(Runnable runnable){
        cachedThreadPool.execute(runnable);
    }

    //终止执行
    public static void shutdownNow(){
        cachedThreadPool.shutdownNow();
    }

}
