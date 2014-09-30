/**
 * @author yaroslav.yermilov
 */

String.metaClass.read = {
    getClass().getResourceAsStream(delegate).text
}


def terms = [:]
(1..10).each { documentIndex ->
    "text-${documentIndex}.txt"
        .read()
        .split()
        .collect {
            it.toLowerCase().replaceAll('\\W', '').replaceAll('\\d', '')
        }
        .grep {
            !it.isEmpty()
        }
        .each { term ->
            if (terms.containsKey(term)) {
                terms."${term}" << documentIndex
            } else {
                terms."${term}" = [ documentIndex ] as Set
            }
        }
}

println terms.sort()

terms.sort().each { term ->
    print "${term.key}".center(20)
    (1..10).each { documentIndex ->
        if (term.value.contains(documentIndex)) {
            print '1'
        } else {
            print '0'
        }
    }
    println()
}