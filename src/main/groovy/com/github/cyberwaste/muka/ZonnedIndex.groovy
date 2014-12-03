package com.github.cyberwaste.muka

/**
 * @author yaroslav.yermilov
 */
class ZonnedIndex {

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
            def parsedXml = new XmlParser().parse(document.read())

            addTo zone: 'title', document: document, text: parsedXml.'FictionBook'.'description'.'title-info'.'book-title'.text()
            addTo zone: 'annotation', document: document, text: parsedXml.'FictionBook'.'description'.'title-info'.'annotation'.text()
            addTo zone: 'text', document: document, text: document.read()
        }

        terms.each { it.value = it.value.sort() }
    }

    void addTo(Map params) {
        def tokens = params.text
                        .read()
                        .split()
                        .collect {
                            it.toLowerCase().replaceAll('\\W', '').replaceAll('\\d', '')
                        }
                        .grep {
                            !it.isEmpty()
                        }

        tokens.eachWithIndex { term, index ->
            if (terms.containsKey(term)) {
                if (terms[term].find {it.document == params.document} != null) {
                    terms[term].find {it.document == params.document}.zones << params.zone
                } else {
                    terms[term] << [document: params.document, zones: [params.zone]]
                }
            } else {
                terms[term] = [ [document: params.document, zones: [params.zone]] ] as Set
            }
        }
    }
}
