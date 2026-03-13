package com.dailynews.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.dailynews.core.services.ArticleFindService;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;
import com.day.cq.search.PredicateGroup;

import org.json.JSONArray;
import org.json.JSONObject;

@Component(service = ArticleFindService.class)
public class ArticleFindServiceImpl implements ArticleFindService {
    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resolverFactory;

    private static final String NEWS_PATH = "/content/dailynews/en/news";

    @Override
    public JSONArray findArticles(String keyword) throws Exception {
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-reader");

        JSONArray articles = new JSONArray();

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceMap)) {
            Session session = resolver.adaptTo(Session.class);

            Map<String, String> map = buildQueryMap(keyword);

            Query query = queryBuilder.createQuery(
                    PredicateGroup.create(map),
                    session
            );

            SearchResult result = query.getResult();

            for (Hit hit : result.getHits()) {
                Resource resource = hit.getResource();
                ValueMap props = resource.getChild("jcr:content").getValueMap();

                JSONObject article = new JSONObject();

                article.put("path", hit.getPath());
                article.put("title", props.get("jcr:title", ""));
                article.put("subtitle", props.get("subtitle", ""));

                articles.put(article);
            }
        }
        return articles;
    }

    private Map<String, String> buildQueryMap(String keyword) {
        Map<String, String> map = new HashMap<>();

        map.put("path", NEWS_PATH);
        map.put("type", "cq:Page");

        map.put("group.p.or", "true");

        map.put("group.1_property", "jcr:content/jcr:title");
        map.put("group.1_property.value", keyword);
        map.put("group.1_property.operation", "like");

        map.put("group.2_property", "jcr:content/subtitle");
        map.put("group.2_property.value", keyword);
        map.put("group.2_property.operation", "like");

        map.put("orderby", "@jcr:content/publishDate");
        map.put("orderby.sort", "desc");

        map.put("p.limit", "10");

        return map;
    }
}