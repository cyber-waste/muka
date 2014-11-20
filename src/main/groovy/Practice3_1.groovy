import com.github.cyberwaste.muka.Index

/**
 * @author yaroslav.yermilov
 */

def index = new Index(ngram: 2, documents: (1..10).collect { "text-${it}.txt" })
index.load()

println index.terms.sort().collect { it.toString() }.join('\n')

index.terms.sort().each { term ->
    print "${term.key}".center(50)
    (1..10).collect { "text-${it}.txt" }.each { document ->
        if (term.value.contains(document)) {
            print '1'
        } else {
            print '0'
        }
    }
    println()
}