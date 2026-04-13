package ps.emall.orderhub.common.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Get message by key using current locale
     */
    public String getMessage(MessageKey key) {
        return getMessage(key.getKey());
    }

    /**
     * Get message by key with arguments using current locale
     */
    public String getMessage(MessageKey key, Object... args) {
        return getMessage(key.getKey(), args);
    }

    /**
     * Get message by key using current locale
     */
    public String getMessage(String key) {
        return getMessage(key, new Object[]{});
    }

    /**
     * Get message by key with arguments using current locale
     */
    public String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            log.warn("Message not found for key: {} and locale: {}", key, locale);
            return key;
        }
    }

    /**
     * Get message by key using specific locale
     */
    public String getMessage(MessageKey key, Locale locale) {
        return getMessage(key.getKey(), locale);
    }

    /**
     * Get message by key with arguments using specific locale
     */
    public String getMessage(MessageKey key, Locale locale, Object... args) {
        return getMessage(key.getKey(), locale, args);
    }

    /**
     * Get message by key using specific locale
     */
    public String getMessage(String key, Locale locale) {
        return getMessage(key, locale, new Object[]{});
    }

    /**
     * Get message by key with arguments using specific locale
     */
    public String getMessage(String key, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            log.warn("Message not found for key: {} and locale: {}", key, locale);
            return key;
        }
    }

    /**
     * Get message in English
     */
    public String getMessageInEnglish(MessageKey key, Object... args) {
        return getMessage(key.getKey(), Locale.ENGLISH, args);
    }

    /**
     * Get message in Arabic
     */
    public String getMessageInArabic(MessageKey key, Object... args) {
        return getMessage(key.getKey(), new Locale("ar"), args);
    }

    /**
     * Get current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}