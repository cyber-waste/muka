import com.github.cyberwaste.muka.Index

/**
 * @author yaroslav.yermilov
 */

def index = new Index(documents: (1..10).collect { "text-${it}.txt" })
index.load()

println index.terms.sort().collect { it.toString() }.join('\n')