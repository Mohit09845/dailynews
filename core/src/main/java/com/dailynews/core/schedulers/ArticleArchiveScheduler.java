package com.dailynews.core.schedulers;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.commons.scheduler.ScheduleOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dailynews.core.config.ArticleArchiverConfig;

@Component(immediate = true)
@Designate(ocd = ArticleArchiverConfig.class)
public class ArticleArchiveScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleArchiveScheduler.class);

    private static final String JOB_TOPIC = "dailynews/archive/job";
    private static final String SCHEDULER_NAME = "DailyNewsArticleArchiver";

    @Reference
    private Scheduler scheduler;

    @Reference
    private JobManager jobManager;

    private ArticleArchiverConfig config;

    @Activate
    @Modified
    protected void activate(ArticleArchiverConfig config) {
        this.config = config;

        scheduler.unschedule(SCHEDULER_NAME);

        ScheduleOptions options = scheduler.EXPR(config.scheduler_expression());
        options.name(SCHEDULER_NAME);
        options.canRunConcurrently(false);
        options.onLeaderOnly(true);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                triggerJob();
            }
        }, options);

        LOG.info("Article Archiver Scheduler started with cron {}", config.scheduler_expression());
    }

    @Deactivate
    protected void deactivate() {
        scheduler.unschedule(SCHEDULER_NAME);
        LOG.info("Article Archiver Scheduler deactivated");
    }

    private void triggerJob() {
        Map<String, Object> props = new HashMap<>();
        props.put("newsRoot", config.news_root());
        props.put("archiveRoot", config.archive_root());
        props.put("daysLimit", config.days_limit());
        props.put("batchSize", config.batch_size());

        jobManager.addJob(JOB_TOPIC, props);
        LOG.info("Archive job triggered");
    }
}
