package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final static String WELCOME_TEXT =
            "Привет! Это бот:)! Для планирования задачи отправьте её в формате: 01.01.2022 20:00 Сделать домашнюю работу";

    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {

            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                // Process your updates here
                String text = update.message().text();
                Long chatId = update.message().chat().id();
//      Приветственное сообщение при /start




