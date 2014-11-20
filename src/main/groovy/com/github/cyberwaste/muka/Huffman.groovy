package com.github.cyberwaste.muka

class Huffman {

    def symbolWeights
    def root

    void init(String text) {
        root = null
        symbolWeights = [:]
        for (def symbolFromFile in text) {
            def previousWeight = symbolWeights.getOrDefault symbolFromFile, 0
            symbolWeights.put symbolFromFile, previousWeight + 1
        }
        root = initRoot()
    }

    void init(List<String> lines) {
        symbolWeights = [:]
        def alphabetSize = Integer.valueOf(lines[0])
        for (index in 1..alphabetSize) {
            def symbolCode = Integer.valueOf(lines[index].split('@')[0])
            def symbolWeight = Integer.valueOf(lines[index].split('@')[1])
            symbolWeights.put Character.toChars(symbolCode)[0], symbolWeight
        }
        root = initRoot()
    }

    def initRoot() {
        PriorityQueue<CodingItem> priorityQueue = new PriorityQueue<>({
            CodingItem item1, CodingItem item2 -> Integer.compare(item1.weight, item2.weight)
        })
        for (def symbolWeight in symbolWeights) {
            priorityQueue.add new CodingItem(symbols: [ symbolWeight.key ], weight: symbolWeight.value)
        }

        while (priorityQueue.size() > 1) {
            def item0 = priorityQueue.poll()
            def item1 = priorityQueue.poll()

            def summarySymbols = []
            summarySymbols.addAll item0.symbols
            summarySymbols.addAll item1.symbols
            def summaryWeight = item0.weight + item1.weight

            priorityQueue.add new CodingItem(symbols: summarySymbols, weight: summaryWeight, item0: item0, item1: item1)
        }

        priorityQueue.poll()
    }

    def encode(String text) {
        String coded = "${symbolWeights.size()}\n"
        for (def symbolWeight in symbolWeights) {
            coded += "${symbolWeight.key.codePointAt(0)}@${symbolWeight.value}\n"
        }
        for (def symbolFromFile in text) {
            def symbolCode = ""

            def ref = root
            while (ref.symbols.size() > 1) {
                if (ref.item0.symbols.contains(symbolFromFile)) {
                    symbolCode += "0"
                    ref = ref.item0
                } else {
                    symbolCode += "1"
                    ref = ref.item1
                }
            }

            coded += symbolCode
        }

        coded
    }

    def decode(String coded) {
        def decoded = ""

        def ref = root
        for (def codedSymbol in coded) {
            if (codedSymbol == '0') {
                ref = ref.item0
            } else {
                ref = ref.item1
            }

            if (ref.symbols.size() == 1) {
                decoded += ref.symbols[0]
                ref = root
            }
        }

        decoded
    }
}
