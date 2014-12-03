import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.conf.Configured
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.BytesWritable
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.JobClient
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapred.Mapper
import org.apache.hadoop.mapred.OutputCollector
import org.apache.hadoop.mapred.Reporter
import org.apache.hadoop.util.Tool
import org.apache.hadoop.util.ToolRunner

public class EliasOmegaTool extends Configured implements Tool {

    private static final int TASK_TIMEOUT = 1800000; //30 minutes
    private static final int IO_FILE_BUFFER_SIZE = 131072; //128K
    private static final String MAPRED_HEAP_SPACE = "-Xmx2048m";

    private EliasOmegaTool() {
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new EliasOmegaTool(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String inputPath = "/user/platetl/indexing/output";
        String outputDir = "/user/platetl/indexing/compressed";
        String queueName = "platetl";

        Configuration defaults = new Configuration();
        Configuration conf = new Configuration();
        conf.set("mapred.job.queue.name", queueName);

        conf.setInt("mapred.task.timeout", TASK_TIMEOUT);
        conf.setInt("io.file.buffer.size", IO_FILE_BUFFER_SIZE);
        conf.set("mapred.child.java.opts", MAPRED_HEAP_SPACE);

        JobConf job = new JobConf(conf, IndexerMapper.class);
        job.setJobName("yermilov.indexing.compressing: " + inputPath);
        job.setSpeculativeExecution(false);

        job.setInputFormat(TextInputFormat.class);
        org.apache.hadoop.mapred.FileInputFormat.setInputPaths(job, inputPath);

        job.setOutputFormat(SequenceFileAsBinaryOutputFormat.class);
//        job.setOutputFormat(SequenceFileOutputFormat.class);
//        SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
//        job.set("mapred.compress.map.output", "true");
//        job.set("mapred.map.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");

        org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(job, new Path(outputDir));

        job.setMapperClass(EliasOmegaMapper.class);
        job.setNumReduceTasks(0);

        job.setMapOutputKeyClass(BytesWritable.class);
        job.setMapOutputValueClass(BytesWritable.class);

        FileSystem fs = FileSystem.get(defaults);
        Path outputDirPath = new Path(outputDir);
        fs.delete(outputDirPath, true);

        JobClient client = new JobClient();
        client.setConf(job);

        JobClient.runJob(job);
        deleteEmptyFiles(fs, outputDirPath);

        return 0;
    }

    void deleteEmptyFiles(FileSystem fs, Path outputDirPath) throws IOException {
        FileStatus[] statuses = fs.listStatus(outputDirPath);
        for (int i = 0; i < statuses.length; i++) {
            Path filePath = statuses[i].getPath();
            if (isHdfsFileEmpty(fs, filePath)) {
                fs.delete(filePath, true);
            }
        }
    }

    static boolean isHdfsFileEmpty(FileSystem fs, Path hdfsFilePath) throws IOException {
        Long fileSize;
        FileStatus hdfsFileStatus = fs.getFileStatus(hdfsFilePath);
        fileSize = hdfsFileStatus.getLen();
        return fileSize == 0;
    }
}



public class EliasOmegaMapper implements Mapper<LongWritable, Text, BytesWritable, BytesWritable> {

    private final static int WORD = 7;

    private BytesWritable empty = new BytesWritable();
    private BytesWritable encoded = new BytesWritable();

    @Override public void map(LongWritable ignore, Text _text, OutputCollector<BytesWritable, BytesWritable> outputCollector, Reporter reporter) throws IOException {
        String text = _text.toString();
        String encodedText = encode(text);

        int bitsCount = encodedText.length();
        byte[] bytes = new byte[bitsCount / WORD];
        int textIndex = 0;
        int bytesIndex = 0;
        while (textIndex < encodedText.length()) {
            bytes[bytesIndex] = Byte.parseByte(encodedText.substring(textIndex, textIndex + WORD), 2);
            bytesIndex++;
            textIndex += WORD;
        }

        encoded.set(bytes, 0, bytes.length);
        outputCollector.collect(encoded, empty);
    }

    @Override public void configure(JobConf jobConf) {
    }

    @Override public void close() throws IOException {
    }

    private String encode(String text) {
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            encoded.append(encode(text.charAt(i)));
        }
        while (encoded.length() % WORD != 0) { encoded.append("0"); }
        return encoded.toString();
    }

    private String encode(char symbol) {
        int intValue = String.valueOf(symbol).codePointAt(0);
        return encode(intValue);
    }

    private String encode(int intValue) {
        if (intValue > 1) {
            String binary = Integer.toBinaryString(intValue);
            return encode(binary.length() - 1) + binary;
        } else {
            return "0";
        }
    }
}
