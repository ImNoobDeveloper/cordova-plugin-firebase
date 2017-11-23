package org.apache.cordova.firebase;

import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Notification;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.Request;

public class FirebasePluginMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebasePlugin";

    private Manager getNotMgr() {
        return Manager.getInstance(this);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().isEmpty())
            return;


        Boolean esParaBorrar =  remoteMessage.getData().get("BORRAR").equals("SI");
        String idEven =  remoteMessage.getData().get("IDEV");
        Integer idNoti = 0;

        try{
            if (esParaBorrar) Integer.parseInt(idEven);
            idNoti = Integer.parseInt(remoteMessage.getData().get("IDNOTIFICACION"));
        }catch(NumberFormatException e){
            //descartamos la notificación
            return;
        }

        if (esParaBorrar) {

			if(android.os.Build.VERSION.SDK_INT >= 26){
				List<Notification> notifications = getNotMgr().getAll();

				for (Notification notification : notifications) {
				    try {
                        Options ops = notification.getOptions();
                        String data = (String)ops.getDict().get("data");
                        JSONObject job = new JSONObject(data);
                        if (job.getString("IDEV").equals(remoteMessage.getData().get("IDEV")))
                            notification.cancel();
                        //getNotMgr().clear(notification.getId());
                        //de.appplant.cordova.plugin.localnotification.LocalNotification.fireEvent("clear", notification);
                    }catch(JSONException je) {

                    }
				}
			}else{

                NotificationManager nMgr =  (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (TextUtils.isEmpty(idEven)) {
                    try{
                        nMgr.cancel(idNoti);
                    } catch (NullPointerException ex) {}

                } else
                    for(int i=100; i<=120; i++){

                        Integer idBorrable = Integer.parseInt(idEven.concat(String.valueOf(i)));

                        try{
                            nMgr.cancel(idBorrable);
                        } catch (NullPointerException ex) {}
                    }
			}

        }else{
            String titulo = remoteMessage.getData().get("TITULO");
            String cuerpo = remoteMessage.getData().get("CUERPO");

            String idCreador =  remoteMessage.getData().get("IDCREADOR");
            String grupo =  remoteMessage.getData().get("GRUPO");

            JSONObject obj = new JSONObject();

            try {
                obj.put("id", idNoti);
                obj.put("title", titulo);
                obj.put("text", cuerpo);
                if (!grupo.equals("")) {
					if (grupo.equals("EV"))
						obj.put("group", idEven + "EV");
					else
						obj.put("group", idCreador + "CR");
				}
                obj.put("data", new JSONObject(remoteMessage.getData()).toString());
                obj.put("vibrate", true);

                obj.put("actions", new JSONArray());
                obj.put("attachments", new JSONArray());
                obj.put("autoClear", true);
                obj.put("defaults", 0);
                obj.put("foreground", false);
                obj.put("groupSummary", false);
                obj.put("launch", true);
                obj.put("led", true);
                obj.put("lookscreen", true);
                obj.put("number", 0);
                obj.put("priority", 0);

                JSONObject proB = new JSONObject();
                proB.put("enabled", false);
                proB.put("value", 0);
                proB.put("maxValue", 100);
                proB.put("indeterminate", false);
                obj.put("progressBar", proB);

                obj.put("showWhen", true);
                obj.put("silent", false);
				
                obj.put("icon","res://icon.png");
                obj.put("smallIcon", "res://icon");
                obj.put("sound", true);

                JSONObject trig = new JSONObject();
                trig.put("type", "calendar");
                obj.put("trigger", trig);

                obj.put("wakeup", true);

                JSONObject meta = new JSONObject();
                meta.put("plugin","cordova-plugin-local-notifications");
                meta.put("version","0.9-beta");
                obj.put("meta", meta);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            Options options = new Options(obj);
            Request request = new Request(options);

            //de.appplant.cordova.plugin.notification.Notification notiInst =
            getNotMgr().schedule(request, TriggerReceiver.class);
        }
		/*
        if (!FirebasePlugin.inBackground()) {

            Bundle bundle = new Bundle();

            bundle.putInt("id",idNoti);
            bundle.putBoolean("BORRAR", esParaBorrar);
            FirebasePlugin.sendNotification(bundle);
        }
		*/
    }
}
