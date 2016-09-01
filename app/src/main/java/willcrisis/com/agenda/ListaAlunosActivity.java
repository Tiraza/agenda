package willcrisis.com.agenda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import willcrisis.com.agenda.adapter.AlunoAdapter;
import willcrisis.com.agenda.dao.AlunoRealmDAO;
import willcrisis.com.agenda.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;
    private AlunoRealmDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        dao = new AlunoRealmDAO(this);

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);

                Intent intent = new Intent(ListaAlunosActivity.this, NovoAlunoActivity.class);
                intent.putExtra("alunoId", aluno.getId());
                startActivity(intent);
            }
        });

        Button btnNovo = (Button) findViewById(R.id.lista_alunos_novo);
        btnNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListaAlunosActivity.this, NovoAlunoActivity.class);
                startActivity(intent);
            }
        });

        registerForContextMenu(listaAlunos);

        if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lista_alunos_enviar:
                EnvioAlunosTask task = new EnvioAlunosTask(this);
                task.execute();
                break;
            case R.id.menu_lista_alunos_provas:
                Intent intent = new Intent(this, ProvasActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_lista_alunos_mapa:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    Intent mapa = new Intent(this, MapaActivity.class);
                    startActivity(mapa);
                }
                break;
        }
        return true;
    }

    private void listarAlunos() {
        List<Aluno> alunos = getAlunos();

        AlunoAdapter adapter = new AlunoAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    private List<Aluno> getAlunos() {
        return dao.listar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listarAlunos();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(contextMenuInfo.position);

        MenuItem ligar = menu.add("Ligar para aluno");
        ligar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intent);
                }

                return false;
            }
        });

        MenuItem mandarSms = menu.add("Mandar SMS");
        Intent intentSms = new Intent(Intent.ACTION_VIEW);
        intentSms.setData(Uri.parse("sms:" + aluno.getTelefone()));
        mandarSms.setIntent(intentSms);

        MenuItem acharNoMapa = menu.add("Achar no mapa");
        Intent intentGeo = new Intent(Intent.ACTION_VIEW);
        intentGeo.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        acharNoMapa.setIntent(intentGeo);

        MenuItem visitarSite = menu.add("Visitar site");
        String site = aluno.getSite();
        if (!(site.startsWith("http://") || site.startsWith("https://"))) {
            site = "http://" + site;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(site));

        visitarSite.setIntent(intent);

        MenuItem excluir = menu.add("Excluir");
        excluir.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                dao.excluir(aluno);
                listarAlunos();
                return false;
            }
        });
    }
}
