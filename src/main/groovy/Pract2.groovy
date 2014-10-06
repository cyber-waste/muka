/**
 * @author yaroslav.yermilov
 */

def index = new Index(documents: (1..10).collect { "text-${it}.txt" })
index.load()

println index.terms.sort()

index.terms.sort().each { term ->
    print "${term.key}".center(20)
    (1..10).collect { "text-${it}.txt" }.each { document ->
        if (term.value.contains(document)) {
            print '1'
        } else {
            print '0'
        }
    }
    println()
}