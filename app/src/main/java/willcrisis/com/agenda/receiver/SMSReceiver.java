package willcrisis.com.agenda.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.widget.Toast;

import willcrisis.com.agenda.R;
import willcrisis.com.agenda.dao.AlunoRealmDAO;
import willcrisis.com.agenda.modelo.Aluno;

@SuppressLint("NewApi")
public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
        byte[] pdu = (byte[]) pdus[0];
        String format = (String) intent.getSerializableExtra("format");

        SmsMessage sms = SmsMessage.createFromPdu(pdu, format);
        String telefone = sms.getDisplayOriginatingAddress();

        AlunoRealmDAO dao = new AlunoRealmDAO(context);
        Aluno aluno = dao.getQuery().equalTo("telefone", telefone).findFirst();
        if (aluno != null) {
            Toast.makeText(context, "Chegou um SMS de um aluno!", Toast.LENGTH_SHORT).show();
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.msg);
            mediaPlayer.start();
        }
    }
}
