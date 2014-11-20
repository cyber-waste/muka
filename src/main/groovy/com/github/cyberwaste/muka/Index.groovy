package com.github.cyberwaste.muka
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
    def documents = []
    def ngram = 1

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
                    if (index >= ngram) {
                        def nterm = (ngram-1..0).collect { delta -> tokens[index - delta] }.join(' ')
                        if (terms.containsKey(nterm)) {
                            terms[nterm] << document
                        } else {
                            terms[nterm] = [ document ] as Set
                        }
                    }
                }
        }

        terms.each { it.value = it.value.sort() }
    }

    def query(String query) {
        def tokens = query.split()

        def response = []
        tokens.each { def token ->
            if (['AND', 'OR', 'NOT'].contains(token)) {
                response << token
            } else {
                response << search(token)
            }
        }

        while (response.size() > 1) {
            def index = 0
            while (index < response.size()) {
                if (response[index] == 'NOT') {
                    response[index] = doNot(response[index + 1])
                    response.remove(index + 1)
                } else {
                    index++;
                }
            }

            index = 0
            while (index < response.size()) {
                if (response[index] == 'AND') {
                    response[index] = doAnd(response[index - 1], response[index + 1])
                    response.remove(index - 1)
                    response.remove(index)
                } else if (response[index] == 'OR') {
                    response[index] = doOr(response[index - 1], response[index + 1])
                    response.remove(index - 1)
                    response.remove(index)
                } else {
                    index++;
                }
            }
        }

        return response[0]
    }

    def search(String token) {
        return terms[token]
    }

    def doAnd(List<String> arg1, List<String> arg2) {
        if (arg1 == null || arg2 == null) {
            return []
        }

        def result = []
        def index1 = 0, index2 = 0

        while (index1 < arg1.size() && index2 < arg2.size()) {
            if (arg1[index1] == arg2[index2]) {
                result << arg1[index1]
                index1++
                index2++
            } else if (arg1[index1].compareTo(arg2[index2]) < 0) {
                index1++
            } else if (arg1[index1].compareTo(arg2[index2]) > 0) {
                index2++
            }
        }

        return result
    }

    def doOr(List<String> arg1, List<String> arg2) {
        if (arg1 == null) {
            return doOr([], arg2)
        }
        if (arg2 == null) {
            return doOr(arg1, [])
        }

        def result = []
        def index1 = 0, index2 = 0

        while (index1 < arg1.size() || index2 < arg2.size()) {
            if (index1 >= arg1.size()) {
                result << arg2[index2]
                index2++
            } else if (index2 >= arg2.size()) {
                result << arg1[index1]
                index1++
            } else if (arg1[index1] == arg2[index2]) {
                result << arg1[index1]
                index1++
                index2++
            } else if (arg1[index1].compareTo(arg2[index2]) < 0) {
                result << arg1[index1]
                index1++
            } else if (arg1[index1].compareTo(arg2[index2]) > 0) {
                result << arg2[index2]
                index2++
            }
        }

        return result
    }

    def doNot(List<String> arg) {
        if (arg == null) {
            return documents
        }

        def result = []
        def index = 0, dndex = 0

        while (index < arg.size() || dndex < documents.size()) {
            if (index >= arg.size()) {
                result << documents[dndex]
                dndex++
            } else if (dndex >= documents.size()) {
                index++
            } else if (arg[index] == documents[dndex]) {
                index++
                dndex++
            } else if (arg[index].compareTo(documents[dndex]) < 0) {
                index++
            } else if (arg[index].compareTo(documents[dndex]) > 0) {
                result << documents[dndex]
                dndex++
            }
        }

        return result
    }
}
