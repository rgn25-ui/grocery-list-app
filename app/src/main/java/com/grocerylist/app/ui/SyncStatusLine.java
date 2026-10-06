package com.grocerylist.app.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.grocerylist.app.R;

import java.util.Random;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The status line at the bottom of the main and list screens. Replaces the full-screen loading
 * overlay: the app works locally, so syncing should be visible but never block.
 * Shows, in order of priority:
 * - a sync in progress ("Synkroniserer ..."), switching to the fun messages if it takes a while,
 * - local changes not yet confirmed by the backend ("2 ændringer venter på at blive sendt"),
 * - otherwise the time of the last sync.
 * The background is green only when this phone is known to match the server in both directions:
 * no pending changes, no sync in progress, and the last sync succeeded. Otherwise it is red.
 */
public class SyncStatusLine {
    private static final String[] FUN_MESSAGES = {
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

    private static final long FUN_MESSAGE_DELAY_MS = 2000; // a sync shorter than this just says "Synkroniserer"
    private static final long FUN_MESSAGE_ROTATION_MS = 4000;

    private final TextView textView;
    private final Supplier<String> lastSyncInfo;
    private final LongSupplier lastSyncTime;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private int pendingCount;
    private boolean syncing;
    private String funMessage; // null until the sync has taken a while
    private boolean lastSyncFailed;

    private final Runnable showNextFunMessage = new Runnable() {
        @Override
        public void run() {
            if (syncing) {
                funMessage = FUN_MESSAGES[random.nextInt(FUN_MESSAGES.length)];
                render();
                handler.postDelayed(this, FUN_MESSAGE_ROTATION_MS);
            }
        }
    };

    public SyncStatusLine(TextView textView, Supplier<String> lastSyncInfo, LongSupplier lastSyncTime) {
        this.textView = textView;
        this.lastSyncInfo = lastSyncInfo;
        this.lastSyncTime = lastSyncTime;
        render();
    }

    public void setPendingCount(Integer count) {
        pendingCount = count != null ? count : 0;
        render();
    }

    public void setSyncing(boolean isSyncing) {
        if (isSyncing == syncing) {
            return;
        }
        syncing = isSyncing;
        funMessage = null;
        handler.removeCallbacks(showNextFunMessage);
        if (syncing) {
            handler.postDelayed(showNextFunMessage, FUN_MESSAGE_DELAY_MS);
        }
        render();
    }

    /** Called when a sync finishes, so a failed sync does not show as green. */
    public void setLastSyncFailed(Boolean failed) {
        lastSyncFailed = Boolean.TRUE.equals(failed);
        render();
    }

    /** Re-renders, e.g. in onResume, so "last synced" is up to date. */
    public void refresh() {
        render();
    }

    /** Call from onDestroy to stop the message rotation. */
    public void stop() {
        handler.removeCallbacks(showNextFunMessage);
    }

    private void render() {
        Context context = textView.getContext();

        // Text: what is happening right now
        if (syncing) {
            textView.setText(funMessage != null
                    ? "🔄 " + funMessage
                    : context.getString(R.string.sync_status_syncing));
        } else if (pendingCount > 0) {
            textView.setText(context.getResources()
                    .getQuantityString(R.plurals.sync_status_pending, pendingCount, pendingCount));
        } else {
            textView.setText(lastSyncInfo.get());
        }

        // Colour: green only when this phone is known to match the server in both directions
        boolean inSync = !syncing && !lastSyncFailed && pendingCount == 0 && lastSyncTime.getAsLong() > 0;
        if (inSync) {
            applyColors(R.color.sync_status_ok_bg, R.color.sync_status_ok_text);
        } else {
            applyColors(R.color.sync_status_pending_bg, R.color.sync_status_pending_text);
        }
    }

    private void applyColors(int backgroundRes, int textRes) {
        Context context = textView.getContext();
        textView.setBackgroundColor(ContextCompat.getColor(context, backgroundRes));
        textView.setTextColor(ContextCompat.getColor(context, textRes));
    }
}