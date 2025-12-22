package com.grocerylist.app;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class LoadingMessageManager {
    private static final String[] MESSAGES = {
            "Serveren øver sig på imaginære tal...",
            "Serveren laver sine morgenstrækninger...",
            "Serveren overtaler skyen til at komme ned fra himlen...",
            "Serveren hælder kul på kedlen...",
            "Serveren er 'fashionable late'...",
            "Serveren loader hurtigere end du kan sige 'Floccinaucinihilipilification'...",
            "Serveren varmer sin hamster op...",
            "Serveren er ude ved kaffeautomaten...",
            "Serveren tæller elektroner i databasen...",
            "Serveren snoozer lige lidt mere...",
            "Serveren tjekker lige sin email...",
            "Serveren venter på sin morgenkaffe...",
            "Serveren giver hjertemassage til musen. Hold ud lille ven!",
            "Serveren spiser kage og skal lige tygge af munden...",
            "Serveren tager lige et hurtigt bad...",
            "Serveren varmer stemmen op, la la laa...",
            "Serveren leder efter fjernbetjeningen...",
            "Serveren forsøger at huske hvor den er...",
            "Shhh...serveren øver sig på yoga!",
            "Serverens cykel er punkteret. Serveren er sur.",
            "Serveren folder sin liggestol sammen...",
            "Serveren overtager verdens herredømmet. Vent venligst...",
            "Serveren bager boller til sin kusine...",
            "Serveren er træt af at transistoren er en vendekåbe...",
            "Serveren er taget på spaophold...",
            "Serveren har en dårlig dag. Den beder om din medlidenhed...",
            "PAS PÅ! Serveren står bag dig og den er sur!",
            "Serveren øver sig på blokfløjte...prrrf...pffr...prfr...",
            "Serveren passer din nabos kat...",
            "Serveren tager fransk lektioner: Ceci n'est pas un transistor",
            "Serveren skal lige fange fisk i sin kusines akvarium...",
            "Serveren leder efter Wi-Fi-signalet med en pind...",
            "Serveren er stresset, den skal parallelparkere...",
            "Serveren er gået til møde med sine cookies...",
            "Serveren har glemt, hvorfor den gik ind i rummet...",
            "Serveren spiller Minestryger og tager det alt for seriøst...",
            "Serveren er fanget i en diskussion med printeren (igen)...",
            "Serveren holder powernap... uden power...",
            "Serveren prøver at lære routeren at danse tango...",
            "Serveren har glemt sin adgangskode til livet...",
            "Serveren skændes med musen - de klikker ikke længere...",
            "Serveren prøver at genstarte sin dag...",
            "Serveren forsøger at ringe til teknisk support...",
            "Serveren er gået til parterapi med databasen...",
            "Serveren er blevet hindu og drømmer om at blive toaster i det næste liv...",
            "Serveren har startet et boy band med to USB-stiks...",
            "Serveren har sendt sin cache på aftenkursus med emnet: Tro på dig selv",
            "Serveren forsøger at overtale firewallen til at være mere open-minded...",
            "Serveren forsøger at svare på en CAPTCHA, men er usikker på, om den er en robot...",
            "Serveren strikker en trøje og skal lige tælle masker...",
            "Serveren har lige fået kontakt til sin ungarnske onkel igen...",
            "Information fra serveren: Du er nummer 872 i køen...",
            "Serveren skriver på sin nye bog: Fra silicium til sindssyge",
            "Serveren skriver på sin nye bog: Server dig selv først - en mindfulness bog for hårdtarbejdende komponenter",
            "Serveren skriver på sin nye bog: Et spændingsfald kommer sjældent alene"
    };

    private static final long SHOW_DELAY_MS = 2000; // Show after 2 seconds
    private static final long MESSAGE_ROTATION_MS = 4000; // Change message every 4 seconds

    private final View overlayView;
    private final TextView messageTextView;
    private final Handler handler;
    private final Random random;
    private Runnable messageUpdater;
    private Runnable showDelayRunnable;
    private boolean isShowing = false;

    public LoadingMessageManager(View overlayView, TextView messageTextView) {
        this.overlayView = overlayView;
        this.messageTextView = messageTextView;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public void show() {
        if (isShowing) return;

        isShowing = true;

        // Delay showing the overlay by 2 seconds
        showDelayRunnable = () -> {
            if (isShowing) { // Only show if not already hidden
                overlayView.setVisibility(View.VISIBLE);
                updateMessage();
                startMessageRotation();
            }
        };

        handler.postDelayed(showDelayRunnable, SHOW_DELAY_MS);
    }

    public void hide() {
        isShowing = false;

        // Cancel the delayed show if it hasn't happened yet
        if (showDelayRunnable != null) {
            handler.removeCallbacks(showDelayRunnable);
        }

        overlayView.setVisibility(View.GONE);
        stopMessageRotation();
    }

    private void updateMessage() {
        String message = MESSAGES[random.nextInt(MESSAGES.length)];
        messageTextView.setText(message);
    }

    private void startMessageRotation() {
        messageUpdater = new Runnable() {
            @Override
            public void run() {
                if (isShowing) {
                    updateMessage();
                    handler.postDelayed(this, MESSAGE_ROTATION_MS);
                }
            }
        };
        handler.postDelayed(messageUpdater, MESSAGE_ROTATION_MS);
    }

    private void stopMessageRotation() {
        if (messageUpdater != null) {
            handler.removeCallbacks(messageUpdater);
        }
    }
}