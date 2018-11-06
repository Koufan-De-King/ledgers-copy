package de.adorsys.ledgers.um.db.domain;

import javax.persistence.*;

@Entity
@Table(name = "sca_data")
public class ScaUserDataEntity {

    @Id
    @Column(name = "sca_id")
    private String id;

    @Column(nullable = false)
    private ScaMethodType scaMethod;

    @Column(nullable = false)
    private String methodValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "user_id", nullable = false, updatable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethodValue() {
        return methodValue;
    }

    public void setMethodValue(String methodValue) {
        this.methodValue = methodValue;
    }

    public ScaMethodType getScaMethod() {
        return scaMethod;
    }

    public void setScaMethod(ScaMethodType scaMethod) {
        this.scaMethod = scaMethod;
    }
}