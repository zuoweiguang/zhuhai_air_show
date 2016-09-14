package com.navinfo.mapspotter.foundation.util;

import java.io.*;


/**
 * @param <T> 对象类型
 * @author cuiliang
 */
public class SerializeUtil<T> {
    /**
     * 对象序列化
     *
     * @param t 输入对象
     * @return byte数组
     */
    public byte[] serialize(T t) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) {
            oo.writeObject(t);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bo.toByteArray();
    }

    /**
     * 对象反序列化
     *
     * @param array byte数组
     * @return 对象
     */
    public T deserialize(byte[] array) {
        ByteArrayInputStream bi = new ByteArrayInputStream(array);

        return deserialize(bi);
    }

    /**
     * 反序列化对象
     * @param is 输入流
     * @return
     */
    public T deserialize(InputStream is){
        T t = null;

        try (ObjectInputStream oi = new ObjectInputStream(is)) {
            t = (T) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return t;
    }
}
