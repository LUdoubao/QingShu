package org.doubao.search.service.es.model.message;

import lombok.Data;
import java.util.Date;

public class IndexUpdateMessage {
    public enum Operation {
        CREATE, UPDATE, DELETE
    }
    
    private Operation operation;
    private Long copywritingId;
    private Date timestamp;

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Long getCopywritingId() {
        return copywritingId;
    }

    public void setCopywritingId(Long copywritingId) {
        this.copywritingId = copywritingId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
