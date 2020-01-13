package com.hearing.micromsgstepmodify.util;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * @author liujiadong
 * @since 2020/1/13
 */
public class StepUtil {

    public static HashMap<Integer, Object> readStep(String path) {

        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            HashMap map = (HashMap<Integer, Object>) ois.readObject();

            Log.d(Constant.TAG, "StepCounterMap = " + map);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int writeStep(String path, long step) {
        HashMap<Integer, Object> map = readStep(path);

        try (FileOutputStream fos = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            if (map == null) {
                return Constant.FAIL;
            }

            String tmp = (String) map.get(301);

            String[] arr = tmp.split(",");
            StringBuilder v = new StringBuilder();
            for (int i = 0; i < arr.length - 3; i++) {
                v.append(arr[i]).append(",");
            }
            v.append(step).append(",").append(step).append(",").append(step);
            map.put(301, v.toString());
            oos.writeObject(map);

        } catch (Exception e) {
            e.printStackTrace();
            return Constant.FAIL;
        }
        return Constant.SUCCESS;
    }

    public static int writeMMStep(String path, long step) {
        HashMap<Integer, Object> map = readStep(path);

        try (FileOutputStream fos = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            if (map == null) {
                return Constant.FAIL;
            }

            map.put(4, step);

            oos.writeObject(map);
        } catch (Exception e) {
            e.printStackTrace();
            return Constant.FAIL;
        }
        return Constant.SUCCESS;
    }
}
