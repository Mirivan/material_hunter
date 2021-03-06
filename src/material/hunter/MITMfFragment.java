package material.hunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import material.hunter.databinding.MitmfGeneralBinding;
import material.hunter.databinding.MitmfInjectBinding;
import material.hunter.databinding.MitmfResponderBinding;
import material.hunter.databinding.MitmfSpoofBinding;
import material.hunter.utils.NhPaths;
import material.hunter.utils.SharePrefTag;
import material.hunter.utils.ShellExecuter;

public class MITMfFragment extends Fragment {

    // ^^ \\
    // static String CommandComposed = "";
    private static final ArrayList<String> CommandComposed = new ArrayList<>();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private TabsPagerAdapter tabsPagerAdapter;
    private Context context;
    private Activity activity;

    public static MITMfFragment newInstance(int sectionNumber) {
        MITMfFragment fragment = new MITMfFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private static void cleanCmd() {
        if (CommandComposed.size() > 0) {
            CommandComposed.subList(0, CommandComposed.size()).clear();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cleanCmd();
        View rootView = inflater.inflate(R.layout.mitmf, container, false);
        tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

        ViewPager mViewPager = rootView.findViewById(R.id.pagerMITMF);
        mViewPager.setAdapter(tabsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                activity.invalidateOptionsMenu();
            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    /* Start execution menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mitmf, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mitmf_menu_start_service:
                start();
                return true;
            case R.id.mitmf_menu_stop_service:
                stop();
                return true;
            case R.id.mitmf_menu_fix:
                cc();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void start() {
        StringBuilder sb = new StringBuilder();
        List<CommandProvider> commandProviders = tabsPagerAdapter.getCommandProviders();
        for (CommandProvider provider : commandProviders) {
            provider.getCommands(sb);
        }

        SharedPreferences o = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        if (o.getString(SharePrefTag.MITMF_CC, "").equals("")) {
            intentClickListener_NH("mitmf" + sb.toString());
        } else {
            intentClickListener_NH(o.getString(SharePrefTag.MITMF_CC, "") + sb.toString());
        }
        NhPaths.showSnack(getView(), getString(R.string.mitmf_started), 1);
    }

    private void stop() {
        ShellExecuter exe = new ShellExecuter();
        String[] command = new String[1];
        exe.RunAsRoot(command);
        NhPaths.showSnack(getView(), getString(R.string.mitmf_stopped), 1);
    }

    private void cc() {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        final View rootView = getLayoutInflater().inflate(R.layout.mitmf_cc, null);
        final EditText o = rootView.findViewById(R.id.mitmf_dialog_cc);
        final Button a = rootView.findViewById(R.id.mitmf_dialog_save);
        final Button b = rootView.findViewById(R.id.mitmf_dialog_clear);
        final SharedPreferences oa = Objects.requireNonNull(getActivity()).getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        o.setText(oa.getString(SharePrefTag.MITMF_CC, ""));
        a.setOnClickListener(view -> {
            oa.edit().putString(SharePrefTag.MITMF_CC, o.getText().toString()).apply();
            NhPaths.showSnack(getView(), getString(R.string.mitmfcc_setted), 1);
        });
        b.setOnClickListener(view -> {
            oa.edit().remove(SharePrefTag.MITMF_CC).apply();
            o.setText("");
            NhPaths.showSnack(getView(), getString(R.string.mitmfcc_cleared), 1);
        });
        AlertDialog ad = adb.setTitle(getString(R.string.mitmfcc)).setView(rootView).create();
        Objects.requireNonNull(ad.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        ad.show();
    }
    /* Stop execution menu */

    private void intentClickListener_NH(final String command) {
        try {
            Intent intent =
                    new Intent("com.offsec.nhterm.RUN_SCRIPT_NH");
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.putExtra("com.offsec.nhterm.iInitialCommand", command);
            startActivity(intent);
        } catch (Exception e) {
            NhPaths.showSnack(getView(), getString(R.string.toast_install_terminal), 1);
        }
    }
    /* Stop Tabs */

    public interface CommandProvider {
        void getCommands(StringBuilder stringBuilder);
    }

    private static class TabsPagerAdapter extends FragmentPagerAdapter {

        private final List<CommandProvider> commandProviders = new ArrayList<>(5);

        TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    MITMfGeneral fragment = new MITMfGeneral();
                    addCommandProvider(fragment);
                    return fragment;
                case 1:
                    MITMfResponder responder = new MITMfResponder();
                    addCommandProvider(responder);
                    return responder;
                case 2:
                    MITMfInject fragment2 = new MITMfInject();
                    addCommandProvider(fragment2);
                    return fragment2;
                case 3:
                    MITMfSpoof spoofFragment = new MITMfSpoof();
                    addCommandProvider(spoofFragment);
                    return spoofFragment;
                case 4:
                    return new MITMfConfigFragment();
                default:
                    return null;
            }
        }

        private void addCommandProvider(CommandProvider fragment) {
            if (!commandProviders.contains(fragment)) {
                commandProviders.add(fragment);
            }
        }

        private List<CommandProvider> getCommandProviders() {
            return commandProviders;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Provider Settings";
                case 2:
                    return "Inject Settings";
                case 3:
                    return "Spoof Settings";
                case 4:
                    return "MITMf Configuration";
                default:
                    return "General Settings";
            }
        }
    }

    public static class MITMfGeneral extends Fragment implements CommandProvider {

        MitmfGeneralBinding generalBinding;
        MITMFViewModel mViewModel;
        private ArrayAdapter<CharSequence> interfaceAdapter;
        private int interfaceSelection;
        private Context context;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            generalBinding = MitmfGeneralBinding.inflate(inflater, container, false);
            mViewModel = new MITMFViewModel();
            generalBinding.setViewModel(mViewModel);

            // Optional Presets Spinner
            Spinner interfaceSpinner = generalBinding.mitmfInterface;
            interfaceAdapter = ArrayAdapter.createFromResource(context,
                    R.array.mitmf_interface_array, android.R.layout.simple_spinner_item);
            interfaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            interfaceSpinner.setAdapter(interfaceAdapter);
            interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    MITMfGeneral.this.interfaceSelection = pos;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    MITMfGeneral.this.interfaceSelection = 0;
                }
            });

            // ScreenShotter Interval Time
            generalBinding.mitmfScreenInterval.setText("10");

            return generalBinding.getRoot();
        }

        @Override
        public void getCommands(StringBuilder stringBuilder) {
            stringBuilder
                    .append(" -i ")
                    .append(interfaceAdapter.getItem(interfaceSelection))
                    .append(generalBinding.mitmfJskey.isChecked() ? " --jskeylogger" : "")
                    .append(generalBinding.mitmfFerretng.isChecked() ? " --ferretng" : "")
                    .append(generalBinding.mitmfBrowserprofile.isChecked() ? " --browserprofiler" : "")
                    .append(generalBinding.mitmfFilepwn.isChecked() ? " --filepwn" : "")
                    .append(generalBinding.mitmfSmb.isChecked() ? " --smbauth" : "")
                    .append(generalBinding.mitmfSmbTrap.isChecked() ? " --smbtrap" : "")
                    .append(generalBinding.mitmfSslstrip.isChecked() ? " --hsts" : "")
                    .append(generalBinding.mitmfAppPoison.isChecked() ? " --appoison" : "")
                    .append(generalBinding.mitmfUpsidedown.isChecked() ? " --upsidedownternet" : "")
                    .append(generalBinding.mitmfScreenshotter.isChecked() ?
                            " --screen --interval " + generalBinding.mitmfScreenInterval.getText().toString() :
                            "");
        }
    }

    public static class MITMfInject extends Fragment implements CommandProvider {

        MitmfInjectBinding injectBinding;
        MITMFViewModel mViewModel = new MITMFViewModel();

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            injectBinding = MitmfInjectBinding.inflate(inflater, container, false);
            injectBinding.setViewModel(mViewModel);
            return injectBinding.getRoot();
        }

        @Override
        public void getCommands(StringBuilder stringBuilder) {
            if (injectBinding.mitmfEnableinject.isChecked()) {
                String injectRateSeconds = injectBinding.mitmfInjectRateseconds.getText().toString();
                String injectTimes = injectBinding.mitmfInjectTimesText.getText().toString();
                String injectJSURL = injectBinding.mitmfInjectjsUrl.getText().toString();
                String injectHtmlUrl = injectBinding.mitmfInjecthtmlUrl.getText().toString();
                String injectHtmlPay = injectBinding.mitmfInjecthtmlpayText.getText().toString();
                String mitmfInjectIp = injectBinding.mitmfInjectIpText.getText().toString();
                String injectNoIp = injectBinding.mitmfInjectNoipText.getText().toString();

                stringBuilder
                        .append(" --inject")
                        .append(!injectRateSeconds.isEmpty() ? " --rate-limit " + injectRateSeconds : "")
                        .append(!injectTimes.isEmpty() ? " --count-limit " + injectTimes : "")
                        .append(injectBinding.mitmfInjectPreservecache.isChecked() ? " --preserve-cache" : "")
                        .append(injectBinding.mitmfInjectOnceperdomain.isChecked() ? " --per-domain" : "")
                        .append(!injectJSURL.isEmpty() ? " --js-url " + injectJSURL : "")
                        .append(!injectHtmlUrl.isEmpty() ? " --html-url " + injectHtmlUrl : "")
                        .append(!injectHtmlPay.isEmpty() ? " --html-payload " + injectHtmlPay : "")
                        .append(!mitmfInjectIp.isEmpty() ? " --white-ips " + mitmfInjectIp : "")
                        .append(!injectNoIp.isEmpty() ? " --black-ips " + injectNoIp : "")
                ;
            }

        }
    }

    public static class MITMfSpoof extends Fragment implements CommandProvider {

        private int spoofOption;
        private MitmfSpoofBinding spoofBinding;
        private int arpModeOption;
        private Context context;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            spoofBinding = MitmfSpoofBinding.inflate(inflater, container, false);
            MITMFViewModel viewModel = new MITMFViewModel();
            spoofBinding.setViewModel(viewModel);


            // Redirect Spinner
            final Spinner redirectSpinner = spoofBinding.mitmfSpoofRedirectspin;
            ArrayAdapter<CharSequence> redirectAdapter = ArrayAdapter.createFromResource(context,
                    R.array.mitmf_spoof_type, android.R.layout.simple_spinner_item);
            redirectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            redirectSpinner.setAdapter(redirectAdapter);

            // ARP Mode Spinner
            ArrayAdapter<CharSequence> arpAdapter = ArrayAdapter.createFromResource(context,
                    R.array.mitmf_spoof_arpmode, android.R.layout.simple_spinner_item);
            arpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spoofBinding.mitmfSpoofArpmodespin.setAdapter(arpAdapter);
            spoofBinding.mitmfSpoofArpmodespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    arpModeOption = pos;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            return spoofBinding.getRoot();
        }

        @Override
        public void getCommands(StringBuilder stringBuilder) {
            if (spoofBinding.mitmfEnablespoof.isChecked() && spoofOption != 0) {
                stringBuilder.append(" --spoof");
                if (spoofOption == 1) {
                    stringBuilder.append(" --arp");
                } else if (spoofOption == 2) {
                    stringBuilder.append(" --icmp");
                } else if (spoofOption == 3) {
                    stringBuilder.append(" --dhcp");
                } else if (spoofOption == 4) {
                    stringBuilder.append(" --dns");
                }

                if (arpModeOption == 1) {
                    stringBuilder.append(" --arpmode req");
                } else if (arpModeOption == 2) {
                    stringBuilder.append(" --arpmode rep");
                }

                String gateway = spoofBinding.mitmfSpoofGatewayText.getText().toString();
                String targets = spoofBinding.mitmfSpoofTargetsText.getText().toString();
                String shellShock = spoofBinding.mitmfSpoofShellshockText.getText().toString();
                stringBuilder.append(!gateway.isEmpty() ? " --gateway " + gateway : "")
                        .append(!targets.isEmpty() ? " --targets " + targets : "")
                        .append(!shellShock.isEmpty() ? " --shellshock " + shellShock : "");
            }

        }
    }

    public static class MITMfResponder extends Fragment implements CommandProvider {

        private MitmfResponderBinding responderBinding;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            responderBinding = MitmfResponderBinding.inflate(inflater, container, false);
            MITMFViewModel viewModel = new MITMFViewModel();
            responderBinding.setViewModel(viewModel);
            return responderBinding.getRoot();
        }

        @Override
        public void getCommands(StringBuilder stringBuilder) {
            if (responderBinding.mitmfResponder.isChecked()) {
                stringBuilder.append(" --responder")
                        .append(responderBinding.mitmfResponderAnalyze.isChecked() ? " --analyze" : "")
                        .append(responderBinding.mitmfResponderFingerprint.isChecked() ?
                                " --fingerprint" : "")
                        .append(responderBinding.mitmfResponderLM.isChecked() ? " --lm" : "")
                        .append(responderBinding.mitmfResponderNBTNS.isChecked() ? " --nbtns" : "")
                        .append(responderBinding.mitmfResponderWPAD.isChecked() ? " --wpad" : "")
                        .append(responderBinding.mitmfResponderWREDIR.isChecked() ? " --wredir" : "")
                ;
            }
        }
    }

    public static class MITMfConfigFragment extends Fragment {
        final ShellExecuter exe = new ShellExecuter();
        private Context context;
        private String configFilePath;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            configFilePath = NhPaths.CHROOT_PATH() + "/etc/mitmf/mitmf.conf";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);

            String description = getResources().getString(R.string.mitmf_config);
            TextView desc = rootView.findViewById(R.id.description);
            desc.setText(description);


            EditText source = rootView.findViewById(R.id.source);
            exe.ReadFile_ASYNC(configFilePath, source);
            Button button = rootView.findViewById(R.id.update);
            button.setOnClickListener(v -> {
                boolean isSaved = exe.SaveFileContents(source.getText().toString(), configFilePath);
                if (isSaved) {
                    NhPaths.showSnack(getView(), getString(R.string.edit_source_updated), 1);
                } else {
                    NhPaths.showSnack(getView(), getString(R.string.edit_source_not_updated), 1);
                }
            });
            return rootView;
        }
    }
}
