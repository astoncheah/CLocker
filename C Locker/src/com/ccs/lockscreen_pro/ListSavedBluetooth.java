package com.ccs.lockscreen_pro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoCalendarAccount;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListSavedBluetooth extends BaseActivity{
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private WifiAdapter adapter = null;
    private List<InfoCalendarAccount> btList = new ArrayList<InfoCalendarAccount>();
    ;
    private List<String> btSavedlist = new ArrayList<String>();
    private ListView list;
    private Button btnCheckAll, btnUncheckAll;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.select_bluetooth);
        setBasicBackKeyAction();
        try{
            myAlertDialog = new MyAlertDialog(this);
            list = (ListView)findViewById(R.id.listSelector);
            btnCheckAll = (Button)findViewById(R.id.btnCalendarCheckAll);
            btnUncheckAll = (Button)findViewById(R.id.btnCalendarUncheckAll);

            loadSettings();
            buttonFunction();

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if(pairedDevices.size()>0){
                // Loop through paired devices
                for(BluetoothDevice device : pairedDevices){
                    // Add the name and address to an array adapter to show in a ListView
                    btList.add(new InfoCalendarAccount(null,null,device.getName()));
                }
                for(int i = 0; i<btSavedlist.size(); i++){
                    for(int pos = 0; pos<btList.size(); pos++){
                        if(btSavedlist.get(i).equals(btList.get(pos).getName())){
                            btList.get(pos).setSelected(true);
                            //Log.e("setSelected: ",cldList.get(pos).getId());
                        }
                    }
                }
                loadData();
            }else{
                myAlertDialog.simpleMsg(getString(R.string.no_bluetooth_found));
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            myAlertDialog.close();
            adapter.clearObjects();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.list_cld_account_main;
    }

    private void loadSettings(){
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        final String str = prefs.getString("strTrustedBluetooth","");
        final String[] items = str.split(";");
        for(int i = 0; i<items.length; i++){
            btSavedlist.add(items[i].toString());
        }
        //Log.e("load",str);
    }

    private void buttonFunction(){
        btnCheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<btList.size(); pos++){
                    btList.get(pos).setSelected(true);
                }
                loadData();
            }
        });
        btnUncheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<btList.size(); pos++){
                    btList.get(pos).setSelected(false);
                }
                loadData();
            }
        });
    }

    private void loadData(){
        try{
            adapter = new WifiAdapter(this,R.layout.list_cld_account,btList);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                    //NOTHING
                }
            });
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void saveSettings(){
        editor = getSharedPreferences(C.PREFS_NAME,0).edit();
        StringBuilder csvList = new StringBuilder();
        for(InfoCalendarAccount item : btList){
            if(item.isSelected()){
                csvList.append(item.getName());
                csvList.append(";");
            }
        }
        if(csvList.length()==0){
            editor.putString("strTrustedBluetooth","");
        }else{
            editor.putString("strTrustedBluetooth",csvList.toString());
        }
        editor.commit();
        //Log.e("save",csvList.toString());
    }

    private class ViewHolder{
        private TextView txtCldAccountName, txtCldAccountEmail;
        private CheckBox cBoxCldAccount;
    }

    private class WifiAdapter extends ArrayAdapter<InfoCalendarAccount>{
        private ViewHolder holder;
        private int layoutId;

        public WifiAdapter(Context context,int layoutId,List<InfoCalendarAccount> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
        }

        @Override
        public View getView(final int position,View convertView,ViewGroup parent){
            View view = null;
            if(convertView==null){
                view = newView(parent);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtCldAccountEmail = (TextView)view.findViewById(R.id.txt_cld_account_email);
                viewHolder.txtCldAccountName = (TextView)view.findViewById(R.id.txt_cld_account_name);
                viewHolder.cBoxCldAccount = (CheckBox)view.findViewById(R.id.cBox_cld_account_name);
                viewHolder.cBoxCldAccount.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        InfoCalendarAccount element = (InfoCalendarAccount)viewHolder.cBoxCldAccount.getTag();
                        element.setSelected(viewHolder.cBoxCldAccount.isChecked());
                        //Log.e("onCheckedChanged",position+"/"+cldList.get(position).getId()+"/"+cldList.get(position).isSelected());
                    }
                });
                view.setTag(viewHolder);
                viewHolder.cBoxCldAccount.setTag(btList.get(position));
            }else{
                view = convertView;
                ((ViewHolder)view.getTag()).cBoxCldAccount.setTag(btList.get(position));
            }
            try{
                holder = (ViewHolder)view.getTag();
                holder.txtCldAccountEmail.setVisibility(View.GONE);
                holder.txtCldAccountName.setText(btList.get(position).getName());
                holder.cBoxCldAccount.setChecked(btList.get(position).isSelected());
                //Log.e("getView",position+"/"+cldList.get(position).getId()+"/"+cldList.get(position).isSelected());
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
            return (view);
        }

        private View newView(ViewGroup parent){
            return (getLayoutInflater().inflate(layoutId,parent,false));
        }

        /*private boolean setCheckBox(int position){
            for(int i=0;i<cldSavedlist.size();i++){
                if(cldSavedlist.get(i).equals(cldList.get(position).getId())){
                    return true;
                }
            }
            return false;
        }*/
        private void clearObjects(){
            clear();
        }
    }
}
