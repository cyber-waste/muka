import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.conf.Configured
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.JobClient
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapred.Mapper
import org.apache.hadoop.mapred.OutputCollector
import org.apache.hadoop.mapred.Reducer
import org.apache.hadoop.mapred.Reporter
import org.apache.hadoop.util.Tool
import org.apache.hadoop.util.ToolRunner

public class IndexerTool extends Configured implements Tool {

    private static final int TASK_TIMEOUT = 1800000; //30 minutes
    private static final int IO_FILE_BUFFER_SIZE = 131072; //128K
    private static final String MAPRED_HEAP_SPACE = "-Xmx2048m";

    private IndexerTool() {
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new IndexerTool(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String inputPath = "/user/platetl/indexing/input";
        String outputDir = "/user/platetl/indexing/output";
        String queueName = "platetl";

        Configuration defaults = new Configuration();
        Configuration conf = new Configuration();
        conf.set("mapred.job.queue.name", queueName);

        conf.setInt("mapred.task.timeout", TASK_TIMEOUT);
        conf.setInt("io.file.buffer.size", IO_FILE_BUFFER_SIZE);
        conf.set("mapred.child.java.opts", MAPRED_HEAP_SPACE);

        JobConf job = new JobConf(conf, IndexerMapper.class);
        job.setJobName("yermilov.indexing: " + inputPath);
        job.setSpeculativeExecution(false);

        job.setInputFormat(StreamInputFormat.class);
        job.set("stream.recordreader.class", "org.apache.hadoop.streaming.StreamXmlRecordReader");
        job.set("stream.recordreader.begin", "<doc>");
        job.set("stream.recordreader.end", "</doc>");
        org.apache.hadoop.mapred.FileInputFormat.setInputPaths(job, inputPath);


        job.setOutputFormat(SequenceFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
        job.set("mapred.compress.map.output", "true");
        job.set("mapred.map.output.compression.codec", "org.apache.hadoop.io.compress.SnappyCodec");
        org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(job, new Path(outputDir));

        job.setMapperClass(IndexerMapper.class);
        job.setReducerClass(IndexerReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

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


public class IndexerMapper implements Mapper<Text, Text, Text, Text> {

    private Text token = new Text();
    private Text key = new Text();

    @Override
    public void configure(JobConf jobConf) {
    }

    @Override
    public void map(Text value, Text ignore, OutputCollector<Text, Text> outputCollector, Reporter reporter) throws IOException {
        String text = value.toString();
        String _text = extract(text, "abstract", "anchor");
        key.set(extract(text, "title"));

        StringTokenizer tokenizer = new StringTokenizer(_text);
        while (tokenizer.hasMoreTokens()) {
            token.set(tokenizer.nextToken());
            outputCollector.collect(token, key);
        }
    }

    private static String extract(String text, String... tags) {
        StringBuilder result = new StringBuilder();
        for (String tag : tags) {
            String startTag = "<" + tag + ">";
            String endTag = "</" + tag + ">";
            int textStart = 0;

            while (true) {
                int startIndex = text.indexOf(startTag, textStart);
                int endIndex = text.indexOf(endTag, textStart);

                if (startIndex == -1 || endIndex == -1) break;

                String tokens = text.substring(startIndex + startTag.length(), endIndex);
                result.append(tokens).append(" ");
                textStart = endIndex + 1;
            }
        }

        return result.toString();
    }

    @Override
    public void close() throws IOException {
    }
}


public class IndexerReducer implements Reducer<Text, Text, Text, Text> {

    private Text output = new Text();

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> outputCollector, Reporter reporter)
            throws IOException {
        Set<String> documents = new HashSet<>();
        while (values.hasNext()) {
            Text val = values.next();
            documents.add(val.toString());
        }

        StringBuilder result = new StringBuilder();
        for (String doc : documents) {
            result.append(doc).append(';');
        }
        output.set(result.toString());

        outputCollector.collect(key, output);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void configure(JobConf jobConf) {

    }
}
