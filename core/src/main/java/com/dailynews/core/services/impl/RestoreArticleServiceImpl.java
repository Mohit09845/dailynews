package com.dailynews.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.dailynews.core.services.RestoreArticleService;

@Component(service = RestoreArticleService.class)
public class RestoreArticleServiceImpl implements RestoreArticleService {
    private static final String NEWS_ROOT = "/content/dailynews/en/news";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void restoreArticle(String articlePath) throws Exception {
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            String articleName = articlePath.substring(articlePath.lastIndexOf("/") + 1);

            String restorePath = NEWS_ROOT + "/" + articleName;

            session.move(articlePath, restorePath);

            session.save();
        }
    }
}