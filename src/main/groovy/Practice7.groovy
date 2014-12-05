import com.github.cyberwaste.muka.ZonnedIndex

/**
 * @author yaroslav.yermilov
 */

def index = new ZonnedIndex(documents: (1..10).collect { "text-${it}.txt" })
index.load()

println index.terms.sort().collect { it.toString() }.join('\n')