/**
 * @author yaroslav.yermilov
 */
class CoordinatedIndex {

    static {
        String.metaClass.read = {
            getClass().getResourceAsStream(delegate).text
        }
    }

    def terms = [:]
    def documents = []

    void load() {
        documents = documents.sort()

        documents.each { document ->
            def tokens = document
                .read()
                .split()
                .collect {
                    it.toLowerCase().replaceAll('\\W', '').replaceAll('\\d', '')
                }
                .grep {
                    !it.isEmpty()
                }

            tokens
                .eachWithIndex { term, index ->
                    if (terms.containsKey(term)) {
                        if (terms[term].find {it.document == document} != null) {
                            terms[term].find {it.document == document}.indexes << index
                        } else {
                            terms[term] << [document: document, indexes: [index]]
                        }
                    } else {
                        terms[term] = [ [document: document, indexes: [index]] ] as Set
                    }
                }
        }

        terms.each { it.value = it.value.sort() }
    }
}
