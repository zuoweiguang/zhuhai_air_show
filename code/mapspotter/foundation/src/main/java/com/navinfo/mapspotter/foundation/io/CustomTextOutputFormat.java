package com.navinfo.mapspotter.foundation.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 自定义MapReduce的文本输出formatter，指定blocksize为512M
 * Created by SongHuiXing on 2016/3/26.
 */
public class CustomTextOutputFormat<K, V> extends TextOutputFormat<K, V> {

    @Override
    public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job)
            throws IOException, InterruptedException {

        Configuration conf = job.getConfiguration();

        boolean isCompressed = getCompressOutput(job);

        String keyValueSeparator= conf.get(SEPERATOR, "\t");

        CompressionCodec codec = null;
        String extension = "";
        if (isCompressed) {
            Class<? extends CompressionCodec> codecClass =
                    getOutputCompressorClass(job, GzipCodec.class);

            codec = ReflectionUtils.newInstance(codecClass, conf);

            extension = codec.getDefaultExtension();
        }

        Path file = getDefaultWorkFile(job, extension);

        FileSystem fs = file.getFileSystem(conf);

        int buffersize = fs.getConf().getInt("io.file.buffer.size", 4096);
        short replication = fs.getDefaultReplication(file);
        int blocksize = 512 * 1024 * 1024; //512M

        if (!isCompressed) {
            FSDataOutputStream fileOut = fs.create(file, false, buffersize, replication, blocksize);

            return new LineRecordWriter<>(fileOut, keyValueSeparator);
        } else {
            FSDataOutputStream fileOut = fs.create(file, false, buffersize, replication, blocksize);

            DataOutputStream outputStream = new DataOutputStream(codec.createOutputStream(fileOut));

            return new LineRecordWriter<>(outputStream, keyValueSeparator);
        }
    }
}
