package com.github.cyberwaste.muka

/**
 * @author yaroslav.yermilov
 */
class PermutationIndex {

    static {
        String.metaClass.read = {
            getClass().getResourceAsStream("/${delegate}").text
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

            tokens.each { String token ->
                (0..token.length()-1).each { permutationIndex ->
                    String term = "${token.substring(permutationIndex)}\$${token.substring(0, permutationIndex)}"
                    if (terms.containsKey(term)) {
                        terms[term] << document
                    } else {
                        terms[term] = [ document ] as Set
                    }
                }
            }
        }
    }
}
