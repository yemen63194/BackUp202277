package com.example.carecareforeldres.api;
import okhttp3.*;

import java.io.IOException;

public class WhatsAppSender {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // Remplacez "{TOKEN}" par votre token et "{INSTANCE_ID}" par votre instance
        RequestBody body = new FormBody.Builder()
                .add("token", "4kv1xiah0sjkb4h4")
                .add("to", "")
                .add("body", "")
                .build();

        Request request = new Request.Builder()
                .url("https://api.ultramsg.com/instance80452/messages/chat")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();

        // Vérifiez la réponse
        if (!response.isSuccessful()) {
            throw new IOException("Erreur: " + response);
        } else {
            System.out.println("Message envoyé avec succès.");
        }

        // Assurez-vous de fermer la réponse pour libérer les ressources
        response.close();
    }

}
