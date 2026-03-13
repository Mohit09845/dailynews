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

import com.dailynews.core.services.ArchiveArticleService;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/dailynews/archive",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class ArchiveArticleServlet extends SlingAllMethodsServlet {
    @Reference
    private ArchiveArticleService archiveArticleService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String articlePath = request.getParameter("articlePath");

        if (articlePath == null || articlePath.isEmpty()) {
            response.getWriter().write("Article path missing");
            return;
        }

        try {
            archiveArticleService.archiveArticle(articlePath);
            response.getWriter().write("Article archived successfully");
        } catch (Exception e) {
            response.getWriter().write("Error archiving article: " + e.getMessage());
        }
    }
}