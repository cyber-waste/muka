import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.mapreduce.Reducer

/**
 * @author yaroslav.yermilov
 */

class IndexMapper extends Mapper<LongWritable, Text, Text, Text> {

    Text output = new Text()

    @Override
    protected void map(LongWritable key, Text value, Mapper.Context context) throws IOException, InterruptedException {
        String fileName = context.inputSplit.path.name
        value.toString()
                .split()
                .collect {
                    it.toLowerCase().replaceAll('\\W', '').replaceAll('\\d', '')
                }
                .grep {
                    !it.isEmpty()
                }
                .each { token ->
                    output.set(token)
                    context.write(output, fileName)
                }
    }
}

class IndexReducer extends Reducer<Text, Text, Text, Text> {

    Text output = new Text()

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        def counts = [:]
        values.each { document -> counts[document] = (counts[document]?:0) + 1 }
        output.set(counts.toString())

        context.write(key, output)
    }
}
