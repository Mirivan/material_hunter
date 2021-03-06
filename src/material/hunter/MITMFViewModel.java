package material.hunter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class MITMFViewModel extends BaseObservable {

    private boolean injectionEnabled = false;
    private boolean spoofEnabled = false;
    private boolean responderChecked = false;
    private boolean injectJSEmpty = true;
    public TextWatcher injectJSWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectJSEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };
    private boolean injectHtmlURLEmpty = true;
    public TextWatcher injectHtmlUrlWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectHtmlURLEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };
    private boolean injectHtmlEmpty = true;
    public TextWatcher injectHtmlWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectHtmlEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };

    public void clickInject(View view) {
        injectionEnabled = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.injectionEnabled);
        notifyEnabledChanged();
    }

    public void clickSpoof(View view) {
        spoofEnabled = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.spoofEnabled);
    }

    @Bindable
    public boolean isInjectionEnabled() {
        return injectionEnabled;
    }

    private void notifyEnabledChanged() {
        notifyPropertyChanged(BR.injectJSEnabled);
        notifyPropertyChanged(BR.injectHtmlUrlEnabled);
        notifyPropertyChanged(BR.injectHtmlEnabled);
    }

    @Bindable
    public boolean isInjectJSEnabled() {
        return injectHtmlURLEmpty && injectHtmlEmpty && injectionEnabled;
    }

    @Bindable
    public boolean isInjectHtmlUrlEnabled() {
        return injectJSEmpty && injectHtmlEmpty && injectionEnabled;
    }

    @Bindable
    public boolean isInjectHtmlEnabled() {
        return injectJSEmpty && injectHtmlURLEmpty && injectionEnabled;
    }

    @Bindable
    public boolean isSpoofEnabled() {
        return spoofEnabled;
    }

    @Bindable
    public boolean isResponderChecked() {
        return responderChecked;
    }

    public void setResponderChecked(boolean responderChecked) {
        this.responderChecked = responderChecked;
        notifyPropertyChanged(BR.responderChecked);
    }

    public void responderClicked(View view) {
        responderChecked = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.responderChecked);
    }

    private static class TextWatcherAdapter implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
