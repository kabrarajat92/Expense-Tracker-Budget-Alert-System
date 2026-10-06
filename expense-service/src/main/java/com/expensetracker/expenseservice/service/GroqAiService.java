package com.expensetracker.expenseservice.service;

import com.expensetracker.expenseservice.entity.Budget;
import com.expensetracker.expenseservice.entity.Expense;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GroqAiService {
	private final WebClient webClient;

    @Value("${groq.model}")
    private String model;
    
    private static final Logger logger = LoggerFactory.getLogger(GroqAiService.class);
    private final CacheManager cacheManager;

    public GroqAiService(@Value("${groq.api.key}") String apiKey,
                          @Value("${groq.api.url}") String apiUrl,CacheManager cacheManager) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.cacheManager = cacheManager;
    }

//    @Cacheable(value = "aiAnalysis", key = "#username")
    public String analyzeSpendingCached(String username, List<Expense> expenses, List<Budget> budgets) throws Exception {
    	Cache cache = cacheManager.getCache("aiAnalysis");

        if (cache != null) {
            Cache.ValueWrapper cached = cache.get(username);
            if (cached != null) {
                logger.info("CACHE HIT — returning cached AI analysis for user '{}'", username);
                return (String) cached.get();
            }
        }

        logger.info("CACHE MISS — calling Groq API for user '{}'", username);
        String result = analyzeSpendingAsync(expenses, budgets).get();

        if (cache != null) {
            cache.put(username, result);
        }

        return result;
    }

//    @CacheEvict(value = "aiAnalysis", key = "#username")
    public void evictAnalysisCache(String username) {
    	Cache cache = cacheManager.getCache("aiAnalysis");
        if (cache != null) {
            cache.evict(username);
            logger.info("CACHE EVICTED — cleared AI analysis cache for user '{}'", username);
        }
    }
    
    @CircuitBreaker(name = "groqApi", fallbackMethod = "fallback")
    @TimeLimiter(name = "groqApi")
    public CompletableFuture<String> analyzeSpendingAsync(List<Expense> expenses, List<Budget> budgets) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = buildAnalysisPrompt(expenses, budgets);
            return callGroq(prompt);
        });
    }
    
    @CircuitBreaker(name = "groqApi", fallbackMethod = "fallback")
    @TimeLimiter(name = "groqApi")
    public CompletableFuture<String> suggestBudgetAsync(List<Expense> expenses, BigDecimal monthlyIncome) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = buildBudgetSuggestionPrompt(expenses, monthlyIncome);
            return callGroq(prompt);
        });
    }

    @CircuitBreaker(name = "groqApi", fallbackMethod = "fallback")
    @TimeLimiter(name = "groqApi")
    public CompletableFuture<String> forecastSpendingAsync(List<Expense> expenses, List<Budget> budgets) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = buildForecastPrompt(expenses, budgets);
            return callGroq(prompt);
        });
    }

    @CircuitBreaker(name = "groqApi", fallbackMethod = "fallback")
    @TimeLimiter(name = "groqApi")
    public CompletableFuture<String> chatAsync(String userMessage, List<Expense> expenses, List<Budget> budgets) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = buildChatPrompt(userMessage, expenses, budgets);
            return callGroq(prompt);
        });
    }


    private String buildAnalysisPrompt(List<Expense> expenses, List<Budget> budgets) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful financial advisor. All amounts below are in Indian Rupees (INR). ");
        sb.append("Always use the ₹ symbol when referring to amounts — never use $ or any other currency symbol.\n\n");
        sb.append("Analyze this user's spending and give 3-4 short, practical tips.\n\n");
        sb.append("Budgets:\n");
        budgets.forEach(b -> sb.append("- ").append(b.getCategory()).append(": limit ₹")
                .append(b.getLimitAmount()).append("\n"));
        sb.append("\nExpenses:\n");
        expenses.forEach(e -> sb.append("- ").append(e.getCategory()).append(": ₹")
                .append(e.getAmount()).append(" on ").append(e.getExpenseDate()).append("\n"));
        return sb.toString();
    }

    private String buildBudgetSuggestionPrompt(List<Expense> expenses, BigDecimal monthlyIncome) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a financial advisor. All amounts are in Indian Rupees (INR). ");
        sb.append("Always use the ₹ symbol, never $.\n\n");
        sb.append("The user's monthly income is ₹").append(monthlyIncome).append(".\n\n");
        sb.append("Here is their past spending history by category:\n");
        expenses.forEach(e -> sb.append("- ").append(e.getCategory()).append(": ₹")
                .append(e.getAmount()).append(" on ").append(e.getExpenseDate()).append("\n"));
        sb.append("\nBased on their income and past spending pattern, suggest a sensible monthly budget limit ");
        sb.append("for each category they've spent in. Keep it practical — total suggested budgets should not ");
        sb.append("exceed roughly 80% of their income, leaving room for savings. Present it as a short list, ");
        sb.append("category by category, with a one-line reason for each.");
        return sb.toString();
    }

    private String buildForecastPrompt(List<Expense> expenses, List<Budget> budgets) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a financial advisor. All amounts are in Indian Rupees (INR). ");
        sb.append("Always use the ₹ symbol, never $.\n\n");
        sb.append("Today's date context: assume we are mid-month.\n\n");
        sb.append("Budgets:\n");
        budgets.forEach(b -> sb.append("- ").append(b.getCategory()).append(": limit ₹")
                .append(b.getLimitAmount()).append("\n"));
        sb.append("\nSpending so far this month:\n");
        expenses.forEach(e -> sb.append("- ").append(e.getCategory()).append(": ₹")
                .append(e.getAmount()).append(" on ").append(e.getExpenseDate()).append("\n"));
        sb.append("\nBased on their spending velocity so far, predict whether they are likely to exceed ");
        sb.append("their budget by the end of the month for each category. Give a short verdict per category ");
        sb.append("(on track / at risk / will exceed) with a one-line reason, and an overall summary at the end.");
        return sb.toString();
    }
    
    private String buildChatPrompt(String userMessage, List<Expense> expenses, List<Budget> budgets) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful financial assistant answering a question about the user's own spending data. ");
        sb.append("All amounts are in Indian Rupees (INR). Always use the ₹ symbol, never $. ");
        sb.append("Answer naturally and concisely, using only the data provided below — don't invent numbers.\n\n");
        sb.append("Budgets:\n");
        budgets.forEach(b -> sb.append("- ").append(b.getCategory()).append(": limit ₹")
                .append(b.getLimitAmount()).append("\n"));
        sb.append("\nExpenses:\n");
        expenses.forEach(e -> sb.append("- ").append(e.getCategory()).append(": ₹")
                .append(e.getAmount()).append(" on ").append(e.getExpenseDate())
                .append(e.getDescription() != null ? " (" + e.getDescription() + ")" : "").append("\n"));
        sb.append("\nUser's question: ").append(userMessage);
        return sb.toString();
    }
    
    private String callGroq(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map response = webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }
    
 // Fallback signature must match: same params as the original + the Throwable
    private CompletableFuture<String> fallback(List<Expense> expenses, List<Budget> budgets, Throwable t) {
        return CompletableFuture.completedFuture(
                "AI analysis is temporarily unavailable. Please try again in a moment."
        );
    }

    private CompletableFuture<String> fallback(List<Expense> expenses, BigDecimal income, Throwable t) {
        return CompletableFuture.completedFuture(
                "AI budget suggestions are temporarily unavailable. Please try again in a moment."
        );
    }

    private CompletableFuture<String> fallback(String userMessage, List<Expense> expenses, List<Budget> budgets, Throwable t) {
        return CompletableFuture.completedFuture(
                "The AI assistant is temporarily unavailable. Please try again in a moment."
        );
    }
    
}
