package com.dailynews.core.schedulers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;

import org.osgi.service.metatype.annotations.Designate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dailynews.core.services.ArchiveArticleService;
import com.dailynews.core.config.ArticleArchiverConfig;

import static org.apache.xmlbeans.impl.util.XsTypeConverter.printDateTime;

@Component(service = Runnable.class, immediate = true)
@Designate(ocd = ArticleArchiverConfig.class)
public class ArticleArchiveScheduler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ArticleArchiveScheduler.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ArchiveArticleService archiveArticleService;

    private ArticleArchiverConfig config;

    @Activate
    @Modified
    protected void activate(ArticleArchiverConfig config) {
        this.config = config;
        LOG.info("Article Archiver Scheduler Activated with cron: {}", config.scheduler_expression());
    }

    @Override
    public void run() {

        LOG.info("Starting DailyNews Automatic Article Archiver...");

        int successCount = 0;
        int failureCount = 0;

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceMap)) {
            Iterator<Resource> oldArticles = findOldArticles(resolver);

            while (oldArticles.hasNext()) {
                Resource articleContent = oldArticles.next();
                String articlePath = articleContent.getParent().getPath();

                try {
                    archiveArticleService.archiveArticle(articlePath);
                    LOG.info("Archived article: {}", articlePath);
                    successCount++;

                } catch (Exception e) {
                    LOG.error("Failed to archive article {}", articlePath, e);
                    failureCount++;
                }
            }

            LOG.info("Article Archiver Summary -> Success: {}, Failed: {}", successCount, failureCount);

        } catch (Exception e) {
            LOG.error("Error executing Article Archiver Scheduler", e);
        }
    }

    private Iterator<Resource> findOldArticles(ResourceResolver resolver) {
        Calendar limitDate = Calendar.getInstance();
        limitDate.add(Calendar.DAY_OF_YEAR, -config.days_limit());
        String query = "SELECT * FROM [cq:PageContent] AS s WHERE ISDESCENDANTNODE(s, '" + config.news_root() + "') " + "AND s.[cq:lastReplicated] < CAST('" + printDateTime(limitDate) + "' AS DATE)";
        return resolver.findResources(query, "JCR-SQL2");
    }
}