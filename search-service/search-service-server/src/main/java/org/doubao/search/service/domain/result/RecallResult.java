package org.doubao.search.service.domain.result;

import java.util.Collections;
import java.util.List;

public class RecallResult {

    private final long total;
    private final List<RecallDoc> docs;

    public RecallResult(long total, List<RecallDoc> docs) {
        this.total = total;
        this.docs = docs == null ? Collections.emptyList() : Collections.unmodifiableList(docs);
    }

    public long getTotal() {
        return total;
    }

    public List<RecallDoc> getDocs() {
        return docs;
    }
}
