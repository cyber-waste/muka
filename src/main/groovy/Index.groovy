/**
 * @author yaroslav.yermilov
 */
class Index {

    static {
        String.metaClass.read = {
            getClass().getResourceAsStream(delegate).text
        }
    }

    def terms = [:]

    void load(Iterable<String> documents) {
        documents.each { document ->
            document
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
                        terms[term] << document
                    } else {
                        terms[term] = [ document ] as Set
                    }
                }
        }
    }
}
