package com.paulvarry.intra42.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.paulvarry.intra42.R;
import com.paulvarry.intra42.adapters.ListAdapterClusterMapContribute;
import com.paulvarry.intra42.api.cluster_map_contribute.Cluster;
import com.paulvarry.intra42.api.cluster_map_contribute.Location;
import com.paulvarry.intra42.api.cluster_map_contribute.Master;
import com.paulvarry.intra42.api.model.Campus;
import com.paulvarry.intra42.cache.CacheCampus;
import com.paulvarry.intra42.ui.BasicThreadActivity;
import com.paulvarry.intra42.utils.ClusterMapContributeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ClusterMapContributeActivity
        extends BasicThreadActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener, BasicThreadActivity.GetDataOnThread, ListAdapterClusterMapContribute.OnEditClickListener, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView listView;

    private List<Campus> listCampus;
    private List<Cluster> clusters;
    private List<Master> masters;

    private Master newMaster;

    public static void openIt(Context context) {
        Intent intent = new Intent(context, ClusterMapContributeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_map_contribute);
        setActionBarToggle(ActionBarToggle.ARROW);

        listView = findViewById(R.id.listView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        registerGetDataOnOtherThread(this);

        super.onCreateFinished();
    }

    @Nullable
    @Override
    public String getUrlIntra() {
        return null;
    }

    @Override
    public String getToolbarName() {
        return null;
    }

    @Override
    protected void setViewContent() {

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(false);

        fabBaseActivity.setVisibility(View.VISIBLE);
        fabBaseActivity.setOnClickListener(this);

        ListAdapterClusterMapContribute adapter = new ListAdapterClusterMapContribute(app, masters);
        adapter.setOnEditListener(this);
        listView.setAdapter(adapter);
    }

    @Override
    public String getEmptyText() {
        return null;
    }

    @Override
    public void onClick(View v) {
        if (v == fabBaseActivity) {
            ClusterMapContributeUtils.loadMaster(this, app.getApiServiceClusterMapContribute(), new ClusterMapContributeUtils.LoadMasterCallback() {
                @Override
                public void finish(List<Master> masters) {
                    for (Master m : masters)
                        if (m.name == null) {
                            openEditMetadataDialog(m, null);
                            return;
                        }
                }

                @Override
                public void error(String error) {
                    Toast.makeText(app, error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ClusterMapContributeEditActivity.openIt(this, clusters.get(position));
    }

    /**
     * Triggered when the activity start.
     * <p>
     * This method is run on main Thread, so you can make api call.
     *
     * @return Return ThreadStatusCode of what appending {@link GetDataOnMain#getDataOnMainThread()}.
     */
    @Override
    public void getDataOnOtherThread() throws IOException, RuntimeException {

        listCampus = CacheCampus.getAllowInternet(app.cacheSQLiteHelper, app);

        final Call<List<Master>> call = app.getApiServiceClusterMapContribute().getMasters();

        long start = System.nanoTime();
        try {
            Response<List<Master>> response = call.execute();
            long end = System.nanoTime();
            long diff = end - start;

            Log.d("cluster call time", String.valueOf(diff));

            List<Master> tmpMaster = response.body();
            masters = new ArrayList<>();
            for (Master m : tmpMaster) {

                if (newMaster == null && m.name == null)
                    newMaster = m;

                if (m.name != null)
                    masters.add(m);
            }
            Collections.sort(masters);
        } catch (IOException e) {
            e.printStackTrace();
        }


/*        Gson gson = ServiceGenerator.getGson();
        List<Cluster> list = new ArrayList<>();

        list.add(exportCluster(new Cluster(1, "E1", "e1", true)));
        list.add(exportCluster(new Cluster(1, "E2", "e2", true)));
        list.add(exportCluster(new Cluster(1, "E3", "e3", true)));
        list.add(exportCluster(new Cluster(7, "E1Z1", "e1z1", true)));
        list.add(exportCluster(new Cluster(7, "E1Z2", "e1z2", true)));
        list.add(exportCluster(new Cluster(7, "E1Z3", "e1z3", true)));
        list.add(exportCluster(new Cluster(7, "E1Z4", "e1z4", true)));
        Log.d("export", "finished");*/


/*
        List<Master> listMaster = new ArrayList<>();
        listMaster.add(new Master("Paris - E1", "66i0i1atv", "so144wxi"));

        String content = ServiceGenerator.getGson().toJson(listMaster);

        HashMap<String, String> map = new HashMap<>();
        map.put("key", "q79vdcc1t");
        map.put("pad", content);
        map.put("monospace", "1");

        Response<Void> ret = app.getApiServiceClusterMapContribute().save(map, cookie).execute();
*/
    }

    Cluster exportCluster(Cluster c) {
        int x = c.map[0].length;
        int y = c.map.length;
        Location[][] tmp = new Location[x][];
        Location l;
        for (int i = 0; i < c.map.length; i++) {
            for (int j = 0; j < c.map[0].length; j++) {
                if (tmp[j] == null)
                    tmp[j] = new Location[y];
                l = c.map[i][j];
                tmp[j][i] = l;
                if (l != null && l.host != null && l.host.startsWith(c.hostPrefix))
                    l.host = l.host.replaceFirst(c.hostPrefix, "");
            }
        }
        c.map = tmp;
        return c;
    }

    @Override
    public void onClickEditLayout(View finalConvertView, int groupPosition, final Master master) {

        ClusterMapContributeUtils.loadClusterMapAndLock(this, this.app, master, new ClusterMapContributeUtils.LoadClusterMapCallback() {
            @Override
            public void finish(final Master master, final Cluster cluster, String cookie) {
                Toast.makeText(app, R.string.opening_in_progress, Toast.LENGTH_SHORT).show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ClusterMapContributeEditActivity.openIt(ClusterMapContributeActivity.this, cluster, master);
                    }
                });
            }

            @Override
            public void error(String error) {
                Toast.makeText(app, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClickEditMetadata(View finalConvertView, int groupPosition, final Master master) {

        ClusterMapContributeUtils.loadClusterMap(this, app.getApiServiceClusterMapContribute(), master, new ClusterMapContributeUtils.LoadClusterMapCallback() {
            @Override
            public void finish(final Master master, final Cluster cluster, String cookie) {
                openEditMetadataDialog(master, cluster);
            }

            @Override
            public void error(String error) {
                Toast.makeText(app, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void openEditMetadataDialog(final Master master, @Nullable final Cluster cluster) {
        final LayoutInflater inflater = LayoutInflater.from(ClusterMapContributeActivity.this);
        final View view = inflater.inflate(R.layout.fragment_dialog_cluster_map_contribute_cluster, null);
        final TextInputLayout textInputName = view.findViewById(R.id.textInputName);
        final TextInputLayout textInputNameShort = view.findViewById(R.id.textInputNameShort);
        final EditText editTextPrefix = view.findViewById(R.id.editTextPrefix);
        final EditText editTextName = view.findViewById(R.id.editTextName);
        final EditText editTextNameShort = view.findViewById(R.id.editTextNameShort);
        final EditText editTextPosition = view.findViewById(R.id.editTextPosition);
        final Spinner spinnerCampus = view.findViewById(R.id.spinnerCampus);
        final EditText editTextComment = view.findViewById(R.id.editTextComment);

        final boolean isCreate = cluster == null;

        final FinalWrapper dialog = new FinalWrapper();
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (dialog.dialog != null) {
                    boolean disable = false;
                    if (editTextName.getText().toString().length() > textInputName.getCounterMaxLength()) {
                        textInputName.setError(getString(R.string.error_text_is_too_long));
                        disable = true;
                    } else
                        textInputName.setError(null);
                    if (editTextNameShort.getText().toString().length() > textInputNameShort.getCounterMaxLength()) {
                        textInputNameShort.setError(getString(R.string.error_text_is_too_long));
                        disable = true;
                    } else
                        textInputNameShort.setError(null);
                    dialog.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!disable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        int selection = 0;
        if (listCampus != null) {
            List<String> listCampusString;
            listCampusString = new ArrayList<>();
            Campus c;
            for (int i = 0; i < listCampus.size(); i++) {
                c = listCampus.get(i);
                listCampusString.add(c.name + " (id: " + String.valueOf(c.id) + ")");
                if (cluster != null && c.id == cluster.campusId)
                    selection = i;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ClusterMapContributeActivity.this, android.R.layout.simple_spinner_dropdown_item, listCampusString);
            spinnerCampus.setAdapter(adapter);
        }

        editTextName.addTextChangedListener(textWatcher);
        editTextNameShort.addTextChangedListener(textWatcher);

        final AlertDialog.Builder builder = new AlertDialog.Builder(ClusterMapContributeActivity.this);
        if (cluster != null) {
            editTextPrefix.setText(cluster.hostPrefix);
            editTextNameShort.setText(cluster.nameShort);
            editTextName.setText(cluster.name);
            if (cluster.clusterPosition > 0)
                editTextPosition.setText(String.valueOf(cluster.clusterPosition));
            editTextComment.setText(cluster.comment);
        }
        spinnerCampus.setSelection(selection, false);

        builder.setTitle(R.string.cluster_map_contribute_dialog_metadata_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Cluster newCluster = cluster;
                if (newCluster == null)
                    newCluster = new Cluster(0, null, null);

                newCluster.name = editTextName.getText().toString();
                newCluster.nameShort = editTextNameShort.getText().toString();
                String stringPosition = editTextPosition.getText().toString();
                if (!stringPosition.isEmpty())
                    newCluster.clusterPosition = Integer.decode(stringPosition);
                newCluster.hostPrefix = editTextPrefix.getText().toString();
                newCluster.comment = editTextComment.getText().toString();

                newCluster.campusId = 1;
                int pos = spinnerCampus.getSelectedItemPosition();
                if (pos < listCampus.size()) {
                    Campus c = listCampus.get(pos);
                    newCluster.campusId = c.id;
                }

                ClusterMapContributeUtils.saveClusterMap(ClusterMapContributeActivity.this, app, master, newCluster, isCreate, new ClusterMapContributeUtils.SaveClusterMapCallback() {
                    @Override
                    public void finish() {
                        refresh();
                    }

                    @Override
                    public void error(String error) {
                        Toast.makeText(app, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.dialog = builder.show();
        //verify if text is too long
        if (dialog.dialog != null) {
            boolean disable = false;
            if (editTextName.getText().toString().length() > textInputName.getCounterMaxLength()) {
                textInputName.setError(getString(R.string.error_text_is_too_long));
                disable = true;
            }
            if (editTextNameShort.getText().toString().length() > textInputNameShort.getCounterMaxLength()) {
                textInputNameShort.setError(getString(R.string.error_text_is_too_long));
                disable = true;
            }
            dialog.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!disable);
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        refresh();
    }

    private class FinalWrapper {
        AlertDialog dialog;
    }
}