package com.dailynews.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;
import com.day.cq.search.PredicateGroup;

import org.json.JSONArray;
import org.json.JSONObject;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/dailynews/search",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ArticleFindServlet extends SlingAllMethodsServlet {

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("q");

        if (keyword == null || keyword.trim().isEmpty()) {
            response.getWriter().write("Query parameter 'q' is required.");
            return;
        }

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-reader");

        JSONArray articles = new JSONArray();

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            Map<String, String> map = new HashMap<>();

            map.put("path", "/content/dailynews/en/news");
            map.put("type", "cq:Page");

            map.put("group.p.or", "true");

            map.put("group.1_property", "jcr:content/jcr:title");
            map.put("group.1_property.value", "%" + keyword + "%");
            map.put("group.1_property.operation", "like");

            map.put("group.2_property", "jcr:content/subtitle");
            map.put("group.2_property.value", "%" + keyword + "%");
            map.put("group.2_property.operation", "like");

            map.put("orderby", "@jcr:content/publishDate");
            map.put("orderby.sort", "desc");

            map.put("p.limit", "10");

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

        } catch (Exception e) {
            response.getWriter().write("Error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(articles.toString());
    }
}