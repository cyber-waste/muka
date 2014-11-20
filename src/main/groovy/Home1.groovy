import com.github.cyberwaste.muka.Index

/**
 * @author yaroslav.yermilov
 */

def index = new Index(documents: (1..10).collect { "text-${it}.txt" })
index.load()

def console = new BufferedReader(new InputStreamReader(System.in))
def query = console.readLine()

println index.query(query)