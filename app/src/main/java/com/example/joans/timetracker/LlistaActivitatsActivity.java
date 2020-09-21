package com.example.joans.timetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;

import nucli.Activitat;
import nucli.Projecte;
import nucli.Tasca;


/**
 * Mostra la llista de projectes i tasques filles del projecte pare actual.
 * Inicialment, en engegar la aplicació per primer cop (o quan s'ha engegat el
 * telèfon) mostra doncs les activitats del primer "nivell", considerant que hi
 * ha un projecte arrel invisible de cara als usuaris que és el seu projecte
 * pare.
 * <p>
 * Juntament amb el nom de projecte o tasca se'n mostra el temps total
 * cronometrat. I mentre que s'està cronometrant alguna tasca d'aquestes, o bé
 * descendent d'un dels projectes mostrats, el seu temps es veu que es va
 * actualitzant. Per tal de mostrar nom i durada mitjançant les
 * <code>ListView</code> d'Android, hem hagut de dotar la classe
 * <code>DadesActivitat</code> d'un mètode <code>toString</code> que és invocat
 * per un objecte de classe <code>Adapter</code>, que fa la connexió entre la
 * interfase i les dades que mostra.
 * <p>
 * També gestiona els events que permeten navegar per l'arbre de projectes i
 * tasques :
 * <ul>
 * <li>un click sobre un element de la llista baixa de nivell: passa a mostrar
 * els seus "fills", la siguin subprojectes i tasques (si era un projecte) o
 * intervals (si era tasca)</li>
 * <li>tecla o botó "back" puja de nivell: anem al projecte para del les
 * activitats de les quals mostrem les dades, o si ja són del primer nivell i no
 * podem pujar més, anem a la pantalla "Home"</li>
 * </ul>
 * I també events per tal de cronometrar una tasca p parar-ne el cronòmetre,
 * mitjançant un click llarg.
 * <p>
 * Totes dues funcions no són dutes a terme efectivament aquí sinó a
 * <code>GestorArbreActivitat</code>, que manté l'arbre de tasques, projectes i
 * intervals en memòria. Cal fer-ho així per que Android pot eliminar (
 * <code>destroy</code>) la instància d'aquesta classe quan no és visible per
 * que estem interactuant amb alguna altra aplicació, si necessita memòria. En
 * canvi, un servei com és <code>GestorArbreActivitats</code> només serà
 * destruït en circumstàncies extremes. La comunicació amb el servei es fa
 * mitjançant "intents", "broadcast" i una classe privada "receiver".
 *
 * @author joans & the team
 * @version 6 febrer 2012
 */

public class LlistaActivitatsActivity extends AppCompatActivity implements ListAdapter.PosActivityListener {

    Projecte fatherProject = null;


    private SimpleDateFormat formatTimeElapsed =
            new SimpleDateFormat("HH:mm:ss", Locale.US);

    /**
     * Nom de la classe per fer aparèixer als missatges de logging del LogCat.
     *
     * @see Log
     */
    private final String tag = this.getClass().getSimpleName();

    /**
     * Grup de vistes (controls de la interfase gràfica) que consisteix en un
     * <code>TextView</code> per a cada activitat a mostrar.
     */
    private ListView arrelListView;

    /**
     * Adaptador necessari per connectar les dades de la llista de projectes i
     * tasques filles del projecte pare actual, amb la interfase, segons el
     * mecanisme estàndard d'Android.
     * <p>
     * Per tal de fer-lo servir, cal que la classe <code>DadesActivitat</code>
     * tingui un mètode <code>toString</code> que retornarà l'string a mostrar
     * en els TextView (controls de text) de la llista ListView.
     */
    //private ArrayAdapter<DadesActivitat> aaAct;
    private ListAdapter aaAct;

    /**
     * Llista de dades de les activitats (projectes i tasques) mostrades
     * actualment, filles del (sub)projecte on estem posicionats actualment.
     */
    private List<DadesActivitat> llistaDadesActivitats;

    /**
     * Identificador del View les propietats del qual (establertes amb l'editor
     * XML de la interfase gràfica) estableixen com es mostra cada un els items
     * o elements de la llista d'activitats (tasques i projectes) referenciada
     * per l'adaptador {@link #aaAct}. Si per comptes haguéssim posat
     * <code>android.R.layout.simple_list_item_1</code> llavors fora la
     * visualització per defecte d'un text. Ara la diferència es la mida de la
     * tipografia.
     */
    private int layoutID = R.layout.textview_llista_activitats;

    /**
     * Flag que ens servirà per decidir fer que si premem el botó/tecla "back"
     * quan estem a l'arrel de l'arbre de projectes, tasques i intervals : si és
     * que si, desem l'arbre i tornem a la pantalla "Home", sinó hem d'anar al
     * projecte pare del pare actual (pujar de nivell).
     */
    private boolean activitatPareActualEsArrel;

    private Stack<String> stackPosition = new Stack<>();

    private Projecte emptyProject;

    @Override
    public void activityClicked(int position) {
        Log.i(tag, "activity Clicked!");

        Intent intent = new Intent(LlistaActivitatsActivity.BAIXA_NIVELL);
        intent.putExtra("posicio", position);
        sendBroadcast(intent);
        /**
            BEGIN:CODED BY TEAM
        **/
        if(llistaDadesActivitats.get(position).isProjecte()) {
            //STACK
            stackPosition.push(llistaDadesActivitats.get(position).getNom());
            Log.i(tag, Arrays.toString(stackPosition.toArray()));

            //Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.getMenu().clear();

            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(llistaDadesActivitats.get(position).getNom());
                //Previous view
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            if(llistaDadesActivitats.get(position).getDurada() > 0) {
                fatherProject = llistaDadesActivitats.get(position).getFatherProject();
                //patata = ((Projecte)llistaDadesActivitats.get(position)).getProjectePare();
            } else {
                emptyProject = (Projecte) llistaDadesActivitats.get(position).getCurrentActivity();
            }
            sendBroadcast(new Intent(LlistaActivitatsActivity.DONAM_FILLS));

        } else if (llistaDadesActivitats.get(position).isTasca()) {
            Intent intentToTask = new Intent(LlistaActivitatsActivity.this, LlistaIntervalsActivity.class);
            intentToTask.putExtra("TASK_OBJECT", llistaDadesActivitats.get(position).getCurrentActivity());
            startActivity(intentToTask);
        } else {
            Log.i(tag, "Activitat no projecte ni tasca");
        }
        /**
            END:CODED BY TEAM
        **/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.infoButton:
                infoClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
        FUNCTION CODED BY TEAM
    **/
    public void infoClicked() {
        //HH: hours MM: Minutes SS: seconds
        TimeZone timezone = TimeZone.getTimeZone("GMT+0");
        formatTimeElapsed.setTimeZone(timezone); //set time zone to GMT+0

        if(!llistaDadesActivitats.isEmpty()) {
            fatherProject = llistaDadesActivitats.get(0).getFatherProject();
            AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(this);
            alertDialogInfo.setMessage("Nom:\n" + fatherProject.getNom() +"\n"
                    + "Descripció:\n"+ fatherProject.getDescripcio()+ "\n"
                    + "Data Inicial:\n" + fatherProject.getDataInicial() +"\n"
                    + "Data Final:\n" + fatherProject.getDataFinal() + "\n"
                    + "Durada:\n" + formatTimeElapsed.format(fatherProject.getDurada()));
            AlertDialog alertDialog = alertDialogInfo.create();
            alertDialog.show();
        } else {
            AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(this);
            alertDialogInfo.setMessage("Nom:\n" + emptyProject.getNom() +"\n"
                    + "Descripció:\n"+ emptyProject.getDescripcio()+ "\n"
                    + "Data Inicial:\n" + emptyProject.getDataInicial() +"\n"
                    + "Data Final:\n" + emptyProject.getDataFinal() + "\n"
                    + "Durada:\n" + formatTimeElapsed.format(emptyProject.getDurada()));
            AlertDialog alertDialog = alertDialogInfo.create();
            alertDialog.show();
        }
    }

    @Override
    public void playStopClicked(int position) {
        Intent intent;
        if(llistaDadesActivitats.get(position).isCronometreEngegat()) {
            intent = new Intent(LlistaActivitatsActivity.PARA_CRONOMETRE);
        } else {
            intent = new Intent(LlistaActivitatsActivity.ENGEGA_CRONOMETRE);
        }
        intent.putExtra("posicio", position);
        sendBroadcast(intent);
    }


    /**
     * Rep els "intents" que envia <code>GestorArbreActivitats</code> amb les
     * dades de les activitats a mostrar. El receptor els rep tots (no hi ha cap
     * filtre) per que només se'n n'hi envia un, el "TE_FILLS".
     *
     * @author joans
     * @version 6 febrer 2012
     */
    private class Receptor extends BroadcastReceiver {
        /**
         * Nom de la classe per fer aparèixer als missatges de logging del
         * LogCat.
         *
         * @see Log
         */
        private final String tag = this.getClass().getCanonicalName();

        /**
         * Gestiona tots els intents enviats, de moment només el de la
         * acció TE_FILLS. La gestió consisteix en actualitzar la llista
         * de dades que s'està mostrant mitjançant el seu adaptador.
         *
         * @param context
         * @param intent
         * objecte Intent que arriba per "broadcast" i del qual en fem
         * servir l'atribut "action" per saber quina mena de intent és
         * i els extres per obtenir les dades a mostrar i si el projecte
         * actual és l'arrel de tot l'arbre o no
         *
         */

        /**
        FUNCTION CODED BY TEAM
        **/
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(tag, "onReceive");

            if (intent.getAction().equals(GestorArbreActivitats.TE_FILLS)) {
                activitatPareActualEsArrel = intent.getBooleanExtra(
                        "activitat_pare_actual_es_arrel", false);
                // obtenim la nova llista de dades d'activitat que ve amb
                // l'intent
                @SuppressWarnings("unchecked")
                ArrayList<DadesActivitat> llistaDadesAct =
                        (ArrayList<DadesActivitat>) intent
                                .getSerializableExtra("llista_dades_activitats");
                llistaDadesActivitats = llistaDadesAct;

                //Toolbar
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                toolbar.getMenu().clear();

                if(!stackPosition.isEmpty()) {
                    getSupportActionBar().setTitle(stackPosition.lastElement());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                } else {
                    getSupportActionBar().setTitle(R.string.treeHome);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setDisplayShowHomeEnabled(false);
                }

                aaAct.clear();
                aaAct.add(llistaDadesAct);
                // això farà redibuixar el ListView
                aaAct.notifyDataSetChanged();
                Log.d(tag, "mostro els fills actualitzats");
            } else {
                // no pot ser
                assert false : "intent d'acció no prevista";
            }
        }
    }

    /**
     * Objecte únic de la classe {@link Receptor}.
     */
    private Receptor receptor;

    // Aquests són els "serveis", identificats per un string, que demana
    // aquesta classe a la classe Service GestorArbreActivitats, en funció
    // de la interacció de l'usuari:

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code> la
     * llista de les dades dels fills de l'activitat actual, que és un projecte.
     * Aquesta llista arribarà com a dades extres d'un Intent amb la "acció"
     * TE_FILLS.
     *
     * @see GestorArbreActivitats.Receptor
     */
    public static final String DONAM_FILLS = "Donam_fills";

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code>
     * que engegui el cronòmetre de la tasca clicada.
     */
    public static final String ENGEGA_CRONOMETRE = "Engega_cronometre";

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code>
     * que pari el cronòmetre de la tasca clicada.
     */
    public static final String PARA_CRONOMETRE = "Para_cronometre";

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code>
     * que escrigui al disc l'arbre actual.
     */
    public static final String DESA_ARBRE = "Desa_arbre";

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code>
     * que el projecte pare de les activitats actuals sigui el projecte que
     * l'usuari ha clicat.
     */
    public static final String BAIXA_NIVELL = "Baixa_nivell";

    /**
     * String que defineix l'acció de demanar a <code>GestorActivitats</code>
     * que el projecte pare passi a ser el seu pare, o sigui, pujar de nivell.
     */
    public static final String PUJA_NIVELL = "Puja_nivell";

    /**
     * En voler pujar de nivell quan ja som a dalt de tot vol dir que l'usuari
     * desitja "deixar de treballar del tot" amb la aplicació, així que "parem"
     * el servei <code>GestorActivitats</code>, que vol dir parar el cronòmetre
     * de les tasques engegades, si n'hi ha alguna, desar l'arbre i parar
     * (invocant <code>stopSelf</code>) el servei. Tot això es fa a
     * {@link GestorArbreActivitats#paraServei}.
     */
    public static final String PARA_SERVEI = "Para_servei";

    public static final String CREAR_PROJECTE = "crear_projecte";

    public static final String CREAR_TASCA = "crear_tasca";

    /**
     * Quan aquesta Activity es mostra després d'haver estat ocultada per alguna
     * altra Activity cal tornar a fer receptor i el seu filtre per que atengui
     * als intents que es redifonen (broadcast). I també engegar el servei
     * <code>GestorArbreActivitats</code>, si és la primera vegada que es mostra
     * aquesta Activity. En fer-ho, el servei enviarà la llista de dades de les
     * activitats filles del projecte arrel actual.
     */
    @Override
    public final void onResume() {
        Log.i(tag, "onResume");

        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction(GestorArbreActivitats.TE_FILLS);
        receptor = new Receptor();
        registerReceiver(receptor, filter);

        // Crea el servei GestorArbreActivitats, si no existia ja. A més,
        // executa el mètode onStartCommand del servei, de manera que
        // *un cop creat el servei* = havent llegit ja l'arbre si es el
        // primer cop, ens enviarà un Intent amb acció TE_FILLS amb les
        // dades de les activitats de primer nivell per que les mostrem.
        // El que no funcionava era crear el servei (aquí mateix o
        // a onCreate) i després demanar la llista d'activiats a mostrar
        // per que startService s'executa asíncronament = retorna de seguida,
        // i la petició no trobava el servei creat encara.
        startService(new Intent(this, GestorArbreActivitats.class));

        super.onResume();
        Log.i(tag, "final de onResume");
    }

    /**
     * Just abans de quedar "oculta" aquesta Activity per una altra, anul·lem el
     * receptor de intents.
     */
    @Override
    public final void onPause() {
        Log.i(tag, "onPause");

        unregisterReceiver(receptor);

        super.onPause();
    }

    /**
     * Estableix com a activitats a visualitzar les filles del projecte
     * arrel, així com els dos listeners que gestionen els
     * events de un click normal i un click llarg. El primer serveix per navegar
     * "cap avall" per l'arbre, o sigui, veure els fills d'un projecte o els
     * intervals d'una tasca. El segon per cronometrar, en cas que haguem clicat
     * sobre una tasca.
     *
     * @param savedInstanceState
     *            de tipus Bundle, però no el fem servir ja que el pas de
     *            paràmetres es fa via l'objecte aplicació
     *            <code>TimeTrackerApplication</code>.
     */

    //NEW
    private View.OnClickListener createActivityListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(LlistaActivitatsActivity.this, NewActivity.class);
            startActivity(intent);
        }
    };

    /**
        FUNCTION CODED BY TEAM
    **/
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate");
        setContentView(R.layout.activity_llista_activitats);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.getMenu().clear();

        if(getSupportActionBar() != null) {
            if(fatherProject == null) {
                getSupportActionBar().setTitle(R.string.treeHome);
            }
        }

        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton addButton = (ImageButton) findViewById(R.id.createButton);
        addButton.setOnClickListener(createActivityListener);
        //llistaDadesActivitats = new ArrayList<DadesActivitat>();
        //aaAct = new ArrayAdapter<DadesActivitat>(this, layoutID,
        //        llistaDadesActivitats);
        aaAct = new ListAdapter();
        recyclerView.setAdapter(aaAct);
        aaAct.setPosActivityListener(this);

        // Un "long click" serveix per cronometrar, si es tracta d'una tasca.
        // Si es un projecte, no fara res.

    }

    //Menu Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(!stackPosition.isEmpty())
            inflater.inflate(R.menu.menu, menu);
        else
            inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Gestor de l'event de prémer la tecla 'enrera' del D-pad. El que fem es
     * anar "cap amunt" en l'arbre de tasques i projectes. Si el projecte pare
     * de les activitats que es mostren ara no és nul (n'hi ha), 'pugem' per
     * mostrar-lo a ell i les seves activitats germanes. Si no n'hi ha, paro el
     * servei, deso l'arbre (equivalent a parar totes les tasques que s'estiguin
     * cronometrant) i pleguem de la aplicació.
     */
     
    
    @Override
    public final void onBackPressed() {
        Log.i(tag, "onBackPressed");

        String lastElement = stackPosition.lastElement();
        Log.i(tag, lastElement);
        /**
            BEGIN:CODED BY TEAM
        **/
        if(stackPosition.size() == 1) {
            stackPosition.pop();
            Log.i(tag, Arrays.toString(stackPosition.toArray()));
        } else {
            stackPosition.pop();
            Log.i(tag, Arrays.toString(stackPosition.toArray()));
        }
        /**
            END:CODED BY TEAM
        **/

        if (activitatPareActualEsArrel) {
            Log.d(tag, "parem servei");
            sendBroadcast(new Intent(LlistaActivitatsActivity.PARA_SERVEI));
            super.onBackPressed();
        } else {
            sendBroadcast(new Intent(LlistaActivitatsActivity.PUJA_NIVELL));
            Log.d(tag, "enviat intent PUJA_NIVELL");
            sendBroadcast(new Intent(LlistaActivitatsActivity.DONAM_FILLS));
            Log.d(tag, "enviat intent DONAM_FILLS");
        }
    }

    // D'aqui en avall els mètodes que apareixen són simplement sobrecàrregues
    // de mètodes de Activity per tal que es mostri un missatge de logging i
    // d'aquesta manera puguem entendre el cicle de vida d'un objecte d'aquesta
    // classe i depurar errors de funcionament de la interfase (on posar què).

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     *
     * @param savedInstanceState
     *            objecte de classe Bundle, que no fem servir.
     */
    @Override
    public final void onSaveInstanceState(final Bundle savedInstanceState) {
        Log.i(tag, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Aquesta funció es crida després de <code>onCreate</code> quan hi ha un
     * canvi de configuració = rotar el mòbil 90 graus, passant de "portrait" a
     * apaisat o al revés.
     *
     * @param savedInstanceState
     *            Bundle que de fet no es fa servir.
     *
     * @see onConfigurationChanged
     */
    @Override
    public final void onRestoreInstanceState(final Bundle savedInstanceState) {
        Log.i(tag, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onStop() {
        Log.i(tag, "onStop");
        super.onStop();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onDestroy() {
        Log.i(tag, "onDestroy");
        super.onDestroy();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onStart() {
        Log.i(tag, "onStart");
        super.onStart();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onRestart() {
        Log.i(tag, "onRestart");
        super.onRestart();
    }

    /**
     * Mostra un missatge de logging en rotar 90 graus el dispositiu (o
     * simular-ho en l'emulador). L'event <code>configChanged</code> passa quan
     * girem el dispositiu 90 graus i passem de portrait a landscape (apaisat) o
     * al revés. Això fa que les activitats siguin destruïdes (
     * <code>onDestroy</code>) i tornades a crear (<code>onCreate</code>). En
     * l'emulador del dispositiu, això es simula fent Ctrl-F11.
     *
     * @param newConfig
     *            nova configuració {@link Configuration}
     */
    @Override
    public final void onConfigurationChanged(final Configuration newConfig) {
        Log.i(tag, "onConfigurationChanged");
        if (Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, newConfig.toString());
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
