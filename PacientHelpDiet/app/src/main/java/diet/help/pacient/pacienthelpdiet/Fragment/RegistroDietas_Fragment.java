package diet.help.pacient.pacienthelpdiet.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import diet.help.pacient.pacienthelpdiet.Adaptadores.Pedidos_Adaptador;
import diet.help.pacient.pacienthelpdiet.Interface.OnSelectElementos;
import diet.help.pacient.pacienthelpdiet.Modelos.DetalleDieta;
import diet.help.pacient.pacienthelpdiet.Modelos.Dieta;
import diet.help.pacient.pacienthelpdiet.Modelos.Sugerencias;
import diet.help.pacient.pacienthelpdiet.Modelos.Paciente;
import diet.help.pacient.pacienthelpdiet.Modelos.TipoDietas;
import diet.help.pacient.pacienthelpdiet.R;
import diet.help.pacient.pacienthelpdiet.Servicios.FirebaseReferences;

public class RegistroDietas_Fragment extends Fragment implements AdapterView.OnItemSelectedListener{

    Spinner cmb_paciente,cmb_horario,cmb_tipodieta;
    ArrayList<Sugerencias> alimentos=new ArrayList<Sugerencias>();
    ArrayList<Paciente> pacientes=new ArrayList<Paciente>();
    ArrayList<TipoDietas> tipoDieta=new ArrayList<TipoDietas>();
    RecyclerView rv_pedidos;
    CardView btn_enviar;
    Dieta dieta;
    EditText txt_Observaciones;
    Pedidos_Adaptador pedidos_adaptador;
    private EventBus eventBus=EventBus.getDefault();
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    final DatabaseReference references=database.getReference(FirebaseReferences.PACIENTE_REFERENCIAS);
    final DatabaseReference Tiporeferences=database.getReference(FirebaseReferences.TIPODIETAS_REFERENCIAS);
    final DatabaseReference dietareferences=database.getReference(FirebaseReferences.DIETA_REFERENCIAS);
    private int posicion;
    private int posiciontipo;
    private String horarios;
    String [] itemshorario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_registro_dietas_, container, false);
        rv_pedidos=(RecyclerView)view.findViewById(R.id.rv_detalle);
        cmb_paciente=(Spinner)view.findViewById(R.id.sp_pacientes);
        cmb_horario=(Spinner)view.findViewById(R.id.sp_horario);
        cmb_tipodieta=(Spinner)view.findViewById(R.id.sp_tipo);
        txt_Observaciones=(EditText)view.findViewById(R.id.et_observaciones);
        rv_pedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        pedidos_adaptador=new Pedidos_Adaptador(alimentos);
        rv_pedidos.setAdapter(pedidos_adaptador);
        rv_pedidos.setHasFixedSize(true);
        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),3);
        rv_pedidos.setLayoutManager(layoutManager);
        pedidos_adaptador.setOnSelectElementos(new OnSelectElementos() {
            @Override
            public void onAddClick(int posicion) {
                Toast.makeText(getContext(),"Se eliminado "+alimentos.get(posicion).getNombre(),Toast.LENGTH_SHORT).show();
                alimentos.remove(posicion);
                pedidos_adaptador.notifyDataSetChanged();
            }
        });
        cmb_paciente.setOnItemSelectedListener(this);
        new GetPacientes().execute();
        itemshorario=getResources().getStringArray(R.array.cmb_horarios);
        ArrayAdapter<String>adapterhorario=new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,itemshorario);
        adapterhorario.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmb_horario.setAdapter(adapterhorario);
        cmb_horario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                horarios=itemshorario[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cmb_tipodieta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                posiciontipo=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        new GetTipoDieta().execute();
        btn_enviar=(CardView)view.findViewById(R.id.cv_send);
        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(posicion!=0 && posiciontipo!=0 && horarios!="---Seleccione---") {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String fecha=dateFormat.format(new Date());;
                    dieta=new Dieta();
                    dieta.setKey(dietareferences.push().getKey());
                    dieta.setHorario(horarios);
                    try {
                        dieta.setFecha(dateFormat.parse(fecha));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String especificaciones = null;
                    String observaciones=null;
                    if(!txt_Observaciones.getText().toString().isEmpty()){
                        observaciones=txt_Observaciones.getText().toString().trim();
                    }
                    Dieta dieta1=new Dieta(dieta.getHorario(),pacientes.get(posicion-1).getKey(),observaciones,dieta.getFecha());
                    DatabaseReference dietasreferences=database.getReference(FirebaseReferences.DIETA_REFERENCIAS+"/"+dieta.getKey());
                    DatabaseReference detallereference=database.getReference(FirebaseReferences.DETALLE_REFERENCIAS);
                    for (int i=0;i<alimentos.size();i++){
                        if(i==0){
                            especificaciones=alimentos.get(i).getNombre();
                        }else{
                            especificaciones=especificaciones+","+alimentos.get(i).getNombre();
                        }
                    }
                    DetalleDieta detalleDieta=new DetalleDieta(dieta.getKey(),especificaciones,tipoDieta.get(posiciontipo).getKey());
                    if(especificaciones!=null){
                        dietasreferences.setValue(dieta1);
                        detallereference.push().setValue(detalleDieta);
                        Toast.makeText(getContext(),"Se a guardado exitosamente los datos",Toast.LENGTH_SHORT).show();
                        alimentos.removeAll(alimentos);
                        pedidos_adaptador.notifyDataSetChanged();
                        txt_Observaciones.setText("");
                    }else {
                        Toast.makeText(getContext(),"Porfavor Agregar Sugerencias",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(posicion==0){
                        Toast.makeText(getContext(),"Selecione un paciente",Toast.LENGTH_SHORT).show();
                    }
                    if(posiciontipo==0){
                        Toast.makeText(getContext(),"Selecione un tipo de dieta",Toast.LENGTH_SHORT).show();
                    }
                    if (horarios.equals("---Seleccione---")){
                        Toast.makeText(getContext(),"Selecione un horario",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }
    @Subscribe
    public void EjecutarComunicacion(Sugerencias sugerencias){
        alimentos.add(sugerencias);
        pedidos_adaptador.notifyDataSetChanged();
    }

    private void ListadoPacientes(){
        ArrayList<String> paciente=new ArrayList<String>();
        paciente.add("----Seleccione----");
        Log.i("string3",String.valueOf(pacientes.size()));
        for(int i=0;i<pacientes.size();i++){
            Log.i("string",pacientes.get(i).getNombre()+" "+pacientes.get(i).getApellido());
            paciente.add(pacientes.get(i).getNombre()+" "+pacientes.get(i).getApellido());
        }
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, paciente);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmb_paciente.setAdapter(spinnerAdapter);
    }

    private void ListadoTipoDietas(){
        ArrayList<String> tipodieta=new ArrayList<String>();
        tipodieta.add("----Seleccione----");
        Log.i("string3",String.valueOf(pacientes.size()));
        for(int i=0;i<tipoDieta.size();i++){
            Log.i("string",tipoDieta.get(i).getTipo()+" "+tipoDieta.get(i).getDescripcion());
            tipodieta.add(tipoDieta.get(i).getTipo());
        }
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, tipodieta);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmb_tipodieta.setAdapter(spinnerAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
           posicion=position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class GetPacientes extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            references.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    pacientes.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        pacientes.add(new Paciente(ds.child("cedula").getValue().toString(),ds.child("nombre").getValue().toString(),ds.child("apellido").getValue().toString(),ds.getKey()));
                    }
                    ListadoPacientes();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Log.i("string1", String.valueOf(pacientes.size()));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListadoPacientes();
        }

    }

    private class GetTipoDieta extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Tiporeferences.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tipoDieta.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        tipoDieta.add(new TipoDietas(ds.child("tipo").getValue().toString(),ds.child("descripcion").getValue().toString(),ds.getKey()));
                    }
                    ListadoTipoDietas();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Log.i("string1", String.valueOf(tipoDieta.size()));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListadoTipoDietas();
        }

    }
}
