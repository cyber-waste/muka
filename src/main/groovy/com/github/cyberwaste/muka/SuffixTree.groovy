package com.github.cyberwaste.muka

/**
 * @author yaroslav.yermilov
 */
class SuffixTree {

    static {
        String.metaClass.read = {
            getClass().getResourceAsStream("/${delegate}").text
        }
    }

    def documents = []
    def root = [:]

    void init() {
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

            tokens.each { token ->
                add(document, token)
            }
        }
    }

    private void add(String document, String token) {
        add(root, document, token, -1)
    }

    private static void add(def node, String document, String token, int index) {
        if (node[token[index]] == null) {
            if (token.length() == -index) {
                node[token[index]] = new Expando(symbol: token[index], document: document)
            } else {
                node[token[index]] = new Expando(symbol: token[index])
                add(node[token[index]], document, token, index - 1)
            }
        } else {
            if (token.length() == -index) {
                node[token[index]].document = document
            } else {
                add(node[token[index]], document, token, index - 1)
            }
        }
    }
}
