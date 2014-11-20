import com.github.cyberwaste.muka.PermutationIndex

/**
 * @author yaroslav.yermilov
 */

def index = new PermutationIndex(documents: (1..1).collect { "text-${it}.txt" })
index.load()

println index.terms.sort().collect { it.toString() }.join('\n')
