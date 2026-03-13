package com.dailynews.core.jobs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.event.jobs.Job;

import org.osgi.service.component.annotations.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dailynews.core.services.ArchiveArticleService;

@Component(
        service = JobConsumer.class,
        property = {
                JobConsumer.PROPERTY_TOPICS + "=dailynews/archive/job"
        }
)
public class ArticleArchiveJobConsumer implements JobConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleArchiveJobConsumer.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ArchiveArticleService archiveArticleService;

    @Override
    public JobResult process(Job job) {

        String newsRoot = (String) job.getProperty("newsRoot");
        int daysLimit = (Integer) job.getProperty("daysLimit");
        // Fix 2: Read batchSize from job properties
        int batchSize = (Integer) job.getProperty("batchSize");

        int successCount = 0;
        int failureCount = 0;

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceMap)) {

            Iterator<Resource> oldArticles = findOldArticles(resolver, newsRoot, daysLimit);

            int processed = 0;

            while (oldArticles.hasNext() && processed < batchSize) {
                Resource articleContent = oldArticles.next();
                String articlePath = articleContent.getParent().getPath();

                try {
                    archiveArticleService.archiveArticle(articlePath);
                    successCount++;
                    processed++;
                    LOG.info("Archived article {}", articlePath);

                } catch (Exception e) {
                    failureCount++;
                    LOG.error("Failed archiving {}", articlePath, e);
                }
            }

            LOG.info("Archive Summary -> Success: {}, Failed: {}, Batch limit: {}", successCount, failureCount, batchSize);

        } catch (LoginException e) {
            LOG.error("Could not obtain ResourceResolver — check service user mapping", e);
            return JobResult.CANCEL;

        } catch (Exception e) {
            LOG.error("Unexpected error during archive job", e);
            return JobResult.FAILED;
        }

        return JobResult.OK;
    }

    private Iterator<Resource> findOldArticles(ResourceResolver resolver, String newsRoot, int daysLimit) {
        Calendar limitDate = Calendar.getInstance();
        limitDate.add(Calendar.DAY_OF_YEAR, -daysLimit);

        String dateStr = new SimpleDateFormat(DATE_FORMAT).format(limitDate.getTime());

        String query =
                "SELECT * FROM [cq:PageContent] AS s" +
                        " WHERE ISDESCENDANTNODE(s, '" + newsRoot + "')" +
                        " AND s.[cq:lastReplicated] < CAST('" + dateStr + "' AS DATE)";

        return resolver.findResources(query, "JCR-SQL2");
    }
}