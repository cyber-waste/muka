import groovyx.net.http.RESTClient
import org.apache.commons.io.FileUtils

/**
 * @author yaroslav.yermilov
 */

def client = new RESTClient('http://api.exchangeratelab.com/api/')

(6..10).each {
    def data = (1..20).collect {
        client.get(path: 'scigen.cgi').data
    }.join()
    FileUtils.writeStringToFile(new File("text-${it}.txt"), data)
}