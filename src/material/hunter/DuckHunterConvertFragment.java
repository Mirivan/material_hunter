package material.hunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class DuckHunterConvertFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "DuckHunterConvert";
    private static final String loadFilePath = "/duckyscripts/";
    private static final int PICKFILE_RESULT_CODE = 1;
    private String duckyInputFile;
    private String duckyOutputFile;
    private Context context;
    private Activity activity;
    private boolean isReceiverRegistered;
    private EditText editsource;
    private ConvertDuckyBroadcastReceiver convertDuckyBroadcastReceiver = new ConvertDuckyBroadcastReceiver();

    public DuckHunterConvertFragment(String inFilePath, String outFilePath) {
        this.duckyInputFile = inFilePath;
        this.duckyOutputFile = outFilePath;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.duck_hunter_convert, container, false);
        TextView t2 = rootView.findViewById(R.id.reference_text);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        editsource = rootView.findViewById(R.id.editSource);
        editsource.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                activity.sendBroadcast(new Intent().putExtra("ACTION", "SHOULDCONVERT").putExtra("SHOULDCONVERT", true).setAction(BuildConfig.APPLICATION_ID + ".SHOULDCONVERT"));
            }
        });

        Button b = rootView.findViewById(R.id.duckyLoad);
        Button b1 = rootView.findViewById(R.id.duckySave);
        b.setOnClickListener(this);
        b1.setOnClickListener(this);

        // Duckhunter preset spinner templates
        String[] duckyscript_file = getDuckyScriptFiles();
        Spinner duckyscriptSpinner = rootView.findViewById(R.id.duckhunter_preset_spinner);
        ArrayAdapter<String> duckyscriptAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, duckyscript_file);
        duckyscriptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duckyscriptSpinner.setAdapter(duckyscriptAdapter);
        duckyscriptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                getPreset(duckyscriptSpinner.getSelectedItem().toString());
                write_ducky();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            activity.registerReceiver(convertDuckyBroadcastReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".WRITEDUCKY"));
            isReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            activity.unregisterReceiver(convertDuckyBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        editsource = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (getView() == null) {
                        return;
                    }
                    String FilePath = data.getData().getPath();
                    FilePath = new ShellExecuter().RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
                    EditText editsource = getView().findViewById(R.id.editSource);
                    try {
                        String text = "";
                        BufferedReader br = new BufferedReader(new FileReader(FilePath));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text += line + '\n';
                        }
                        br.close();
                        editsource.setText(text);
                        NhPaths.showSnack(getView(), getString(R.string.script_loaded), 1);
                    } catch (Exception e) {
                        NhPaths.showMessage(context, e.getMessage());
                    }
                    break;
                }
                break;

        }
    }

    private void getPreset(String filename) {
        if (getView() == null) {
            return;
        }
        String filename_path = "/duckyscripts/";
        filename = filename_path + filename;
        EditText editsource = getView().findViewById(R.id.editSource);
        File file = new File(NhPaths.APP_SD_FILES_PATH, filename);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, "exception", e);
        }
        editsource.setText(text);
    }

    private String[] getDuckyScriptFiles() {
        List<String> result = new ArrayList<>();
        File script_folder = new File(NhPaths.APP_SD_FILES_PATH + "/duckyscripts");
        File[] filesInFolder = script_folder.listFiles();
        for (File file : filesInFolder) {
            if (!file.isDirectory()) {
                result.add(file.getName());
            }
        }
        Collections.sort(result);
        return result.toArray(new String[0]);
    }

    private void write_ducky() {
        try {
            File myFile = new File(duckyInputFile);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(editsource.getText().toString());
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            NhPaths.showMessage(context, e.getMessage());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.duckyLoad:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                Uri selectedUri = Uri.parse(NhPaths.APP_SD_FILES_PATH/* + loadFilePath*/);
                intent.setDataAndType(selectedUri, "*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
                break;
            case R.id.duckySave:
                try {
                    File scriptsDir = new File(NhPaths.APP_SD_FILES_PATH, loadFilePath);
                    if (!scriptsDir.exists()) scriptsDir.mkdirs();
                } catch (Exception e) {
                    NhPaths.showMessage(context, e.getMessage());
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);

                alert.setTitle("Name");
                alert.setMessage("Please enter a name for your script.");

                // Set an EditText view to get user input
                final EditText input = new EditText(activity);
                alert.setView(input);

                alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                    String value = input.getText().toString();
                    if (value.length() > 0) {
                        File scriptFile = new File(NhPaths.APP_SD_FILES_PATH + loadFilePath + File.separator + value);
                        System.out.println(scriptFile.getAbsolutePath());
                        try {
                            if (getView() != null) {
                                EditText source = getView().findViewById(R.id.editSource);
                                String text = source.getText().toString();
                                if (!scriptFile.exists()) {
                                    scriptFile.createNewFile();
                                    FileOutputStream fOut = new FileOutputStream(scriptFile);
                                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                    myOutWriter.append(text);
                                    myOutWriter.close();
                                    fOut.close();
                                    NhPaths.showSnack(getView(), getString(R.string.script_saved), 1);
                                } else {
                                    AlertDialog.Builder alert1 = new AlertDialog.Builder(activity);
                                    alert1.setTitle(getString(R.string.file_exists)).setMessage(getString(R.string.file_q_overwrite))
                                            .setNegativeButton(getString(R.string.no), null)
                                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                                                try {
                                                    scriptFile.createNewFile();
                                                    FileOutputStream fOut = new FileOutputStream(scriptFile);
                                                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                                    myOutWriter.append(text);
                                                    myOutWriter.close();
                                                    fOut.close();
                                                    NhPaths.showSnack(getView(), getString(R.string.script_saved), 1);
                                                } catch (Exception e) {
                                                    NhPaths.showMessage(context, e.getMessage());
                                                }
                                            }).create().show();
                                }
                            }
                        } catch (Exception e) {
                            NhPaths.showMessage(context, e.getMessage());
                        }
                    } else {
                        NhPaths.showSnack(getView(), getString(R.string.wrong_name_provider), 1);
                    }
                });

                alert.setNegativeButton(getString(R.string.cancel), null);
                alert.show();
                break;
            default:
                NhPaths.showSnack(getView(), getString(R.string.unknown_click), 1);
                break;
        }
    }

    public class ConvertDuckyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("ACTION")) {
                case "WRITEDUCKY":
                    write_ducky();
                    activity.sendBroadcast(new Intent().putExtra("ACTION", "PREVIEWDUCKY").setAction(BuildConfig.APPLICATION_ID + ".PREVIEWDUCKY"));
                    break;
            }
        }
    }
}
