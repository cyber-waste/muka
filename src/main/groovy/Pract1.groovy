/**
 * @author yaroslav.yermilov
 */

def index = new Index()
index.load((1..10).collect { "text-${it}.txt" })

println index.terms.sort()