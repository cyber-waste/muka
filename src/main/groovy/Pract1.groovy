/**
 * @author yaroslav.yermilov
 */

String.metaClass.read = {
    getClass().getResourceAsStream(delegate).text
}


def terms = [] as Set
(1..10).each {
    def words = "text-${it}.txt"
        .read()
        .split()
        .collect {
            it.toLowerCase().replaceAll('\\W', '').replaceAll('\\d', '')
        }
        .grep {
            !it.isEmpty()
        }

    terms.addAll words
}

println terms.sort()