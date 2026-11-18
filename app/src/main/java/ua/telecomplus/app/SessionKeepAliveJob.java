package ua.telecomplus.app;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/**
 * Портал выдаёт сессионный cookie со скользящим сроком жизни 1 час: каждый запрос продлевает его.
 * Если приложение не открывали дольше часа, сессия умирает. Периодический лёгкий запрос продлевает её.
 */
public final class SessionKeepAliveJob extends JobService {
    private static final int JOB_ID = 7301;

    static void schedule(Context context) {
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) {
                return;
            }
            for (JobInfo info : scheduler.getAllPendingJobs()) {
                if (info.getId() == JOB_ID) {
                    return;
                }
            }
            JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(context, SessionKeepAliveJob.class))
                    .setPeriodic(15L * 60L * 1000L)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(job);
        } catch (Exception unused) {
        }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        final TelecomPlusApplication app = (TelecomPlusApplication) getApplication();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (app.gateway != null && app.gateway.hasSavedSession()) {
                        app.gateway.validateSession();
                    }
                } catch (Exception unused) {
                }
                jobFinished(params, false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
