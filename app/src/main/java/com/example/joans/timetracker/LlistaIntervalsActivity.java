package com.example.joans.timetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import nucli.Interval;
import nucli.Tasca;

/**
 * Mostra la llista d'intervals d'alguna tasca. De cada interval mostra les
 * dates inicial i final en format dd-MM-aa hh:mm. Si la tasca està sent
 * cronometrada, veiem com el temps i data final va canviant.
 * <p>
 * Aquesta Activity es torna la activa quan, navegant per l'arbre a
 * {@link LlistaActivitatsActivity}, fem un click normal sobre una tasca. En prémer la
 * tecla o botó 'back', 'pugem' un nivell a l'arbre i tornem a veure la tasca i
 * les seves activitats germanes.
 *
 * @author joans & the team
 * @version 6 febrer 2012
 */
public class LlistaIntervalsActivity extends AppCompatActivity implements Serializable {

    private Tasca fatherTask = null;

    private Tasca currentTask;

    private SimpleDateFormat formatTimeElapsed =
            new SimpleDateFormat("HH:mm:ss", Locale.US);


    /**
     * Llista de dades dels intervals de la tasca però havent fet un cast a la
     * classe abstracta <code>List</code> per tal de fer servir aquest atribut
     * conjuntament amb un <code>Adapter</code> d'Android.
     */
    private List<DadesInterval> llistaDadesIntervals;

    /**
     * Grup de vistes (controls de la interfase gràfica) que consisteix en un
     * <code>TextView</code> per a cada interval a mostrar.
     */
    private ListView intervalsListView;

    /**
     * Adaptador necessari per connectar les dades de la llista d'intervals de
     * la tasca pare actual, amb la interfase, segons el mecanisme estàndard de
     * <code>ListView</code> d'Android.
     * <p>
     * Per tal de fer-lo servir, he hagut d'afegir a la classe
     * <code>DadesInterval</code> tingui un mètode <code>toString</code> que
     * retornarà l'string a mostrar en els <code>TextView</code> (controls de
     * text) de la llista <code>ListView</code>.
     */
    private IntervalAdapter aaAct;

    /**
     * Identificador del View les propietats del qual (establertes amb l'editor
     * XML de la interfase gràfica) estableixen com es mostra cada un els items
     * o elements de la llista d'intervals referenciada per l'adaptador
     * {@link #aaAct}.
     *
     * @see LlistaActivitatsActivity#layoutID
     */
    private int layoutID = R.layout.textview_llista_intervals;

    /**
     * Nom de la classe per fer aparèixer als missatges de logging del LogCat.
     *
     * @see Log
     */
    private final String tag = this.getClass().getSimpleName();

    /**
     * Estableix com a intervals a visualitzar els de la tasca
     * <code>fatherTask</code>. Aquest mètode és invocat just a l'inici del cicle
     * de vida de la Activity.
     *
     * @param savedInstanceState
     *            de tipus Bundle, però no el fem servir ja que el pas de
     *            paràmetres es fa via l'objecte aplicació
     *            <code>TimeTrackerApplication</code>.
     * @see LlistaActivitatsActivity#onCreate
     */
     
    /**
            FUNCTION CODED BY TEAM
    **/
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate intervals");

        // Tot aquest mecanisme és anàleg al que trobem al onCreate
        // de LlistaActivitatsActivity.
        setContentView(R.layout.activity_llista_intervals);
        //intervalsListView = (ListView) this.findViewById(R.id.listViewIntervals);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.getMenu().clear();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Intervals");
            //Previous view
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        RecyclerView recyclerInterval = (RecyclerView) this.findViewById(R.id.recyclerInterval);
        recyclerInterval.setLayoutManager(new LinearLayoutManager(this));

        //llistaDadesIntervals = new ArrayList<DadesInterval>();


        aaAct = new IntervalAdapter();
        /*
        aaAct = new ArrayAdapter<DadesInterval>(this, layoutID,
                llistaDadesIntervals);
        */
        recyclerInterval.setAdapter(aaAct);
    }
    
    /**
            FUNCTION CODED BY TEAM
    **/
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

        if(fatherTask != null) {
            AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(this);
            alertDialogInfo.setMessage("Nom:\n" + fatherTask.getNom() + "\n"
                    + "Descripció:\n" + fatherTask.getDescripcio() + "\n"
                    + "Data Inicial:\n" + fatherTask.getDataInicial() + "\n"
                    + "Data Final:\n" + fatherTask.getDataFinal() + "\n"
                    + "Durada:\n" + formatTimeElapsed.format(fatherTask.getDurada()));
            AlertDialog alertDialog = alertDialogInfo.create();
            alertDialog.show();
        } else {
            AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(this);
            alertDialogInfo.setMessage("Nom:\n" + currentTask.getNom() + "\n"
                    + "Descripció:\n" + currentTask.getDescripcio() + "\n"
                    + "Data Inicial:\n" + currentTask.getDataInicial() + "\n"
                    + "Data Final:\n" + currentTask.getDataFinal() + "\n"
                    + "Durada:\n" + formatTimeElapsed.format(currentTask.getDurada()));
            AlertDialog alertDialog = alertDialogInfo.create();
            alertDialog.show();
        }
    }

    /**
           FUNCTION CODED BY TEAM
    **/
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Aquests són els "serveis" que demana aquesta classe
    // a la classe Service GestorArbreActivitats

    /**
     * Sol·licita les dades dels intervals fills d'una tasca que és la activitat
     * pare actual.
     */
    public static final String DONAM_FILLS = "Donam_fills";

    /**
     * Demana que l'activitat pare actual passi a ser el projecte pare de la
     * tasca que és ara l'activitat actual.
     */
    public static final String PUJA_NIVELL = "Puja_nivell";

    /**
     * Rep els "intents" que envia <code>GestorArbreActivitats</code> amb les
     * dades de intervals a mostrar. El receptor els rep tots (no hi ha cap
     * filtre) per que només se'n n'hi envia un, el "TE_FILLS".
     *
     * @author joans
     * @version 6 febrer 2012
     */
    public class Receptor extends BroadcastReceiver {
        /**
         * Nom de la classe per fer aparèixer als missatges de logging del
         * LogCat.
         *
         * @see Log
         */
        private final String tag = this.getClass().getCanonicalName();

        /**
         * Gestiona tots els intents enviats, de moment només el de la acció
         * TE_FILLS. La gestió consisteix en actualitzar la llista de dades que
         * s'està mostrant mitjançant el seu adaptador.
         *
         * @param context
         *            el context (classe) des del qual s'ha llençat l'intent.
         * @param intent
         *            objecte Intent que arriba per "broadcast" i del qual en
         *            fem servir l'atribut "action" per saber quina mena de
         *            intent és i els extres per obtenir les dades a mostrar.
         */
         
        /**
            FUNCTION CODED BY TEAM
        **/
        @Override
        public final void onReceive(final Context context,
                                    final Intent intent) {
            Log.d(tag, "onReceive Receptor LlistaIntervals");
            if (intent.getAction().equals(GestorArbreActivitats.TE_FILLS)) {
                @SuppressWarnings("unchecked")
                ArrayList<DadesInterval> llistaDadesInter =
                        (ArrayList<DadesInterval>) intent
                                .getSerializableExtra("llista_dades_intervals");

                if(!llistaDadesInter.isEmpty())
                    fatherTask = llistaDadesInter.get(0).getFatherTask();
                else
                    currentTask = (Tasca) getIntent().getSerializableExtra("TASK_OBJECT");

                aaAct.clear();
                /*
                for (DadesInterval dadesInter : llistaDadesInter) {
                    aaAct.add(dadesInter);
                }
                */
                aaAct.add(llistaDadesInter);
                aaAct.notifyDataSetChanged();
            }
            Log.i(tag, "final de onReceive LlistaIntervals");
        }
    }

    /**
            FUNCTION CODED BY TEAM
    **/
    //Menu Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_interval, menu);
        return true;
    }

    /**
     * Objecte únic de la classe {@link Receptor}.
     */
    private Receptor receptor;

    @Override
    public final void onBackPressed() {
        Log.i(tag, "onBackPressed");
        sendBroadcast(new Intent(LlistaIntervalsActivity.PUJA_NIVELL));
        Log.d(tag, "enviat intent PUJA_NIVELL");
        super.onBackPressed();
    }

    /**
     * Quan aquesta Activity es mostra per primer cop o després d'haver estat
     * ocultada per alguna altra Activity cal tornar a fer receptor i el seu
     * filtre per que atengui als intents que es redifonen (broadcast). I
     * després, demanar la llista de dades d'interval a mostrar.
     */
    @Override
    public final void onResume() {
        Log.i(tag, "onResume intervals");

        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction(GestorArbreActivitats.TE_FILLS);
        receptor = new Receptor();
        registerReceiver(receptor, filter);

        sendBroadcast(new Intent(LlistaIntervalsActivity.DONAM_FILLS));
        Log.d(tag, "enviat intent DONAM_FILLS");

        super.onResume();
    }

    /**
     * Just abans de quedar "oculta" aquesta Activity per una altra, anul·lem el
     * receptor de intents.
     */
    @Override
    public final void onPause() {
        Log.i(tag, "onPause intervals");

        unregisterReceiver(receptor);

        super.onPause();
    }

    // D'aqui en avall els mètodes que apareixen són simplement sobrecàrregues
    // de mètodes de Activity per tal que es mostri un missatge de logging i
    // d'aquesta manera puguem entendre el cicle de vida d'un objecte d'aquesta
    // classe i depurar errors de funcionament de la interfase (on posar què).

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onDestroy() {
        Log.i(tag, "onDestroy intervals");
        super.onDestroy();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onStart() {
        Log.i(tag, "onStart intervals");
        super.onStart();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onStop() {
        Log.i(tag, "onStop intervals");
        super.onStop();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     */
    @Override
    public final void onRestart() {
        Log.i(tag, "onRestart intervals");
        super.onRestart();
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     *
     * @param savedInstanceState
     *            Bundle que de fet no es fa servir.
     */
    @Override
    public final void onSaveInstanceState(final Bundle savedInstanceState) {
        Log.i(tag, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Mostra un missatge de log per entendre millor el cicle de vida d'una
     * Activity.
     *
     * @param savedInstanceState
     *            Bundle que de fet no es fa servir.
     */
    @Override
    public final void onRestoreInstanceState(final Bundle savedInstanceState) {
        Log.i(tag, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Mostra un missatge de logging en rotar 90 graus el dispositiu (o
     * simular-ho en l'emulador).
     *
     * @param newConfig
     *            nova configuració {@link Configuration}
     * @see LlistaActivitatsActivity#onConfigurationChanged
     */
    @Override
    public final void onConfigurationChanged(final Configuration newConfig) {
        Log.i(tag, "onConfigurationChanged");
        if (Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, newConfig.toString());
        }
    }

}
