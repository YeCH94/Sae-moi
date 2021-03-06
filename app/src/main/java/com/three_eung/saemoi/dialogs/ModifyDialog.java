package com.three_eung.saemoi.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import com.three_eung.saemoi.InitApp;
import com.three_eung.saemoi.R;
import com.three_eung.saemoi.Utils;
import com.three_eung.saemoi.infos.HousekeepInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ModifyDialog extends DialogFragment implements View.OnClickListener {
    private View mView;
    private Calendar calendar;
    private Button dateButton;
    private DatePickerDialog datePickerDialog;
    private Spinner categorySpinner;
    private EditText valueText, memoText;
    private ModifyListener mListener;
    private DatePickerDialog.OnDateSetListener dateListener;
    private ArrayAdapter<String> mAdapter;
    private TableRow mInclude;
    private RadioGroup mRadioGroup;
    private CheckBox mBudgetChk;
    private HashMap<String, Integer> inCate, exCate;
    private ArrayList<String> mInCate, mExCate;
    private HousekeepInfo toModify;

    public static ModifyDialog newInstance(HousekeepInfo housekeepInfo, ModifyListener mListener) {
        ModifyDialog modifyDialog = new ModifyDialog();
        modifyDialog.mListener = mListener;
        modifyDialog.toModify = housekeepInfo;

        return modifyDialog;
    }

    public interface ModifyListener {
        void onDataInputComplete(String id, HousekeepInfo housekeepInfo);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.dialog_modify_housekeep, null);

        calendar = Calendar.getInstance();
        calendar.setTime(Utils.stringToDate(toModify.getDate()));

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(year, month, day);
                dateButton.setText(Utils.toYearMonthDay(calendar));
            }
        };

        initView();

        setView();

        dateButton.setText(Utils.toYearMonthDay(calendar));

        builder.setView(mView)
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setValue();
                    }
                }).setNegativeButton("취소", null);

        Dialog dialog = builder.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(R.color.pink_background);
    }

    private void initView() {
        categorySpinner = (Spinner) mView.findViewById(R.id.dialog_modify_category);
        dateButton = (Button) mView.findViewById(R.id.dialog_modify_datebtn);
        valueText = (EditText) mView.findViewById(R.id.dialog_modify_value);
        mRadioGroup = (RadioGroup) mView.findViewById(R.id.dialog_modify_group);
        mBudgetChk = (CheckBox) mView.findViewById(R.id.dialog_modify_check);
        memoText = (EditText) mView.findViewById(R.id.dialog_modify_memo);
        mInclude = (TableRow) mView.findViewById(R.id.dialog_modify_include);

        inCate = ((InitApp) (getActivity().getApplication())).getInCate();
        exCate = ((InitApp) (getActivity().getApplication())).getExCate();

        mInCate = new ArrayList<>();
        mExCate = new ArrayList<>();

        mInCate.addAll(inCate.keySet());

        mExCate.addAll(exCate.keySet());

        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, (ArrayList<String>) mInCate.clone());
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(mAdapter);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.dialog_modify_in:
                        mAdapter.clear();
                        mAdapter.addAll((ArrayList<String>) mInCate.clone());
                        mAdapter.notifyDataSetChanged();
                        mInclude.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.dialog_modify_out:
                        mAdapter.clear();
                        mAdapter.addAll((ArrayList<String>) mExCate.clone());
                        mAdapter.notifyDataSetChanged();
                        mInclude.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        datePickerDialog = new DatePickerDialog(this.getContext(), dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        dateButton.setOnClickListener(this);

        valueText.addTextChangedListener(new TextWatcher() {
            String strAmount = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(strAmount)) {
                    strAmount = setStrDataToComma(s.toString().replace(",", ""));
                    valueText.setText(strAmount);
                    Editable e = valueText.getText();
                    Selection.setSelection(e, strAmount.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }

            private String setStrDataToComma(String str) {
                if(str.length() == 0)
                    return "";
                long value = Long.parseLong(str);

                return Utils.toCurrencyFormat(value);
            }
        });
    }

    private void setView() {
        if (toModify.getIsIncome()) {
            mRadioGroup.check(R.id.dialog_modify_in);
            mInclude.setVisibility(View.INVISIBLE);
        } else {
            mRadioGroup.check(R.id.dialog_modify_out);
            mBudgetChk.setChecked(toModify.getIsBudget());
            mInclude.setVisibility(View.VISIBLE);
        }

        valueText.setText(Utils.toCurrencyFormat(toModify.getValue()));
        memoText.setText(toModify.getMemo());

        if (toModify.getIsIncome()) {
            for (int i = 0; i < mInCate.size(); i++) {
                if(toModify.getCategory().equals(mInCate.get(i)))
                    categorySpinner.setSelection(i);
            }
        } else {
            for (int i = 0; i < mExCate.size(); i++) {
                if(toModify.getCategory().equals(mExCate.get(i)))
                    categorySpinner.setSelection(i);
            }
        }
    }

    private void setValue() {
        if(!TextUtils.isEmpty(memoText.getText().toString()) && !TextUtils.isEmpty(valueText.getText().toString())) {
            boolean isIncome = mRadioGroup.getCheckedRadioButtonId() == R.id.dialog_modify_in;
            boolean isCheck = mBudgetChk.isChecked();
            String category = categorySpinner.getSelectedItem().toString();
            String memo = memoText.getText().toString();
            long value = Long.parseLong(valueText.getText().toString().replace(",", ""));

            Calendar now = Calendar.getInstance();
            now.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

            if (isIncome) {
                isCheck = false;
            }

            String toDate = Utils.dateToString(now.getTime());
            HousekeepInfo housekeepInfo = new HousekeepInfo(category, value, isIncome, isCheck, memo, toDate);
            mListener.onDataInputComplete(toModify.getId(), housekeepInfo);
        } else {
            Toast.makeText(getContext(), "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_housekeep_datebtn:
                datePickerDialog.show();
        }
    }
}