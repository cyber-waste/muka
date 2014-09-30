import groovyx.net.http.RESTClient
import org.apache.commons.io.FileUtils

/**
 * @author yaroslav.yermilov
 */

def client = new RESTClient('http://apps.pdos.lcs.mit.edu/cgi-bin/')

(6..10).each {
    def data = (1..20).collect {
        client.get(path: 'scigen.cgi').data
    }.join()
    FileUtils.writeStringToFile(new File("text-${it}.txt"), data)
}