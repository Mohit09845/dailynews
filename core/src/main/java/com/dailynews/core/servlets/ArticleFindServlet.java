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

import com.dailynews.core.services.ArticleFindService;

import org.json.JSONArray;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/dailynews/search",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ArticleFindServlet extends SlingAllMethodsServlet {
    @Reference
    private ArticleFindService articleFindService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        String keyword = request.getParameter("q");

        if (keyword == null || keyword.trim().isEmpty()) {
            response.getWriter().write("Query parameter 'q' is required.");
            return;
        }

        try {
            JSONArray articles = articleFindService.findArticles(keyword);

            response.setContentType("application/json");
            response.getWriter().write(articles.toString());

        } catch (Exception e) {
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}