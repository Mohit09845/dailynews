package com.dailynews.core.services;

import org.json.JSONArray;

public interface ArticleFindService {
    JSONArray findArticles(String keyword) throws Exception;
}