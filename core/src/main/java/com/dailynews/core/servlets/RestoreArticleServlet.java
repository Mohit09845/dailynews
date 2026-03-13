package com.dailynews.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.apache.sling.api.servlets.ServletResolverConstants;

import com.dailynews.core.services.RestoreArticleService;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/dailynews/restore",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class RestoreArticleServlet extends SlingAllMethodsServlet {
    @Reference
    private RestoreArticleService restoreArticleService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String articlePath = request.getParameter("articlePath");

        if (articlePath == null || articlePath.isEmpty()) {
            response.getWriter().write("Article path missing");
            return;
        }

        try {
            restoreArticleService.restoreArticle(articlePath);
            response.getWriter().write("Article restored successfully");
        } catch (Exception e) {
            response.getWriter().write("Error restoring article: " + e.getMessage());
        }
    }
}