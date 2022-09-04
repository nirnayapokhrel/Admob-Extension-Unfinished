package com.oseamiya.admobsdks;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.ads.consent.*;
import com.google.android.gms.ads.AdInspectorError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnAdInspectorClosedListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class AdmobSdks extends AndroidNonvisibleComponent {
    private final Context context;
    private ConsentForm consentForm;


    public AdmobSdks(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
    }

    @SimpleEvent
    public void SdkInitialized() {
        EventDispatcher.dispatchEvent(this, "SdkInitialized");
    }

    @SimpleFunction
    public void InitializeSdk() {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull @NotNull InitializationStatus initializationStatus) {
                SdkInitialized();
            }
        });
    }

    /**
     * Tag for child directed treatment can either be 0, 1 or -1. You can find more in developers.google.com/admob/android/targeting
     * RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE == 1;
     * RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE == 0;
     * RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED == -1;
     */
    @SimpleProperty
    public void TagForChildDirectedTreatment(int value) {
        if (value == 0 || value == 1 || value == -1) {
            RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .setTagForChildDirectedTreatment(value)
                    .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
        } else {
            throw new YailRuntimeError("Value for TagForChildDirectedTreatment can either be 1,0 or -1. Read Documentation Carefully", "RuntimeError");
        }
    }

    /**
     * RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE == 1;
     * RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE == 0;
     * RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED == -1;
     */
    @SimpleProperty
    public void TagForUnderAgeOfConsent(int value) {
        if (value == 0 || value == 1 || value == -1) {
            RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .setTagForUnderAgeOfConsent(value)
                    .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
            if (value == 1) {
                ConsentInformation.getInstance(context).setTagForUnderAgeOfConsent(true);
            } else if (value == 0) {
                ConsentInformation.getInstance(context).setTagForUnderAgeOfConsent(false);
            }
        } else {
            throw new YailRuntimeError("Value for TagForUnderAgeOfConsent can either be 1,0 or -1. Read Documentation Carefully", "RuntimeError");
        }
    }

    /**
     * value can either be G, MA, PG, or T
     * Content that are suitable for general audiences, including families can be "G";
     * Content that are suitable for only matured audiences can be "MA";
     * Content that are suitable for most audiences with parental guidance can be "PG";
     * Content that are suitable for teen and older audiences can be "T"
     */
    @SimpleProperty
    public void MaxAdContentRating(String value) {
        String upperCaseValue = value.toUpperCase();
        if (upperCaseValue.equals("G") || upperCaseValue.equals("MA") || upperCaseValue.equals("PG") || upperCaseValue.equals("T")) {
            RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                    .toBuilder()
                    .setMaxAdContentRating(upperCaseValue)
                    .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
        } else {
            throw new YailRuntimeError("Value for MaxAdContentRating can either be G,MA,PG or T. Read Documentation Carefully", "RuntimeError");
        }
    }

    @SimpleEvent
    public void AdInspectorClosed(int code, String message) {
        EventDispatcher.dispatchEvent(this, "AdInspectorClosed", code, message);
    }

    @SimpleFunction
    public void OpenAdInspector() {
        MobileAds.openAdInspector(this.context, new OnAdInspectorClosedListener() {
            @Override
            public void onAdInspectorClosed(@Nullable @org.jetbrains.annotations.Nullable AdInspectorError adInspectorError) {
                AdInspectorClosed(adInspectorError.getCode(), adInspectorError.getMessage());
            }
        });
    }

    // Requesting consent from EU users

    @SimpleEvent
    public void ConsentFormLoaded() {
        EventDispatcher.dispatchEvent(this, "ConsentFormLoaded");
    }

    @SimpleEvent
    public void ConsentFormOpened() {
        EventDispatcher.dispatchEvent(this, "ConsentFormOpened");
    }

    @SimpleEvent
    public void ConsentFormClosed(boolean isUserPrefersAdFree, String consentStatus) {
        EventDispatcher.dispatchEvent(this, "ConsentFormClosed", isUserPrefersAdFree, consentStatus);
    }

    @SimpleEvent
    public void ConsentFormError(String error) {
        EventDispatcher.dispatchEvent(this, "ConsentFormError", error);
    }

    @SimpleEvent
    public void GotConsentStatus(String status, String message) {
        EventDispatcher.dispatchEvent(this, "ConsentStatus", status, message);
    }

    @SimpleEvent
    public void FailedToGetConsentStatus(String error) {
        EventDispatcher.dispatchEvent(this, "FailedToGetConsentStatus", error);
    }

    @SimpleFunction
    public void GetConsentStatus(String publisherId) {
        ConsentInformation consentInformation = ConsentInformation.getInstance(context);
        consentInformation.requestConsentInfoUpdate(new String[]{publisherId}, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if (ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown()) {
                    switch (consentStatus) {
                        case UNKNOWN:
                            GotConsentStatus("UNKNOWN", "The user has neither granted nor declined consent for personalized or non-personalized ads.");
                            break;
                        case PERSONALIZED:
                            GotConsentStatus("PERSONALIZED", "The user has granted consent for personalized ads.");
                            break;
                        case NON_PERSONALIZED:
                            GotConsentStatus("NON_PERSONALIZED", "The user has granted consent for non-personalized ads.");
                            break;

                    }
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                FailedToGetConsentStatus(errorDescription);
            }
        });

    }

    @DesignerProperty()
    @SimpleProperty()
    public void AddTestDevice(String deviceId) {
        if (!Objects.equals(deviceId, "")) {
            ConsentInformation.getInstance(this.context).addTestDevice(deviceId);
        }
    }

    @DesignerProperty(
            defaultValue = "Disabled",
            editorArgs = {"Disabled", "EEA", "NOT EEA"},
            editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES
    )
    @SimpleProperty
    public void DebugGeography(String geography) {
        if (Objects.equals(geography, "EEA")) {
            ConsentInformation.getInstance(this.context).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        } else if (Objects.equals(geography, "NOT EEA")) {
            ConsentInformation.getInstance(this.context).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA);
        } else {
            ConsentInformation.getInstance(this.context).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_DISABLED);
        }
    }


    @SimpleFunction
    public void LoadConsentForm(String privacyUrl) {
        URL pU = null;
        try {
            pU = new URL(privacyUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        consentForm = new ConsentForm.Builder(context, pU)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        ConsentFormLoaded();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        ConsentFormOpened();
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        String consentStatusName = "";
                        switch (consentStatus) {
                            case UNKNOWN:
                                consentStatusName = "UNKNOWN";
                                break;
                            case PERSONALIZED:
                                consentStatusName = "PERSONALIZED";
                                break;
                            case NON_PERSONALIZED:
                                consentStatusName = "NON_PERSONALIZED";
                                break;

                        }
                        ConsentFormClosed(userPrefersAdFree, consentStatusName);
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        ConsentFormError(errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();
        consentForm.load();
    }

    @SimpleFunction
    public void ShowConsentForm() {
        if (!consentForm.isShowing()) {
            if (consentForm != null) {
                consentForm.show();
            } else {
                throw new YailRuntimeError("Load Consent Form Before Showing To Users", "RuntimeError");
            }
        }
    }


}

