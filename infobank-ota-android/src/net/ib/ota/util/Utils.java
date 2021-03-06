package net.ib.ota.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import net.ib.ota.Const;
import net.ib.ota.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Utils implements Const {
    protected final static String APKLIST_FILENAME = "apklist_saved.dat";

    public static boolean checkApiLevel(int level) {
        return (Build.VERSION.SDK_INT >= level);
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static ConcurrentHashMap<String, Integer> loadApkList(Context context) {
        FileInputStream fis;
        Object obj = null;
        try {
            fis = context.openFileInput(APKLIST_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            obj =  is.readObject();
            if (obj instanceof ConcurrentHashMap<?, ?>) {
            } else {
                obj = new ConcurrentHashMap<String, Integer>();
            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return (ConcurrentHashMap<String, Integer>)obj;
    }

    public static void saveApkList(Context context, ConcurrentHashMap<String, Integer> hashMap) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(APKLIST_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(hashMap);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tm =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String pn = tm.getLine1Number();
        if (TextUtils.isEmpty(pn)) {
            pn = tm.getDeviceId();
        }

        return pn.replace("-", "");
    }

    public static String[] parseFilename(String filename) {
        return filename.split("_");
    }

    public static String getTitle(String filename) {
        String title = filename;
        String [] list = parseFilename(filename);

        if (filename.contains("messagetong_release") || filename.contains("ib-ota_release")) {
            if (list != null && list.length == 5) {
                title = "[" + list[4].split("\\.")[0] + "] v" + list[3] + " - " + list[2];
            }
        } else if (filename.contains("messagetong") && filename.endsWith(".apk")) {
            title = list[2] + "_" + list[3] + "_" + list[4];
        }

        return title;
    }

    public static int getImageResource(String filename, Date createdAt) {
        int resId = R.drawable.ic_default_app;

        if (filename.contains("ib-ota") && filename.endsWith(".apk")) {
            resId = R.drawable.ic_launcher;
        } else if (filename.endsWith(".xml")) {
            resId = R.drawable.ic_xml;
        }

        return resId;
    }

    public static byte[] convertFileToByteArray(File f) {
        byte[] byteArray = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;

        try {
            is = new FileInputStream(f);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[(int)f.length()];
            int bytesRead = 0;

            while ((bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArray;
    }

    public static ArrayList<String> getChangeLog(String log) {
        if (TextUtils.isEmpty(log) || log.startsWith("<log/>")) return new ArrayList<String>();

        if (log.startsWith("<?xml")) return getChangeLogSvn(log);
        else if (log.startsWith("Changes in branch ")) return getChangeLogGit(log);
        else return new ArrayList<String>();
    }

    @SuppressLint("NewApi")
    public static ArrayList<String> getChangeLogSvn(String xml_str) {
        DocumentBuilderFactory t_dbf = null;
        DocumentBuilder t_db = null;
        Document t_doc = null;
        NodeList t_nodes = null;
        Node t_node = null;
        Element t_element = null;
        InputSource t_is = new InputSource();

        ArrayList<String> changeLogList = new ArrayList<String>();

        try {
            t_dbf = DocumentBuilderFactory.newInstance();
            t_db = t_dbf.newDocumentBuilder();
            t_is = new InputSource();
            t_is.setCharacterStream(new StringReader(xml_str));
            t_doc = t_db.parse(t_is);
            t_nodes = t_doc.getElementsByTagName("msg");

            if (checkApiLevel(8)) {
                for (int i = 0, t_len = t_nodes.getLength();  i < t_len; i++) {
                    t_element = (Element) t_nodes.item(i);
                    if (!TextUtils.isEmpty(t_element.getTextContent())) {
                        changeLogList.add(t_element.getTextContent());
                    }
                    //Log.e("" + t_element.getNodeName() + ", " + t_element.getNodeValue() + ", " + t_element.getTextContent());
                }
            } else {
                Log.e("Fail to parse svn change log files becase of SDK level is too low. Minimum 8 is required!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return changeLogList;
    }

    public static ArrayList<String> getChangeLogGit(String str) {
        ArrayList<String> changeLogList = new ArrayList<String>();
        String lines[] = str.split("\n");
        for (String line : lines) {
            if (line.startsWith(" ")) {
                if (!changeLogList.contains(line.trim())) {
                    changeLogList.add(line.trim());
                }
            }
        }

        return changeLogList;
    }

    public static String getFileContent(File file) {
        String xml_content = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            xml_content = sb.toString();
        } catch (IOException e3) {
            xml_content = null;
            e3.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
            xml_content = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e4) {
                }
            }

        }
        return xml_content;
    }

    public static boolean isDebuggable(Context context) {
        return ( 0 != ( context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }
}
