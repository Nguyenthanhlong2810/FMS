package org.opentcs.kernel.extensions.statistics;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class ErrorDecoder {

    private static HashMap<String, String> errors;

    static {
        errors = new HashMap<>();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader("config/errors.txt"));
            String str = "";
            while ((str=input.readLine())!=null && str.length()!=0) {
                String[] pair = str.split("\\|", 2);
                errors.put(pair[0].trim(), pair[1].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toMessage(String key) {
        return errors.get(key);
    }

    private int previousErrorCode = 0;

    private int errorCode = 0;

    private ArrayList<String> errorList = new ArrayList<>();

    public ErrorDecoder() {}

    public ErrorDecoder(int errorCode) {
        decode(errorCode);
    }

    public ErrorDecoder decode(int errorCode) {
        if (this.errorCode == errorCode) {
            return this;
        }
        this.previousErrorCode = this.errorCode;
        this.errorCode = errorCode;
        errorList = decodeToList(errorCode);
        return this;
    }

    private ArrayList<String> decodeToList(int errorCode) {
        ArrayList<String> arrayList = new ArrayList<>();
        int i = 0;
        for (Entry<String, String> entry : errors.entrySet()) {
            if (((errorCode >> i) & 1) == 1) {
                arrayList.add(entry.getKey());
            }
            i++;
        }
        return arrayList;
    }

    public ArrayList<String> getNewErrorFromPrevious() {
        int newErrors = (previousErrorCode ^ errorCode) & errorCode;
        return decodeToList(newErrors);
    }

    public String getError(int index) {
        if (errorList.isEmpty()) {
            return null;
        }
        return errorList.get(index);
    }

    public String getFirstError() {
        return getError(0);
    }

    public String getLastError() {
        return getError(errorList.size() - 1);
    }

    public boolean haveWrongPointError() {
        return (errorList.contains("WRONG_POINT"));
    }
}
