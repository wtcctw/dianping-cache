package com.dianping.cache.alarm.lifecycle;


import com.dianping.swallow.common.internal.lifecycle.LifecycleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvshiyun on 15/11/23.
 */
public abstract class AbstractLifeCycle extends AbstractComponentMonitorable implements Lifecycle, LifecycleComponentStatus {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private LifecycleManager lifecycleManager = new DefaultLifecycleManager(this);

    @Override
    public void initialize() throws Exception {
        lifecycleManager.initialize(new LifecycleCallback() {
            @Override
            public void onTransition() {
                try {
                    doInitialize();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    protected void doInitialize() throws Exception {

    }

    @Override
    public void start() throws Exception {
        lifecycleManager.start(new LifecycleCallback() {
            @Override
            public void onTransition() {
                try {
                    doStart();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void doStart() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        lifecycleManager.stop(new LifecycleCallback() {
            @Override
            public void onTransition() {
                try {
                    doStop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    protected void doStop() throws Exception {

    }

    @Override
    public void dispose() throws Exception {
        lifecycleManager.dispose(new LifecycleCallback() {
            @Override
            public void onTransition() {
                try {
                    doDispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    protected void doDispose() throws Exception {

    }

    @Override
    public int getOrder() {

        //middle
        return 0;
    }

    @Override
    public String getLifecyclePhase() {
        return lifecycleManager.getCurrentPhaseName();
    }
}
