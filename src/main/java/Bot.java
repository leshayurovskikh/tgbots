import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "game121995_bot";
    }

    @Override
    public String getBotToken() {
        return "8022317534:AAEXlBoNAncBWKpc91E0zAvLU6iwkkuk-5w";
    }

    private final Map<Long, Integer> userState = new HashMap<>(); // Состояние пользователя
    private final Map<String, String> userInfo = new HashMap<>();// хранение переменных
    private Map<String,Object> infoPVZ = new HashMap<>();

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            int currentState = userState.getOrDefault(chatId, 0); // Получаем текущее состояние пользователя

            switch (currentState) {
                case 0:
                    sendResponse(chatId, "Привет! Веди свой номер телефона в формате 7961905****");
                    userState.put(chatId, 1);
                    // Переходим к следующему состоянию
                    break;
                case 1:
                    String phoneNumber = messageText;
                    String httpToken = HttpClientExample.getToken(phoneNumber); //получаем временный токен
                    userInfo.put("Phone", phoneNumber);
                    userInfo.put("httpToken", httpToken);
                    sendResponse(chatId, "Отлично, вы ввели номер телефона " + messageText + "! Теперь введите полученный код?");

                    userState.put(chatId, 2);
                    break;
                case 2:
                    String code = messageText;
                    httpToken = userInfo.get("httpToken");
                    userInfo.put("xToken",HttpClientExample.validate(code, httpToken)); //получаем токен валидации кладем в мапу
                    sendResponse(chatId, "Спасибо! Вы ввели код  " + messageText + ". Введите id вашего пвз");
                    userState.put(chatId, 3);
                    break;
                case 3:
                    int idPvz = Integer.parseInt(messageText);
                    String adress = HttpClientExample.getPicpoints(userInfo.get("xToken"),idPvz);
                    sendResponse(chatId, "Спасибо за информацию! адрес Вашего пвз - " + adress + ".");
                    //String res = String.valueOf(HttpClientExample.getStars());
                   // sendResponse(chatId, "шк код клиента каторый поставил вам плохую оценку" + res);
                    //userState.remove(chatId); // Завершаем сессию, убираем состояние
                    userState.put(chatId, 4);
                    break;
                case 4:
                    userState.put(chatId, 5);
                default:
                    sendResponse(chatId, "Я не понимаю, пожалуйста, начните заново.");
                    userState.remove(chatId);

                    break;
            }
        }
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
