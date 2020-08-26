package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.Data;

@Data
public class StartScaOprTO {
    private String oprId;
    private String authorisationId;
    private OpTypeTO opType;

    public StartScaOprTO(String oprId, OpTypeTO opType) {
        this.setOprId(oprId);
        this.setOpType(opType);
    }

    public StartScaOprTO(String oprId, String authorizationId, OpTypeTO opType) {
        this.setOprId(oprId);
        this.setOpType(opType);
        this.authorisationId=authorizationId;
    }
}