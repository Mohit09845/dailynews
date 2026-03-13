package com.dailynews.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.dailynews.core.services.ArchiveArticleService;

@Component(service = ArchiveArticleService.class)
public class ArchiveArticleServiceImpl implements ArchiveArticleService {
    private static final String ARCHIVE_ROOT = "/content/dailynews/en/news/archive";
    private static final String NEWS_ROOT = "/content/dailynews/en/news";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void archiveArticle(String articlePath) throws Exception {
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceMap)) {
            Session session = resolver.adaptTo(Session.class);  // We converted Sling API to JCR API because JCR Session supports node move operations.

            String articleName = articlePath.substring(articlePath.lastIndexOf("/") + 1);

            String destinationPath = ARCHIVE_ROOT + "/" + articleName;

            ensureArchiveFolder(session);

            session.move(articlePath, destinationPath);

            session.save();
        }
    }

    private void ensureArchiveFolder(Session session) throws Exception {
        if (!session.nodeExists(ARCHIVE_ROOT)) {
            Node newsNode = session.getNode(NEWS_ROOT);
            newsNode.addNode("archive", "sling:OrderedFolder");
            session.save();
        }
    }
}

// Session supports move Node, check Node(nodeExists), add Node.