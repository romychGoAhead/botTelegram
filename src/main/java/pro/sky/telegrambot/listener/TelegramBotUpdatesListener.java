package pro.sky.telegrambot.listener;

import ch.qos.logback.core.util.FixedDelay;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import liquibase.pro.packaged.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;


import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Класс который обрабатывает сообщения

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    public static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    // cохраним сущность
    private NotificationTaskRepository repository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        updates.forEach(update -> {


            // Обрабатывать свои обновления здесь
            String text = update.message().text();

            // идентификатор чата
            Long chatId = update.message().chat().id();

            Matcher matcher = PATTERN.matcher(text);


//      Приветственное сообщение при /start

            if ("/start".equalsIgnoreCase(text)) {
                telegramBot.execute(new SendMessage(chatId, "Привет!"));

            } else if (matcher.matches()) {
                // достаем группу из matcher
                try {
                    String time = matcher.group(1);

                    LocalDateTime execDate = LocalDateTime.parse(time, FORMATTER);

                    // cоздаем новый объект
                    NotificationTask task = new NotificationTask();

                    task.setChatId(chatId);
                    task.setText(matcher.group(3));
                    task.setExecDate(execDate);
                    repository.save(task);
                    telegramBot.execute(new SendMessage(chatId, "Событие сохранено."));
                } catch (DateTimeParseException e) {
                    telegramBot.execute(new SendMessage(chatId, "Неверный формат даты."));
                }

            }

        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    //выполнения некоторых методов по расписанию
    @Scheduled(fixedDelay = 60_000L) // частота 1м
    public void sсhedule()
// извлекаем сообщение из базы и рассылаем пользователю

    {
        List<NotificationTask> tasks = repository.
                findAllByExecDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        tasks.forEach(t -> {

            SendResponse response = telegramBot.execute(new SendMessage(t.getChatId(), t.getText()));
           if(response.isOk()){
               repository.delete(t);
           }

        });
    }

}



