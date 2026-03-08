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

import org.apache.sling.api.servlets.ServletResolverConstants;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/dailynews/restore",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)

public class RestoreArticleServlet extends SlingAllMethodsServlet {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response)
            throws ServletException, IOException {

        String articlePath = request.getParameter("articlePath");

        if (articlePath == null || articlePath.isEmpty()) {
            response.getWriter().write("Article path missing");
            return;
        }

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            /* Extract article name */
            String articleName =
                    articlePath.substring(articlePath.lastIndexOf("/") + 1);

            /* Construct original news path */
            String restorePath =
                    "/content/dailynews/en/news/" + articleName;

            /* Move article from archive → news */
            session.move(articlePath, restorePath);

            session.save();

            response.getWriter().write("Article restored successfully");

        } catch (Exception e) {

            response.getWriter().write(
                    "Error restoring article: " + e.getMessage());
        }
    }
}