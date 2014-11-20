import com.github.cyberwaste.muka.CoordinatedIndex

/**
 * @author yaroslav.yermilov
 */

def index = new CoordinatedIndex(documents: (1..10).collect { "text-${it}.txt" })
index.load()

println index.terms.sort().collect { it.toString() }.join('\n')