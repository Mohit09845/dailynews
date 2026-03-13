package com.dailynews.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "DailyNews Article Archiver Configuration",
        description = "Configuration for automatic archiving of old news articles"
)
public @interface ArticleArchiverConfig {

    @AttributeDefinition(name = "Cron Expression", description = "Scheduler cron expression (3:00 AM = 0 0 3 * * ?)")
    String scheduler_expression() default "0 0 3 * * ?";

    @AttributeDefinition(name = "News Root Path", description = "Path where active news articles are stored")
    String news_root() default "/content/dailynews/en/news";

    @AttributeDefinition(name = "Archive Root Path", description = "Path where old articles will be archived")
    String archive_root() default "/content/dailynews/en/news/archive";

    @AttributeDefinition(name = "Archive After (Days)", description = "Articles older than this number of days will be archived")
    int days_limit() default 365;

    @AttributeDefinition(name = "Batch Size", description = "Number of articles processed per batch")
    int batch_size() default 20;
}