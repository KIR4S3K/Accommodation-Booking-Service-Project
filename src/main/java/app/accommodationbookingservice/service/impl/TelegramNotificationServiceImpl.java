package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.service.NotificationService;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TelegramNotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory
            .getLogger(TelegramNotificationServiceImpl.class);

    private final OkHttpClient client;
    private final String token;
    private final String chatId;

    public TelegramNotificationServiceImpl(
            OkHttpClient client,
            @Value("${telegram.bot-token}") String token,
            @Value("${telegram.chat-id}") String chatId) {
        this.client = client;
        this.token = token;
        this.chatId = chatId;

        if (!StringUtils.hasText(token) || !StringUtils.hasText(chatId)) {
            logger.warn("TelegramNotificationService configured without token or chatId; "
                    + "notifications will be disabled");
        }
    }

    @Override
    public void notify(String message) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(chatId)) {
            return;
        }

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.telegram.org")
                .addPathSegment("bot" + token)
                .addPathSegment("sendMessage")
                .addQueryParameter("chat_id", chatId)
                .addQueryParameter("text", message)
                .build();

        Request req = new Request.Builder().url(url).get().build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                logger.warn("Failed to send Telegram message. Response code: {}, body: {}",
                        res.code(), res.body() != null ? res.body().string() : "n/a");
            }
        } catch (Exception e) {
            logger.error("Exception while sending Telegram message", e);
        }
    }
}
